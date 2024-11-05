package com.brokage.firm.challenge.inghubs.controller;

import com.brokage.firm.challenge.inghubs.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("matchedOrder/{orderId}")
    public ResponseEntity<Void> matchOrder(@PathVariable Long orderId) {
        adminService.matchOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
