package com.example.doanchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchFriendAdapter extends RecyclerView.Adapter<SearchFriendAdapter.ViewHolder> {
    private List<User> users;
    private int currentUserId;

    public SearchFriendAdapter(List<User> users, int currentUserId) {
        this.users = users;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.btnAddFriend.setOnClickListener(v -> {
            new connect().addFriend(currentUserId, user.getUserId(), (success, errorMessage) -> {
                if (success) {
                    Toast.makeText(holder.itemView.getContext(), "Đã thêm bạn thành công", Toast.LENGTH_SHORT).show();
                    users.remove(position);
                    notifyItemRemoved(position);
                    if (users.isEmpty()) {
                        holder.itemView.getRootView().findViewById(R.id.tvNoResults).setVisibility(View.VISIBLE);
                        holder.itemView.getRootView().findViewById(R.id.rv_search_results).setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Thêm bạn thất bại: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername;
        Button btnAddFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername); // Sửa ở đây
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend); // Sửa ở đây
        }
    }
}