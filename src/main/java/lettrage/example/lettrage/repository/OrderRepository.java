package lettrage.example.lettrage.repository;

import lettrage.example.lettrage.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
