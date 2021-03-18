package com.example.homework3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class ProfileActivity extends AppCompatActivity {

    ListView listViewProfile;
    Button buttonBack;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        listViewProfile = findViewById(R.id.listViewProfile);
        buttonBack = findViewById(R.id.buttonBack);

        sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);

        listViewProfile.setAdapter(new ArrayAdapter(ProfileActivity.this, android.R.layout.simple_list_item_1, getIntent().getStringArrayListExtra("profileInfo")));

        listViewProfile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listViewProfile.getItemAtPosition(position).toString().contains("Phone Number")) {

                    if (ActivityCompat.checkSelfPermission(ProfileActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                        showPermissionAlert();
                        return;
                    }

                    showNotification("Phone number clicked");
                    sendSms("Phone number clicked", sharedPreferences.getString("phoneNumber", "123").toString());
                }
            }
        });

        buttonBack.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 101: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                } else {
                    // permission denied
                }
            }
        }
    }

    private void showNotification(String text) {
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
                .setContentTitle("ITM Reopened")
                .setContentText(text);

        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ProfileActivity.this, 0, intent, 0);
        mBuilder.setContentIntent(pendingIntent);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    private void sendSms(String message, String phoneNumber) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(message, null, phoneNumber, null, null);
        Toast.makeText(ProfileActivity.this, "SMS Sent", Toast.LENGTH_SHORT).show();
    }

    private void showPermissionAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
        }
    }
}