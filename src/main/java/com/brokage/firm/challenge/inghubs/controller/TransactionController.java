package com.brokage.firm.challenge.inghubs.controller;

import com.brokage.firm.challenge.inghubs.service.TransactionServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionServiceImpl transactionServiceImpl;

    public TransactionController(TransactionServiceImpl transactionServiceImpl) {
        this.transactionServiceImpl = transactionServiceImpl;
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@RequestParam Long customerId, @RequestParam BigDecimal amount) {
        transactionServiceImpl.deposit(customerId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestParam Long customerId, @RequestParam BigDecimal amount) {
        transactionServiceImpl.withdraw(customerId, amount);
        return ResponseEntity.ok().build();
    }
}
