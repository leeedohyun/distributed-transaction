package example.point.application.dto;

public record PointUseCommand(String requestId, Long userId, Long amount) {
}
