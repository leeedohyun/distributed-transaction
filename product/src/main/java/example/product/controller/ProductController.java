package example.product.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import example.product.application.ProductService;
import example.product.application.RedisLockService;
import example.product.application.dto.ProductBuyResult;
import example.product.controller.dto.ProductBuyRequest;
import example.product.controller.dto.ProductBuyResponse;

@RestController
public class ProductController {

    private final ProductService productService;
    private final RedisLockService redisLockService;

    public ProductController(ProductService productService, RedisLockService redisLockService) {
        this.productService = productService;
        this.redisLockService = redisLockService;
    }

    @PostMapping("/product/buy")
    public ProductBuyResponse buy(@RequestBody ProductBuyRequest request) {
        String lockKey = "product:orchestration:" + request.requestId();

        boolean lockAcquired = redisLockService.tryLock(lockKey, request.requestId());

        if (!lockAcquired) {
            System.out.println("락 획득에 실패하였습니다.");
            throw new RuntimeException("락 획득에 실패하였습니다.");
        }

        try {
            ProductBuyResult buyResult = productService.buy(request.toCommand());

            return new ProductBuyResponse(buyResult.totalPrice());
        } finally {
            redisLockService.releaseLock(lockKey);
        }
    }
}
