package com.example.parchapp;

import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button continueButton = findViewById(R.id.continue_button);
        View googleButton = findViewById(R.id.google_button);
        Button signInButton = findViewById(R.id.sign_in_button);

        continueButton.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        googleButton.setOnClickListener(v -> showMessage("Inicio con Google próximamente"));
        signInButton.setOnClickListener(v -> showMessage("Validando credenciales"));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
