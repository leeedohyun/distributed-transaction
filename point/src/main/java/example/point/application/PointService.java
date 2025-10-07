package example.point.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import example.point.application.dto.PointReserveCommand;
import example.point.domain.Point;
import example.point.domain.PointReservation;
import example.point.infrastructure.PointRepository;
import example.point.infrastructure.PointReservationRepository;

@Service
public class PointService {

    private final PointRepository pointRepository;
    private final PointReservationRepository pointReservationRepository;

    public PointService(PointRepository pointRepository, PointReservationRepository pointReservationRepository) {
        this.pointRepository = pointRepository;
        this.pointReservationRepository = pointReservationRepository;
    }

    @Transactional
    public void tryReserve(PointReserveCommand command) {
        PointReservation reservation = pointReservationRepository.findByRequestId(command.requestId());

        if (reservation != null) {
            System.out.println("이미 예약된 요청입니다.");
            return;
        }

        Point point = pointRepository.findByUserId(command.userId());

        point.reserve(command.reserveAmount());
        pointReservationRepository.save(
                new PointReservation(
                        command.requestId(),
                        point.getId(),
                        command.reserveAmount()
                )
        );
    }
}
