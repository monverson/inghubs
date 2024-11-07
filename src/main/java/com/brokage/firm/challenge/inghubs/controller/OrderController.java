package com.brokage.firm.challenge.inghubs.controller;

import com.brokage.firm.challenge.inghubs.entity.Order;
import com.brokage.firm.challenge.inghubs.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }

    @GetMapping
    public ResponseEntity<List<Order>> listOrders(@RequestParam Long customerId,
                                                  @RequestParam LocalDateTime startDate,
                                                  @RequestParam LocalDateTime endDate) {
        return ResponseEntity.ok(orderService.listOrders(customerId, startDate, endDate));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
