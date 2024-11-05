package com.brokage.firm.challenge.inghubs.exception;

public class PendingOrderNotMatchedException extends RuntimeException {
    public PendingOrderNotMatchedException(String message) {
        super(message);
    }
}
