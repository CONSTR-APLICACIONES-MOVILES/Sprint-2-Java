package com.example.parchapp.data;

import com.example.parchapp.UserSession;
import com.example.parchapp.util.Callback;
import com.example.parchapp.util.MainThread;

import java.util.Collections;
import java.util.Map;

class InMemoryProfileRepository implements ProfileRepository {
    @Override
    public void getProfile(Callback<ProfileData> callback) {
        ProfileData data = ProfileData.fromMap(Collections.singletonMap("name",
                UserSession.getCurrentUser().getFullName()));
        MainThread.post(() -> callback.onSuccess(data));
    }

    @Override
    public void updateProfile(Map<String, Object> fields, Callback<ProfileData> callback) {
        getProfile(callback);
    }

    @Override
    public void updatePresence(String status, String visibility, String expiresAt, Callback<Void> callback) {
        MainThread.post(() -> callback.onSuccess(null));
    }
}
