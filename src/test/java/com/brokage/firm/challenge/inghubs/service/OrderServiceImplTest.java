package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.*;
import com.brokage.firm.challenge.inghubs.exception.InsufficientFundsException;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import com.brokage.firm.challenge.inghubs.repository.CustomerRepository;
import com.brokage.firm.challenge.inghubs.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static com.brokage.firm.challenge.inghubs.helper.Constant.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class OrderServiceImplTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CustomerRepository customerRepository;

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
    void createBuyOrder_withSufficientFunds_shouldCreateOrder() {
        //given
        Order buyOrder = new Order();
        buyOrder.setCustomer(testCustomer);
        buyOrder.setAssetName("Gold");
        buyOrder.setOrderSide(Side.BUY);
        buyOrder.setSize(BigDecimal.valueOf(10));
        buyOrder.setPrice(BigDecimal.valueOf(100));

        //when
        Order createdOrder = orderService.createOrder(buyOrder);

        //then
        assertNotNull(createdOrder.getId());
        assertEquals("Gold", createdOrder.getAssetName());
        assertEquals(Status.PENDING, createdOrder.getStatus());
    }

    @Test
    void createBuyOrder_withInsufficientFunds_shouldThrowException() {
        Order buyOrder = new Order();
        buyOrder.setCustomer(testCustomer);
        buyOrder.setAssetName("Silver");
        buyOrder.setOrderSide(Side.BUY);
        buyOrder.setSize(BigDecimal.valueOf(200)); // Exceeds TRY asset balance of 10,000
        buyOrder.setPrice(BigDecimal.valueOf(100));

        Exception exception = assertThrows(InsufficientFundsException.class, () -> orderService.createOrder(buyOrder));
        assertEquals(INSUFFICIENT_FUNDS_FOR_BUY_ORDER, exception.getMessage());
    }

    @Test
    void createSellOrder_withSufficientFunds_shouldCreateOrder() {
        //given
        Order buyOrder = new Order();
        buyOrder.setCustomer(testCustomer);
        buyOrder.setAssetName(TRY);
        buyOrder.setOrderSide(Side.SELL);
        buyOrder.setSize(BigDecimal.valueOf(10));
        buyOrder.setPrice(BigDecimal.valueOf(100));

        //when
        Order createdOrder = orderService.createOrder(buyOrder);

        //then
        assertNotNull(createdOrder.getId());
        assertEquals(TRY, createdOrder.getAssetName());
        assertEquals(Status.PENDING, createdOrder.getStatus());
    }

    @Test
    void createSellOrder_withInsufficientFunds_shouldThrowException() {
        Order buyOrder = new Order();
        buyOrder.setCustomer(testCustomer);
        buyOrder.setAssetName(TRY);
        buyOrder.setOrderSide(Side.SELL);
        buyOrder.setSize(BigDecimal.valueOf(20000)); // Exceeds usable size
        buyOrder.setPrice(BigDecimal.valueOf(100));

        Exception exception = assertThrows(InsufficientFundsException.class, () -> orderService.createOrder(buyOrder));
        assertEquals(INSUFFICIENT_ASSET_TO_MATCH_THE_SELL_ORDER, exception.getMessage());
    }

    @Test
    void cancelOrder() {
    }
}