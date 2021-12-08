package com.main.notesapplication.View.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputLayout;
import com.main.notesapplication.Control.FireBase;
import com.main.notesapplication.Control.Permission;
import com.main.notesapplication.Model.Note;
import com.main.notesapplication.Control.OnImageSaveListener;
import com.main.notesapplication.R;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NoteActivity extends AppCompatActivity {

    private ImageView IMG_addImage;
    private TextInputLayout LBL_title, LBL_body, LBL_date;
    private Button BTN_save, BTN_delete;
    private Bitmap imageBitmap = null;
    private static final int TAKE_PHOTO_REQUEST_CODE = 0;
    private static final int CHOOSE_FROM_GALLERY_REQUEST_CODE = 1;
    private Note note = null;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            note = bundle.getParcelable("note");
            Log.d("pttt", "onCreate: NOTE ID IMG " + note.getImageId() + " " + note.getBody() + note.getDate());
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setContentView(R.layout.activity_note);
        findViews();
        initViews();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == TAKE_PHOTO_REQUEST_CODE) && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            IMG_addImage.setImageBitmap(imageBitmap);
        } else if ((requestCode == CHOOSE_FROM_GALLERY_REQUEST_CODE) && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                IMG_addImage.setImageBitmap(imageBitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews() {
        BTN_save.setClickable(true);
        if (note != null)
            updateViews();
        else {
            LBL_date.getEditText().setText(getDateString());
        }
        BTN_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkForValidInput();
            }
        });
        BTN_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(note!=null)
                    FireBase.getInstance().deleteFromFireStore(note.getNoteNumber());
                    NoteActivity.this.finish();
            }
        });
        IMG_addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("pttt", "onClick: IMG_addImage");
                if(note!=null)
                    Toast.makeText(NoteActivity.this,"Image Uneditable !",Toast.LENGTH_SHORT).show();
                else
                    openCameraDialog();
            }
        });
    }

    private void updateViews() {
        LBL_date.getEditText().setText(note.getDate());
        LBL_title.getEditText().setText(note.getTitle());
        LBL_body.getEditText().setText(note.getBody());
        if (note.getImageId() != null) {
            FireBase.getInstance().downloadStorageData(note.getImageId(), this, IMG_addImage);
        }
    }

    private void openCameraDialog() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d("pttt", "onClick: Permission granted");
            chooseImageDialog();
        } else {
            Log.d("TODO", "openCameraPermissionDialog: ");
        }
    }

    public void chooseImageDialog() {
        Log.d("Pttt", "chooseImage: ");
        final CharSequence[] optionsMenu = {"Take Photo", "Choose from Gallery", "Exit"}; // create a menuOption Array
        // create a dialog for showing the optionsMenu
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // set the items in builder
        builder.setItems(optionsMenu, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (optionsMenu[i].equals("Take Photo")) {
                    // Open the camera and get the photo
                    Intent takePicture = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, TAKE_PHOTO_REQUEST_CODE);
                } else if (optionsMenu[i].equals("Choose from Gallery")) {
                    // choose from  external storage
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, CHOOSE_FROM_GALLERY_REQUEST_CODE);
                } else if (optionsMenu[i].equals("Exit")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public String getDateString() {
        String pattern = "MM/dd/yyyy HH:mm:ss";
        DateFormat df = new SimpleDateFormat(pattern);
        Date today = Calendar.getInstance().getTime();
        String todayAsString = df.format(today);
        return todayAsString;
    }

    private void checkForValidInput() {
        if (LBL_title.getEditText().getText().toString().trim().length() == 0) {
            Log.d("pttt", "checkForValidInput:title can't be null! ");
            LBL_title.getEditText().setError("title can't be null!");
            return;
        }
        if (LBL_body.getEditText().getText().toString().trim().length() == 0) {
            Log.d("pttt", "checkForValidInput: title can't be null!");
            LBL_body.getEditText().setError("body can't be null!");
            return;
        }
        BTN_save.setClickable(false);
        checkImageForNote();


    }

    private void checkImageForNote() {
        Log.d("pttt", "checkImageForNote: ");
        if (note == null || note.getImageId() == null) {
            FireBase.getInstance().uploadImageToCloud(imageBitmap, new OnImageSaveListener() {
                @Override
                public void imageSaved(String imageId) {
                    updateNote(imageId);
                }
            });
        } else
            updateNote(note.getImageId());
    }

    private void updateNote(String imageId) {
        Log.d("pttt", "updateNote: ");
        boolean flag = false;
        if (note == null) {
            note = new Note();
            flag = true;
        }
        note.setDate(LBL_date.getEditText().getText().toString());
        note.setImageId(imageId);
        note.setTitle(LBL_title.getEditText().getText().toString());
        note.setBody(LBL_body.getEditText().getText().toString());
        if (flag) {
            updateLocation();
        }else
            FireBase.getInstance().updateNoteInFireStore(note);
        this.finish();
    }

    private void updateLocation() {
        Log.d("pttt", "updateLocation: ");
        Permission permission = new Permission(this);
        if (!permission.checkForFineLocationPermission() ||!permission.checkForFineLocationPermission()) {
            permission.requestForLocation();
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            Log.d("pttt", "onSuccess: "+location.getLongitude());
                            note.setLat(location.getLatitude());
                            note.setLon(location.getLongitude());
                            FireBase.getInstance().addNoteInFireStore(note);
                        }
                    }
                });
    }

    private void findViews() {
        IMG_addImage =(ImageView)findViewById(R.id.noteActivity_IMG_addImage);
        LBL_title = (TextInputLayout)findViewById(R.id.noteActivity_LBL_title);
        LBL_body = (TextInputLayout)findViewById(R.id.noteActivity_LBL_body);
        LBL_date = (TextInputLayout)findViewById(R.id.noteActivity_LBL_date);
        BTN_save =(Button)findViewById(R.id.noteActivity_BTN_save);
        BTN_delete =(Button)findViewById(R.id.noteActivity_BTN_delete);
    }



}