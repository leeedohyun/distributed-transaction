package example.order.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

import example.order.domain.CompensationRegistry;

public interface CompensationRegistryRepository extends JpaRepository<CompensationRegistry, Long> {
}
