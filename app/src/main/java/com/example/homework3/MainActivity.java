package com.example.homework3;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import Common.GlobalVariables;
import models.UserModel;
import utility.BitmapUtility;
import Db.Db;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mlocationCallback;
    private static final int REQUEST_CHECK_SETTINGS = 102;

    MediaPlayer mediaPlayer;

    ImageView imageViewProfileAvatar;
    EditText editTextName, editTextPassword, editTextConfirmPassword, editTextPhoneNumber, editTextDateOfBirth;
    RadioGroup radioGroupBloodGroup;
    Spinner spinnerQualification;
    TextView textViewCoordinates;
    CheckBox checkBoxIAgree;
    Button buttonSave;

    Db db;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPreferencesEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
        mLocationRequest = createLocationRequest();
        mlocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                textViewCoordinates.setText(location.getLatitude() + " - " + location.getLongitude());
                fusedLocationClient.removeLocationUpdates(new LocationCallback() {
                });
            }
        };

        mediaPlayer = MediaPlayer.create(this, R.raw.doink);

        imageViewProfileAvatar = findViewById(R.id.imageViewProfileAvatar);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextName = findViewById(R.id.editTextName);
        editTextDateOfBirth = findViewById(R.id.editTextDateOfBirth);
        radioGroupBloodGroup = findViewById(R.id.radioGroupBloodGroup);
        spinnerQualification = findViewById(R.id.spinnerQualification);
        textViewCoordinates = findViewById(R.id.textViewCoordinates);
        checkBoxIAgree = findViewById(R.id.checkBoxIAgree);
        buttonSave = findViewById(R.id.buttonSave);

        db = new Db(this);

        sharedPreferences = getSharedPreferences(GlobalVariables.SharedPreferencesName, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();

        Seeder();

        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getExtras() != null) {
                            imageViewProfileAvatar.setImageBitmap((Bitmap) data.getExtras().get("data"));
                        }
                    }
                });

        imageViewProfileAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            myActivityResultLauncher.launch(intent);
        });

        DatePickerDialog.OnDateSetListener datePickerDialog = (view, year, monthOfYear, dayOfMonth) -> {
            Calendar calendar = new GregorianCalendar(year, monthOfYear, dayOfMonth);
            editTextDateOfBirth.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime()));
        };

        editTextDateOfBirth.setOnClickListener(v -> {
            Calendar currentTime = Calendar.getInstance();
            new DatePickerDialog(this, datePickerDialog, currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH)).show();
        });

        textViewCoordinates.setOnClickListener(v -> startLocationUpdates());

        buttonSave.setOnClickListener(v -> {

            UserModel userModel = GetUserData();
            if (userModel == null) {
                return;
            }

            UserModel dbUserModel = db.GetUser(userModel.PhoneNumber);
            if (dbUserModel != null) {
                AlertWarning("Phone number is already used!");
                return;
            }

            String confirmPassword = editTextConfirmPassword.getText().toString();
            if (confirmPassword == null || !confirmPassword.equals(userModel.Password)) {
                AlertWarning("Passwords don't match!");
                return;
            }

            sharedPreferencesEditor.putString(GlobalVariables.SharedPreferencesPhoneNumberKey, userModel.PhoneNumber);
            sharedPreferencesEditor.apply();

            long result = db.CreateUser(userModel);

            if (result == -1) {
                AlertWarning(getString(R.string.GeneralError) + "MainActivity");
                return;
            }

            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {// If request is cancelled, the result arrays are empty.
            //permission is granted now start a background service
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                textViewCoordinates.performClick();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            textViewCoordinates.performClick();
        }
    }

    //Location start

    protected LocationRequest createLocationRequest() {
        return LocationRequest.create()
                .setInterval(30000)
                .setFastestInterval(10000)
                .setSmallestDisplacement(30)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                showPermissionAlert();
            }
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest, mlocationCallback, null /* Looper */);
    }

    private void showPermissionAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
    }

    // Location end

    public void onClickAbout(MenuItem item) {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private UserModel GetUserData() {
        if (checkBoxIAgree.isChecked()) {

            String name = editTextName.getText().toString();
            if (name == null || name.isEmpty()) {
                AlertWarning("Please fill name");
                return null;
            }

            String phoneNumber = editTextPhoneNumber.getText().toString();
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                AlertWarning("Please fill phoneNumber");
                return null;
            }

            String password = editTextPassword.getText().toString();
            if (password == null || password.isEmpty()) {
                AlertWarning("Please fill password");
                return null;
            }

            String dateOfBirth = editTextDateOfBirth.getText().toString();
            if (dateOfBirth == null || dateOfBirth.isEmpty()) {
                AlertWarning("Please choose date of birth");
                return null;
            }

            RadioButton radioButtonSelectedBloodGroup = findViewById(radioGroupBloodGroup.getCheckedRadioButtonId());
            if (radioButtonSelectedBloodGroup == null) {
                AlertWarning("Please choose blood group");
                return null;
            }
            String bloodGroup = radioButtonSelectedBloodGroup.getText().toString();

            String qualification = spinnerQualification.getSelectedItem().toString();

            String coordinates = textViewCoordinates.getText().toString();
            if (coordinates.isEmpty()) {
                AlertWarning("Please fill coordinates");
                return null;
            }

            return new UserModel() {
                {
                    PhoneNumber = phoneNumber;
                    Name = name;
                    Password = password;
                    DateOfBirth = dateOfBirth;
                    BloodGroup = bloodGroup;
                    Qualification = qualification;
                    Coordinates = coordinates;
                    Avatar = BitmapUtility.getBytes(((BitmapDrawable) imageViewProfileAvatar.getDrawable()).getBitmap());
                }
            };

        } else {
            AlertWarning("Please agree to continue");
            return null;
        }
    }

    private void AlertWarning(String message) {
        mediaPlayer.start();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Okay", null);
        alertDialogBuilder.show();
    }

    private void Seeder() {
        ArrayList<String> qualifications = new ArrayList<>(Arrays.asList("Higher Certificate", "National Diploma", "Bachelor's Degree", "Honours Degree", "Master's Degree", "Doctoral Degree"));
        spinnerQualification.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, qualifications));
    }
}