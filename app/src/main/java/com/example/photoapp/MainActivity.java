package com.example.photoapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    Button btnCam, btnGallery, btnYourPhoto;
    Uri mUri;
    String selectedImagePath = null;

    private static final int GALLERY_REQUEST = 1;
    private static final int PHOTO_INTENT_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCam = (Button)findViewById(R.id.btnCam);
        btnGallery = (Button)findViewById(R.id.btnGallery);
        btnYourPhoto = (Button)findViewById(R.id.btnYourPhoto);

        Permissions permissions = new Permissions(this);
        permissions.permissionCheck();

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    public void buttonClick(View view) {

        switch (view.getId()) {

            case R.id.btnCam:
                openCamera();
                break;

            case R.id.btnGallery:
                openGallery();
                break;

            case R.id.btnYourPhoto:
                openYourPhoto();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Permissions ResultPermission = new Permissions(this);
        ResultPermission.onRequestPermResult(requestCode, permissions, grantResults);
    }

    private void openYourPhoto(){
        Uri fileUri = (Uri) Uri.fromFile(new File(String.valueOf(new File(Environment.getExternalStorageDirectory(), "PhotoApp"))));

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void openGallery(){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GALLERY_REQUEST);
    }

    private void openCamera(){

        mUri = generateFileUri();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        startActivityForResult(intent, PHOTO_INTENT_REQUEST_CODE);
    }

    private Uri generateFileUri(){
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
            return null;

        File path = new File (Environment.getExternalStorageDirectory(), "PhotoApp");
        if (! path.exists()){
            if (! path.mkdirs()){
                return null;
            }
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        File newFile = new File(path.getPath() + File.separator + timeStamp + ".jpg");
        return Uri.fromFile(newFile);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        RealPathFromURI realPathFromURI = new RealPathFromURI(MainActivity.this);

        Log.d("myLog", "resultCode " + resultCode);
        Log.d("myLog", "imageReturnedIntent " + imageReturnedIntent);


        if (resultCode != 0) {

            if (requestCode == 100) {
                selectedImagePath = realPathFromURI.getRealPathFromURI(mUri);
            } else if (requestCode == 1) {
                Uri selectedImageUri = imageReturnedIntent.getData();
                selectedImagePath = realPathFromURI.getRealPathFromURI(selectedImageUri);
            }

            Log.d("myLog", "Путь файла " + selectedImagePath.toString());

            Intent intent = new Intent(this, PhotoActivity.class);
            intent.putExtra("selectedImagePath", selectedImagePath);
            this.startActivity(intent);
        }

        else {
            if (requestCode == 100)
                Toast.makeText(this, "Повторите выбор камеры", Toast.LENGTH_SHORT).show();
            else if(requestCode == 1)
                Toast.makeText(this, "Повторите выбор файла", Toast.LENGTH_SHORT).show();
        }
    }
}
