package lettrage.example.lettrage.services;

import lettrage.example.lettrage.config.OrderProcessor;
import lettrage.example.lettrage.model.Order;
import lettrage.example.lettrage.repository.OrderRepository;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static lettrage.example.lettrage.config.OrderProcessor.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    public OrderService(OrderRepository orderRepository ) {
        this.orderRepository = orderRepository;
    }

    public List<Order> getOrders() {
        return this.orderRepository.findAll();
    }

    public void processExcel(MultipartFile file) throws IOException {
        if (isValidExcelFile(file)) {
            logger.error("file isnt a excel file");
            throw new RuntimeException("file isnt a excel file");
        }
        logger.info("Converting  Data from excel  ");
        List<Order> orders = OrderProcessor.getOrdersFromExcel(file.getInputStream());
        logger.info("Sort  Data in batch  ");
        Map<String, Map<String, Map<String, List<Order>>>> SortData= SortData(orders);
        logger.info("Convert  Data in batch  ");

        List<Order> sortedOrderEntities = convertToSortedOrderEntities(SortData);
        logger.info("Split  Data in batch  ");

//        List<List<Order>> batches= splitIntoBatches(sortedOrderEntities,5000);

        logger.info("Save  Data in batch  ");
        saveSortedOrdersInBatches(sortedOrderEntities,5000);


    }
    @Transactional
    public void saveSortedOrdersInBatches(List<Order> sortedOrderEntities, int batchSize) {
        int totalEntities = sortedOrderEntities.size();
        for (int i = 0; i < totalEntities; i += batchSize) {
            // Calculate the end index for the current batch
            int endIndex = Math.min(i + batchSize, totalEntities);
            // Extract the sublist for the current batch
            List<Order> batch = sortedOrderEntities.subList(i, endIndex);
            // Save the current batch
            orderRepository.saveAll(batch);
            // Log the progress (optional)
            logger.info("Saved batch from index " + i + " to " + (endIndex - 1));
        }
    }

    private void processOrders(List<Order> orders) {
        try {
            // Print batch details to console
          logger.info("Processing batch of " + orders.size() + " orders...");

            // Save the orders to the database
            orderRepository.saveAll(orders);
           logger.info("Successfully saved batch of " + orders.size() + " orders.");
        } catch (Exception e) {
            logger.error("Error saving batch: " + e.getMessage());
        }
    }

}
