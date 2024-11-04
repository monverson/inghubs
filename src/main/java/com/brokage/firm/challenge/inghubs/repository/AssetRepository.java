package com.brokage.firm.challenge.inghubs.repository;

import com.brokage.firm.challenge.inghubs.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByCustomerIdAndAssetName(Long customerId, String assetName);
}
