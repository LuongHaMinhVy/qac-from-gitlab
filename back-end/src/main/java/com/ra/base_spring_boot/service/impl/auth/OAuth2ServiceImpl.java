package com.ra.base_spring_boot.service.impl.auth;

import com.ra.base_spring_boot.dto.resp.OAuth2Response;
import com.ra.base_spring_boot.dto.resp.SocialAccountResponse;
import com.ra.base_spring_boot.model.*;
import com.ra.base_spring_boot.model.constants.RoleName;
import com.ra.base_spring_boot.model.constants.SocialProvider;
import com.ra.base_spring_boot.repository.*;
import com.ra.base_spring_boot.repository.account.IAccountRepository;
import com.ra.base_spring_boot.repository.account.IRoleRepository;
import com.ra.base_spring_boot.security.jwt.JwtProvider;
import com.ra.base_spring_boot.service.auth.OAuth2Service;
import com.ra.base_spring_boot.service.auth.OAuth2UserInfo;
import com.ra.base_spring_boot.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuth2ServiceImpl implements OAuth2Service {

    private final IAccountRepository accountRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final UserProfileRepository userProfileRepository;
    private final IRoleRepository roleRepository;
    private final JwtProvider jwtProvider;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final MediaService mediaService;

    @Override
    @Transactional
    public OAuth2Response processOAuth2Login(OAuth2AuthenticationToken authentication) {
        try {
            String registrationId = authentication.getAuthorizedClientRegistrationId().toLowerCase();
            Map<String, Object> attributes = authentication.getPrincipal().getAttributes();

            log.info("Processing OAuth2 login for provider: {}", registrationId);

            OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, attributes);

            validateUserInfoRelaxed(userInfo);

            SocialProvider socialProvider = userInfo.getProvider();

            Optional<SocialLogin> existingSocialLogin = socialLoginRepository
                    .findByProviderAndProviderId(socialProvider, userInfo.getId());

            OAuth2AuthorizedClient authorizedClient = getAuthorizedClient(authentication);
            String oauth2RefreshToken = null;

            if (authorizedClient != null) {
                OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
                if (refreshToken != null) {
                    oauth2RefreshToken = refreshToken.getTokenValue();
                    log.info("OAuth2 refresh token retrieved from provider");
                } else {
                    log.info("OAuth2 provider does not provide refresh token");
                }
            }

            Account account;
            boolean isNewUser = false;

            if (existingSocialLogin.isPresent()) {

                SocialLogin socialLogin = existingSocialLogin.get();
                account = socialLogin.getAccount();

                if ((socialLogin.getEmail() == null || socialLogin.getEmail().isBlank())
                        && userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
                    socialLogin.setEmail(userInfo.getEmail());
                    socialLoginRepository.save(socialLogin);
                    log.info("Updated socialLogin email for provider {} and providerId {}", socialProvider,
                            userInfo.getId());
                }
            } else {

                account = findOrCreateAccount(userInfo);

                if (account.getCreatedAt() != null
                        && account.getCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1))) {
                    isNewUser = true;
                    log.info("New user created via {}: {}", socialProvider, account.getEmail());
                } else {
                    log.info("Existing user linked new provider {}: {}", socialProvider, account.getEmail());
                }
            }

            updateLastLogin(account);

            String jwtAccessToken = jwtProvider.generateToken(account.getUsername());
            String jwtRefreshToken = jwtProvider.generateRefreshToken(account.getUsername());

            LocalDateTime jwtTokenExpiresAt = LocalDateTime.now().plusHours(24);

            String refreshTokenToSave = oauth2RefreshToken != null ? oauth2RefreshToken : jwtRefreshToken;

            log.info("JWT tokens generated - AccessToken expires at: {}", jwtTokenExpiresAt);
            if (oauth2RefreshToken != null) {
                log.info("Using OAuth2 refresh token from provider");
            } else {
                log.info("Using JWT refresh token (OAuth2 provider does not provide refresh token)");
            }

            if (existingSocialLogin.isPresent()) {
                updateSocialLogin(existingSocialLogin.get(), userInfo, jwtAccessToken, refreshTokenToSave,
                        jwtTokenExpiresAt);
                log.info("Existing user logged in via {}: {}", socialProvider, account.getEmail());
            } else {
                createSocialLogin(account, userInfo, jwtAccessToken, refreshTokenToSave, jwtTokenExpiresAt);
            }

            String token = jwtAccessToken;

            User userProfile = getUserProfile(account, userInfo);

            List<String> roles = getAccountRoles(account);

            return buildOAuth2Response(token, account, userProfile, roles, socialProvider, isNewUser);

        } catch (Exception e) {
            log.error("Error processing OAuth2 login: ", e);
            throw new RuntimeException("Lỗi đăng nhập OAuth2: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public String linkSocialAccount(Integer accountId, SocialProvider provider, OAuth2UserInfo userInfo) {

        if (socialLoginRepository.existsByAccount_AccountIdAndProvider(accountId, provider)) {
            throw new RuntimeException("Tài khoản đã được liên kết với " + provider);
        }

        if (socialLoginRepository.existsByProviderAndProviderId(provider, userInfo.getId())) {
            throw new RuntimeException(provider + " account đã được liên kết với tài khoản khác");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account không tồn tại"));

        SocialLogin socialLogin = SocialLogin.builder()
                .account(account)
                .provider(provider)
                .providerId(userInfo.getId())
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .avatar(userInfo.getAvatar())
                .connectedAt(LocalDateTime.now())
                .build();

        socialLoginRepository.save(socialLogin);

        log.info("Social account linked: {} -> {}", provider, account.getEmail());
        return "Liên kết " + provider + " thành công";
    }

    @Override
    @Transactional
    public String unlinkSocialAccount(Integer accountId, SocialProvider provider) {
        SocialLogin socialLogin = socialLoginRepository
                .findByAccount_AccountIdAndProvider(accountId, provider)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy liên kết với " + provider));

        Account account = socialLogin.getAccount();

        long socialLoginCount = account.getSocialLogins() != null ? account.getSocialLogins().stream()
                .filter(sl -> !sl.getProvider().equals(provider))
                .count() : 0;

        boolean isOAuthOnlyPassword = isUUID(account.getPasswordHash());

        if (socialLoginCount == 0 && isOAuthOnlyPassword) {
            throw new RuntimeException("Không thể hủy liên kết phương thức đăng nhập duy nhất");
        }

        socialLoginRepository.delete(socialLogin);

        log.info("Social account unlinked: {} from account {}", provider, accountId);
        return "Hủy liên kết " + provider + " thành công";
    }

    @Override
    public List<SocialAccountResponse> getUserSocialAccounts(Integer accountId) {
        List<SocialLogin> socialLogins = socialLoginRepository.findAll()
                .stream()
                .filter(sl -> sl.getAccount().getAccountId().equals(accountId))
                .toList();

        return socialLogins.stream()
                .map(this::convertToSocialAccountResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isAccountLinked(Integer accountId, SocialProvider provider) {
        return socialLoginRepository.existsByAccount_AccountIdAndProvider(accountId, provider);
    }

    private void validateUserInfoRelaxed(OAuth2UserInfo userInfo) {
        if (userInfo == null) {
            throw new RuntimeException("OAuth2 user info is null");
        }
        if (userInfo.getProvider() != SocialProvider.FACEBOOK) {
            if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
                throw new RuntimeException("Không thể lấy email từ " + userInfo.getProvider() +
                        ". Vui lòng cấp quyền truy cập email.");
            }
        }
    }

    private Account findOrCreateAccount(OAuth2UserInfo userInfo) {
        String email = userInfo.getEmail();
        boolean pseudoEmail = false;

        if (email == null || email.isBlank()) {
            email = generatePseudoEmail(userInfo.getProvider(), userInfo.getId());
            pseudoEmail = true;
            log.warn("Using pseudo-email for provider {}: {}", userInfo.getProvider(), email);
        }

        Optional<Account> existingAccount = accountRepository.findByEmail(email);

        if (existingAccount.isPresent()) {
            return existingAccount.get();
        }

        Account newAccount = Account.builder()
                .username(generateUniqueUsername(userInfo))
                .email(email)
                .passwordHash(generateRandomPassword())
                .isActive(true)
                .emailVerified(!pseudoEmail)
                .createdAt(LocalDateTime.now())
                .build();

        Role userRole = roleRepository.findByRoleCode(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("ROLE_USER không tồn tại"));

        if (newAccount.getRoles() == null) {
            newAccount.setRoles(new HashSet<>());
        }
        newAccount.getRoles().add(userRole);

        return accountRepository.save(newAccount);
    }

    private void createSocialLogin(Account account, OAuth2UserInfo userInfo,
            String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        SocialLogin socialLogin = SocialLogin.builder()
                .account(account)
                .provider(userInfo.getProvider())
                .providerId(userInfo.getId())
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .avatar(userInfo.getAvatar())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenExpiresAt(tokenExpiresAt)
                .connectedAt(LocalDateTime.now())
                .build();

        socialLoginRepository.save(socialLogin);
        log.info("SocialLogin created with tokens for provider: {}", userInfo.getProvider());
    }

    private void updateSocialLogin(SocialLogin socialLogin, OAuth2UserInfo userInfo,
            String accessToken, String refreshToken, LocalDateTime tokenExpiresAt) {
        if (userInfo.getName() != null && !userInfo.getName().isBlank()) {
            socialLogin.setName(userInfo.getName());
        }
        if (userInfo.getAvatar() != null && !userInfo.getAvatar().isBlank()) {
            socialLogin.setAvatar(userInfo.getAvatar());
        }

        if ((socialLogin.getEmail() == null || socialLogin.getEmail().isBlank())
                && userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
            socialLogin.setEmail(userInfo.getEmail());
        }

        if (accessToken != null) {
            socialLogin.setAccessToken(accessToken);
        }
        if (refreshToken != null) {
            socialLogin.setRefreshToken(refreshToken);
        }
        if (tokenExpiresAt != null) {
            socialLogin.setTokenExpiresAt(tokenExpiresAt);
        }
        socialLoginRepository.save(socialLogin);
        log.info("SocialLogin updated with new tokens for provider: {}", userInfo.getProvider());
    }

    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AuthenticationToken authentication) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof OAuth2AuthenticationToken oauth2Token) {
                String clientRegistrationId = oauth2Token.getAuthorizedClientRegistrationId();
                String principalName = oauth2Token.getName();
                return authorizedClientService.loadAuthorizedClient(clientRegistrationId, principalName);
            }
        } catch (Exception e) {
            log.warn("Could not retrieve OAuth2AuthorizedClient: {}", e.getMessage());
        }
        return null;
    }

    private void updateLastLogin(Account account) {
        account.setLastLoginAt(LocalDateTime.now());
        accountRepository.save(account);
    }

    private List<String> getAccountRoles(Account account) {
        if (account.getRoles() == null)
            return Collections.emptyList();

        return account.getRoles().stream()
                .map(role -> role.getRoleCode().name())
                .collect(Collectors.toList());
    }

    private User getUserProfile(Account account, OAuth2UserInfo userInfo) {
        return userProfileRepository.findByAccount_Username(account.getUsername())
                .orElseGet(() -> {
                    User profile = User.builder()
                            .account(account)
                            .fullName(userInfo.getName())
                            .avatar(userInfo.getAvatar() != null
                                    ? mediaService.createExternal(userInfo.getAvatar(), userInfo.getName() + "_avatar",
                                            account)
                                    : null)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return userProfileRepository.save(profile);
                });
    }

    private String generateUniqueUsername(OAuth2UserInfo userInfo) {
        String baseUsername;

        if (userInfo.getEmail() != null && !userInfo.getEmail().isBlank()) {
            baseUsername = userInfo.getEmail().split("@")[0];
        } else if (userInfo.getName() != null) {
            baseUsername = userInfo.getName().replaceAll("\\s+", "").toLowerCase();
        } else {
            baseUsername = "user";
        }

        baseUsername = baseUsername.replaceAll("[^a-zA-Z0-9]", "");

        String finalUsername = baseUsername;
        int counter = 1;

        while (accountRepository.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + counter;
            counter++;

            if (counter > 100) {
                finalUsername = "user_" + UUID.randomUUID().toString().substring(0, 8);
                break;
            }
        }

        return finalUsername.toLowerCase();
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString();
    }

    private String generatePseudoEmail(SocialProvider provider, String providerId) {
        return provider.name().toLowerCase() + "_" + providerId + "@noemail.local";
    }

    private OAuth2Response buildOAuth2Response(String token, Account account,
            User userProfile, List<String> roles,
            SocialProvider provider, boolean isNewUser) {
        return new OAuth2Response(
                token,
                "Bearer",
                account.getUsername(),
                account.getEmail(),
                userProfile.getFullName(),
                userProfile.getAvatar() != null ? mediaService.convertToDTO(userProfile.getAvatar()) : null,
                roles,
                provider,
                isNewUser,
                isNewUser ? "Tài khoản mới được tạo từ " + provider : "Đăng nhập thành công với " + provider,
                LocalDateTime.now().plusHours(24));
    }

    private SocialAccountResponse convertToSocialAccountResponse(SocialLogin socialLogin) {
        return new SocialAccountResponse(
                socialLogin.getProvider(),
                socialLogin.getProviderId(),
                socialLogin.getEmail(),
                socialLogin.getName(),
                socialLogin.getAvatar(),
                socialLogin.getConnectedAt());
    }

    private boolean isUUID(String str) {
        if (str == null || str.length() != 36) {
            return false;
        }
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
