package com.autopay.parking.service;

import com.autopay.parking.model.TariffConfig;
import com.autopay.parking.repository.TariffConfigStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TariffServiceTest {

    private final TariffConfigStore repository = new MemoryTariffStore();
    private final TariffService service = new TariffService(repository);

    @Test
    void calculaMontoProporcionalAlTiempo() {
        TariffConfig t = tariff(new BigDecimal("2.00"), 30, null);
        LocalDateTime entry = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 1, 1, 11, 30);

        BigDecimal amount = service.calculate(entry, exit, t);
        assertThat(amount).isEqualByComparingTo("3.00");
    }

    @Test
    void aplicaMinimoCuandoSeExcede() {
        TariffConfig t = tariff(new BigDecimal("2.00"), 30, null);
        LocalDateTime entry = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 1, 1, 10, 5);

        BigDecimal amount = service.calculate(entry, exit, t);
        // 5 min < mínimo 30 -> cobra media hora = 1.00
        assertThat(amount).isEqualByComparingTo("1.00");
    }

    @Test
    void aplicaTopeDiario() {
        TariffConfig t = tariff(new BigDecimal("2.00"), 30, new BigDecimal("4.00"));
        LocalDateTime entry = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime exit = LocalDateTime.of(2026, 1, 1, 22, 0);

        BigDecimal amount = service.calculate(entry, exit, t);
        assertThat(amount).isEqualByComparingTo("4.00");
    }

    @Test
    void sinTiempoTranscurridoEsCero() {
        TariffConfig t = tariff(new BigDecimal("2.00"), 30, null);
        LocalDateTime now = LocalDateTime.now();

        BigDecimal amount = service.calculate(now, now, t);
        assertThat(amount).isEqualByComparingTo("0.00");
    }

    @Test
    void encuentraTarifaActiva() {
        repository.save(tariff(new BigDecimal("1.50"), 15, null));
        Optional<TariffConfig> t = service.findActiveTariff();
        assertThat(t).isPresent();
        assertThat(t.get().getRatePerHour()).isEqualByComparingTo("1.50");
    }

    @Test
    void minutosAHorasConMinimo() {
        assertThat(service.minutesToBillableHours(90, 30)).isEqualTo(1.5);
        assertThat(service.minutesToBillableHours(15, 30)).isEqualTo(0.5);
        assertThat(service.minutesToBillableHours(0, 30)).isZero();
    }

    private TariffConfig tariff(BigDecimal ratePerHour, int minMinutes,
                                BigDecimal maxDaily) {
        TariffConfig t = new TariffConfig("Test", ratePerHour, minMinutes, maxDaily);
        t.setActive(true);
        return t;
    }

    /** Implementación en memoria del contrato de persistencia de tarifas. */
    private static class MemoryTariffStore implements TariffConfigStore {
        private final List<TariffConfig> data = new ArrayList<>();
        private long nextId = 1;

        @Override
        public TariffConfig save(TariffConfig entity) {
            entity.setId(nextId++);
            data.add(entity);
            return entity;
        }

        @Override
        public List<TariffConfig> findAll() {
            return new ArrayList<>(data);
        }

        @Override
        public Optional<TariffConfig> findFirstByActiveTrue() {
            return data.stream().filter(TariffConfig::isActive).findFirst();
        }

        @Override
        public void deleteById(Long id) {
            data.removeIf(t -> id != null && id.equals(t.getId()));
        }
    }
}
