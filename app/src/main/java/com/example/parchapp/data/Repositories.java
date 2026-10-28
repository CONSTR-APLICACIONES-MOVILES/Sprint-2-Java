package com.example.parchapp.data;

import android.content.Context;

import com.example.parchapp.BuildConfig;
import com.example.parchapp.domain.StatusService;
import com.example.parchapp.util.MainThread;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctions;

/* Creates the repositories once and hands them to the controllers. When the app is built without {@code google-services.json}, in-memory versions with the demo data are used instead, so the prototype keeps working.
 */
public final class Repositories {
    private static boolean firebaseAvailable;
    private static GroupRepository groups;
    private static ActivityRepository activities;
    private static AuthRepository auth;
    private static InsightsRepository insights;
    private static AlertsRepository alerts;
    private static ProfileRepository profile;
    private static StatusService status;
    private static ScheduleRepository schedules;

    private Repositories() {
    }

    public static void init(Context context) {
        if (groups != null) {
            return;
        }
        if (BuildConfig.USE_FIREBASE_EMULATORS && FirebaseApp.getApps(context).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setProjectId("demo-parchapp")
                    // Placeholders shaped like real values: Cloud Functions validates the format before
                    // calling the emulator. They never reach Google, because debug talks to the emulators.
                    .setApplicationId("1:1234567890:android:0123456789abcdef")
                    .setApiKey("AIzaSyDemoParchappEmulatorOnly000000000")
                    .build();
            FirebaseApp.initializeApp(context, options);
        }
        firebaseAvailable = !FirebaseApp.getApps(context).isEmpty();
        if (firebaseAvailable) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            if (BuildConfig.USE_FIREBASE_EMULATORS) {
                firebaseAuth.useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 9099);
                db.useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 8080);
                FirebaseFunctions.getInstance("us-central1")
                        .useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 5001);
            }
            FirebaseFunctions functions = FirebaseFunctions.getInstance("us-central1");
            if (BuildConfig.USE_FIREBASE_EMULATORS) {
                functions.useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, 5001);
            }
            // Now then in the scenario of offline persistence, screens read the local copy when there is no signal (scenario 9.2).
            db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder()
                    .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
                    .build());
            groups = new FirestoreGroupRepository(db);
            activities = new FirestoreActivityRepository(db);
            auth = new FirebaseAuthRepository(firebaseAuth);
            insights = new FirestoreInsightsRepository(db);
            alerts = new FirebaseAlertsRepository();
            profile = new FirebaseProfileRepository();
            schedules = new FunctionsScheduleRepository(functions);
        } else {
            groups = new InMemoryGroupRepository();
            activities = new InMemoryActivityRepository();
            auth = new DemoAuthRepository();
            insights = callback -> MainThread.post(() ->
                    callback.onSuccess(InsightsRepository.DEFAULT_RECOMMENDED_POSITION));
            alerts = new InMemoryAlertsRepository();
            profile = new InMemoryProfileRepository();
            schedules = new InMemoryScheduleRepository();
        }
        status = new StatusService(context);
    }

    public static boolean isFirebaseAvailable() {
        return firebaseAvailable;
    }

    public static boolean isFirebaseAnalyticsAvailable() {
        return firebaseAvailable && !BuildConfig.USE_FIREBASE_EMULATORS;
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

    public static AlertsRepository alerts() {
        return alerts;
    }

    public static ProfileRepository profile() {
        return profile;
    }

    public static ScheduleRepository schedules() {
        return schedules;
    }

    public static StatusService status() {
        return status;
    }
}
