package com.example.parchapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CreateGroupActivity extends AppCompatActivity {
    private LinearLayout emailChips;
    private EditText emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        emailChips = findViewById(R.id.email_chips);
        emailInput = findViewById(R.id.email_input_group);

        findViewById(R.id.header_search_btn).setOnClickListener(v -> toast("Search coming soon"));
        findViewById(R.id.header_notif_btn).setOnClickListener(v -> toast("No new notifications"));
        findViewById(R.id.header_profile_btn).setOnClickListener(v -> toast("Profile coming soon"));

        findViewById(R.id.back_group_button).setOnClickListener(v -> finish());
        findViewById(R.id.activity_date_input).setOnClickListener(v -> toast("Date picker coming soon"));
        findViewById(R.id.activity_time_input).setOnClickListener(v -> toast("Time picker coming soon"));
        findViewById(R.id.copy_group_link_button).setOnClickListener(v -> copyGroupLink());
        findViewById(R.id.share_group_link_button).setOnClickListener(v -> toast("Share link opened"));
        findViewById(R.id.show_qr_button).setOnClickListener(v -> {
            View qr = findViewById(R.id.qr_container);
            qr.setVisibility(qr.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        findViewById(R.id.add_email_button).setOnClickListener(v -> addEmail());
        findViewById(R.id.create_group_button).setOnClickListener(v ->
                startActivity(new Intent(this, ActivityCreatedActivity.class)));
        findViewById(R.id.cancel_create_group).setOnClickListener(v -> finish());
        findViewById(R.id.nav_home_create).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
        findViewById(R.id.nav_schedule_create).setOnClickListener(v -> toast("Schedule coming soon"));
    }

    private void addEmail() {
        String email = emailInput.getText().toString().trim();
        if (email.isEmpty()) {
            emailInput.setError("Enter an email");
            return;
        }

        TextView chip = new TextView(this);
        chip.setText(email + "  ×");
        chip.setTextSize(11);
        chip.setTextColor(getColor(R.color.brand_primary));
        chip.setBackgroundResource(R.drawable.bg_light_blue);
        chip.setPadding(18, 8, 18, 8);
        chip.setOnClickListener(v -> emailChips.removeView(v));
        emailChips.addView(chip);
        emailInput.setText("");
    }

    private void copyGroupLink() {
        String link = ((TextView) findViewById(R.id.group_link_text)).getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Group link", link));
        toast("Group link copied");
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
