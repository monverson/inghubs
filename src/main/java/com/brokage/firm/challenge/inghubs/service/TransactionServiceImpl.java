package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.Asset;
import com.brokage.firm.challenge.inghubs.entity.Customer;
import com.brokage.firm.challenge.inghubs.exception.InsufficientFundsException;
import com.brokage.firm.challenge.inghubs.exception.NotFoundException;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.brokage.firm.challenge.inghubs.helper.Constant.*;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final AssetRepository assetRepository;

    public TransactionServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public void deposit(Long customerId, BigDecimal amount) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, TRY).orElseGet(() -> createAsset(customerId));

        asset.setSize(asset.getSize().add(amount));
        asset.setUsableSize(asset.getUsableSize().add(amount));
        assetRepository.save(asset);
    }

    @Override
    public void withdraw(Long customerId, BigDecimal amount) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(customerId, TRY).orElseThrow(() -> new NotFoundException(CUSTOMER_ASSET_NOT_FOUND));
        if (asset.getUsableSize().compareTo(amount) < 0) {
            throw new InsufficientFundsException(INSUFFICIENT_FUNDS_FOR_BUY_ORDER);
        }
        asset.setUsableSize(asset.getUsableSize().subtract(amount));
        assetRepository.save(asset);
    }

    private Asset createAsset(Long customerId) {
        Asset asset = new Asset();
        asset.setCustomer(new Customer(customerId));
        asset.setAssetName(TRY);
        asset.setSize(BigDecimal.ZERO);
        asset.setUsableSize(BigDecimal.ZERO);
        return asset;
    }
}
