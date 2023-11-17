package com.example.urbancycle.urbancycle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.urbancycle.R;
import com.example.urbancycle.urbancycle.Authentication.LoginFragment;
import com.example.urbancycle.urbancycle.Authentication.RegisterFragment;

public class MainActivity_Authentication extends AppCompatActivity{

    private Button login;
    private Button Register;
    private ImageView Icon;
    private RelativeLayout Wave1, Wave2;
    private TextView AppTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_authentication);

        Icon = findViewById(R.id.UrbanCycleImage);
        Wave1 = findViewById(R.id.Wave1);
        Wave2 = findViewById(R.id.Wave2);
        AppTitle = findViewById(R.id.AppTitle);

        login = findViewById(R.id.Login);
        Register = findViewById(R.id.Register);

        login.setOnClickListener(view ->{

            // Hide the components
            login.setVisibility(View.GONE);
            Register.setVisibility(View.GONE);
            Icon.setVisibility(View.GONE);

            AppTitle.setVisibility(View.GONE);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .addToBackStack(null)
                    .commit();
        });

        Register.setOnClickListener(view -> {

            login.setVisibility(View.GONE); // Hide the login button
            Register.setVisibility(View.GONE); // Hide the register button
            Icon.setVisibility(View.GONE);
            Wave1.setVisibility(View.GONE);
            Wave2.setVisibility(View.GONE);
            AppTitle.setVisibility(View.GONE);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new RegisterFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Set up a listener for back stack changes
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            // When there are no more fragments on the stack, make the buttons visible
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                showButtons();
            }
        });
    }

    private void showButtons() {
        // Making things visible here

        login.setVisibility(View.VISIBLE);
        Register.setVisibility(View.VISIBLE);
        Icon.setVisibility(View.VISIBLE);
        Wave1.setVisibility(View.VISIBLE);
        Wave2.setVisibility(View.VISIBLE);
        AppTitle.setVisibility(View.VISIBLE);

    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed(); // This will exit the app if the back stack is empty
        }
    }

}