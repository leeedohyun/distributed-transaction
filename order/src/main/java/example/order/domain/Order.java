package example.order.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public Order() {
        status = OrderStatus.CREATED;
    }

    public void reserve() {
        if (this.status != OrderStatus.CREATED) {
            throw new RuntimeException("생성된 단계에서만 예약할 수 있습니다.");
        }

        this.status = OrderStatus.RESERVED;
    }

    public void cancel() {
        if (this.status != OrderStatus.RESERVED) {
            throw new RuntimeException("예약된 단계에서만 취소할 수 있습니다.");
        }

        this.status = OrderStatus.CANCELLED;
    }

    public Long getId() {
        return id;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void complete() {
        status = OrderStatus.COMPLETED;
    }

    public void confirm() {
        if (this.status != OrderStatus.RESERVED && this.status != OrderStatus.PENDING) {
            throw new RuntimeException("예약된 단계 혹은 Pending에서만 확정할 수 있습니다.");
        }

        this.status = OrderStatus.CONFIRMED;
    }

    public void pending() {
        if (this.status != OrderStatus.RESERVED) {
            throw new RuntimeException("예약된 단계에서만 확정할 수 있습니다.");
        }

        this.status = OrderStatus.PENDING;
    }

    public enum OrderStatus {
        CREATED,
        RESERVED,
        CANCELLED,
        CONFIRMED,
        PENDING,
        COMPLETED
    }
}
