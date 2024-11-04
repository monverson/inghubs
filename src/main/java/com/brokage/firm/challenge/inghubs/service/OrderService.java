package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.Asset;
import com.brokage.firm.challenge.inghubs.entity.Order;
import com.brokage.firm.challenge.inghubs.entity.Side;
import com.brokage.firm.challenge.inghubs.entity.Status;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import com.brokage.firm.challenge.inghubs.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.brokage.firm.challenge.inghubs.helper.Constant.*;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    private final AssetRepository assetRepository;

    public OrderService(OrderRepository orderRepository, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
    }


    public Order createOrder(Order order) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), ASSET_NAME)
                .orElseThrow(() -> new RuntimeException(CUSTOMER_ASSET_NOT_FOUND));

        // Check enough funds for requesting orders
        if (order.getOrderSide() == Side.BUY) {
            BigDecimal totalCost = order.getPrice().multiply(order.getSize());
            if (asset.getUsableSize().compareTo(totalCost) < 0) {
                throw new RuntimeException(INSUFFICIENT_FUNDS);
            }
            asset.setUsableSize(asset.getUsableSize().subtract(totalCost));
        } else if (order.getOrderSide() == Side.SELL) {
            Asset sellAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName())
                    .orElseThrow(() -> new RuntimeException(ASSET_NOT_FOUND));
            if (sellAsset.getUsableSize().compareTo(order.getSize()) < 0) {
                throw new RuntimeException(INSUFFICIENT_ASSET_SIZE);
            }
            sellAsset.setUsableSize(sellAsset.getUsableSize().subtract(order.getSize()));
        }

        order.setStatus(Status.PENDING);
        order.setCreateDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    public List<Order> listOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    public void deleteOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException(ORDER_NOT_FOUND));
        if (order.getStatus() != Status.PENDING) {
            throw new RuntimeException(PENDING_STATUS_CAN_BE_DELETED);
        }
        if (order.getOrderSide() == Side.BUY) {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), ASSET_NAME)
                    .orElseThrow(() -> new RuntimeException(CUSTOMER_ASSET_NOT_FOUND));
            asset.setUsableSize(asset.getUsableSize().add(order.getPrice().multiply(order.getSize())));
        } else if (order.getOrderSide() == Side.SELL) {
            Asset sellAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName())
                    .orElseThrow(() -> new RuntimeException(CUSTOMER_ASSET_NOT_FOUND));
            sellAsset.setUsableSize(sellAsset.getUsableSize().add(order.getSize()));
        }

        order.setStatus(Status.CANCELED);
        orderRepository.save(order);
    }
}
