package com.ra.base_spring_boot.repository.account;


import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.AccountActivationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivationTokenRepo extends JpaRepository<AccountActivationToken,Integer> {
    Optional<AccountActivationToken> findByAccount(Account account);
    void deleteByAccount(Account account);

    void deleteByAccountIn(List<Account> accounts);
}
