package kafkaredis.marketpulse.controller;

import jakarta.validation.constraints.NotBlank;
import kafkaredis.marketpulse.dto.StockQuoteDTO;
import kafkaredis.marketpulse.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@Validated
public class StockController {

    private final StockService stockService;

    @GetMapping("/{symbol}")
    public ResponseEntity<StockQuoteDTO> getStock(@PathVariable @NotBlank String symbol) {
        return ResponseEntity.ok(stockService.getStock(symbol));
    }
}
