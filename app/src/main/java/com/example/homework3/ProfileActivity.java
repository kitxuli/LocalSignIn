package com.example.homework3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import Common.GlobalVariables;
import Db.Db;
import models.UserModel;
import utility.BitmapUtility;

public class ProfileActivity extends AppCompatActivity {

    ImageView imageViewProfileAvatar;

    TextView textViewGreeting, textViewPhoneNumber, textViewDateOfBirth, textViewBloodGroup, textViewQualification, textViewCoordinates;

    Button buttonEdit;

    SharedPreferences sharedPreferences;

    Db db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imageViewProfileAvatar = findViewById(R.id.imageViewProfileAvatar);

        textViewGreeting = findViewById(R.id.textViewGreeting);
        textViewPhoneNumber = findViewById(R.id.textViewPhoneNumber);
        textViewDateOfBirth = findViewById(R.id.textViewDateOfBirth);
        textViewBloodGroup = findViewById(R.id.textViewBloodGroup);
        textViewQualification = findViewById(R.id.textViewQualification);
        textViewCoordinates = findViewById(R.id.textViewCoordinates);

        buttonEdit = findViewById(R.id.buttonEdit);

        sharedPreferences = getSharedPreferences(GlobalVariables.SharedPreferencesName, MODE_PRIVATE);

        db = new Db(this);

        UserModel userModel = db.GetUser(sharedPreferences.getString(GlobalVariables.SharedPreferencesPhoneNumberKey, ""));

        if (userModel == null) {
            Toast.makeText(this, R.string.GeneralError, Toast.LENGTH_SHORT).show();
            return;
        }

        imageViewProfileAvatar.setImageBitmap(BitmapUtility.getImage(userModel.Avatar));
        textViewGreeting.setText("Welcome " + userModel.Name);
        textViewPhoneNumber.setText(userModel.PhoneNumber);
        textViewDateOfBirth.setText(userModel.DateOfBirth);
        textViewBloodGroup.setText(userModel.BloodGroup);
        textViewQualification.setText(userModel.Qualification);
        textViewCoordinates.setText(userModel.Coordinates);

        showNotification("Welcome " + userModel.Name, "You are on board!");

        buttonEdit.setOnClickListener(v -> Toast.makeText(this, "This feature will be available soon!", Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profilemenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onClickSingOut(MenuItem item) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void showNotification(String title, String text) {
        NotificationManager notificationManager = (NotificationManager) ProfileActivity.this.getSystemService(ProfileActivity.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);

            NotificationChannel mChannel2 = new NotificationChannel("channel-02", "second channel", importance);
            notificationManager.createNotificationChannel(mChannel2);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ProfileActivity.this, channelId)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(title)
                .setContentText(text);

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ProfileActivity.this, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        notificationManager.notify(notificationId, mBuilder.build());
    }
}