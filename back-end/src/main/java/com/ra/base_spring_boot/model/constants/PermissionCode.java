package com.ra.base_spring_boot.model.constants;

public class PermissionCode {
    // Content Management
    public static final String ARTICLE_CREATE = "ARTICLE_CREATE";
    public static final String ARTICLE_UPDATE_OWN = "ARTICLE_UPDATE_OWN";
    public static final String ARTICLE_UPDATE_ALL = "ARTICLE_UPDATE_ALL";
    public static final String ARTICLE_APPROVE = "ARTICLE_APPROVE";
    public static final String ARTICLE_PUBLISH = "ARTICLE_PUBLISH";
    public static final String ARTICLE_DELETE = "ARTICLE_DELETE";

    public static final String VIDEO_CREATE = "VIDEO_CREATE";
    public static final String VIDEO_UPDATE_OWN = "VIDEO_UPDATE_OWN";
    public static final String VIDEO_UPDATE_ALL = "VIDEO_UPDATE_ALL";
    public static final String VIDEO_DELETE = "VIDEO_DELETE";
    public static final String MEDIA_MANAGE = "MEDIA_MANAGE";
    public static final String CATEGORY_MANAGE = "CATEGORY_MANAGE";
    public static final String TAG_MANAGE = "TAG_MANAGE";
    public static final String BADWORD_MANAGE = "BADWORD_MANAGE";

    // User & IAM
    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DEACTIVATE = "USER_DEACTIVATE";
    public static final String ROLE_ASSIGN = "ROLE_ASSIGN";
    public static final String PERMISSION_MANAGE = "PERMISSION_MANAGE";
    public static final String ROLE_REQUEST_APPROVE = "ROLE_REQUEST_APPROVE";

    // Engagement
    public static final String COMMENT_CREATE = "COMMENT_CREATE";
    public static final String COMMENT_UPDATE_OWN = "COMMENT_UPDATE_OWN";
    public static final String COMMENT_DELETE_OWN = "COMMENT_DELETE_OWN";
    public static final String COMMENT_DELETE_ALL = "COMMENT_DELETE_ALL";
    public static final String NOTIFICATION_SEND = "NOTIFICATION_SEND";
    public static final String MENTION_USE = "MENTION_USE";

    // System Admin & Logs
    public static final String SYSTEM_SETTING = "SYSTEM_SETTING";
    public static final String MENU_MANAGE = "MENU_MANAGE";
    public static final String ACTIVITY_LOG_VIEW = "ACTIVITY_LOG_VIEW";
    public static final String API_LOG_VIEW = "API_LOG_VIEW";
    public static final String ANALYTICS_VIEW = "ANALYTICS_VIEW";
    public static final String EXPORT_DATA = "EXPORT_DATA";
}
