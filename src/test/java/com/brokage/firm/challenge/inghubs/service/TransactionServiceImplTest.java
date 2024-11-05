package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.entity.Asset;
import com.brokage.firm.challenge.inghubs.entity.Customer;
import com.brokage.firm.challenge.inghubs.exception.InsufficientFundsException;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import com.brokage.firm.challenge.inghubs.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static com.brokage.firm.challenge.inghubs.helper.Constant.INSUFFICIENT_FUNDS_FOR_BUY_ORDER;
import static com.brokage.firm.challenge.inghubs.helper.Constant.TRY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class TransactionServiceImplTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer firstTestCustomer;
    private Customer secondTestCustomer;

    @BeforeEach
    void setUp() {
        firstTestCustomer = new Customer();
        firstTestCustomer.setUsername("testUser");
        firstTestCustomer.setPassword("password");
        firstTestCustomer.setRole("CUSTOMER");
        customerRepository.save(firstTestCustomer);

        Asset tryAsset = new Asset();
        tryAsset.setCustomer(firstTestCustomer);
        tryAsset.setAssetName(TRY);
        tryAsset.setSize(BigDecimal.valueOf(10000));
        tryAsset.setUsableSize(BigDecimal.valueOf(10000));
        assetRepository.save(tryAsset);

        secondTestCustomer = new Customer();
        secondTestCustomer.setUsername("testUser");
        secondTestCustomer.setPassword("password");
        secondTestCustomer.setRole("CUSTOMER");
        customerRepository.save(secondTestCustomer);

        Asset goldAsset = new Asset();
        goldAsset.setCustomer(secondTestCustomer);
        goldAsset.setAssetName("Gold");
        goldAsset.setSize(BigDecimal.valueOf(10000));
        goldAsset.setUsableSize(BigDecimal.valueOf(10000));
        assetRepository.save(goldAsset);
    }

    @Test
    void deposit_shouldIncreaseTRYBalance() {
        //given

        //when
        transactionService.deposit(firstTestCustomer.getId(), BigDecimal.valueOf(5000));
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(firstTestCustomer.getId(), TRY).orElseThrow();

        //then
        assertEquals(BigDecimal.valueOf(15000), tryAsset.getSize());
        assertEquals(BigDecimal.valueOf(15000), tryAsset.getUsableSize());
    }

    @Test
    void deposit_NoTRYAssetExist_CreateNewAsset() {
        //given

        //when
        transactionService.deposit(secondTestCustomer.getId(), BigDecimal.valueOf(5000));
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(secondTestCustomer.getId(), TRY).orElseThrow();

        //then
        assertEquals(BigDecimal.valueOf(5000), tryAsset.getSize());
        assertEquals(BigDecimal.valueOf(5000), tryAsset.getUsableSize());
    }

    @Test
    void withdraw_withSufficientFunds_shouldDecreaseTRYBalance() {
        //given

        //when
        transactionService.withdraw(firstTestCustomer.getId(), BigDecimal.valueOf(5000));
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(firstTestCustomer.getId(), TRY).orElseThrow();

        //then
        assertEquals(BigDecimal.valueOf(5000), tryAsset.getUsableSize());
    }

    @Test
    void withdraw_withInsufficientFunds_shouldThrowException() {
        //given

        //when
        Exception exception = assertThrows(InsufficientFundsException.class, () -> transactionService.withdraw(firstTestCustomer.getId(), BigDecimal.valueOf(15000)));

        //then
        assertEquals(INSUFFICIENT_FUNDS_FOR_BUY_ORDER, exception.getMessage());
    }
}