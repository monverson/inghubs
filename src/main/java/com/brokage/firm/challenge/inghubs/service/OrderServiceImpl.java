package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.Asset;
import com.brokage.firm.challenge.inghubs.entity.Order;
import com.brokage.firm.challenge.inghubs.entity.Side;
import com.brokage.firm.challenge.inghubs.entity.Status;
import com.brokage.firm.challenge.inghubs.exception.InsufficientFundsException;
import com.brokage.firm.challenge.inghubs.exception.NotFoundException;
import com.brokage.firm.challenge.inghubs.exception.PendingStatusCanBeDeletedException;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import com.brokage.firm.challenge.inghubs.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.brokage.firm.challenge.inghubs.helper.Constant.*;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final AssetRepository assetRepository;

    public OrderServiceImpl(OrderRepository orderRepository, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
    }

    @Override
    public Order createOrder(Order order) {
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), TRY).orElseThrow(() -> new NotFoundException(CUSTOMER_ASSET_NOT_FOUND));

        BigDecimal totalCost = order.getPrice().multiply(order.getSize());
        if (order.getOrderSide() == Side.BUY && tryAsset.getUsableSize().compareTo(totalCost) < 0) {
            throw new InsufficientFundsException(INSUFFICIENT_FUNDS_FOR_BUY_ORDER);
        } else if (order.getOrderSide() == Side.SELL) {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName()).orElseThrow(() -> new NotFoundException(ASSET_NOT_FOUND_FOR_SELL_ORDER));
            if (asset.getUsableSize().compareTo(order.getSize()) < 0) {
                throw new InsufficientFundsException(INSUFFICIENT_ASSET_TO_MATCH_THE_SELL_ORDER);
            }
        }

        order.setStatus(Status.PENDING);
        order.setCreateDate(LocalDateTime.now());
        return orderRepository.save(order);
    }

    @Override
    public List<Order> listOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));
        if (order.getStatus() != Status.PENDING) {
            throw new PendingStatusCanBeDeletedException(PENDING_STATUS_CAN_BE_DELETED);
        }
        if (order.getOrderSide() == Side.BUY) {
            Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), TRY).orElseThrow(() -> new NotFoundException(CUSTOMER_ASSET_NOT_FOUND));
            asset.setUsableSize(asset.getUsableSize().add(order.getPrice().multiply(order.getSize())));
        } else if (order.getOrderSide() == Side.SELL) {
            Asset sellAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName()).orElseThrow(() -> new NotFoundException(CUSTOMER_ASSET_NOT_FOUND));
            sellAsset.setUsableSize(sellAsset.getUsableSize().add(order.getSize()));
        }

        order.setStatus(Status.CANCELED);
        orderRepository.save(order);
    }

}
