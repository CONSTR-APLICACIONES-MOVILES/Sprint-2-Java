package com.example.parchapp;

/**
 * Datos visibles del usuario. Actualmente usa valores de demostracion.
 * TODO: ajustar segun usuario y reemplazar por datos de autenticacion/backend.
 */
public class UserProfile {
    private final String firstName;
    private final String initials;
    private final String fullName;
    private final String inviteLink;
    private final int activePlans;

    public UserProfile(String firstName, String initials, String fullName,
                       String inviteLink, int activePlans) {
        this.firstName = firstName;
        this.initials = initials;
        this.fullName = fullName;
        this.inviteLink = inviteLink;
        this.activePlans = activePlans;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getInitials() {
        return initials;
    }

    public String getFullName() {
        return fullName;
    }

    public String getInviteLink() {
        return inviteLink;
    }

    public int getActivePlans() {
        return activePlans;
    }
}
