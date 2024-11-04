package com.brokage.firm.challenge.inghubs.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Customer customer;
    private String assetName;
    private String orderSide;
    private BigDecimal size;
    private BigDecimal price;
    private Status status;
    private LocalDateTime createDate;
}