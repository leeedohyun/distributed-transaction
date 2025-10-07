package example.point.controller.dto;

import example.point.application.PointReserveConfirmCommand;

public record PointReserveConfirmRequest(String requestId) {

    public PointReserveConfirmCommand toCommand() {
        return new PointReserveConfirmCommand(requestId);
    }
}
