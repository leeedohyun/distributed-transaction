package example.product.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.product.application.dto.ProductBuyCancelCommand;
import example.product.application.dto.ProductBuyCancelResult;
import example.product.application.dto.ProductBuyCommand;
import example.product.application.dto.ProductBuyResult;
import example.product.domain.Product;
import example.product.domain.ProductTransactionHistory;
import example.product.domain.ProductTransactionHistory.TransactionType;
import example.product.infrastructure.ProductRepository;
import example.product.infrastructure.ProductTransactionHistoryRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductTransactionHistoryRepository productTransactionHistoryRepository;

    public ProductService(ProductRepository productRepository, ProductTransactionHistoryRepository productTransactionHistoryRepository) {
        this.productRepository = productRepository;
        this.productTransactionHistoryRepository = productTransactionHistoryRepository;
    }

    @Transactional
    public ProductBuyResult buy(ProductBuyCommand command) {
        List<ProductTransactionHistory> histories = productTransactionHistoryRepository.findAllByRequestIdAndTransactionType(
                command.requestId(),
                TransactionType.PURCHASE
        );

        if (!histories.isEmpty()) {
            System.out.println("이미 구매한 이력이 있습니다.");

            long totalPrice = histories.stream()
                    .mapToLong(ProductTransactionHistory::getPrice)
                    .sum();

            return new ProductBuyResult(totalPrice);
        }

        Long totalPrice = 0L;

        for (ProductBuyCommand.ProductInfo productInfo : command.productInfos()) {
            Product product = productRepository.findById(productInfo.productId()).orElseThrow();

            product.buy(productInfo.quantity());
            Long price = product.calculatePrice(productInfo.quantity());
            totalPrice += price;

            productTransactionHistoryRepository.save(
                    new ProductTransactionHistory(
                            command.requestId(),
                            productInfo.productId(),
                            productInfo.quantity(),
                            price,
                            TransactionType.PURCHASE
                    )
            );
        }

        return new ProductBuyResult(totalPrice);
    }

    @Transactional
    public ProductBuyCancelResult cancel(ProductBuyCancelCommand command) {
        List<ProductTransactionHistory> buyHistories = productTransactionHistoryRepository.findAllByRequestIdAndTransactionType(
                command.requestId(),
                TransactionType.PURCHASE
        );

        // 원래는 사용 이력이 없으면 예외를 던지도록 되어 있었음.
        // 하지만 보상 트랜잭션 수행 시 예외를 던지면 retry 로직이 실행됨.
        // 따라서 사용 이력이 없더라도 정상적으로 처리하도록 변경
        if (buyHistories.isEmpty()) {
            return new ProductBuyCancelResult(0L);
        }

        List<ProductTransactionHistory> cancelHistories = productTransactionHistoryRepository.findAllByRequestIdAndTransactionType(
                command.requestId(),
                TransactionType.CANCEL
        );

        if (!cancelHistories.isEmpty()) {
            System.out.println("이미 취소되었습니다.");
            long totalPrice = cancelHistories.stream()
                    .mapToLong(ProductTransactionHistory::getPrice)
                    .sum();

            return new ProductBuyCancelResult(totalPrice);
        }

        Long totalPrice = 0L;

        for (ProductTransactionHistory history : buyHistories) {
            Product product = productRepository.findById(history.getProductId()).orElseThrow();

            product.cancel(history.getQuantity());
            totalPrice += history.getPrice();

            productTransactionHistoryRepository.save(
                    new ProductTransactionHistory(
                            command.requestId(),
                            history.getProductId(),
                            history.getQuantity(),
                            history.getPrice(),
                            TransactionType.CANCEL
                    )
            );
        }

        return new ProductBuyCancelResult(totalPrice);
    }
}
