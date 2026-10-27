package com.example.parchapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.parchapp.data.Alert;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AlertsActivity extends AppCompatActivity {
    private final List<Alert> alerts = new ArrayList<>();
    private LinearLayout container;
    private TextView stateText;
    private TextView unreadCount;
    private String category = "all";
    private boolean unreadOnly;
    private String search = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        container = findViewById(R.id.alerts_container);
        stateText = findViewById(R.id.alerts_state_text);
        unreadCount = findViewById(R.id.alerts_unread_count);
        findViewById(R.id.alerts_back_button).setOnClickListener(v -> finish());
        bindFilter(R.id.alerts_filter_all, "all", false);
        bindFilter(R.id.alerts_filter_invitations, "invitations", false);
        bindFilter(R.id.alerts_filter_schedule, "schedule", false);
        bindFilter(R.id.alerts_filter_groups, "groups", false);
        bindFilter(R.id.alerts_filter_unread, "all", true);
        ((android.widget.EditText) findViewById(R.id.alerts_search_input)).addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                search = s.toString().trim().toLowerCase(Locale.ROOT);
                render();
            }
            @Override public void afterTextChanged(Editable s) { }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadAlerts();
    }

    private void loadAlerts() {
        stateText.setText("Loading alerts...");
        Repositories.alerts().getAlerts(new Callback<List<Alert>>() {
            @Override public void onSuccess(List<Alert> result) {
                alerts.clear();
                alerts.addAll(result);
                render();
            }
            @Override public void onError(Exception error) {
                stateText.setText("Could not load alerts");
                Toast.makeText(AlertsActivity.this, "Could not load alerts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render() {
        container.removeAllViews();
        int unread = 0;
        int visible = 0;
        for (Alert alert : alerts) {
            if (!alert.isRead()) unread++;
            if (!matches(alert)) continue;
            container.addView(createCard(alert));
            visible++;
        }
        unreadCount.setText(unread == 0 ? "" : unread + " new");
        unreadCount.setVisibility(unread == 0 ? View.GONE : View.VISIBLE);
        stateText.setText(visible == 0 ? "No alerts match your filters" : "");
        stateText.setVisibility(visible == 0 ? View.VISIBLE : View.GONE);
    }

    private boolean matches(Alert alert) {
        if (unreadOnly && alert.isRead()) return false;
        if (!"all".equals(category) && !category.equals(alert.getCategory())) return false;
        return search.isEmpty() || (alert.getTitle() + " " + alert.getDescription())
                .toLowerCase(Locale.ROOT).contains(search);
    }

    private View createCard(Alert alert) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.setBackgroundResource(alert.isRead() ? R.drawable.bg_dashboard_card : R.drawable.bg_light_blue);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dp(10);
        card.setLayoutParams(cardParams);
        card.setOnClickListener(v -> markRead(alert));

        TextView meta = text(alert.getDisplayKind() + (alert.getTimeLabel().isEmpty() ? "" : " • " + alert.getTimeLabel()), 10, R.color.brand_primary);
        TextView title = text(alert.getTitle(), 15, R.color.brand_dark);
        title.setPadding(0, dp(6), 0, 0);
        TextView description = text(alert.getDescription(), 12, R.color.brand_slate);
        description.setPadding(0, dp(4), 0, 0);
        card.addView(meta);
        card.addView(title);
        if (!alert.getDescription().isEmpty()) card.addView(description);

        if ("invitation".equals(alert.getKind()) && !alert.getActivityId().isEmpty()) {
            LinearLayout actions = new LinearLayout(this);
            actions.setPadding(0, dp(10), 0, 0);
            addResponseButton(actions, alert, "accepted", "Accept", true);
            addResponseButton(actions, alert, "maybe", "Maybe", false);
            addResponseButton(actions, alert, "declined", "Decline", false);
            card.addView(actions);
        }
        return card;
    }

    private void addResponseButton(LinearLayout parent, Alert alert, String response, String label, boolean primary) {
        AppCompatButton button = new AppCompatButton(this);
        button.setText(label);
        button.setTextSize(11);
        button.setAllCaps(false);
        button.setBackgroundResource(primary ? R.drawable.bg_primary_pill : R.drawable.bg_light_blue);
        button.setTextColor(getColor(primary ? android.R.color.white : R.color.brand_primary));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(38), 1);
        params.setMargins(dp(2), 0, dp(2), 0);
        button.setLayoutParams(params);
        button.setOnClickListener(v -> respond(alert, response));
        parent.addView(button);
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(getColor(color));
        return view;
    }

    private void respond(Alert alert, String response) {
        Repositories.alerts().respond(alert.getActivityId(), response, new Callback<Void>() {
            @Override public void onSuccess(Void ignored) {
                markRead(alert);
                Toast.makeText(AlertsActivity.this, "Response saved", Toast.LENGTH_SHORT).show();
            }
            @Override public void onError(Exception error) {
                Toast.makeText(AlertsActivity.this, "Could not save response", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markRead(Alert alert) {
        if (!alert.isRead()) {
            Repositories.alerts().markRead(java.util.Collections.singletonList(alert.getId()), new Callback<Void>() {
                @Override public void onSuccess(Void ignored) { loadAlerts(); }
                @Override public void onError(Exception error) { }
            });
        }
    }

    private void bindFilter(int id, String value, boolean unread) {
        findViewById(id).setOnClickListener(v -> {
            category = value;
            unreadOnly = unread;
            render();
        });
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
