package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.Asset;
import com.brokage.firm.challenge.inghubs.entity.Customer;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.brokage.firm.challenge.inghubs.helper.Constant.*;

@Service
public class TransactionService {

    private final AssetRepository assetRepository;

    public TransactionService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public void deposit(Long customerId, BigDecimal amount) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, ASSET_NAME)
                .orElseGet(() -> createAsset(customerId));

        asset.setSize(asset.getSize().add(amount));
        asset.setUsableSize(asset.getUsableSize().add(amount));
        assetRepository.save(asset);
    }

    public void withdraw(Long customerId, BigDecimal amount) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, ASSET_NAME)
                .orElseThrow(() -> new RuntimeException(CUSTOMER_ASSET_NOT_FOUND));
        if (asset.getUsableSize().compareTo(amount) < 0) {
            throw new RuntimeException(INSUFFICIENT_FUNDS);
        }
        asset.setUsableSize(asset.getUsableSize().subtract(amount));
        assetRepository.save(asset);
    }

    private Asset createAsset(Long customerId) {
        Asset asset = new Asset();
        Customer customer = new Customer();
        customer.setId(customerId);
        asset.setCustomer(customer);
        asset.setAssetName(ASSET_NAME);
        asset.setSize(BigDecimal.ZERO);
        asset.setUsableSize(BigDecimal.ZERO);
        return asset;
    }
}
