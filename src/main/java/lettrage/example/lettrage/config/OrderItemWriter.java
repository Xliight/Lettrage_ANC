//package lettrage.example.lettrage.config;
//
//import lettrage.example.lettrage.model.Order;
//import lettrage.example.lettrage.repository.OrderRepository;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//public class OrderItemWriter implements ItemWriter<Order> {
//
//    private final OrderRepository orderRepository;
//
//    public OrderItemWriter(OrderRepository orderRepository) {
//        this.orderRepository = orderRepository;
//    }
//
//
//    @Override
//    @Transactional
//    public void write(Chunk<? extends Order> chunk) throws Exception {
//        orderRepository.saveAll(chunk); // Bulk save for performance
//
//    }
//}
