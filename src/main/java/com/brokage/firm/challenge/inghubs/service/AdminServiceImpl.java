package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.Asset;
import com.brokage.firm.challenge.inghubs.entity.Order;
import com.brokage.firm.challenge.inghubs.entity.Side;
import com.brokage.firm.challenge.inghubs.entity.Status;
import com.brokage.firm.challenge.inghubs.exception.NotFoundException;
import com.brokage.firm.challenge.inghubs.exception.PendingOrderNotMatchedException;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import com.brokage.firm.challenge.inghubs.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.brokage.firm.challenge.inghubs.helper.Constant.*;

@Service
public class AdminServiceImpl implements AdminService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    public AdminServiceImpl(OrderRepository orderRepository, AssetRepository assetRepository) {
        this.orderRepository = orderRepository;
        this.assetRepository = assetRepository;
    }

    @Override
    public void matchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));

        if (order.getStatus() != Status.PENDING) {
            throw new PendingOrderNotMatchedException(PENDING_ORDER_CAN_BE_MATCHED);
        }

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), TRY).orElseThrow(() -> new NotFoundException(CUSTOMER_ASSET_NOT_FOUND));

        if (order.getOrderSide() == Side.BUY) {
            BigDecimal totalCost = order.getPrice().multiply(order.getSize());

            if (tryAsset.getUsableSize().compareTo(totalCost) < 0) {
                throw new RuntimeException(INSUFFICIENT_FUNDS_TO_MATCHED_TO_BUY_ORDER);
            }

            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));

            Asset buyAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName()).orElseGet(() -> new Asset(order.getCustomer(), order.getAssetName(), BigDecimal.ZERO, BigDecimal.ZERO));

            buyAsset.setSize(buyAsset.getSize().add(order.getSize()));
            buyAsset.setUsableSize(buyAsset.getUsableSize().add(order.getSize()));

            assetRepository.save(tryAsset);
            assetRepository.save(buyAsset);

        } else if (order.getOrderSide() == Side.SELL) {
            Asset sellAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName()).orElseThrow(() -> new RuntimeException(ASSET_NOT_FOUND_FOR_SELL_ORDER));

            if (sellAsset.getUsableSize().compareTo(order.getSize()) < 0) {
                throw new RuntimeException(INSUFFICIENT_ASSET_TO_MATCH_THE_SELL_ORDER);
            }

            sellAsset.setUsableSize(sellAsset.getUsableSize().subtract(order.getSize()));

            BigDecimal sellValue = order.getPrice().multiply(order.getSize());
            tryAsset.setSize(tryAsset.getSize().add(sellValue));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(sellValue));

            assetRepository.save(tryAsset);
            assetRepository.save(sellAsset);
        }

        order.setStatus(Status.MATCHED);
        orderRepository.save(order);
    }
}
