package example.order.application;

import org.springframework.stereotype.Component;

import example.order.application.dto.OrderDto;
import example.order.application.dto.PlaceOrderCommand;
import example.order.domain.CompensationRegistry;
import example.order.infrastructure.CompensationRegistryRepository;
import example.order.infrastructure.point.PointApiClient;
import example.order.infrastructure.point.PointUseApiRequest;
import example.order.infrastructure.point.PointUseCancelApiRequest;
import example.order.infrastructure.product.ProductApiClient;
import example.order.infrastructure.product.ProductBuyApiRequest;
import example.order.infrastructure.product.ProductBuyApiRequest.ProductInfo;
import example.order.infrastructure.product.ProductBuyApiResponse;
import example.order.infrastructure.product.ProductBuyCancelApiRequest;
import example.order.infrastructure.product.ProductBuyCancelApiResponse;

@Component
public class OrderCoordinator {

    private final OrderService orderService;
    private final CompensationRegistryRepository compensationRegistryRepository;
    private final ProductApiClient productApiClient;
    private final PointApiClient pointApiClient;

    public OrderCoordinator(OrderService orderService, CompensationRegistryRepository compensationRegistryRepository, ProductApiClient productApiClient, PointApiClient pointApiClient) {
        this.orderService = orderService;
        this.compensationRegistryRepository = compensationRegistryRepository;
        this.productApiClient = productApiClient;
        this.pointApiClient = pointApiClient;
    }

    public void placeOrder(PlaceOrderCommand command) {
        orderService.request(command.orderId());
        OrderDto orderDto = orderService.getOrder(command.orderId());

        try {
            ProductBuyApiRequest productBuyApiRequest = new ProductBuyApiRequest(
                    command.orderId().toString(),
                    orderDto.orderItems().stream()
                            .map(item -> new ProductInfo(item.productId(), item.quantity()))
                            .toList()
            );

            ProductBuyApiResponse buyApiResponse = productApiClient.buy(productBuyApiRequest);

            PointUseApiRequest pointUseApiRequest = new PointUseApiRequest(
                    command.orderId().toString(),
                    1L,
                    buyApiResponse.totalPrice()
            );

            pointApiClient.use(pointUseApiRequest);

            orderService.complete(command.orderId());
        } catch (Exception e) {
            rollback(command.orderId());

            throw e;
        }
    }

    public void rollback(Long orderId) {
        try {
            ProductBuyCancelApiRequest productBuyCancelApiRequest = new ProductBuyCancelApiRequest(orderId.toString());

            ProductBuyCancelApiResponse productBuyCancelApiResponse = productApiClient.cancel(productBuyCancelApiRequest);

            if (productBuyCancelApiResponse.totalPrice() > 0) {
                PointUseCancelApiRequest pointUseCancelApiRequest = new PointUseCancelApiRequest(orderId.toString());

                pointApiClient.cancel(pointUseCancelApiRequest);
            }

            orderService.fail(orderId);
        } catch (Exception e) {
            compensationRegistryRepository.save(
                    new CompensationRegistry(orderId)
            );
            throw e;
        }
    }
}
