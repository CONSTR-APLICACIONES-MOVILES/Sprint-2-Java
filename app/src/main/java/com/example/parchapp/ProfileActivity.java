package com.example.parchapp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.parchapp.data.ProfileData;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.util.Callback;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ProfileData profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        findViewById(R.id.profile_back_button).setOnClickListener(v -> finish());
        findViewById(R.id.profile_edit_button).setOnClickListener(v -> showEditDialog());
        findViewById(R.id.profile_status_button).setOnClickListener(v -> showStatusDialog());
        findViewById(R.id.profile_logout_button).setOnClickListener(v -> logout());
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadProfile();
    }

    private void loadProfile() {
        Repositories.profile().getProfile(new Callback<ProfileData>() {
            @Override public void onSuccess(ProfileData result) { profile = result; render(); }
            @Override public void onError(Exception error) {
                Toast.makeText(ProfileActivity.this, "Could not load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render() {
        ((android.widget.TextView) findViewById(R.id.profile_initials)).setText(profile.getInitials());
        ((android.widget.TextView) findViewById(R.id.profile_name)).setText(value(profile.getName(), "Name not specified"));
        ((android.widget.TextView) findViewById(R.id.profile_program)).setText(value(profile.getProgram(), "Program not specified"));
        ((AppCompatButton) findViewById(R.id.profile_status_button)).setText(
                value(profile.getStatus(), "Status not specified"));
        ((android.widget.TextView) findViewById(R.id.profile_plans_count)).setText(
                profile.getPlansCompleted() + "\nPlans completed");
        ((android.widget.TextView) findViewById(R.id.profile_groups_count)).setText(
                profile.getActiveGroups() + "\nActive groups");
        ((android.widget.TextView) findViewById(R.id.profile_rate)).setText(
                profile.getOnTimeRate() == null ? "No data\nOn-time rate" :
                        String.format(Locale.US, "%.0f%%\nOn-time rate", profile.getOnTimeRate()));
        ((android.widget.TextView) findViewById(R.id.profile_academic_card)).setText(
                "Academic information\n" + value(profile.getUniversity(), "University not specified")
                        + "\n" + value(profile.getCampus(), "Campus not specified")
                        + "\n" + (profile.getSemester() > 0 ? "Semester " + profile.getSemester() : "Semester not specified")
                        + "\n" + value(profile.getAcademicLevel(), "Academic level not specified"));
        ((android.widget.TextView) findViewById(R.id.profile_groups_card)).setText(
                "Groups\n" + profile.getActiveGroups() + " active groups");
        ((android.widget.TextView) findViewById(R.id.profile_calendar_card)).setText(
                "Calendar and privacy\nAvailability is only shared according to your backend privacy settings.");
    }

    private void showEditDialog() {
        if (profile == null) return;
        LinearLayout fields = new LinearLayout(this);
        fields.setOrientation(LinearLayout.VERTICAL);
        fields.setPadding(dp(20), dp(4), dp(20), 0);
        EditText name = field("Name", profile.getName());
        EditText program = field("Program", profile.getProgram());
        EditText campus = field("Campus", profile.getCampus());
        fields.addView(name); fields.addView(program); fields.addView(campus);
        new AlertDialog.Builder(this).setTitle("Edit profile").setView(fields)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {
                    Map<String, Object> values = new HashMap<>();
                    values.put("name", name.getText().toString().trim());
                    values.put("program", program.getText().toString().trim());
                    values.put("campus", campus.getText().toString().trim());
                    Repositories.profile().updateProfile(values, new Callback<ProfileData>() {
                        @Override public void onSuccess(ProfileData result) { profile = result; render(); }
                        @Override public void onError(Exception error) { Toast.makeText(ProfileActivity.this, "Could not update profile", Toast.LENGTH_SHORT).show(); }
                    });
                }).show();
    }

    private void showStatusDialog() {
        String[] values = {"active", "busy", "inClass", "focused", "inTransit"};
        new AlertDialog.Builder(this).setTitle("Set status").setItems(values, (dialog, which) -> {
            Calendar expiry = Calendar.getInstance();
            expiry.add(Calendar.HOUR_OF_DAY, 1);
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            Repositories.profile().updatePresence(values[which], "groups", format.format(expiry.getTime()), new Callback<Void>() {
                @Override public void onSuccess(Void ignored) { loadProfile(); }
                @Override public void onError(Exception error) { Toast.makeText(ProfileActivity.this, "Could not update status", Toast.LENGTH_SHORT).show(); }
            });
        }).show();
    }

    private EditText field(String hint, String value) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value);
        return field;
    }

    private void logout() {
        Repositories.auth().signOut();
        UserSession.setCurrentUser(null);
        startActivity(new android.content.Intent(this, MainActivity.class));
        finishAffinity();
    }

    private String value(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
