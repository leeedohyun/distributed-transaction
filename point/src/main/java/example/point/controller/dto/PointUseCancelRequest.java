package example.point.controller.dto;

import example.point.application.dto.PointUseCancelCommand;

public record PointUseCancelRequest(String requestId) {

    public PointUseCancelCommand toCommand() {
        return new PointUseCancelCommand(requestId);
    }
}
