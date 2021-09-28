package com.tejasvi.whatsappintentproject;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.hbb20.CountryCodePicker;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout l;
    private static final int SELECT_PICTURE = 100;
    private static final String TAG = "SelectImageActivity";
    public  Uri selectedImageUri1=null,imageInternalUri =null;
    final Context c = this;
    Button btnSendMessage,button_share_on_other_apps,button_load;
    EditText etMessage, etMobileNumber;
    CountryCodePicker countryCodePicker;
    String strMessage, strMobileNumber = null,myURL="";
    private ProgressDialog mProgressDialog;
    private Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        l=findViewById(R.id.l1);
        button_load = findViewById(R.id.btnload);
        button_share_on_other_apps = findViewById(R.id.button);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        etMessage = findViewById(R.id.etMessage);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        countryCodePicker = findViewById(R.id.countryCodePicker);
        findViewById(R.id.btn_select).setOnClickListener(this);
        myURL=null;
        // Get the application context
        mActivity = MainActivity.this;

        handlePermission();
        // Initialize the progress dialog
        mProgressDialog = new ProgressDialog(mActivity);
        mProgressDialog.setIndeterminate(true);
        // Progress dialog horizontal style
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        // Progress dialog title
        mProgressDialog.setTitle("Loading your image..");
        // Progress dialog message
        mProgressDialog.setMessage("Please wait, we are downloading your image file...");


        // click on load button
    button_load.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(c);
        View mView = layoutInflaterAndroid.inflate(R.layout.dialog, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(c);
        alertDialogBuilderUserInput.setView(mView);

        selectedImageUri1= null;
        final EditText userInputDialogEditText = (EditText) mView.findViewById(R.id.userInputDialog);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("LOAD", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        //  user input
                        String theString = userInputDialogEditText.getText().toString();
                        myURL = theString;
                        Toast.makeText(MainActivity.this, "URL Entered", Toast.LENGTH_SHORT).show();
                        new DownloadImageTask((ImageView) findViewById(R.id.imgView))
                                .execute(myURL);
                    }
                })

                .setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                dialogBox.cancel();
                                Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }
    });

        //share on other apps
        button_share_on_other_apps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                strMessage = etMessage.getText().toString();
                strMobileNumber = etMobileNumber.getText().toString();
                //if user has given only text
                if (imageInternalUri == null && selectedImageUri1 == null) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, strMessage);
                    shareIntent.putExtra(Intent.EXTRA_STREAM, selectedImageUri1);
                    shareIntent.setType("image/*");
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(shareIntent, "Share your messsage..."));
                    Toast.makeText(MainActivity.this, "message has been sent successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (selectedImageUri1 == null) {
                        File file = new File("/storage/emulated/0/Android/data/com.tejasvi.whatsappintentproject/files/Download/to-share.jpeg");
                        Intent install = new Intent(Intent.ACTION_VIEW);

                        install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
// New Approach
                        Uri apkURI = FileProvider.getUriForFile(
                                c,
                                c.getApplicationContext()
                                        .getPackageName() + ".provider", file);
                        install.setDataAndType(apkURI, "image/jpeg");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        c.startActivity(install);
                        c.startActivity(install);
                    } else {
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, strMessage);
                        shareIntent.putExtra(Intent.EXTRA_STREAM, selectedImageUri1);
                        shareIntent.setType("image/*");
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Share your messsage..."));
                        Toast.makeText(MainActivity.this, "message has been sent successfully", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });


        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                strMessage = etMessage.getText().toString();
                strMobileNumber = etMobileNumber.getText().toString();

                if (etMobileNumber.getText().toString().isEmpty() && etMessage.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Enter Mobile Number and Message you want to send", Toast.LENGTH_SHORT).show();
                }else {

                    countryCodePicker.registerCarrierNumberEditText(etMobileNumber);
                    strMobileNumber = countryCodePicker.getFullNumber();

                    boolean installed = appInstalledOrNot();
                    if (installed){
                        if(myURL==null) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + strMobileNumber
                                    + "&text=" + strMessage ));
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "Rediecting", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=" + strMobileNumber
                                    + "&text=" + strMessage + "\nLink : " + myURL));
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "Rediecting", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        Toast.makeText(MainActivity.this, "WhatsApp not installed on your Device ,u can still share", Toast.LENGTH_SHORT).show();
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT,strMessage);
                        shareIntent.putExtra(Intent.EXTRA_STREAM,selectedImageUri1);
                        shareIntent.setType("image/*");
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(shareIntent, "Share your messsage..."));
                        Toast.makeText(MainActivity.this, "message has been sent successfully", Toast.LENGTH_SHORT).show();

                    }
                }
            }
        });
    }

    private boolean appInstalledOrNot(){
        PackageManager packageManager = getPackageManager();
        boolean appInstalled;

        try {
            packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES);
            appInstalled = true;
        }catch (PackageManager.NameNotFoundException e){
            appInstalled = false;
        }
        return appInstalled;
    }


    private void handlePermission() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SELECT_PICTURE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case SELECT_PICTURE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                        if (showRationale) {
                            Toast.makeText(MainActivity.this, "Access Denied", Toast.LENGTH_SHORT).show();
                            //   message here Access Denied
                            finish();
                        } else {}
                    }else{
                        Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
                        //   message here Permission Granted
                    }
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /* Choose an image from Gallery */
    void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (resultCode == RESULT_OK) {
                    if (requestCode == SELECT_PICTURE) {
                        // Get the url from data
                        final Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            // Get the path from the Uri
                            String path = getPathFromURI(selectedImageUri);
                            Log.i(TAG, "Image Path : " + path);
                            selectedImageUri1=selectedImageUri;
                            // Set the image in ImageView
                            findViewById(R.id.imgView).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ImageView) findViewById(R.id.imgView)).setImageURI(selectedImageUri);
                                }
                            });

                        }
                    }
                }
            }
        }).start();

    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    @Override
    public void onClick(View v) {
        openImageChooser();
    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        // Before the tasks execution
        protected void onPreExecute() {
            // Display the progress dialog when loading starts
            mProgressDialog.show();
        }

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        Bitmap mIcon11 = null;

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];

            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            mProgressDialog.dismiss();
            if (result == null) {
                Toast.makeText(MainActivity.this, "Can't be Loaded", Toast.LENGTH_SHORT).show();
                Toast.makeText(MainActivity.this, "Please enter valid URL", Toast.LENGTH_SHORT).show();
            } else {
                // Save bitmap to internal storage
                imageInternalUri = saveImageExternal(result);
                // Set the ImageView image from internal storage
                bmImage.setImageURI(imageInternalUri);
            }
        }


          //Saves the image as PNG to the app's private external storage folder.

        private Uri saveImageExternal(Bitmap image) {

            Uri uri = null;
            try {
                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "to-share.jpeg");
                FileOutputStream stream = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                stream.close();
                uri = Uri.fromFile(file);
            } catch (IOException e) {
                Log.d(TAG, "IOException while trying to write file for sharing: " + e.getMessage());
            }
            return uri;
        }
    }

}

