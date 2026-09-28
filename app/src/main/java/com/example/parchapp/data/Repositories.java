package com.example.parchapp.data;

import android.content.Context;

import com.example.parchapp.domain.StatusService;
import com.example.parchapp.util.MainThread;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

/* Creates the repositories once and hands them to the controllers. When the app is built without {@code google-services.json}, in-memory versions with the demo data are used instead, so the prototype keeps working.
 */
public final class Repositories {
    private static boolean firebaseAvailable;
    private static GroupRepository groups;
    private static ActivityRepository activities;
    private static AuthRepository auth;
    private static InsightsRepository insights;
    private static StatusService status;

    private Repositories() {
    }

    public static void init(Context context) {
        if (groups != null) {
            return;
        }
        firebaseAvailable = !FirebaseApp.getApps(context).isEmpty();
        if (firebaseAvailable) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            // Now then in the scenario of offline persistence, screens read the local copy when there is no signal (scenario 9.2).
            db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build());
            groups = new FirestoreGroupRepository(db);
            activities = new FirestoreActivityRepository(db);
            auth = new FirebaseAuthRepository(FirebaseAuth.getInstance());
            insights = new FirestoreInsightsRepository(db);
        } else {
            groups = new InMemoryGroupRepository();
            activities = new InMemoryActivityRepository();
            auth = new DemoAuthRepository();
            insights = callback -> MainThread.post(() ->
                    callback.onSuccess(InsightsRepository.DEFAULT_RECOMMENDED_POSITION));
        }
        status = new StatusService(context);
    }

    public static boolean isFirebaseAvailable() {
        return firebaseAvailable;
    }

    public static GroupRepository groups() {
        return groups;
    }

    public static ActivityRepository activities() {
        return activities;
    }

    public static AuthRepository auth() {
        return auth;
    }

    public static InsightsRepository insights() {
        return insights;
    }

    public static StatusService status() {
        return status;
    }
}
