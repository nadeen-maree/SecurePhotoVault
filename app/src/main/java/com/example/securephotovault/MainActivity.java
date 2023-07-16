package com.example.securephotovault;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PICK_IMAGE_REQUEST = 2;

    private GridLayout gridLayout;
    private List<Uri> imageList;
    private List<CheckBox> checkBoxes;
    private Button addButton;
    private Button selectButton;
    private Button deleteButton;

    private Set<Uri> deletedImages;
    private boolean isSelectionMode = false;

    private SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        gridLayout = findViewById(R.id.gridLayout);
        addButton = findViewById(R.id.addButton);
        selectButton = findViewById(R.id.selectButton);
        deleteButton = findViewById(R.id.deleteButton);

        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);


        imageList = new ArrayList<>();
        checkBoxes = new ArrayList<>();
        deletedImages = new HashSet<>();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionAndOpenImagePicker();
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleSelectionMode();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteSelectedImages();
            }
        });

        // Load saved images from external storage
        loadSavedImages();
    }

    private void checkPermissionAndOpenImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imageList.add(selectedImageUri);
                Log.d("Tag", selectedImageUri.toString());
                addImageToGridLayout(selectedImageUri);
                saveEncryptedImageToExternalStorage(selectedImageUri);
            }
        }
    }

    private void addImageToGridLayout(Uri imageUri) {
        View itemView = getLayoutInflater().inflate(R.layout.image_item, null);
        ImageView imageView = itemView.findViewById(R.id.imageView);
        CheckBox checkBox = itemView.findViewById(R.id.checkBox);

        imageView.setImageURI(imageUri);
        Log.d("Tag", imageUri.toString());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(8, 8, 8, 8);

        checkBoxes.add(checkBox);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = getResources().getDisplayMetrics().widthPixels / 3;
        params.height = getResources().getDisplayMetrics().widthPixels / 3;
        params.setMargins(8, 8, 8, 8);

        itemView.setLayoutParams(params);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = gridLayout.indexOfChild(itemView);
                Uri imageUri = imageList.get(position);
                boolean isEncrypted = imageUri.toString().endsWith(".enc");

                if (isEncrypted) {
                    imageUri = decryptImage(imageUri) ;
                    viewImageFullScreen(imageUri);

                } else {
                    viewImageFullScreen(imageUri);
                }
            }
        });

        gridLayout.addView(itemView);
    }

    private void viewImageFullScreen(Uri imageUri) {
        Intent intent = new Intent(MainActivity.this, FullScreenImageActivity.class);
        intent.putExtra("imageUri", imageUri.toString());
        Log.d("Tag", imageUri.toString());
        startActivity(intent);
    }

    private void toggleSelectionMode() {
        isSelectionMode = !isSelectionMode;

        if (isSelectionMode) {
            selectButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.VISIBLE);

            for (CheckBox checkBox : checkBoxes) {
                checkBox.setVisibility(View.VISIBLE);
            }
        } else {
            selectButton.setVisibility(View.VISIBLE);
            deleteButton.setVisibility(View.GONE);

            for (CheckBox checkBox : checkBoxes) {
                checkBox.setVisibility(View.GONE);
                checkBox.setChecked(false);
            }
        }
    }

    private void deleteSelectedImages() {
        List<Uri> selectedImages = new ArrayList<>();

        for (int i = 0; i < checkBoxes.size(); i++) {
            CheckBox checkBox = checkBoxes.get(i);
            if (checkBox.isChecked()) {
                Uri imageUri = imageList.get(i);
                selectedImages.add(imageUri);
                deleteImageFromExternalStorage(imageUri); // Delete the image from external storage
                gridLayout.removeViewAt(i);
                checkBoxes.remove(i);
                imageList.remove(i);
                i--;
                deletedImages.add(imageUri);
            }
        }
        toggleSelectionMode();
    }

    private void deleteImageFromExternalStorage(Uri imageUri) {
        File file = new File(imageUri.getPath());
        if (file.exists()) {
            if (file.delete()) {
                Toast.makeText(this, "Image deleted from external storage", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to delete image from external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveEncryptedImageToExternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] photoData = readBytes(inputStream);
            inputStream.close();

            String password = sharedPreferences.getString("pin_code",""); // Replace with your own encryption password
            byte[] encryptedData = encrypt(photoData, password);

            File externalStorageDir = getExternalFilesDir(null);
            String fileName = "image_" + System.currentTimeMillis() + ".enc";
            File destinationFile = new File(externalStorageDir, fileName);
            FileOutputStream outputStream = new FileOutputStream(destinationFile);
            outputStream.write(encryptedData);
            outputStream.close();

            Toast.makeText(this, "Image saved to external storage", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] readBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private byte[] encrypt(byte[] data, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(password.getBytes("UTF-8"));
            SecretKeySpec key = new SecretKeySpec(digest.digest(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadSavedImages() {
        File externalStorageDir = getExternalFilesDir(null);
        if (externalStorageDir != null) {
            File[] files = externalStorageDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".enc")) {
                        Uri imageUri = Uri.fromFile(file);
                        if (!deletedImages.contains(imageUri)) {
                            imageList.add(imageUri);
                            addImageToGridLayout(decryptImage(imageUri));
                        }
                    }
                }
            }
        }
    }

    private Uri decryptImage(Uri encryptedImageUri) {
        try {
            FileInputStream inputStream = new FileInputStream(encryptedImageUri.getPath());
            byte[] encryptedData = readBytes(inputStream);
            inputStream.close();

            String password = sharedPreferences.getString("pin_code",""); // Replace with your own encryption password
            byte[] decryptedData = decrypt(encryptedData, password);

            String originalFileName = new File(encryptedImageUri.getPath()).getName();
            String decryptedFileName = "decrypted_" + originalFileName + "_" + System.currentTimeMillis() + ".jpg";

            File decryptedFile = new File(getCacheDir(), decryptedFileName);
            FileOutputStream outputStream = new FileOutputStream(decryptedFile);
            outputStream.write(decryptedData);
            outputStream.close();

            return Uri.fromFile(decryptedFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private byte[] decrypt(byte[] data, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(password.getBytes("UTF-8"));
            SecretKeySpec key = new SecretKeySpec(digest.digest(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(data);
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
