package example.point.controller.dto;

import example.point.application.dto.PointUseCommand;

public record PointUseRequest(String requestId, Long userId, Long amount) {

    public PointUseCommand toCommand() {
        return new PointUseCommand(requestId, userId, amount);
    }
}
