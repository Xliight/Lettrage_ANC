package lettrage.example.lettrage.config;

import lettrage.example.lettrage.model.Order;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class OrderProcessor {

    public  static boolean isValidExcelFile(MultipartFile file) {
        return Objects.equals(file.getContentType(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" );
    }


    public static List<Order> getOrdersFromExcel(InputStream inputStream) {
        List<Order> orders = new ArrayList<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheet("orders");
            int rowws = sheet.getLastRowNum();  // Get the last row number

            // Loop through rows, skipping the header (start at row 1)
            for (int r = 1; r <= rowws; r++) {
                Order order = new Order();
                XSSFRow row1 = sheet.getRow(r);

                // Ensure the row is not null to avoid NullPointerException
                if (row1 != null) {
                    // Process each cell based on its index and type
                    order.setCompteClient(getCellValue(row1.getCell(0)));
                    order.setProfilValidation(getCellValue(row1.getCell(1)));
                    order.setMontant(getCellBigDecimal(row1.getCell(2)));
                    order.setMontantRegle(getCellBigDecimal(row1.getCell(3)));
                    order.setDevise(getCellValue(row1.getCell(4)));
                    order.setSettlement(getCellLong(row1.getCell(5)));
                    order.setNumeroDocument(getCellValue(row1.getCell(6)));
                    order.setOrdrePaiement(getCellValue(row1.getCell(7)));

                    // Parse dates where applicable
                    order.setDateDocument(parseDate(row1.getCell(8)));
                    order.setDocumentNum(getCellValue(row1.getCell(9)));
                    order.setDateEcheance(parseDate(row1.getCell(10)));
                    order.setFacture(getCellValue(row1.getCell(11)));
                    order.setDateTransaction(parseDate(row1.getCell(12)));
                    order.setDateDernierReglement(parseDate(row1.getCell(13)));
                    order.setLastSettleVoucher(getCellValue(row1.getCell(14)));
                    order.setNumeroLettrage(getCellValue(row1.getCell(15)));
                }

                // Add order to list
                orders.add(order);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error reading Excel file", e);
        }

        return orders;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private static BigDecimal getCellBigDecimal(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        return BigDecimal.valueOf(cell.getNumericCellValue());
    }

    private static Long getCellLong(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return null;
        }
        return (long) cell.getNumericCellValue();
    }

    private static LocalDateTime parseDate(Cell cell) {
        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.STRING) {
            String cellValue = cell.getStringCellValue();
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
                return LocalDateTime.parse(cellValue, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format: " + cellValue);
                return null;
            }
        } else if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        return null;
    }

    public static   Map<String, Map<String, Map<String, List<Order>>>> SortData(List<Order> orders){
       return orders.stream()
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


    }
    public static List<Order> convertToSortedOrderEntities(Map<String, Map<String, Map<String, List<Order>>>> groupedOrders) {
        List<Order> OrderEntities = new ArrayList<>();

        for (Map.Entry<String, Map<String, Map<String, List<Order>>>> compteClientEntry : groupedOrders.entrySet()) {
            for (Map.Entry<String, Map<String, List<Order>>> profilValidationEntry : compteClientEntry.getValue().entrySet()) {
                for (Map.Entry<String, List<Order>> factureEntry : profilValidationEntry.getValue().entrySet()) {
                    List<Order> orders = factureEntry.getValue();
                    for (Order order : orders) {
                        Order sortedOrder = convertToSortedOrder(order);
                        OrderEntities.add(sortedOrder); // Add to the list for batch save
                    }
                }
            }
        }

        return OrderEntities;
    }

    public static Order convertToSortedOrder(Order order) {
        return Order.builder()
                .compteClient(order.getCompteClient())
                .profilValidation(String.valueOf(order.getProfilValidation()))
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





    private static String extractFacturePrefix(String facture) {
        if (facture != null && facture.length() >= 4) {
            return facture.substring(0, 4); // First 4 digits of the facture
        }
        return ""; // Default empty string if facture is null or too short
    }
//    public static <T> List<List<T>> splitIntoBatches(List<T> items, int batchSize) {
//        int totalSize = items.size();
//        int batchNums = (totalSize + batchSize - 1) / batchSize;
//
//        List<List<T>> batches = new ArrayList<>();
//        for (int i = 0; i < batchNums; i++) {
//            int start = i * batchSize;
//            int end = Math.min(totalSize, (i + 1) * batchSize);
//            batches.add(items.subList(start, end));
//        }
//
//        return batches;
//    }


    public static void shutdownExecutor(ExecutorService executorServicee) {
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

