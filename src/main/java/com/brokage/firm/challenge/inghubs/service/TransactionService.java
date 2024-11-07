package com.brokage.firm.challenge.inghubs.service;

import java.math.BigDecimal;

public interface TransactionService {
    void deposit(Long customerId, BigDecimal amount);
    void withdraw(Long customerId, BigDecimal amount);
}
