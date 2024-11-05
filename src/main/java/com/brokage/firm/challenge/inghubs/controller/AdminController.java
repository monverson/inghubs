package com.brokage.firm.challenge.inghubs.controller;

import com.brokage.firm.challenge.inghubs.service.AdminServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminServiceImpl adminServiceImpl;

    public AdminController(AdminServiceImpl adminServiceImpl) {
        this.adminServiceImpl = adminServiceImpl;
    }

    @PostMapping("matchedOrder/{orderId}")
    public ResponseEntity<Void> matchOrder(@PathVariable Long orderId) {
        adminServiceImpl.matchOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
