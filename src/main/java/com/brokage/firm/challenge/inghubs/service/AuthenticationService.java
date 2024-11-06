package com.brokage.firm.challenge.inghubs.service;

import com.brokage.firm.challenge.inghubs.configuration.JwtService;
import com.brokage.firm.challenge.inghubs.entity.Customer;
import com.brokage.firm.challenge.inghubs.entity.LoginRequest;
import com.brokage.firm.challenge.inghubs.entity.LoginResponse;
import com.brokage.firm.challenge.inghubs.entity.RegisterRequest;
import com.brokage.firm.challenge.inghubs.repository.CustomerRepository;
import com.brokage.firm.challenge.inghubs.repository.TokenRepository;
import com.brokage.firm.challenge.inghubs.token.Token;
import com.brokage.firm.challenge.inghubs.token.TokenType;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private final CustomerRepository customerRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;


    public LoginResponse register(RegisterRequest request) {
        Customer customer = Customer.builder()
                .username(request.getUserName())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        Customer savedCustomer = customerRepository.save(customer);
        String jwtToken = jwtService.generateToken(customer);
        saveUserToken(savedCustomer, jwtToken);
        return LoginResponse.builder()
                .accessToken(jwtToken)
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUserName(),
                        request.getPassword()
                )
        );
        Customer customer = customerRepository.findByUsername(request.getUserName())
                .orElseThrow();
        String jwtToken = jwtService.generateToken(customer);
        revokeAllUserTokens(customer);
        saveUserToken(customer, jwtToken);
        return LoginResponse.builder()
                .accessToken(jwtToken)
                .build();
    }

    private void saveUserToken(Customer customer, String jwtToken) {
        Token token = Token.builder()
                .customer(customer)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(Customer customer) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(customer.getId());
        if (validUserTokens.isEmpty()) {
            return;
        }
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

}
