package example.product.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
