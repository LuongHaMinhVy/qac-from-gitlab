package com.ra.base_spring_boot.service.auth;

import com.ra.base_spring_boot.model.constants.SocialProvider;

import java.util.Map;

public interface OAuth2UserInfo {
    String getId();
    String getEmail();
    String getName();
    String getAvatar();
    Map<String, Object> getAttributes();
    SocialProvider getProvider();
}

