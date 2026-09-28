package com.example.parchapp.data;

import androidx.annotation.Nullable;

import com.example.parchapp.UserProfile;
import com.example.parchapp.util.Callback;

/* Authentication in this case. Passwords are handed to the provider and never stored by the app. */
public interface AuthRepository {
    /* The session restored from a previous launch, or null. */
    @Nullable
    UserProfile getSignedInUser();

    void signIn(String email, String password, Callback<UserProfile> callback);

    void register(String email, String password, Callback<UserProfile> callback);

    void sendPasswordReset(String email, Callback<Void> callback);

    void signOut();
}
