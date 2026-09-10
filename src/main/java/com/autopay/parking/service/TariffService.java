package com.autopay.parking.service;

import com.autopay.parking.model.TariffConfig;
import com.autopay.parking.repository.TariffConfigStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Cálculo de tarifas por tiempo de permanencia.
 *
 * <p>La tarifa activa se lee de la base de datos, por lo que puede ajustarse
 * desde el panel administrativo sin recompilar.</p>
 */
@Service
@Transactional
public class TariffService {

    private final TariffConfigStore tariffRepository;

    public TariffService(TariffConfigStore tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    /**
     * Calcula el monto a pagar para una permanencia dada, aplicando el
     * mínimo de minutos y el tope diario si está configurado.
     */
    public BigDecimal calculate(LocalDateTime entryTime, LocalDateTime exitTime,
                                TariffConfig tariff) {
        if (tariff == null || tariff.getRatePerHour() == null) {
            throw new IllegalStateException("No hay una tarifa configurada.");
        }
        if (exitTime == null) {
            exitTime = LocalDateTime.now();
        }
        long minutes = Math.max(0, Duration.between(entryTime, exitTime).toMinutes());

        double billedHours = minutesToBillableHours(minutes, tariff.getMinimumMinutes());
        BigDecimal amount = tariff.getRatePerHour()
                .multiply(BigDecimal.valueOf(billedHours));

        if (tariff.getMaxDailyRate() != null
                && tariff.getMaxDailyRate().signum() > 0
                && amount.compareTo(tariff.getMaxDailyRate()) > 0) {
            amount = tariff.getMaxDailyRate();
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Convierte minutos a horas facturable aplicando el mínimo.
     * Ej.: 90 min, mínimo 30 -> 1.5 horas. 15 min, mínimo 30 -> 0.5 (mínimo).
     */
    double minutesToBillableHours(long minutes, int minimumMinutes) {
        if (minutes <= 0) {
            return 0.0;
        }
        long applied = Math.max(minutes, minimumMinutes);
        return applied / 60.0;
    }

    public Optional<TariffConfig> findActiveTariff() {
        return tariffRepository.findFirstByActiveTrue();
    }

    public List<TariffConfig> findAll() {
        return tariffRepository.findAll();
    }

    public TariffConfig save(TariffConfig tariff) {
        return tariffRepository.save(tariff);
    }

    public void delete(Long id) {
        tariffRepository.deleteById(id);
    }
}


