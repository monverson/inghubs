package com.brokage.firm.challenge.inghubs;

import com.brokage.firm.challenge.inghubs.entity.*;
import com.brokage.firm.challenge.inghubs.repository.AssetRepository;
import com.brokage.firm.challenge.inghubs.repository.CustomerRepository;
import com.brokage.firm.challenge.inghubs.service.AuthenticationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

import static com.brokage.firm.challenge.inghubs.entity.Role.ADMIN;
import static com.brokage.firm.challenge.inghubs.entity.Role.USER;
import static com.brokage.firm.challenge.inghubs.helper.Constant.TRY;

@SpringBootApplication
public class Application {

	public static void main(String[] args) throws JsonProcessingException {
		SpringApplication.run(Application.class, args);

	}
	@Bean
	public CommandLineRunner commandLineRunner(
			AuthenticationService service, CustomerRepository customerRepository, AssetRepository assetRepository
			) {
		return args -> {
			RegisterRequest admin = RegisterRequest.builder()
					.userName("admin@mail.com")
					.password("password")
					.role(ADMIN)
					.build();
			System.out.println("Admin token: " + service.register(admin).getAccessToken());

			RegisterRequest manager = RegisterRequest.builder()
					.userName("user")
					.password("password")
					.role(USER)
					.build();
			System.out.println("User token: " + service.register(manager).getAccessToken());

			Customer testCustomer = new Customer();
			testCustomer.setUsername("admin");
			testCustomer.setPassword("password");
			testCustomer.setRole(Role.ADMIN);
			customerRepository.save(testCustomer);

			Asset tryAsset = new Asset();
			tryAsset.setCustomer(testCustomer);
			tryAsset.setAssetName(TRY);
			tryAsset.setSize(BigDecimal.valueOf(10000));
			tryAsset.setUsableSize(BigDecimal.valueOf(10000));
			assetRepository.save(tryAsset);

			Order buyOrder = new Order();
			buyOrder.setCustomer(testCustomer);
			buyOrder.setAssetName("Gold");
			buyOrder.setOrderSide(Side.BUY);
			buyOrder.setSize(BigDecimal.valueOf(10));
			buyOrder.setPrice(BigDecimal.valueOf(100));

			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(buyOrder);
			System.out.println(json);
		};
	}
}
