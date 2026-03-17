package com.ra.base_spring_boot.service.impl.auth;

import com.ra.base_spring_boot.exception.HttpBadRequest;
import com.ra.base_spring_boot.model.Account;
import com.ra.base_spring_boot.model.AccountActivationToken;
import com.ra.base_spring_boot.model.constants.AccountStatus;
import com.ra.base_spring_boot.repository.account.ActivationTokenRepo;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.service.auth.AccountActivationService;
import com.ra.base_spring_boot.utils.OtpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AccountActivationServiceImpl implements AccountActivationService {

    private final ActivationTokenRepo activationTokenRepo;
    private final IAccountRepository accountRepo;
    private final EmailService emailService;

    @Override
    public void createActivationToken(Account account) {
        activationTokenRepo.deleteByAccount(account);

        String otp = OtpUtil.generateOtp(6);

        AccountActivationToken activationToken = new AccountActivationToken(
                otp,
                account,
                LocalDateTime.now().plusMinutes(10));
        activationTokenRepo.save(activationToken);

        String content = "Your activation code is: " + otp +
                "\nIt will expire in 10 minutes.";
        emailService.sendMail(account.getEmail(), "Account Activation", content);
    }

    @Override
    public void activateAccount(String email, String otp) {
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("No account found with email: " + email));

        AccountActivationToken token = activationTokenRepo.findByAccount(account)
                .orElseThrow(() -> new HttpBadRequest("No activation code found"));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            activationTokenRepo.delete(token);
            throw new HttpBadRequest("Activation code has expired");
        }

        if (!token.getOtp().equals(otp)) {
            throw new HttpBadRequest("Invalid activation code");
        }

        account.setEmailVerified(true);
        account.setIsActive(true);
        accountRepo.save(account);

        activationTokenRepo.delete(token);
    }

    @Override
    public void resendActivationToken(String email) {
        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new NoSuchElementException("No account found with email: " + email));

        if (account.getIsActive() == Boolean.TRUE) {
            throw new HttpBadRequest("Account is already activated");
        }

        createActivationToken(account);
    }

}
