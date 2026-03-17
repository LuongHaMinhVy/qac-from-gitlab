package com.ra.base_spring_boot.service.auth;


import com.ra.base_spring_boot.model.Account;

public interface AccountActivationService {
    void createActivationToken(Account account);
    void activateAccount(String email, String otp);
    void resendActivationToken(String email);
}
