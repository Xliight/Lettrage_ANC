package lettrage.example.lettrage.controller;

import com.opencsv.exceptions.CsvValidationException;
import lettrage.example.lettrage.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        return new ResponseEntity<>(orderService.getOrders(), HttpStatus.FOUND);
    }
}
