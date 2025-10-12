package example.point.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.point.application.dto.PointUseCancelCommand;
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

    @Transactional
    public void cancel(PointUseCancelCommand command) {
        PointTransactionHistory useHistory = pointTransactionHistoryRepository.findByRequestIdAndTransactionType(
                command.requestId(),
                TransactionType.USE
        );

        // 원래는 사용 이력이 없으면 예외를 던지도록 되어 있었음.
        // 하지만 보상 트랜잭션 수행 시 예외를 던지면 retry 로직이 실행됨.
        // 따라서 사용 이력이 없더라도 정상적으로 처리하도록 변경
        if (useHistory == null) {
            return;
        }

        PointTransactionHistory cancelHistory = pointTransactionHistoryRepository.findByRequestIdAndTransactionType(
                command.requestId(),
                TransactionType.CANCEL
        );

        if (cancelHistory != null) {
            System.out.println("이미 취소된 요청입니다.");
            return;
        }

        Point point = pointRepository.findById(useHistory.getPointId()).orElseThrow();

        point.cancel(useHistory.getAmount());
        pointTransactionHistoryRepository.save(
                new PointTransactionHistory(
                        command.requestId(),
                        point.getId(),
                        useHistory.getAmount(),
                        TransactionType.CANCEL
                )
        );
    }
}
