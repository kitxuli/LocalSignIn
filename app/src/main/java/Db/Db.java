package Db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import Common.GlobalVariables;
import models.UserModel;

public class Db extends SQLiteOpenHelper {

    private final String TableName = "Users";

    private final String ColumnPhoneNumber = "PhoneNumber";
    private final String ColumnPassword = "PhonePassword";
    private final String ColumnName = "Name";
    private final String ColumnDateOfBirth = "DateOfBirth";
    private final String ColumnBloodGroup = "BloodGroup";
    private final String ColumnQualification = "Qualification";
    private final String ColumnCoordinates = "Coordinates";
    private final String ColumnAvatar = "Avatar";

    SQLiteDatabase sqLiteDatabase;

    public Db(Context context) {
        super(context, GlobalVariables.DataBaseName, null, 1);
        sqLiteDatabase = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TableName + " ("
                + ColumnPhoneNumber + " TEXT,"
                + ColumnPassword + " TEXT,"
                + ColumnName + " TEXT,"
                + ColumnDateOfBirth + " TEXT,"
                + ColumnBloodGroup + " TEXT,"
                + ColumnQualification + " TEXT,"
                + ColumnCoordinates + " TEXT,"
                + ColumnAvatar + " BLOB)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long CreateUser(UserModel userModel) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(ColumnPhoneNumber, userModel.PhoneNumber);
        contentValues.put(ColumnPassword, userModel.Password);
        contentValues.put(ColumnName, userModel.Name);
        contentValues.put(ColumnDateOfBirth, userModel.DateOfBirth);
        contentValues.put(ColumnBloodGroup, userModel.BloodGroup);
        contentValues.put(ColumnQualification, userModel.Qualification);
        contentValues.put(ColumnCoordinates, userModel.Coordinates);
        contentValues.put(ColumnAvatar, userModel.Avatar);

        return sqLiteDatabase.insert(TableName, null, contentValues);
    }

    public UserModel GetUser(String phoneNumber) {
        Cursor cursor = sqLiteDatabase.query(TableName, null, ColumnPhoneNumber + " = ?", new String[]{phoneNumber}, null, null, null);

        if (cursor.getCount() != 1) {
            return null;
        }

        cursor.moveToFirst();

        UserModel userModel = new UserModel() {
            {
                PhoneNumber = cursor.getString(cursor.getColumnIndex(ColumnPhoneNumber));
                Password = cursor.getString(cursor.getColumnIndex(ColumnPassword));
                Name = cursor.getString(cursor.getColumnIndex(ColumnName));
                DateOfBirth = cursor.getString(cursor.getColumnIndex(ColumnDateOfBirth));
                BloodGroup = cursor.getString(cursor.getColumnIndex(ColumnBloodGroup));
                Qualification = cursor.getString(cursor.getColumnIndex(ColumnQualification));
                Coordinates = cursor.getString(cursor.getColumnIndex(ColumnCoordinates));
                Avatar = cursor.getBlob(cursor.getColumnIndex(ColumnAvatar));
            }
        };

        cursor.close();

        return userModel;
    }
}
