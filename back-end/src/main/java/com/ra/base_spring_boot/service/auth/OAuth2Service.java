package com.ra.base_spring_boot.service.auth;

import com.ra.base_spring_boot.dto.resp.OAuth2Response;
import com.ra.base_spring_boot.dto.resp.SocialAccountResponse;
import com.ra.base_spring_boot.model.constants.SocialProvider;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.List;


public interface OAuth2Service {


    OAuth2Response processOAuth2Login(OAuth2AuthenticationToken authentication);


    String linkSocialAccount(Integer accountId, SocialProvider provider, OAuth2UserInfo userInfo);


    String unlinkSocialAccount(Integer accountId, SocialProvider provider);


    List<SocialAccountResponse> getUserSocialAccounts(Integer accountId);


    boolean isAccountLinked(Integer accountId, SocialProvider provider);
}