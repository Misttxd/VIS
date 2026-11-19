package cz.gastro.controller;

import cz.gastro.dto.DeliveryRequest;
import cz.gastro.dto.StockOverview;
import cz.gastro.model.Delivery;
import cz.gastro.model.StockLevel;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import cz.gastro.service.StockService;

@RestController
@RequestMapping("/api")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/deliveries")
    public List<Delivery> deliveries() {
        return stockService.deliveries();
    }

    @PostMapping("/deliveries")
    public long receive(@RequestBody DeliveryRequest delivery) {
        return stockService.receive(delivery);
    }

    @PostMapping("/deliveries/{id}/withdraw")
    public void withdraw(@PathVariable long id, @RequestParam BigDecimal quantity) {
        stockService.withdraw(id, quantity);
    }

    @GetMapping("/stock")
    public List<StockLevel> levels(@RequestParam LocalDate day) {
        return stockService.levels(day);
    }

    @GetMapping("/stock/overview")
    public StockOverview overview(@RequestParam LocalDate day) {
        return stockService.overview(day);
    }

    @GetMapping("/expiring")
    public List<Delivery> expiring(@RequestParam LocalDate day) {
        return stockService.expiring(day);
    }
}
