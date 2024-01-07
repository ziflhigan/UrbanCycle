package com.example.urbancycle.Profile;

// AvatarAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.urbancycle.R;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.ViewHolder> {

    private int[] avatarList;
    private String selectedImagePath;

    public AvatarAdapter(int[] avatarList) {
        this.avatarList = avatarList;
    }

    public void updateAvatar(String imagePath) {
        selectedImagePath = imagePath;
        notifyDataSetChanged();
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
            Glide.with(holder.itemView.getContext()).load(selectedImagePath).into(holder.avatarImageView);
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
