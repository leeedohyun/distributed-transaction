package example.product.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import example.product.application.ProductFacadeService;
import example.product.application.RedisLockService;
import example.product.application.dto.ProductReservedResult;
import example.product.controller.dto.ProductReserveCancelRequest;
import example.product.controller.dto.ProductReserveConfirmRequest;
import example.product.controller.dto.ProductReserveRequest;
import example.product.controller.dto.ProductReserveResponse;

@RestController
public class ProductController {

    private final ProductFacadeService productFacadeService;
    private final RedisLockService redisLockService;

    public ProductController(ProductFacadeService productFacadeService, RedisLockService redisLockService) {
        this.productFacadeService = productFacadeService;
        this.redisLockService = redisLockService;
    }

    @PostMapping("/product/reserve")
    public ProductReserveResponse reserveProducts(@RequestBody ProductReserveRequest request) {
        String key = "product:" + request.requestId();
        boolean acquired = redisLockService.tryLock(key, request.requestId());

        if (!acquired) {
            throw new RuntimeException("락 획득에 실패했습니다.");
        }

        try {
            ProductReservedResult result = productFacadeService.tryReserve(request.toCommand());

            return new ProductReserveResponse(result.totalPrice());
        } finally {
            redisLockService.releaseLock(key);
        }
    }

    @PostMapping("/product/confirm")
    public void confirm(@RequestBody ProductReserveConfirmRequest request) {
        String key = "product:" + request.requestId();
        boolean acquiredLock = redisLockService.tryLock(key, request.requestId());

        if (!acquiredLock) {
            throw new RuntimeException("락 획득에 실패했습니다.");
        }

        try {
            productFacadeService.confirmReserve(request.toCommand());
        } finally {
            redisLockService.releaseLock(key);
        }
    }

    @PostMapping("/product/cancel")
    public void cancel(@RequestBody ProductReserveCancelRequest request) {
        String key = "product:" + request.requestId();
        boolean acquiredLock = redisLockService.tryLock(key, request.requestId());

        if (!acquiredLock) {
            throw new RuntimeException("락 획득에 실패했습니다.");
        }

        try {
            productFacadeService.cancelReserve(request.toCommand());
        } finally {
            redisLockService.releaseLock(key);
        }
    }
}
