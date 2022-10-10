package com.mar.project.randomimage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private ImageView image;
    private Button randomButton, shareButton, saveButton;
    private ProgressBar progressBar;
    private FileOutputStream fileOutputStream;
    private Intent intent;

    private final static int CODE = 100;

    //this is the image that you want to put in image view
    Data d01 = new Data("http://stacktoheap.com/images/stackoverflow.png");
    Data d02 = new Data("http://goo.gl/gEgYUd");
    Data d03 = new Data("http://via.placeholder.com/300.png");
    Data d04 = new Data("https://raw.githubusercontent.com/bumptech/glide/master/static/glide_logo.png");

    Data[] arrays = new Data[]{
            d01, d02, d03, d04
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = findViewById(R.id.image);
        randomButton = findViewById(R.id.randomButton);
        shareButton = findViewById(R.id.shareButton);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);

        loadImage();
        /**Glide.with(this)
                .load(arrays[0].getImage())
                .into(image);*/

        randomButton.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            shareButton.setClickable(false);
            shareButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));
            saveButton.setClickable(false);
            saveButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_200)));

            getRandomImage();
            loadImage();
            /**Glide.with(this)
                    .load(arrays[0].getImage())
                    .into(image);*/
        });

        shareButton.setOnClickListener(v -> shareImage());

        saveButton.setOnClickListener(v -> {
            if(ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                saveImage();
            } else {
                //asking permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, CODE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(CODE == requestCode){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImage();
            } else {
                Toast.makeText(MainActivity.this, "You need to accept the permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getRandomImage(){
        Collections.shuffle(Arrays.asList(arrays));
    }

    private void shareImage(){
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        BitmapDrawable dwb = (BitmapDrawable) image.getDrawable();
        Bitmap bmp = dwb.getBitmap();

        File file = new File(getExternalCacheDir() + "/" +"ShareImage" + ".jpg");

        try{
            fileOutputStream = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        } catch (IOException e){
            e.printStackTrace();
        }

        startActivity(Intent.createChooser(intent, "Share Image"));
    }

    private void saveImage(){
        File dir = new File(Environment.getExternalStorageDirectory(), "Download Image");
        if(!dir.exists()){
            dir.mkdir();
        }

        BitmapDrawable dwb = (BitmapDrawable) image.getDrawable();
        Bitmap bmp = dwb.getBitmap();
        File file = new File(dir, System.currentTimeMillis() + ".jpg");

        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e){
            e.printStackTrace();
        }

        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        Toast.makeText(MainActivity.this, "Download success", Toast.LENGTH_SHORT).show();

        try{
            fileOutputStream.flush();
            fileOutputStream.close();

            intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(Uri.fromFile(file));
            sendBroadcast(intent);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private void loadImage(){
        Glide.with(this)
                .load(arrays[0].getImage())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        shareButton.setClickable(true);
                        shareButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));
                        saveButton.setClickable(true);
                        saveButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.purple_700)));
                        return false;
                    }
                })
                .into(image);
    }
}