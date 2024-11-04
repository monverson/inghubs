package com.brokage.firm.challenge.inghubs.repository;

import com.brokage.firm.challenge.inghubs.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
