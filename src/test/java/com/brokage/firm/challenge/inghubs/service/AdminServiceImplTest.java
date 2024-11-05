package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.*;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import com.brokage.firm.challenge.inghubs.repository.CustomerRepository;
import com.brokage.firm.challenge.inghubs.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static com.brokage.firm.challenge.inghubs.helper.Constant.TRY;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AdminServiceImplTest {

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AdminService adminService;

    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setUsername("testUser");
        testCustomer.setPassword("password");
        testCustomer.setRole("CUSTOMER");
        customerRepository.save(testCustomer);

        Asset tryAsset = new Asset();
        tryAsset.setCustomer(testCustomer);
        tryAsset.setAssetName(TRY);
        tryAsset.setSize(BigDecimal.valueOf(10000));
        tryAsset.setUsableSize(BigDecimal.valueOf(10000));
        assetRepository.save(tryAsset);
    }

    @Test
    void matchOrder_shouldUpdateOrderAndAssets() {
        //given

        // Create a pending buy order
        Order buyOrder = new Order();
        buyOrder.setCustomer(testCustomer);
        buyOrder.setAssetName("Gold");
        buyOrder.setOrderSide(Side.BUY);
        buyOrder.setSize(BigDecimal.valueOf(10));
        buyOrder.setPrice(BigDecimal.valueOf(100));
        Order createdOrder = orderService.createOrder(buyOrder);

        //when

        // Match the order
        adminService.matchOrder(createdOrder.getId());

        // Retrieve updated data
        Order matchedOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(testCustomer.getId(), "TRY").orElseThrow();
        Asset aaplAsset = assetRepository.findByCustomerIdAndAssetName(testCustomer.getId(), "Gold").orElseThrow();

        //then
        assertEquals(Status.MATCHED, matchedOrder.getStatus());
        assertEquals(BigDecimal.valueOf(9000), tryAsset.getUsableSize()); // 10000 - (10 * 100)
        assertEquals(BigDecimal.valueOf(10), aaplAsset.getSize());
    }
}