package lettrage.example.lettrage.config;

import lettrage.example.lettrage.model.Order;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LettrageProcessor {

    public static List<Order> applyLettrage(List<Order> orders) {
        // Group by compteClient and ordrePaiement (first 4 characters already handled)
        Map<String, List<Order>> groupedOrders = orders.stream()
                .collect(Collectors.groupingBy(order -> {
                    String ordrePaiement = order.getFacture();
                    // Handle null ordrePaiement
                    if (ordrePaiement != null && ordrePaiement.length() >= 4) {
                        return order.getCompteClient() + "-" + ordrePaiement.substring(0, 4);
                    } else {
                        // If ordrePaiement is null or doesn't have enough characters, handle accordingly
                        return order.getCompteClient() + "-UNKNOWN";
                    }
                }));

        for (List<Order> orderGroup : groupedOrders.values()) {
            // Separate positive and negative amounts
            List<Order> positiveOrders = orderGroup.stream()
                    .filter(order -> order.getMontant().compareTo(BigDecimal.ZERO) > 0)
                    .sorted(Comparator.comparing(Order::getDateTransaction).reversed()) // Newest first
                    .collect(Collectors.toList());

            List<Order> negativeOrders = orderGroup.stream()
                    .filter(order -> order.getMontant().compareTo(BigDecimal.ZERO) < 0)
                    .sorted(Comparator.comparing(Order::getDateTransaction).reversed()) // Newest first
                    .collect(Collectors.toList());

            // Process positive orders (payments) and match them to negative orders (debts)
            BigDecimal remainingNegative = BigDecimal.ZERO;

            for (Order positive : positiveOrders) {
                BigDecimal remainingPositive = positive.getMontant();
                remainingNegative = remainingNegative.add(remainingPositive); // Add positive amount to remaining negative

                // Apply the accumulated payment to negative orders
                for (Order negative : negativeOrders) {
                    if (remainingNegative.compareTo(BigDecimal.ZERO) <= 0) break; // No remaining payment

                    BigDecimal remainingNegativeAmount = negative.getMontant().abs();

                    if (remainingNegative.compareTo(remainingNegativeAmount) >= 0) {
                        // Fully cover the debt
                        remainingNegative = remainingNegative.subtract(remainingNegativeAmount);
                        negative.setNumeroLettrage("lettre"); // Fully covered
                    } else {
                        // Partially cover the debt
                        negative.setNumeroLettrage("partiellement_lettre");
                        remainingNegative = BigDecimal.ZERO;
                    }
                }

                // Mark positive entry based on remaining balance
                if (remainingNegative.compareTo(BigDecimal.ZERO) == 0) {
                    positive.setNumeroLettrage("lettre"); // Fully covered
                } else if (remainingNegative.compareTo(positive.getMontant()) < 0) {
                    positive.setNumeroLettrage("partiellement_lettre"); // Partially covered
                } else {
                    positive.setNumeroLettrage("non_lettre"); // Not covered at all
                }
            }

            // After applying all payments, check for any leftover debts
            for (Order negative : negativeOrders) {
                if (negative.getNumeroLettrage() == null) {
                    BigDecimal remainingNegativeAmount = negative.getMontant().abs();
                    boolean covered = false;

                    // Check if there is any unprocessed remainder from previous payments
                    if (remainingNegativeAmount.compareTo(BigDecimal.ZERO) > 0) {
                        for (Order positive : positiveOrders) {
                            if (positive.getNumeroLettrage() == null && remainingNegativeAmount.compareTo(BigDecimal.ZERO) > 0) {
                                BigDecimal paymentAmount = positive.getMontant();

                                if (paymentAmount.compareTo(remainingNegativeAmount) >= 0) {
                                    // Fully cover the debt
                                    negative.setNumeroLettrage("lettre");
                                    covered = true;
                                    break;
                                } else {
                                    // Partially cover the debt
                                    negative.setNumeroLettrage("partiellement_lettre");
                                    remainingNegativeAmount = remainingNegativeAmount.subtract(paymentAmount);
                                }
                            }
                        }
                    }

                    // If still not covered, mark as "non_lettre"
                    if (!covered) {
                        negative.setNumeroLettrage("non_lettre");
                    }
                }
            }

            for (Order order : orderGroup) {
                if (order.getNumeroLettrage() == null) {
                    order.setNumeroLettrage("non_lettre"); // Default to non_lettre if no lettrage was applied
                }
            }
        }
        return orders;
    }
}
