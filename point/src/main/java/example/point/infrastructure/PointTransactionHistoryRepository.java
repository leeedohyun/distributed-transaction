package example.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.point.domain.PointTransactionHistory;
import example.point.domain.PointTransactionHistory.TransactionType;

public interface PointTransactionHistoryRepository extends JpaRepository<PointTransactionHistory, Long> {

    PointTransactionHistory findByRequestIdAndTransactionType(String requestId, TransactionType transactionType);
}
