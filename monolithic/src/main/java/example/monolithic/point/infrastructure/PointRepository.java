package example.monolithic.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.monolithic.point.domain.Point;

public interface PointRepository extends JpaRepository<Point, Long> {

    Point findByUserId(Long userId);
}
