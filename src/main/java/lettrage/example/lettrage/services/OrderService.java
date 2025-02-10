package lettrage.example.lettrage.services;

import lettrage.example.lettrage.config.LettrageProcessor;
import lettrage.example.lettrage.config.OrderProcessor;
import lettrage.example.lettrage.model.Order;
import lettrage.example.lettrage.repository.OrderRepository;

import java.io.IOException;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static lettrage.example.lettrage.config.OrderProcessor.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private LettrageProcessor lettrageProcessor;
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;

    }

    public List<Order> getOrders(int limit) {
        Pageable pageable = PageRequest.of(0, limit); // Fetch 'limit' orders
        Page<Order> ordersPage = orderRepository.findAll(pageable);
        return ordersPage.getContent();               // Correct: Get the list from the Page
    }
    public void processExcel(MultipartFile file) throws IOException {
        if (!isValidExcelFile(file)) {
            logger.error("file isnt a excel file");
            throw new RuntimeException("file isnt a excel file");
        }
        logger.info("Converting  Data from excel  ");
        List<Order> orders = OrderProcessor.getOrdersFromExcel(file.getInputStream());
        logger.info("Sort  Data in batch  ");
        Map<String, Map<String, Map<String, List<Order>>>> SortData= SortData(orders);
        logger.info("Convert  Data in batch  ");

        List<Order> sortedOrderEntities = convertToSortedOrderEntities(SortData);


        logger.info("Save  Data in batch  ");
        saveSortedOrdersInBatches(sortedOrderEntities,10000);

    }


    @Transactional
    public void saveSortedOrdersInBatches(List<Order> sortedOrderEntities, int batchSize) {
        int totalEntities = sortedOrderEntities.size();
        for (int i = 0; i < totalEntities; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalEntities);
            List<Order> batch = sortedOrderEntities.subList(i, endIndex);
            orderRepository.saveAll(batch);
            logger.info("Saved batch from index " + i + " to " + (endIndex - 1));
        }

    }

    public List<Order> processLettrage() {
        List<Order> list=orderRepository.findAll();
        lettrageProcessor.applyLettrage(list);
        saveSortedOrdersInBatches(list,10000);
        return list;
    }
    public List<Order> getAllOrders() {
        // Retrieve all orders from the database
        return orderRepository.findAll();
    }




    }
