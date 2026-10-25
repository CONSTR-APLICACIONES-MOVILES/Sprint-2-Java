package com.example.parchapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parchapp.analytics.AnalyticsTracker;
import com.example.parchapp.data.AuthRepository;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.util.Callback;

public class CreateAccountActivity extends AppCompatActivity {
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AuthRepository auth = Repositories.auth();
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmInput;
    private Button createButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        emailInput = findViewById(R.id.create_email_input);
        passwordInput = findViewById(R.id.create_password_input);
        confirmInput = findViewById(R.id.create_confirm_password_input);
        createButton = findViewById(R.id.create_account_button);

        findViewById(R.id.back_to_sign_in).setOnClickListener(v -> finish());
        createButton.setOnClickListener(v -> createAccount());
    }

    private void createAccount() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        String confirmation = confirmInput.getText().toString();
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email");
            emailInput.requestFocus();
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordInput.setError("At least " + MIN_PASSWORD_LENGTH + " characters");
            passwordInput.requestFocus();
            return;
        }
        if (!password.equals(confirmation)) {
            confirmInput.setError("Passwords do not match");
            confirmInput.requestFocus();
            return;
        }

        createButton.setEnabled(false);
        auth.register(email, password, new Callback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                UserSession.setCurrentUser(user);
                AnalyticsTracker.getInstance().setUserId(user.getId());
                startActivity(new Intent(CreateAccountActivity.this, DashboardActivity.class));
                finishAffinity();
            }

            @Override
            public void onError(Exception error) {
                createButton.setEnabled(true);
                Toast.makeText(CreateAccountActivity.this,
                        "Could not create the account: " + error.getLocalizedMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
