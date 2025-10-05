package example.product.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import example.product.application.ProductService;
import example.product.application.RedisLockService;
import example.product.application.dto.ProductReservedResult;
import example.product.controller.dto.ProductReserveRequest;
import example.product.controller.dto.ProductReserveResponse;

@RestController
public class ProductController {

    private final ProductService productService;
    private final RedisLockService redisLockService;

    public ProductController(ProductService productService, RedisLockService redisLockService) {
        this.productService = productService;
        this.redisLockService = redisLockService;
    }

    @PostMapping("/products/reserve")
    public ProductReserveResponse reserveProducts(@RequestBody ProductReserveRequest request) {
        String key = "product:" + request.requestId();
        boolean acquired = redisLockService.tryLock(key, request.requestId());

        if (!acquired) {
            throw new RuntimeException("락 획득에 실패했습니다.");
        }

        try {
            ProductReservedResult result = productService.tryReserve(request.toCommand());

            return new ProductReserveResponse(result.totalPrice());
        } finally {
            redisLockService.releaseLock(key);
        }
    }
}
