package com.example.homework3;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import Common.GlobalVariables;
import Db.Db;
import models.UserModel;

public class LoginActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    EditText editTextPhoneNumber, editTextPassword;
    Button buttonSignIn, buttonSignUp;

    Db db;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mediaPlayer = MediaPlayer.create(this, R.raw.doink);

        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        db = new Db(this);

        sharedPreferences = getSharedPreferences(GlobalVariables.SharedPreferencesName, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        buttonSignIn.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString();
            String password = editTextPassword.getText().toString();

            UserModel userModel = db.GetUser(phoneNumber);

            if(userModel == null || !password.equals(userModel.Password)){
                AlertWarning("User was not found or password is incorrect!");
                return;
            }

            sharedPreferencesEditor.putString(GlobalVariables.SharedPreferencesPhoneNumberKey, userModel.PhoneNumber);
            sharedPreferencesEditor.apply();

            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });

        buttonSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });
    }

    private void AlertWarning(String message) {
        mediaPlayer.start();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this)
                .setMessage(message)
                .setPositiveButton("Okay", null);
        alertDialogBuilder.show();
    }
}