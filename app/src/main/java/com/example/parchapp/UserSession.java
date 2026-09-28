package com.example.parchapp;

import com.example.parchapp.data.DemoData;

public final class UserSession {
    private static UserProfile currentUser;

    private UserSession() {
    }

    public static void setCurrentUser(UserProfile user) {
        currentUser = user;
    }

    /* The signed-in user, or the demo user when nobody has signed in is shown. */
    public static UserProfile getCurrentUser() {
        if (currentUser != null) {
            return currentUser;
        }
        return demoUser();
    }

    public static UserProfile demoUser() {
        return new UserProfile(
                DemoData.DEMO_USER_ID,
                "alex.valenzuela@university.edu",
                "Alex",
                "AK",
                "Alex Valenzuela",
                "parchapp.me/u/alex-k26",
                2
        );
    }
}
