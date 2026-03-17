package com.ra.base_spring_boot.service.impl.auth;

import com.ra.base_spring_boot.model.constants.SocialProvider;
import com.ra.base_spring_boot.service.auth.OAuth2UserInfo;
import lombok.Data;

import java.util.Map;

@Data
class FacebookOAuth2UserInfo implements OAuth2UserInfo {
    private Map<String, Object> attributes;

    public FacebookOAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
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
        if (attributes.containsKey("picture")) {
            try {
                Map<String, Object> picture = (Map<String, Object>) attributes.get("picture");
                if (picture.containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) picture.get("data");
                    return (String) data.get("url");
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.FACEBOOK;
    }
}
