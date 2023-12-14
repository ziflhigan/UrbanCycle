package com.example.urbancycle.Profile;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.urbancycle.R;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// PreferenceFragment.java
// PreferenceFragment.java
public class PreferenceFragment extends Fragment {

    private int[] avatarList = { R.drawable.custom_icon, R.drawable.avatar2, R.drawable.avatar3 };
    private AvatarAdapter avatarAdapter;
    private String selectedImagePath; // store the path

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preference, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.avatarRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        avatarAdapter = new AvatarAdapter(avatarList);
        recyclerView.setAdapter(avatarAdapter);
        Button btnPickImage = view.findViewById(R.id.btnPickImage);
        btnPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri selectedImageUri = data.getData();
                        if (selectedImageUri != null) {
                            selectedImagePath = getPathFromUri(selectedImageUri);
                            avatarAdapter.updateAvatar(selectedImagePath);
                        }
                    }
                }
            }
    );

    private String getPathFromUri(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = requireContext().getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String imagePath = cursor.getString(column_index);
            cursor.close();
            return imagePath;
        }
        return uri.getPath(); // 如果获取不到，返回 URI 的路径
    }

}

// AvatarAdapter.java
 class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {

    private int[] avatarList;
    private String selectedImagePath; // 存储选定图像的路径

    public AvatarAdapter(int[] avatarList) {
        this.avatarList = avatarList;
    }

    public void updateAvatar(String imagePath) {
        selectedImagePath = imagePath;
        notifyDataSetChanged(); // 刷新 RecyclerView
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.avatar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (selectedImagePath != null && position == 0) {
            // 如果有选定图像，使用选定图像
            holder.avatarImageView.setImageURI(Uri.parse(selectedImagePath));
        } else {

            holder.avatarImageView.setImageResource(avatarList[position]);
        }
    }

    @Override
    public int getItemCount() {
        return avatarList.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
        }
    }
}
