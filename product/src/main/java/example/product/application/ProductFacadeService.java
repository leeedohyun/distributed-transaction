package example.product.application;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import example.product.application.dto.ProductReserveCancelCommand;
import example.product.application.dto.ProductReserveCommand;
import example.product.application.dto.ProductReserveConfirmCommand;
import example.product.application.dto.ProductReservedResult;

@Component
public class ProductFacadeService {

    private final ProductService productService;

    public ProductFacadeService(ProductService productService) {
        this.productService = productService;
    }

    public ProductReservedResult tryReserve(ProductReserveCommand command) {
        int tryCount = 0;

        while (tryCount < 3) {
            try {
                return productService.tryReserve(command);
            } catch (ObjectOptimisticLockingFailureException e) {
                tryCount++;
            }
        }

        throw new RuntimeException("예약에 실패하였습니다.");
    }

    public void confirmReserve(ProductReserveConfirmCommand command) {
        int tryCount = 0;

        while (tryCount < 3) {
            try {
                productService.confirmReserve(command);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                tryCount++;
            }
        }

        throw new RuntimeException("예약에 실패하였습니다.");
    }

    public void cancelReserve(ProductReserveCancelCommand command) {
        int tryCount = 0;

        while (tryCount < 3) {
            try {
                productService.cancelReserve(command);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                tryCount++;
            }
        }

        throw new RuntimeException("예약에 실패하였습니다.");
    }
}
