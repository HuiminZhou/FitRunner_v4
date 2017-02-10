package com.example.huimin_zhou.Huimin_Zhou_FitRunner;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;

/**
 * Created by Lucidity on 17/1/17.
 */

public class ProfileActivity extends Activity {
    public static final String PREFER_NAME = "myPref";
    public static final String MY_NAME = "myName";
    public static final String MY_EMAIL = "myEmail";
    public static final String MY_PHONE = "myPhone";
    public static final String MY_GENDER = "myGender";
    public static final String MY_MAJOR = "myMajor";
    public static final String MY_CLASS = "myClass";
    public static final String MY_PHOTO = "myPhoto";
    public static final String URI_KEY = "uri_record";
    private Uri UriPath = null;
    private ImageView mImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mImageView = (ImageView) findViewById(R.id.view_photo);

        if (savedInstanceState != null) {
            UriPath = savedInstanceState.getParcelable(URI_KEY);
        }
        loadProfile();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the settings before the activity goes into background
        outState.putParcelable(URI_KEY, UriPath);
    }

    // load the saved settings if there are ones
    protected void loadProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFER_NAME, MODE_PRIVATE);
        String name = sharedPreferences.getString(MY_NAME, "");
        String email = sharedPreferences.getString(MY_EMAIL, "");
        String phone = sharedPreferences.getString(MY_PHONE, "");
        String myclass = sharedPreferences.getString(MY_CLASS, "");
        String mymajor = sharedPreferences.getString(MY_MAJOR, "");
        String photo = sharedPreferences.getString(MY_PHOTO, "");
        int gender = sharedPreferences.getInt(MY_GENDER, -1);

        ((EditText)findViewById(R.id.edit_name)).setText(name);
        ((EditText)findViewById(R.id.edit_email)).setText(email);
        ((EditText)findViewById(R.id.edit_phone)).setText(phone);
        ((EditText)findViewById(R.id.edit_class)).setText(myclass);
        ((EditText)findViewById(R.id.edit_major)).setText(mymajor);
        if (gender >= 0) {
            RadioButton radioButton = (RadioButton) ((RadioGroup)findViewById(R.id.radio_gender))
                    .getChildAt(gender);
            radioButton.setChecked(true);
        }
        if (UriPath != null) {
            mImageView.setImageURI(UriPath);
        } else if (photo.length() != 0) {
            UriPath = Uri.parse(photo);
            mImageView.setImageURI(Uri.parse(photo));
        } else {
            mImageView.setImageResource(R.drawable.default_photo);
        }
    }

    private void getPermission() {
        if (Build.VERSION.SDK_INT >= 23
                && (checkSelfPermission(Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    0);
        }
    }

    // take a picture and crop it
    public void onChangeClick(View view) {
        DialogFragment fragment = DialogsFragment.newInstance(Global.DIALOG_ID_PHOTO_PICKER);
        fragment.show(getFragmentManager(), "DIALOG_ID_PHOTO_PICKER");
    }

    public void onCamera(int which) {
        Intent cameraIntent;
        getPermission();
        try {
            File tmpFile = File.createTempFile(
                    "tmpPhoto",
                    ".jpg",
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES));
            UriPath = FileProvider.getUriForFile(
                    this,
                    "com.example.huimin_zhou.Huimin_Zhou_FitRunner.fileprovider",
                    tmpFile);
        } catch (IOException e) {
            Log.d("Output", "IOException");
        }
        switch (which) {
            case Global.ID_PHOTO_PICKER_FROM_CAMERA:
                cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, UriPath);
                    startActivityForResult(cameraIntent, Global.ID_PHOTO_PICKER_FROM_CAMERA);
                }
                break;
            case Global.ID_PHOTO_PICKER_FROM_GALLERY:
                cameraIntent = new Intent();
                cameraIntent.setType("image/*");
                cameraIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(cameraIntent, "Select Picture"),
                        Global.ID_PHOTO_PICKER_FROM_GALLERY);
                break;
        }
    }

    protected void onActivityResult(int request_code, int result_code, Intent data) {
        if (result_code == RESULT_OK) {
            try {
                switch (request_code) {
                    case Global.ID_PHOTO_PICKER_FROM_CAMERA:
                        // TODO: crop the photo and save in the same uri
                        Crop.of(UriPath, UriPath).asSquare().start(this);
                        break;
                    case Global.ID_PHOTO_PICKER_FROM_GALLERY:
                        Uri dataData = data.getData();
                        Crop.of(dataData, UriPath).asSquare().start(this);
                        break;
                    case Crop.REQUEST_CROP:
                        mImageView.setImageURI(Crop.getOutput(data));
                        break;
                    default:
                        return;
                }
            } catch (ActivityNotFoundException e) {
                (Toast.makeText(this, "Crop is not supported", Toast.LENGTH_SHORT)).show();
            }
        }
    }

    // save all the current settings
    public void onSaveClick(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFER_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();

        String name = ((EditText)findViewById(R.id.edit_name)).getText().toString();
        String email = ((EditText)findViewById(R.id.edit_email)).getText().toString();
        String phone = ((EditText)findViewById(R.id.edit_phone)).getText().toString();
        String major = ((EditText)findViewById(R.id.edit_major)).getText().toString();
        RadioGroup genderGroup = (RadioGroup)findViewById(R.id.radio_gender);
        int gender = genderGroup.indexOfChild(findViewById(genderGroup.getCheckedRadioButtonId()));
        String myclass = ((EditText)findViewById(R.id.edit_class)).getText().toString();

        editor.putString(MY_NAME, name);
        editor.putString(MY_EMAIL, email);
        editor.putString(MY_PHONE, phone);
        editor.putInt(MY_GENDER, gender);
        editor.putString(MY_MAJOR, major);
        editor.putString(MY_CLASS, myclass);
        if (UriPath != null) {
            editor.putString(MY_PHOTO, UriPath.toString());
        }
        editor.commit();

        Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    // return to calling activity
    public void onCancelClick(View view) {
        Toast.makeText(getApplicationContext(), "Cancelled", Toast.LENGTH_SHORT).show();
        finish();
    }

}
