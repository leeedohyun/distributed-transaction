package example.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.point.domain.Point;

public interface PointRepository extends JpaRepository<Point, Long> {

    Point findByUserId(Long userId);
}
