package lettrage.example.lettrage.services;

import lettrage.example.lettrage.config.OrderProcessor;
import lettrage.example.lettrage.model.Order;
import lettrage.example.lettrage.repository.OrderRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository ) {
        this.orderRepository = orderRepository;
    }

//    public Map<String, Map<String, Map<String, List<Order>>>> groupOrders() {
//        List<Order> orders = orderRepository.findAll();
//
//        return orders.stream()
//                .sorted(Comparator.comparing(Order::getDateTransaction, Comparator.nullsLast(Comparator.naturalOrder())))
//                .collect(Collectors.groupingBy(
//                        Order::getCompteClient, // Group by compteClient
//                        LinkedHashMap::new,
//                        Collectors.groupingBy(
//                                Order::getProfilValidation, // Group by profilValidation
//                                LinkedHashMap::new,
//                                Collectors.groupingBy(
//                                        order -> extractFacturePrefix(order.getFacture()), // Group by first 4 digits of facture
//                                        LinkedHashMap::new,
//                                        Collectors.toList()
//                                )
//                        )
//                ));
//    }
//
//    private String extractFacturePrefix(String facture) {
//        return (facture != null && facture.length() >= 4) ? facture.substring(0, 4) : "UNKNOWN";
//    }
//



















    public List<Order> getOrders() {
        return this.orderRepository.findAll();
    }

    public void processExcel(MultipartFile file) throws IOException {
        // Use the getOrdersFromExcel method to process the Excel file and get a list of orders
        List<Order> orders = OrderProcessor.getOrdersFromExcel(file.getInputStream());

        List<List<Order>> batches = splitIntoBatches(orders, 5000);

        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor(); // Virtual threads

        List<CompletableFuture<Void>> futures = batches
                .stream()
                .map(batch -> CompletableFuture.runAsync(() -> processOrders(batch), executorService))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        shutdownExecutor(executorService);
    }

    private void processOrders(List<Order> orders) {
        try {
            // Print batch details to console
            System.out.println("Processing batch of " + orders.size() + " orders...");

            // Save the orders to the database
            orderRepository.saveAll(orders);
            System.out.println("Successfully saved batch of " + orders.size() + " orders.");
        } catch (Exception e) {
            System.err.println("Error saving batch: " + e.getMessage());
        }
    }

    private List<List<Order>> splitIntoBatches(List<Order> orders, int batchSize) {
        int totalSize = orders.size();
        int batchNums = (totalSize + batchSize - 1) / batchSize;

        List<List<Order>> batches = new ArrayList<>();
        for (int i = 0; i < batchNums; i++) {
            int start = i * batchSize;
            int end = Math.min(totalSize, (i + 1) * batchSize);
            batches.add(orders.subList(start, end));
        }

        return batches;
    }

    private void shutdownExecutor(ExecutorService executorServicee) {
        try {
            executorServicee.shutdown();
            if (!executorServicee.awaitTermination(60, TimeUnit.SECONDS)) {
                executorServicee.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorServicee.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
