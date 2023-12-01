package com.example.urbancycle.Profile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.urbancycle.R;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class PreferenceFragment extends androidx.preference.PreferenceFragmentCompat {

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        // Find the preference item by its key
        androidx.preference.Preference imagePreference = findPreference("key_image_preference");

        // Set the click listener for the image preference
        if (imagePreference != null) {
            imagePreference.setOnPreferenceClickListener(new androidx.preference.Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(androidx.preference.Preference preference) {
                    // Launch image picker when the preference is clicked
                    openImagePicker();
                    return true;
                }
            });
        }
    }

    private void openImagePicker() {
        // Create an intent to open the image picker
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            // Handle the selected image URI
            Uri selectedImageUri = data.getData();
            // Do something with the selected image URI, such as displaying it or saving it.
        }
    }
}
