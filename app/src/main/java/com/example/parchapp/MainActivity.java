package com.example.parchapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.example.parchapp.analytics.AnalyticsTracker;
import com.example.parchapp.data.AuthRepository;
import com.example.parchapp.data.Repositories;
import com.example.parchapp.util.Callback;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 6;

    private final AuthRepository auth = Repositories.auth();
    private EditText emailInput;
    private EditText passwordInput;
    private Button signInButton;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button continueButton = findViewById(R.id.continue_button);
        View googleButton = findViewById(R.id.google_button);
        signInButton = findViewById(R.id.sign_in_button);
        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> handleGoogleResult(result.getData()));

        // Returning users skip the form cause they have an account: Firebase restored their session in ParchApplication.
        UserProfile restored = auth.getSignedInUser();
        if (restored != null) {
            findViewById(R.id.restored_session_card).setVisibility(View.VISIBLE);
            ((android.widget.TextView) findViewById(R.id.restored_user_name)).setText(restored.getFirstName());
            continueButton.setText("Continue as " + restored.getFirstName());
            continueButton.setOnClickListener(v -> openDashboard(restored));
        } else if (Repositories.isFirebaseAvailable()) {
            findViewById(R.id.restored_session_card).setVisibility(View.GONE);
            continueButton.setOnClickListener(v -> {
                showMessage("Sign in with your university email to continue");
                emailInput.requestFocus();
            });
        } else {
            findViewById(R.id.restored_session_card).setVisibility(View.GONE);
            continueButton.setOnClickListener(v -> openDashboard(UserSession.demoUser()));
        }

        googleButton.setOnClickListener(v -> beginGoogleSignIn());
        signInButton.setOnClickListener(v -> submit(false));
        findViewById(R.id.create_account).setOnClickListener(v ->
                startActivity(new Intent(this, CreateAccountActivity.class)));
        findViewById(R.id.forgot_password).setOnClickListener(v -> sendPasswordReset());
    }

    private void submit(boolean register) {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString();
        if (!isValidEmail(email)) {
            return;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordInput.setError("At least " + MIN_PASSWORD_LENGTH + " characters");
            return;
        }

        signInButton.setEnabled(false);
        Callback<UserProfile> callback = new Callback<UserProfile>() {
            @Override
            public void onSuccess(UserProfile user) {
                signInButton.setEnabled(true);
                passwordInput.setText("");
                openDashboard(user);
            }

            @Override
            public void onError(Exception error) {
                signInButton.setEnabled(true);
                showMessage(register
                        ? "Could not create the account: " + error.getLocalizedMessage()
                        : "Email or password is incorrect");
            }
        };
        if (register) {
            auth.register(email, password, callback);
        } else {
            auth.signIn(email, password, callback);
        }
    }

    private void sendPasswordReset() {
        String email = emailInput.getText().toString().trim();
        if (!isValidEmail(email)) {
            return;
        }
        auth.sendPasswordReset(email, new Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showMessage("Password reset email sent to " + email);
            }

            @Override
            public void onError(Exception error) {
                showMessage("Could not send the reset email");
            }
        });
    }

    private void beginGoogleSignIn() {
        if (!Repositories.isFirebaseAvailable()) {
            auth.signInWithGoogle("demo", new OpenDashboardCallback());
            return;
        }
        int clientIdResource = getResources().getIdentifier(
                "default_web_client_id", "string", getPackageName());
        if (clientIdResource == 0) {
            showMessage("Configure the Firebase OAuth client before using Google sign-in");
            return;
        }
        String clientId = getString(clientIdResource);
        if (clientId.trim().isEmpty()) {
            showMessage("Configure the Firebase OAuth client before using Google sign-in");
            return;
        }
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();
        googleSignInLauncher.launch(GoogleSignIn.getClient(this, options).getSignInIntent());
    }

    private void handleGoogleResult(Intent data) {
        if (data == null) {
            showMessage("Google sign-in was cancelled");
            return;
        }
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try {
            GoogleSignInAccount account = task.getResult(com.google.android.gms.common.api.ApiException.class);
            if (account == null || account.getIdToken() == null) {
                showMessage("Google did not return an identity token");
                return;
            }
            auth.signInWithGoogle(account.getIdToken(), new OpenDashboardCallback());
        } catch (com.google.android.gms.common.api.ApiException error) {
            showMessage("Google sign-in failed: " + error.getStatusCode());
        }
    }

    private final class OpenDashboardCallback implements Callback<UserProfile> {
        @Override
        public void onSuccess(UserProfile user) {
            openDashboard(user);
        }

        @Override
        public void onError(Exception error) {
            showMessage("Google sign-in failed");
        }
    }

    private boolean isValidEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter your university email");
            emailInput.requestFocus();
            return false;
        }
        return true;
    }

    private void openDashboard(UserProfile user) {
        UserSession.setCurrentUser(user);
        AnalyticsTracker.getInstance().setUserId(user.getId());
        startActivity(new Intent(this, DashboardActivity.class));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
