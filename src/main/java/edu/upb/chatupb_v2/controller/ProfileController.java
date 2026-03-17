package edu.upb.chatupb_v2.controller;

import edu.upb.chatupb_v2.model.repository.UserProfileDao;

public class ProfileController {
    private final UserProfileDao userProfileDao;

    public ProfileController() {
        this.userProfileDao = new UserProfileDao();
    }

    public String findUserName(String userId) {
        return userProfileDao.findUserName(userId);
    }

    public void upsertUserName(String userId, String userName) {
        userProfileDao.upsertUserName(userId, userName);
    }

    public String findThemeId(String userId) {
        return userProfileDao.findThemeId(userId);
    }

    public void upsertThemeId(String userId, String themeId) {
        userProfileDao.upsertThemeId(userId, themeId);
    }
}
