package com.example.parchapp.data;

import com.example.parchapp.util.Callback;

import java.util.Map;

public interface ProfileRepository {
    void getProfile(Callback<ProfileData> callback);
    void updateProfile(Map<String, Object> fields, Callback<ProfileData> callback);
    void updatePresence(String status, String visibility, String expiresAt, Callback<Void> callback);
}
