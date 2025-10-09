package example.point.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import example.point.application.PointFacadeService;
import example.point.application.RedisLockService;
import example.point.controller.dto.PointReserveCancelRequest;
import example.point.controller.dto.PointReserveConfirmRequest;
import example.point.controller.dto.PointReserveRequest;

@RestController
public class PointController {

    private final PointFacadeService pointFacadeService;
    private final RedisLockService redisLockService;

    public PointController(PointFacadeService pointFacadeService, RedisLockService redisLockService) {
        this.pointFacadeService = pointFacadeService;
        this.redisLockService = redisLockService;
    }

    int count = 0;

    @PostMapping("/point/reserve")
    public void reserve(@RequestBody PointReserveRequest request) throws InterruptedException {
        System.out.println("진입!!");
        if (count % 2 == 0) {
            count++;
            Thread.sleep(2000);
        }
        String key = "point:" + request.requestId();
        boolean acquiredLock = redisLockService.tryLock(key, request.requestId());

        if (!acquiredLock) {
            throw new RuntimeException("락 획득에 실패하였습니다.");
        }

        try {
            pointFacadeService.tryReserve(request.toCommand());
        } finally {
            redisLockService.releaseLock(key);
        }
    }

    @PostMapping("/point/confirm")
    public void confirm(@RequestBody PointReserveConfirmRequest request) {
        String key = "point:" + request.requestId();
        boolean acquiredLock = redisLockService.tryLock(key, request.requestId());

        if (!acquiredLock) {
            throw new RuntimeException("락 획득에 실패하였습니다.");
        }

        try {
            pointFacadeService.confirmReserve(request.toCommand());
        } finally {
            redisLockService.releaseLock(key);
        }
    }

    @PostMapping("/point/cancel")
    public void cancel(@RequestBody PointReserveCancelRequest request) {
        String key = "point:" + request.requestId();
        boolean acquiredLock = redisLockService.tryLock(key, request.requestId());

        if (!acquiredLock) {
            throw new RuntimeException("락 획득에 실패하였습니다.");
        }

        try {
            pointFacadeService.cancelReserve(request.toCommand());
        } finally {
            redisLockService.releaseLock(key);
        }
    }
}
