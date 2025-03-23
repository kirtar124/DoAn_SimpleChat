package com.example.doanchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {
    private List<User> friends;
    private OnFriendClickListener listener; // Listener để xử lý sự kiện click

    // Interface để xử lý sự kiện click vào một người bạn
    public interface OnFriendClickListener {
        void onFriendClick(User user);
    }

    // Constructor nhận danh sách bạn bè và listener
    public FriendAdapter(List<User> friends, OnFriendClickListener listener) {
        this.friends = friends;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout item_friend.xml (bạn cần tạo layout này)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_friend, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User friend = friends.get(position);
        holder.tvFriendName.setText(friend.getUsername());

        // Xử lý sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) { // Kiểm tra null listener
                listener.onFriendClick(friend);
            }
        });
    }

    @Override
    public int getItemCount() {
        return friends == null ? 0 : friends.size(); // Trả về 0 nếu danh sách null
    }

    // Phương thức để cập nhật danh sách bạn bè
    public void setFriends(List<User> newFriends) {
        this.friends = newFriends;
        notifyDataSetChanged(); // Thông báo cho RecyclerView biết dữ liệu đã thay đổi
    }

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFriendName; // TextView để hiển thị tên bạn bè
        // Bạn có thể thêm các View khác ở đây (ví dụ: ImageView cho avatar, Button để nhắn tin, ...)

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFriendName = itemView.findViewById(R.id.tvFriendName); // Ánh xạ TextView từ layout
            // Ánh xạ các View khác nếu có
        }
    }
}