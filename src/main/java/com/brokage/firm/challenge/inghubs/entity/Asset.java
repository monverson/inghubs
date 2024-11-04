package com.brokage.firm.challenge.inghubs.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Customer customer;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
}
