package com.example.parchapp.data;

import android.util.Log;

import com.example.parchapp.domain.model.PlannedActivity;
import com.example.parchapp.util.Callback;
import com.example.parchapp.util.MainThread;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

class FirestoreActivityRepository implements ActivityRepository {
    private static final String TAG = "FirestoreActivityRepo";

    private final FirebaseFirestore db;

    FirestoreActivityRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public void createActivity(PlannedActivity activity, Callback<PlannedActivity> callback) {
        DocumentReference ref = db.collection("activities").document();
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", activity.getGroupId());
        map.put("title", activity.getTitle());
        map.put("category", activity.getCategory());
        map.put("date", activity.getDate());
        map.put("time", activity.getTime());
        map.put("location", activity.getLocation());
        map.put("status", activity.getStatus());
        map.put("createdBy", FirebaseAuth.getInstance().getUid());
        map.put("createdAt", FieldValue.serverTimestamp());

        // The write task only completes when the server confirms it, which never happens offline (cause it is when every user accomplish it?).
        // The local cache already has the activity, so the UI can move on right away.
        ref.set(map).addOnFailureListener(e -> Log.w(TAG, "Activity " + ref.getId() + " was rejected", e));
        PlannedActivity saved = activity.withId(ref.getId());
        MainThread.post(() -> callback.onSuccess(saved));
    }
}
