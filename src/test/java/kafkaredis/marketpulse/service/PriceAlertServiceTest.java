package kafkaredis.marketpulse.service;

import kafkaredis.marketpulse.dto.CreatePriceAlertRequestDTO;
import kafkaredis.marketpulse.dto.PriceAlertDTO;
import kafkaredis.marketpulse.entity.PriceAlert;
import kafkaredis.marketpulse.entity.PriceAlertConditionType;
import kafkaredis.marketpulse.entity.PriceAlertStatus;
import kafkaredis.marketpulse.entity.User;
import kafkaredis.marketpulse.exception.InvalidSymbolException;
import kafkaredis.marketpulse.exception.PriceAlertAlreadyExistsException;
import kafkaredis.marketpulse.exception.UserNotFoundException;
import kafkaredis.marketpulse.repository.PriceAlertRepository;
import kafkaredis.marketpulse.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PriceAlertServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final PriceAlertRepository priceAlertRepository = mock(PriceAlertRepository.class);

    private final PriceAlertService priceAlertService = new PriceAlertService(userRepository, priceAlertRepository);

    // Test that creating a PriceAlert saves the normalized symbol with ACTIVE status
    @Test
    void createPriceAlert_savesNormalizedActiveAlert() {
        User user = new User("user", "user@example.com", "hashed-password");
        CreatePriceAlertRequestDTO request = new CreatePriceAlertRequestDTO(
                " aapl ",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(priceAlertRepository.existsByUserIdAndSymbolAndConditionTypeAndTargetPriceAndStatus(
                1L,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000"),
                PriceAlertStatus.ACTIVE
        )).thenReturn(false);
        when(priceAlertRepository.save(any(PriceAlert.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PriceAlertDTO result = priceAlertService.createPriceAlert(1L, request);

        assertEquals("AAPL", result.symbol());
        assertEquals(PriceAlertConditionType.ABOVE, result.conditionType());
        assertEquals(PriceAlertStatus.ACTIVE, result.status());
        assertEquals(0, new BigDecimal("200.0000").compareTo(result.targetPrice()));
        assertNotNull(result.createdAt());
        assertNotNull(result.expiresAt());
        assertEquals(result.createdAt().plus(24, ChronoUnit.HOURS), result.expiresAt());
        verify(priceAlertRepository).save(any(PriceAlert.class));
    }

    // Test that creating a PriceAlert fails if the user does not exist
    @Test
    void createPriceAlert_throwsWhenUserDoesNotExist() {
        CreatePriceAlertRequestDTO request = new CreatePriceAlertRequestDTO(
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> priceAlertService.createPriceAlert(1L, request)
        );

        verify(priceAlertRepository, never()).save(any(PriceAlert.class));
    }

    // Test that creating a duplicate ACTIVE PriceAlert throws a PriceAlertAlreadyExistsException
    @Test
    void createPriceAlert_throwsWhenActiveAlertAlreadyExists() {
        User user = new User("user", "user@example.com", "hashed-password");
        CreatePriceAlertRequestDTO request = new CreatePriceAlertRequestDTO(
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(priceAlertRepository.existsByUserIdAndSymbolAndConditionTypeAndTargetPriceAndStatus(
                1L,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000"),
                PriceAlertStatus.ACTIVE
        )).thenReturn(true);

        assertThrows(
                PriceAlertAlreadyExistsException.class,
                () -> priceAlertService.createPriceAlert(1L, request)
        );

        verify(priceAlertRepository, never()).save(any(PriceAlert.class));
    }

    // Test that creating a PriceAlert with an invalid symbol throws an InvalidSymbolException
    @Test
    void createPriceAlert_throwsWhenSymbolIsInvalid() {
        User user = new User("user", "user@example.com", "hashed-password");
        CreatePriceAlertRequestDTO request = new CreatePriceAlertRequestDTO(
                "AAPL!",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(
                InvalidSymbolException.class,
                () -> priceAlertService.createPriceAlert(1L, request)
        );

        verify(priceAlertRepository, never()).save(any(PriceAlert.class));
    }

    // Test that getPriceAlerts returns a user's PriceAlerts as DTOs
    @Test
    void getPriceAlerts_returnsAlertsForUser() {
        User user = new User("user", "user@example.com", "hashed-password");
        PriceAlert apple = new PriceAlert(
                user,
                "AAPL",
                PriceAlertConditionType.ABOVE,
                new BigDecimal("200.0000")
        );
        PriceAlert tesla = new PriceAlert(
                user,
                "TSLA",
                PriceAlertConditionType.BELOW,
                new BigDecimal("180.0000")
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(priceAlertRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(apple, tesla));

        List<PriceAlertDTO> result = priceAlertService.getPriceAlerts(1L);

        assertEquals(2, result.size());
        assertEquals("AAPL", result.get(0).symbol());
        assertEquals("TSLA", result.get(1).symbol());
    }

    // Test that getPriceAlerts fails if the user does not exist
    @Test
    void getPriceAlerts_throwsWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> priceAlertService.getPriceAlerts(1L)
        );

        verify(priceAlertRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }
}
