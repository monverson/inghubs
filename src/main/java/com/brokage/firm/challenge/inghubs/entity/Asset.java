package com.brokage.firm.challenge.inghubs.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Customer customer;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;

    public Asset() {}

    public Asset(Customer customer, String assetName, BigDecimal size, BigDecimal usableSize) {
        this.customer = customer;
        this.assetName = assetName;
        this.size = size;
        this.usableSize = usableSize;
    }
   
}
