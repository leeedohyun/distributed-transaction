package example.point.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.point.application.dto.PointUseCommand;
import example.point.domain.Point;
import example.point.domain.PointTransactionHistory;
import example.point.domain.PointTransactionHistory.TransactionType;
import example.point.infrastructure.PointRepository;
import example.point.infrastructure.PointTransactionHistoryRepository;

@Service
public class PointService {

    private final PointRepository pointRepository;
    private final PointTransactionHistoryRepository pointTransactionHistoryRepository;

    public PointService(PointRepository pointRepository, PointTransactionHistoryRepository pointTransactionHistoryRepository) {
        this.pointRepository = pointRepository;
        this.pointTransactionHistoryRepository = pointTransactionHistoryRepository;
    }

    @Transactional
    public void use(PointUseCommand command) {
        PointTransactionHistory useHistory = pointTransactionHistoryRepository.findByRequestIdAndTransactionType(
                command.requestId(),
                TransactionType.USE
        );

        if (useHistory != null) {
            System.out.println("이미 사용한 이력이 존재합니다.");
            return;
        }

        Point point = pointRepository.findByUserId(command.userId());

        if (point == null) {
            throw new RuntimeException("포인트가 존재하지 않습니다.");
        }

        point.use(command.amount());
        pointTransactionHistoryRepository.save(
                new PointTransactionHistory(
                        command.requestId(),
                        point.getId(),
                        command.amount(),
                        TransactionType.USE
                )
        );
    }
}
