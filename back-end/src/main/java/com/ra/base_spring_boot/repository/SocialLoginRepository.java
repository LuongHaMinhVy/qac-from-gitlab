package com.ra.base_spring_boot.repository;

import com.ra.base_spring_boot.model.SocialLogin;
import com.ra.base_spring_boot.model.constants.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialLoginRepository extends JpaRepository<SocialLogin, Integer> {
    Optional<SocialLogin> findByProviderAndProviderId(SocialProvider provider, String providerId);
    Optional<SocialLogin> findByProviderAndEmail(SocialProvider provider, String email);
    boolean existsByProviderAndProviderId(SocialProvider provider, String providerId);
    boolean existsByAccount_AccountIdAndProvider(Integer accountId, SocialProvider provider);
    Optional<SocialLogin> findByAccount_AccountIdAndProvider(Integer accountId, SocialProvider provider);
}