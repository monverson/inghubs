package com.brokage.firm.challenge.inghubs.entity;

public enum Status {
    PENDING("pending"),
    MATCHED("matched"),
    CANCELED("canceled");

    private final String name;

    Status(String name) {
        this.name = name;
    }
}
