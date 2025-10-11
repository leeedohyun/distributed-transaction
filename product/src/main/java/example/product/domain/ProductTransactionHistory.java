package example.product.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_transaction_histories")
public class ProductTransactionHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId;

    private Long productId;

    private Long quantity;

    private Long price;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    public ProductTransactionHistory() {
    }

    public ProductTransactionHistory(String requestId, Long productId, Long quantity, Long price, TransactionType transactionType) {
        this.requestId = requestId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
        this.transactionType = transactionType;
    }

    public Long getPrice() {
        return price;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public enum TransactionType {
        PURCHASE, CANCEL
    }
}
