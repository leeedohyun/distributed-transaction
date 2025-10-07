package example.point.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "point_reservations")
public class PointReservation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId;

    private Long pointId;

    private Long reservedAmount;

    @Enumerated(EnumType.STRING)
    private PointReservationStatus status;

    public PointReservation() {
    }

    public PointReservation(String requestId, Long pointId, Long reservedAmount) {
        this.requestId = requestId;
        this.pointId = pointId;
        this.reservedAmount = reservedAmount;
        this.status = PointReservationStatus.RESERVED;
    }

    public void confirm() {
        if (this.status == PointReservationStatus.CANCELLED) {
            throw new RuntimeException("취소된 예약은 확정할 수 없습니다.");
        }

        this.status = PointReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (this.status == PointReservationStatus.CONFIRMED) {
            throw new RuntimeException("확정된 예약은 취소할 수 없습니다.");
        }

        this.status = PointReservationStatus.CANCELLED;
    }

    public Long getPointId() {
        return pointId;
    }

    public Long getReservedAmount() {
        return reservedAmount;
    }

    public PointReservationStatus getStatus() {
        return status;
    }

    public enum PointReservationStatus {
        RESERVED, CONFIRMED, CANCELLED
    }
}
