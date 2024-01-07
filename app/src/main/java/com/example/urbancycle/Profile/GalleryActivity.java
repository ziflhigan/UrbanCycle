package com.example.urbancycle.Profile;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Size;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.urbancycle.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    private final int MY_PERMISSIONS_REQUEST_READ_MEDIA_IMAGES = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_READ_MEDIA_IMAGES);
                return;
            }
        }

        List<Image> imageList = getPhotos();
        ImageView IVThumb1 = findViewById(R.id.IVThumb1);
        ImageView IVThumb2 = findViewById(R.id.IVThumb2);
        ImageView IVThumb3 = findViewById(R.id.IVThumb3);

        if (imageList.size() >= 3) {
            loadImageThumbnail(imageList.get(0).uri, IVThumb1);
            loadImageThumbnail(imageList.get(1).uri, IVThumb2);
            loadImageThumbnail(imageList.get(2).uri, IVThumb3);
        }

        IVThumb1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(imageList.get(0).uri);
            }
        });

        IVThumb2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(imageList.get(1).uri);
            }
        });

        IVThumb3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleImageClick(imageList.get(2).uri);
            }
        });
    }

    private void handleImageClick(Uri selectedImageUri) {
        Intent intent = new Intent();
        intent.putExtra("selectedImageUri", selectedImageUri.toString());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void loadImageThumbnail(Uri imageUri, ImageView imageView) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            try {
                Bitmap thumbnail = getApplicationContext().getContentResolver().loadThumbnail(
                        imageUri, new Size(640, 480), null);
                imageView.setImageBitmap(thumbnail);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    class Image {
        final Uri uri;
        private final String name;
        private final String date_taken;

        public Image(Uri uri, String name, String date_taken) {
            this.uri = uri;
            this.name = name;
            this.date_taken = date_taken;
        }
    }

    protected List<Image> getPhotos() {
        List<Image> ImageList = new ArrayList<Image>();
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN
        };
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " ASC";

        try (Cursor cursor = getApplicationContext().getContentResolver().query(
                collection,
                projection,
                "",
                null,
                sortOrder)) {

            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int datetakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                String dateadded = cursor.getString(datetakenColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                // Stores column values and the contentUri in a local object
                // that represents the media file.
                ImageList.add(new Image(contentUri, name, dateadded));
            }
        }
        return ImageList;
    }
}