package com.example.saveimage;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    String encodeImage;
    private EditText textFilename;
    private Button btnBrowse, btnCamera, btnUpload;
    private ImageView imageUpload;
    private Bitmap bitmap, bitmapCash;
    private String URL ="http://192.168.2.67/LoginRegister/saveimage.php";
    private final int GALLERY_REQ_CODE = 1000;
    private final int CAMERA_REQ_CODE = 100;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK) {
            if (requestCode==GALLERY_REQ_CODE) {
                Uri filepath = data.getData();
                InputStream inputStream = null;
                try {
                    inputStream = getContentResolver().openInputStream(filepath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                bitmapCash = BitmapFactory.decodeStream(inputStream);
                try {
                    saveBitmapToCache(bitmapCash);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                imageUpload.setImageBitmap(getBitmapFromCache());
            }

            if (requestCode==CAMERA_REQ_CODE) {
                bitmapCash = (Bitmap)(data.getExtras().get("data"));
                imageUpload.setImageBitmap(bitmap);
            }
        }
    }

    public void saveBitmapToCache(Bitmap bitmap) throws IOException {
        String filename = "final_image.jpg";
        File cacheFile = new File(getApplicationContext().getCacheDir(), filename);
        OutputStream out = new FileOutputStream(cacheFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, (int)100, out);
        out.flush();
        out.close();
    }

    public Bitmap getBitmapFromCache(){
        File cacheFile = new File(getApplicationContext().getCacheDir(), "final_image.jpg");
        Bitmap myBitmap = BitmapFactory.decodeFile(cacheFile.getAbsolutePath());
        return myBitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnCamera = findViewById(R.id.buttonCam);
        btnUpload = findViewById(R.id.buttonUpload);
        imageUpload = findViewById(R.id.img);
        btnBrowse = findViewById(R.id.buttonBrowse);
        textFilename = findViewById(R.id.editTextFilename);

            btnBrowse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dexter.withActivity(MainActivity.this)
                            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                            .withListener(new PermissionListener() {
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    Intent intentGallery = new Intent(Intent.ACTION_PICK);
                                    intentGallery.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(intentGallery, GALLERY_REQ_CODE);
                                }
                                public void onPermissionDenied(PermissionDeniedResponse response) {

                                }
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                    }).check();
                }
            });

            btnUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    if (bitmap !=null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                        byte[] bytes = byteArrayOutputStream.toByteArray();
                        String encodeImage = android.util.Base64.encodeToString(bytes, Base64.DEFAULT);
                        StringRequest request2 = new StringRequest(Request.Method.POST, URL,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response2) {
                                        if (response2.equals("success")) {
                                            imageUpload.setImageResource(R.drawable.browse_image);
                                            textFilename.setText("");
                                            Toast.makeText(MainActivity.this, "success", Toast.LENGTH_LONG).show();
                                        } else if (response2.equals("failed to insert to Database")) {
                                            Toast.makeText(MainActivity.this, "failed to insert to Database", Toast.LENGTH_LONG).show();
                                        } else if (response2.equals("failed to upload image")) {
                                            Toast.makeText(MainActivity.this, "failed to upload image", Toast.LENGTH_LONG).show();
                                        } else if (response2.equals("no image found")) {
                                            Toast.makeText(MainActivity.this, "no image found", Toast.LENGTH_LONG).show();
                                        } else if (response2.equals("Database connetion failed")) {
                                            Toast.makeText(MainActivity.this, "Database connetion failed", Toast.LENGTH_LONG).show();
                                        } else if (response2.equals("failure")) {
                                            Toast.makeText(MainActivity.this, "failure", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error2) {
                                Toast.makeText(getApplicationContext(), error2.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }) {
                            protected Map<String, String> getParams() throws AuthFailureError {
                                String name1 = textFilename.getText().toString().trim();
                                Map<String, String> map = new HashMap<>();
                                map.put("name", name1);
                                map.put("upload", encodeImage);
                                return map;
                            }
                        };
                        RequestQueue queue2 = Volley.newRequestQueue(getApplicationContext());
                        queue2.add(request2);
                    } else Toast.makeText(getApplicationContext(), "оберіть зображення", Toast.LENGTH_LONG).show();
                }
            });

            imageUpload.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, CropActivity.class);
                    startActivity(intent);
                }
            });

            btnCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Dexter.withActivity(MainActivity.this)
                            .withPermission(Manifest.permission.CAMERA)
                            .withListener(new PermissionListener() {
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    startActivityForResult(intentCamera, CAMERA_REQ_CODE);
                                }
                                public void onPermissionDenied(PermissionDeniedResponse response) {

                                }
                                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }
                            }).check();
                }
            });
    }
}
