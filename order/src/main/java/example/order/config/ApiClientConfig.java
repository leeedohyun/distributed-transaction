package example.order.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import example.order.infrastructure.point.PointApiClient;
import example.order.infrastructure.product.ProductApiClient;

@Configuration
public class ApiClientConfig {

    @Bean
    public ProductApiClient productApiClient() {
        return new ProductApiClient(
                RestClient.builder()
                        .baseUrl("http://localhost:8082")
                        .build()
        );
    }

    @Bean
    public PointApiClient pointApiClient() {
        return new PointApiClient(
                RestClient.builder()
                        .baseUrl("http://localhost:8081")
                        .build()
        );
    }
}
