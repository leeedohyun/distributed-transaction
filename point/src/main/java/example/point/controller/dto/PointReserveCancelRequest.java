package example.point.controller.dto;

import example.point.application.dto.PointReserveCancelCommand;

public record PointReserveCancelRequest(String requestId) {

    public PointReserveCancelCommand toCommand() {
        return new PointReserveCancelCommand(requestId);
    }
}
