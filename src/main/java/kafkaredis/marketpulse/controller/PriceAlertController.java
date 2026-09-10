package kafkaredis.marketpulse.controller;

import jakarta.validation.Valid;
import kafkaredis.marketpulse.dto.CreatePriceAlertRequestDTO;
import kafkaredis.marketpulse.dto.PriceAlertDTO;
import kafkaredis.marketpulse.service.PriceAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/alerts")
@RequiredArgsConstructor
@Validated
public class PriceAlertController {

    private final PriceAlertService priceAlertService;

    @PostMapping
    public ResponseEntity<PriceAlertDTO> createPriceAlert(
            @PathVariable Long userId,
            @Valid @RequestBody CreatePriceAlertRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(priceAlertService.createPriceAlert(
                userId, request));
    }

    @GetMapping
    public ResponseEntity<List<PriceAlertDTO>> getAllPriceAlerts(
            @PathVariable Long userId) {

        return ResponseEntity.ok(priceAlertService.getPriceAlerts(userId));
    }
}
