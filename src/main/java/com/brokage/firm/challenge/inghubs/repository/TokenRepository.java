package com.brokage.firm.challenge.inghubs.repository;

import com.brokage.firm.challenge.inghubs.token.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Integer> {

    @Query(value = """
            select t from Token t inner join Customer c\s
            on t.customer.id = c.id\s
            where c.id = :id and (t.expired = false or t.revoked = false)\s
            """)
    List<Token> findAllValidTokenByUser(Long id);

    Optional<Token> findByToken(String token);
}
