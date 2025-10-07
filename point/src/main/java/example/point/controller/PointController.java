package example.point.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import example.point.application.PointFacadeService;
import example.point.application.RedisLockService;
import example.point.controller.dto.PointReserveRequest;

@RestController
public class PointController {

    private final PointFacadeService pointFacadeService;
    private final RedisLockService redisLockService;

    public PointController(PointFacadeService pointFacadeService, RedisLockService redisLockService) {
        this.pointFacadeService = pointFacadeService;
        this.redisLockService = redisLockService;
    }

    @PostMapping("/point/reserve")
    public void reserve(@RequestBody PointReserveRequest request) {
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
}
