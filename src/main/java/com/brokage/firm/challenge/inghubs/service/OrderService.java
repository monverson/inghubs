package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.Order;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    Order createOrder(Order order);

    List<Order> listOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate);

    void cancelOrder(Long orderId);
}
