package lettrage.example.lettrage.controller;

import com.opencsv.exceptions.CsvValidationException;
import lettrage.example.lettrage.config.OrderProcessor;
import lettrage.example.lettrage.model.Order;
import lettrage.example.lettrage.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;


    @PostMapping(value = "/import", consumes = {"multipart/form-data"})
    public ResponseEntity<?> importOrders(@RequestParam("file") MultipartFile file) throws IOException, CsvValidationException {
        orderService.processExcel(file);
        return ResponseEntity.ok(Map.of("message", "Successfully imported orders"));
    }
    @GetMapping("/{num}")
    public ResponseEntity<?> getAllOrders(@PathVariable int num) {
        return new ResponseEntity<>(orderService.getOrders(num), HttpStatus.FOUND);
    }
    @PostMapping("/apply")
    public void applyLettrage() {
        orderService.processLettrage();
    }

    @GetMapping("/export/orders")
    public ResponseEntity<byte[]> exportOrdersToCsv(@RequestParam(value = "fileName", defaultValue = "orders.csv") String fileName) {
        // Fetch the orders from the database
        List<Order> orders = orderService.getAllOrders(); // Adjust based on your service

        // Create a temporary output stream to store the CSV data
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // Write orders to CSV format using OrderProcessor
            OrderProcessor.writeOrdersToCsv(orders, String.valueOf(outputStream));

            // Prepare the response with the CSV file
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            // Convert the byte array output stream to byte array and return as response
            return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
