package com.example.parchapp.data;

import androidx.annotation.Nullable;

import com.example.parchapp.UserProfile;
import com.example.parchapp.util.Callback;
import com.example.parchapp.util.MainThread;

/*Used when Firebase is not configured, as to say any well-formed email and password is accepted and the session is not kept between launches.
 */
class DemoAuthRepository implements AuthRepository {
    private UserProfile signedInUser;

    @Nullable
    @Override
    public UserProfile getSignedInUser() {
        return signedInUser;
    }

    @Override
    public void signIn(String email, String password, Callback<UserProfile> callback) {
        signedInUser = UserProfile.fromAccount(email.toLowerCase(java.util.Locale.ROOT), email, null);
        UserProfile user = signedInUser;
        MainThread.post(() -> callback.onSuccess(user));
    }

    @Override
    public void register(String email, String password, Callback<UserProfile> callback) {
        signIn(email, password, callback);
    }

    @Override
    public void sendPasswordReset(String email, Callback<Void> callback) {
        MainThread.post(() -> callback.onSuccess(null));
    }

    @Override
    public void signOut() {
        signedInUser = null;
    }
}
