package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.Asset;
import com.brokage.firm.challenge.inghubs.entity.Order;
import com.brokage.firm.challenge.inghubs.entity.Side;
import com.brokage.firm.challenge.inghubs.entity.Status;
import com.brokage.firm.challenge.inghubs.exception.NotFoundException;
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
        // Retrieve the order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ORDER_NOT_FOUND));

        // Check that the order is in PENDING status
        if (order.getStatus() != Status.PENDING) {
            throw new RuntimeException(PENDING_ORDER_CAN_BE_MATCHED);
        }

        // Retrieve TRY asset for the customer
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), TRY)
                .orElseThrow(() -> new NotFoundException(CUSTOMER_ASSET_NOT_FOUND));

        if (order.getOrderSide() == Side.BUY) {
            // Handle BUY order
            BigDecimal totalCost = order.getPrice().multiply(order.getSize());

            if (tryAsset.getUsableSize().compareTo(totalCost) < 0) {
                throw new RuntimeException(INSUFFICIENT_FUNDS_TO_MATCHED_TO_BUY_ORDER);
            }

            // Deduct TRY amount from customer's usable size
            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));

            // Add the ordered asset to customer's portfolio
            Asset buyAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName())
                    .orElseGet(() -> new Asset(order.getCustomer(), order.getAssetName(), BigDecimal.ZERO, BigDecimal.ZERO));

            buyAsset.setSize(buyAsset.getSize().add(order.getSize()));
            buyAsset.setUsableSize(buyAsset.getUsableSize().add(order.getSize()));

            // Save both assets
            assetRepository.save(tryAsset);
            assetRepository.save(buyAsset);

        } else if (order.getOrderSide()==Side.SELL) {
            // SELL order
            Asset sellAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomer().getId(), order.getAssetName())
                    .orElseThrow(() -> new RuntimeException(ASSET_NOT_FOUND_FOR_SELL_ORDER));

            if (sellAsset.getUsableSize().compareTo(order.getSize()) < 0) {
                throw new RuntimeException(INSUFFICIENT_ASSET_TO_MATCH_THE_SELL_ORDER);
            }

            // Deduct the asset size from customer's usable size
            sellAsset.setUsableSize(sellAsset.getUsableSize().subtract(order.getSize()));

            // Add TRY equivalent to customer's TRY asset
            BigDecimal sellValue = order.getPrice().multiply(order.getSize());
            tryAsset.setSize(tryAsset.getSize().add(sellValue));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(sellValue));

            // Save both assets
            assetRepository.save(tryAsset);
            assetRepository.save(sellAsset);
        }

        // Step 5: Update order status to MATCHED
        order.setStatus(Status.MATCHED);
        orderRepository.save(order);
    }
}
