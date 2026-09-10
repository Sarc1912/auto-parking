package com.autopay.parking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Tasa de cambio del día (BCV y paralela).
 *
 * <p>Se guarda en base de datos para poder actualizarla manualmente desde el
 * panel administrativo sin tocar código.</p>
 */
@Entity
@Table(name = "exchange_rates")
public class ExchangeRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bcv_rate", nullable = false)
    private BigDecimal bcvRate;

    @Column(name = "parallel_rate")
    private BigDecimal parallelRate;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getBcvRate() {
        return bcvRate;
    }

    public void setBcvRate(BigDecimal bcvRate) {
        this.bcvRate = bcvRate;
    }

    public BigDecimal getParallelRate() {
        return parallelRate;
    }

    public void setParallelRate(BigDecimal parallelRate) {
        this.parallelRate = parallelRate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
