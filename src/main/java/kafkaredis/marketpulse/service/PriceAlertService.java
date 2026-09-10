package kafkaredis.marketpulse.service;

import org.springframework.transaction.annotation.Transactional;
import kafkaredis.marketpulse.dto.CreatePriceAlertRequestDTO;
import kafkaredis.marketpulse.dto.PriceAlertDTO;
import kafkaredis.marketpulse.entity.PriceAlert;
import kafkaredis.marketpulse.entity.PriceAlertStatus;
import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.exception.InvalidSymbolException;
import kafkaredis.marketpulse.exception.PriceAlertAlreadyExistsException;
import kafkaredis.marketpulse.exception.UserNotFoundException;
import kafkaredis.marketpulse.repository.PriceAlertRepository;
import kafkaredis.marketpulse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private static final String SYMBOL_PATTERN = "[A-Z0-9.-]{1,15}";

    private final UserRepository userRepository;
    private final PriceAlertRepository priceAlertRepository;

    // Create a Price Alert for a User
    @Transactional
    public PriceAlertDTO createPriceAlert(Long userId, CreatePriceAlertRequestDTO request) {
        User user = requireUser(userId);
        String normalizedSymbol = normalizeSymbol(request.symbol());

        // Check if PriceAlert already exists for a user
        if(priceAlertRepository.existsByUserIdAndSymbolAndConditionTypeAndTargetPriceAndStatus(
                userId,
                normalizedSymbol,
                request.conditionType(),
                request.targetPrice(),
                PriceAlertStatus.ACTIVE
        )) {
            throw new PriceAlertAlreadyExistsException("Price alert already exists");
        }

        PriceAlert savedAlert = priceAlertRepository.save(new PriceAlert(
                user,
                normalizedSymbol,
                request.conditionType(),
                request.targetPrice()
        ));

        return toDTO(savedAlert);
    }

    // Get all price alerts for a user (frontend display)
    @Transactional(readOnly = true)
    public List<PriceAlertDTO> getPriceAlerts(Long userId) {
        requireUser(userId);

        return priceAlertRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()// Process each PriceAlert entity one at a time
                .map(this::toDTO)// For each PriceAlert, call the toDTO method. .map(priceAlert -> this.toDTO(priceAlert))
                .toList(); // Collect the converted results into a list
    }

    // Check user exists in the User Table
    private User requireUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("User was not found"));
    }

    // Normalize the stock input symbol
    private String normalizeSymbol(String symbol) {
        String normalizedSymbol = symbol.trim().toUpperCase();

        if (normalizedSymbol.isBlank()) {
            throw new InvalidSymbolException("Stock symbol must not be blank");
        }

        if (!normalizedSymbol.matches(SYMBOL_PATTERN)) {
            throw new InvalidSymbolException("Stock symbol format is invalid");
        }

        return normalizedSymbol;
    }

    // Convert PriceAlert entity into a PriceAlertDTO to be returned to the controller
    private PriceAlertDTO toDTO(PriceAlert alert) {
        return new PriceAlertDTO(
                alert.getId(),
                alert.getSymbol(),
                alert.getConditionType(),
                alert.getTargetPrice(),
                alert.getStatus(),
                alert.getCreatedAt(),
                alert.getExpiresAt()
        );
    }
}
