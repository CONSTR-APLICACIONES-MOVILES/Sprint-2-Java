package com.example.parchapp.data;

import androidx.annotation.Nullable;

import com.example.parchapp.UserProfile;
import com.example.parchapp.util.Callback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/* Firebase keeps the token and restores the session on the next launch. */
class FirebaseAuthRepository implements AuthRepository {
    private final FirebaseAuth auth;

    FirebaseAuthRepository(FirebaseAuth auth) {
        this.auth = auth;
    }

    @Nullable
    @Override
    public UserProfile getSignedInUser() {
        FirebaseUser user = auth.getCurrentUser();
        return user == null ? null : toProfile(user);
    }

    @Override
    public void signIn(String email, String password, Callback<UserProfile> callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(toProfile(result.getUser())))
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void register(String email, String password, Callback<UserProfile> callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess(toProfile(result.getUser())))
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void sendPasswordReset(String email, Callback<Void> callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(callback::onSuccess)
                .addOnFailureListener(callback::onError);
    }

    @Override
    public void signOut() {
        auth.signOut();
    }

    private static UserProfile toProfile(FirebaseUser user) {
        return UserProfile.fromAccount(user.getUid(), user.getEmail(), user.getDisplayName());
    }
}
