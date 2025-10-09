package example.order.infrastructure.product;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import example.order.infrastructure.product.dto.ProductReserveApiRequest;
import example.order.infrastructure.product.dto.ProductReserveApiResponse;
import example.order.infrastructure.product.dto.ProductReserveCancelApiRequest;
import example.order.infrastructure.product.dto.ProductReserveConfirmApiRequest;

@Component
public class ProductApiClient {

    private final RestClient restClient;

    public ProductApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public ProductReserveApiResponse reserve(ProductReserveApiRequest request) {
        return restClient.post()
                .uri("/product/reserve")
                .body(request)
                .retrieve()
                .body(ProductReserveApiResponse.class);
    }

    public void confirm(ProductReserveConfirmApiRequest request) {
        restClient.post()
                .uri("/product/confirm")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void cancel(ProductReserveCancelApiRequest request) {
        restClient.post()
                .uri("/product/cancel")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
