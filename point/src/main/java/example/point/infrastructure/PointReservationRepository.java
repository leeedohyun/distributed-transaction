package example.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.point.domain.PointReservation;

public interface PointReservationRepository extends JpaRepository<PointReservation, Long> {

    PointReservation findByRequestId(String requestId);
}
