package com.brokage.firm.challenge.inghubs.controller;

import com.brokage.firm.challenge.inghubs.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<Void> deposit(@RequestParam Long customerId, @RequestParam BigDecimal amount) {
        transactionService.deposit(customerId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw(@RequestParam Long customerId, @RequestParam BigDecimal amount) {
        transactionService.withdraw(customerId, amount);
        return ResponseEntity.ok().build();
    }
}
