package com.ra.base_spring_boot.service.impl.auth;

import com.ra.base_spring_boot.model.constants.SocialProvider;
import com.ra.base_spring_boot.service.auth.OAuth2UserInfo;
import lombok.Data;

import java.util.Map;

@Data
class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getAvatar() {
        return (String) attributes.get("picture");
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GOOGLE;
    }
}
