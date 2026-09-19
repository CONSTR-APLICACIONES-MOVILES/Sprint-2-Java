package com.example.parchapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;

public class ActivityCreatedActivity extends AppCompatActivity {

    private static final String[] REMINDER_OPTIONS = {"15 min before", "30 min before", "1 hour before", "None"};
    private int reminderIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_created);

        findViewById(R.id.close_created_button).setOnClickListener(v -> finish());
        findViewById(R.id.copy_invite_button).setOnClickListener(v -> copyInviteLink());
        findViewById(R.id.whatsapp_button).setOnClickListener(v -> toast("Opening WhatsApp share"));

        SwitchCompat calendarToggle = findViewById(R.id.calendar_toggle);
        calendarToggle.setOnCheckedChangeListener((CompoundButton button, boolean isChecked) ->
                toast(isChecked ? "Calendar sync active (Google & Apple)" : "External calendar sync paused"));

        AppCompatButton reminderButton = findViewById(R.id.reminder_button);
        reminderButton.setOnClickListener(v -> {
            reminderIndex = (reminderIndex + 1) % REMINDER_OPTIONS.length;
            String option = REMINDER_OPTIONS[reminderIndex];
            reminderButton.setText(option + " ⌄");
            toast("Reminder set to: " + option);
        });

        findViewById(R.id.view_schedule_button).setOnClickListener(v -> toast("Opening schedule"));
        findViewById(R.id.back_home_button).setOnClickListener(v -> finishAffinity());
    }

    private void copyInviteLink() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Invite link", "parchapp.co/p/algebra-calc-session-24"));
        toast("Invite link copied to clipboard");
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
