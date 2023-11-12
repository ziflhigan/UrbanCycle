package com.example.urbancycle.urbancycle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.urbancycle.R;

import java.sql.Connection;

public class MainActivity_Authentication extends AppCompatActivity{

    private Button login;
    private Button Register;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_authentication);

        login = findViewById(R.id.Login);
        Register = findViewById(R.id.Register);

        login.setOnClickListener(view ->{

            login.setVisibility(View.GONE); // Hide the login button
            Register.setVisibility(View.GONE); // Hide the register button

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();

        });

        Register.setOnClickListener(view -> {

            login.setVisibility(View.GONE); // Hide the login button
            Register.setVisibility(View.GONE); // Hide the register button

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();

        });
    }

}