package lettrage.example.lettrage.services;

import lettrage.example.lettrage.model.Order;
import lettrage.example.lettrage.model.SortedOrder;
import lettrage.example.lettrage.repository.OrderRepository;
import lettrage.example.lettrage.repository.SortedOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service

public class OrderSortedService {

    private final OrderRepository orderRepository;
    private final SortedOrderRepository sortedOrderRepository;

    public OrderSortedService(OrderRepository orderRepository, SortedOrderRepository sortedOrderRepository) {
        this.orderRepository = orderRepository;
        this.sortedOrderRepository = sortedOrderRepository;
    }

    @Transactional
    public void groupSortAndSaveOrders() {
        List<Order> orders = orderRepository.findAll();

        // Group orders by compteClient, then by profilValidation, then by the first 4 digits of facture
        Map<String, Map<String, Map<String, List<Order>>>> groupedOrders = orders.stream()
                .sorted(Comparator.comparing(Order::getDateTransaction, Comparator.nullsLast(Comparator.reverseOrder()))) // Sort from oldest to newest
                .collect(Collectors.groupingBy(
                        Order::getCompteClient, // Group by compteClient
                        LinkedHashMap::new,
                        Collectors.groupingBy(
                                Order::getProfilValidation, // Group by profilValidation
                                LinkedHashMap::new,
                                Collectors.groupingBy(
                                        order -> extractFacturePrefix(order.getFacture()), // Group by the first 4 digits of facture
                                        LinkedHashMap::new,
                                        Collectors.collectingAndThen(
                                                Collectors.toList(),
                                                list -> list.stream()
                                                        .sorted(Comparator.comparing(Order::getDateTransaction, Comparator.nullsLast(Comparator.reverseOrder()))) // Sort by dateTransaction (oldest to newest)
                                                        .collect(Collectors.toList())
                                        )
                                )
                        )
                ));

        // Convert the grouped and sorted orders to SortedOrder entities
        List<SortedOrder> sortedOrderEntities = convertToSortedOrderEntities(groupedOrders);

        // Save all sorted orders into the SortedOrder table
        sortedOrderRepository.saveAll(sortedOrderEntities);
    }

    // Helper method to extract the first 4 digits of the facture
    private String extractFacturePrefix(String facture) {
        if (facture != null && facture.length() >= 4) {
            return facture.substring(0, 4); // First 4 digits of the facture
        }
        return ""; // Default empty string if facture is null or too short
    }

    // Convert the grouped orders to SortedOrder entities for saving in the database
    private List<SortedOrder> convertToSortedOrderEntities(Map<String, Map<String, Map<String, List<Order>>>> groupedOrders) {
        List<SortedOrder> sortedOrderEntities = new ArrayList<>();

        for (Map.Entry<String, Map<String, Map<String, List<Order>>>> compteClientEntry : groupedOrders.entrySet()) {
            for (Map.Entry<String, Map<String, List<Order>>> profilValidationEntry : compteClientEntry.getValue().entrySet()) {
                for (Map.Entry<String, List<Order>> factureEntry : profilValidationEntry.getValue().entrySet()) {
                    List<Order> orders = factureEntry.getValue();
                    for (Order order : orders) {
                        SortedOrder sortedOrder = convertToSortedOrder(order);
                        sortedOrderEntities.add(sortedOrder); // Add to the list for batch save
                    }
                }
            }
        }

        return sortedOrderEntities;
    }

    private SortedOrder convertToSortedOrder(Order order) {
        return SortedOrder.builder()
                .compteClient(order.getCompteClient())
                .profilValidation(order.getProfilValidation())
                .montant(order.getMontant())
                .montantRegle(order.getMontantRegle())
                .devise(order.getDevise())
                .settlement(order.getSettlement())
                .numeroDocument(order.getNumeroDocument())
                .ordrePaiement(order.getOrdrePaiement())
                .dateDocument(order.getDateDocument())
                .documentNum(order.getDocumentNum())
                .dateEcheance(order.getDateEcheance())
                .facture(order.getFacture())
                .dateTransaction(order.getDateTransaction())
                .dateDernierReglement(order.getDateDernierReglement())
                .lastSettleVoucher(order.getLastSettleVoucher())
                .numeroLettrage(order.getNumeroLettrage())
                .build();
    }
}
