package lettrage.example.lettrage.repository;

import lettrage.example.lettrage.model.SortedOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface SortedOrderRepository extends JpaRepository<SortedOrder, UUID> {
}
