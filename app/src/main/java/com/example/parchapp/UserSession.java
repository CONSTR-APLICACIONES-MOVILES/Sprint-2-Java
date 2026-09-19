package com.example.parchapp;

public final class UserSession {
    private UserSession() {
    }

    /**
     * TODO: ajustar segun usuario. Luego debe provenir del login o de la base de datos.
     */
    public static UserProfile getCurrentUser() {
        return new UserProfile(
                "Alex",
                "AK",
                "Alex Valenzuela",
                "parchapp.me/u/alex-k26",
                2
        );
    }
}
