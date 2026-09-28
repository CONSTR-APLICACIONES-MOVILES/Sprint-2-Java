package com.example.parchapp.data;

import com.example.parchapp.util.Callback;
import com.google.firebase.firestore.FirebaseFirestore;

/* Reads {@code insights/bq3_slot_acceptance}, written by the BQ3 scheduled query export. */
class FirestoreInsightsRepository implements InsightsRepository {
    private final FirebaseFirestore db;

    FirestoreInsightsRepository(FirebaseFirestore db) {
        this.db = db;
    }

    @Override
    public void getRecommendedSlotPosition(Callback<Integer> callback) {
        db.collection("insights").document("bq3_slot_acceptance").get()
                .addOnSuccessListener(snapshot -> {
                    Long position = snapshot.getLong("recommended_position");
                    callback.onSuccess(position == null ? DEFAULT_RECOMMENDED_POSITION : position.intValue());
                })
                .addOnFailureListener(e -> callback.onSuccess(DEFAULT_RECOMMENDED_POSITION));
    }
}
