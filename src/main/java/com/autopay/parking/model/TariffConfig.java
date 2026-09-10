package com.autopay.parking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Configuración de una tarifa de estacionamiento.
 *
 * <p>Los valores son editables desde el panel administrativo y se guardan en
 * la base de datos, de modo que cambiar una tarifa no requiere tocar código.</p>
 */
@Entity
@Table(name = "tariff_configs")
public class TariffConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "rate_per_hour", nullable = false)
    private BigDecimal ratePerHour;

    @Column(name = "minimum_minutes", nullable = false)
    private int minimumMinutes;

    @Column(name = "max_daily_rate")
    private BigDecimal maxDailyRate;

    @Column(nullable = false)
    private boolean active = true;

    public TariffConfig() {
    }

    public TariffConfig(String name, BigDecimal ratePerHour, int minimumMinutes,
                        BigDecimal maxDailyRate) {
        this.name = name;
        this.ratePerHour = ratePerHour;
        this.minimumMinutes = minimumMinutes;
        this.maxDailyRate = maxDailyRate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getRatePerHour() {
        return ratePerHour;
    }

    public void setRatePerHour(BigDecimal ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

    public int getMinimumMinutes() {
        return minimumMinutes;
    }

    public void setMinimumMinutes(int minimumMinutes) {
        this.minimumMinutes = minimumMinutes;
    }

    public BigDecimal getMaxDailyRate() {
        return maxDailyRate;
    }

    public void setMaxDailyRate(BigDecimal maxDailyRate) {
        this.maxDailyRate = maxDailyRate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
