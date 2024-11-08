package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.*;
import com.brokage.firm.challenge.inghubs.exception.PendingOrderNotMatchedException;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import com.brokage.firm.challenge.inghubs.repository.CustomerRepository;
import com.brokage.firm.challenge.inghubs.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static com.brokage.firm.challenge.inghubs.helper.Constant.PENDING_ORDER_CAN_BE_MATCHED;
import static com.brokage.firm.challenge.inghubs.helper.Constant.TRY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    private Customer firstTestCustomer;

    private Customer secondTestCustomer;

    @BeforeEach
    void setUp() {
        firstTestCustomer = new Customer();
        firstTestCustomer.setUsername("testUser");
        firstTestCustomer.setPassword("password");
        firstTestCustomer.setRole(Role.ADMIN);
        customerRepository.save(firstTestCustomer);

        secondTestCustomer = new Customer();
        secondTestCustomer.setUsername("testUser1");
        secondTestCustomer.setPassword("password2");
        secondTestCustomer.setRole(Role.USER);
        customerRepository.save(secondTestCustomer);

        Asset firstTryAsset = new Asset();
        firstTryAsset.setCustomer(firstTestCustomer);
        firstTryAsset.setAssetName(TRY);
        firstTryAsset.setSize(BigDecimal.valueOf(10000));
        firstTryAsset.setUsableSize(BigDecimal.valueOf(10000));
        assetRepository.save(firstTryAsset);

        Asset secondTryAsset = new Asset();
        secondTryAsset.setCustomer(secondTestCustomer);
        secondTryAsset.setAssetName(TRY);
        secondTryAsset.setSize(BigDecimal.valueOf(10000));
        secondTryAsset.setUsableSize(BigDecimal.valueOf(10000));
        assetRepository.save(secondTryAsset);

        Asset goldAsset = new Asset();
        goldAsset.setCustomer(secondTestCustomer);
        goldAsset.setAssetName("Gold");
        goldAsset.setSize(BigDecimal.valueOf(10000));
        goldAsset.setUsableSize(BigDecimal.valueOf(10000));
        assetRepository.save(goldAsset);
    }

    @Test
    void matchOrderBuy_shouldUpdateOrderAndAssets() {
        //given

        // Create a pending buy order
        Order buyOrder = new Order();
        buyOrder.setCustomer(firstTestCustomer);
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
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(firstTestCustomer.getId(), TRY).orElseThrow();
        Asset goldAsset = assetRepository.findByCustomerIdAndAssetName(firstTestCustomer.getId(), "Gold").orElseThrow();

        //then
        assertEquals(Status.MATCHED, matchedOrder.getStatus());
        assertEquals(BigDecimal.valueOf(9000), tryAsset.getUsableSize()); // 10000 - (10 * 100)
        assertEquals(BigDecimal.valueOf(10), goldAsset.getSize());
    }

    @Test
    void matchOrderSell_shouldUpdateOrderAndAssets() {
        //given

        // Create a pending sell order
        Order sellOrder = new Order();
        sellOrder.setCustomer(secondTestCustomer);
        sellOrder.setAssetName("Gold");
        sellOrder.setOrderSide(Side.SELL);
        sellOrder.setSize(BigDecimal.valueOf(10));
        sellOrder.setPrice(BigDecimal.valueOf(100));
        Order createdOrder = orderService.createOrder(sellOrder);

        //when

        // Match the order
        adminService.matchOrder(createdOrder.getId());

        // Retrieve updated data
        Order matchedOrder = orderRepository.findById(createdOrder.getId()).orElseThrow();
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(secondTestCustomer.getId(), TRY).orElseThrow();
        Asset goldAsset = assetRepository.findByCustomerIdAndAssetName(secondTestCustomer.getId(), "Gold").orElseThrow();

        //then
        assertEquals(Status.MATCHED, matchedOrder.getStatus());
        assertEquals(BigDecimal.valueOf(11000), tryAsset.getUsableSize()); // 10000 + (10 * 100)
        assertEquals(BigDecimal.valueOf(9990), goldAsset.getUsableSize());
    }

    @Test
    void matchOrder_withNotPendingStatus_shouldThrowException() {
        //given

        // Create a pending sell order
        Order sellOrder = new Order();
        sellOrder.setCustomer(secondTestCustomer);
        sellOrder.setAssetName("Gold");
        sellOrder.setOrderSide(Side.SELL);
        sellOrder.setSize(BigDecimal.valueOf(10));
        sellOrder.setPrice(BigDecimal.valueOf(100));
        Order createdOrder = orderService.createOrder(sellOrder);
        createdOrder.setStatus(Status.CANCELED);
        Long id= createdOrder.getId();

        //when
        Exception exception = assertThrows(PendingOrderNotMatchedException.class, () ->
                adminService.matchOrder(id));

        //then
        assertEquals(PENDING_ORDER_CAN_BE_MATCHED, exception.getMessage());
    }
}