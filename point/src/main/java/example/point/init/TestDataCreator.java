package example.point.init;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import example.point.domain.Point;
import example.point.infrastructure.PointRepository;

@Component
public class TestDataCreator {

    private final PointRepository pointRepository;

    public TestDataCreator(PointRepository pointRepository) {
        this.pointRepository = pointRepository;
    }

    @PostConstruct
    public void createTestData() {
        pointRepository.save(new Point(1L, 10000L));
    }
}
