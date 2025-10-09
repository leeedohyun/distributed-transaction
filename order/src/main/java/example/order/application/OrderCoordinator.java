package example.order.application;

import org.springframework.stereotype.Component;

import example.order.application.dto.OrderDto;
import example.order.application.dto.PlaceOrderCommand;
import example.order.infrastructure.point.PointApiClient;
import example.order.infrastructure.point.dto.PointReserveApiRequest;
import example.order.infrastructure.point.dto.PointReserveCancelApiRequest;
import example.order.infrastructure.point.dto.PointReserveConfirmApiRequest;
import example.order.infrastructure.product.ProductApiClient;
import example.order.infrastructure.product.dto.ProductReserveApiRequest;
import example.order.infrastructure.product.dto.ProductReserveApiRequest.ReserveItem;
import example.order.infrastructure.product.dto.ProductReserveApiResponse;
import example.order.infrastructure.product.dto.ProductReserveCancelApiRequest;
import example.order.infrastructure.product.dto.ProductReserveConfirmApiRequest;

@Component
public class OrderCoordinator {

    private final OrderService orderService;
    private final ProductApiClient productApiClient;
    private final PointApiClient pointApiClient;

    public OrderCoordinator(OrderService orderService, ProductApiClient productApiClient, PointApiClient pointApiClient) {
        this.orderService = orderService;
        this.productApiClient = productApiClient;
        this.pointApiClient = pointApiClient;
    }

    public void placeOrder(PlaceOrderCommand command) {
        reserve(command.orderId());
        confirm(command.orderId());
    }

    private void reserve(Long orderId) {
        String requestId = orderId.toString();
        orderService.reserve(orderId);

        try {
            OrderDto orderInfo = orderService.getOrder(orderId);

            ProductReserveApiRequest productReserveApiRequest = new ProductReserveApiRequest(
                    requestId,
                    orderInfo.orderItems().stream()
                            .map(
                                    orderItem -> new ReserveItem(
                                            orderItem.productId(),
                                            orderItem.quantity()
                                    )
                            ).toList()
            );

            ProductReserveApiResponse productReserveApiResponse = productApiClient.reserve(productReserveApiRequest);

            PointReserveApiRequest pointReserveApiRequest = new PointReserveApiRequest(
                    requestId,
                    1L,
                    productReserveApiResponse.totalPrice()
            );

            pointApiClient.reservePoint(pointReserveApiRequest);
        } catch (Exception e) {
            orderService.cancel(orderId);
            ProductReserveCancelApiRequest productReserveCancelApiRequest = new ProductReserveCancelApiRequest(requestId);

            productApiClient.cancel(productReserveCancelApiRequest);

            PointReserveCancelApiRequest pointReserveCancelApiRequest = new PointReserveCancelApiRequest(requestId);

            pointApiClient.cancelPoint(pointReserveCancelApiRequest);
        }
    }

    public void confirm(Long orderId) {
        String requestId = orderId.toString();
        try {
            ProductReserveConfirmApiRequest productReserveConfirmApiRequest = new ProductReserveConfirmApiRequest(requestId);
            productApiClient.confirm(productReserveConfirmApiRequest);
            PointReserveConfirmApiRequest pointReserveConfirmApiRequest = new PointReserveConfirmApiRequest(requestId);
            pointApiClient.confirmPoint(pointReserveConfirmApiRequest);

            orderService.confirm(orderId);
        } catch (Exception e) {
            orderService.pending(orderId);
            throw e;
        }
    }
}
