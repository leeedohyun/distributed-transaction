package example.point.application;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import example.point.application.dto.PointReserveCommand;

@Component
public class PointFacadeService {

    private final PointService pointService;

    public PointFacadeService(PointService pointService) {
        this.pointService = pointService;
    }

    public void tryReserve(PointReserveCommand command) {
        int tryCount = 0;

        while (tryCount < 3) {
            try {
                pointService.tryReserve(command);
                return;
            } catch (ObjectOptimisticLockingFailureException e) {
                tryCount++;
            }
        }

        throw new RuntimeException("예약에 실패하였습니다.");
    }
}
