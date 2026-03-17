package com.ra.base_spring_boot.repository.account;


import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetPasswordTokenRepo extends JpaRepository<ResetPasswordToken, Integer> {
    Optional<ResetPasswordToken> findByToken(String token);
    void deleteByAccount(Account account);
}

