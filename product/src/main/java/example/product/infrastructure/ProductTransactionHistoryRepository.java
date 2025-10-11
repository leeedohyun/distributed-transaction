package example.product.infrastructure;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import example.product.domain.ProductTransactionHistory;

public interface ProductTransactionHistoryRepository extends JpaRepository<ProductTransactionHistory, Long> {

    List<ProductTransactionHistory> findAllByRequestIdAndTransactionType(
            String requestId,
            ProductTransactionHistory.TransactionType transactionType
    );
}
