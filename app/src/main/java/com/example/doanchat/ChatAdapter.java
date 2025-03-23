package com.example.doanchat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<Message> messageList;
    private int currentUserId;

    public ChatAdapter(List<Message> messageList, int currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.tvMessageText.setText(message.getMessageText());
        holder.tvTime.setText(message.getSentTime().toString());
        if (message.getSenderId() == currentUserId) {
            holder.tvMessageText.setBackgroundResource(R.drawable.bubble_sent); // Style cho tin nhắn gửi
        } else {
            holder.tvMessageText.setBackgroundResource(R.drawable.bubble_received); // Style cho tin nhắn nhận
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void updateMessages(List<Message> newMessages) {
        this.messageList = newMessages;
        notifyDataSetChanged();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageText, tvTime;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageText = itemView.findViewById(R.id.tvMessageText); // Sửa ID
            tvTime = itemView.findViewById(R.id.tvSentTime); // Sửa ID
        }
    }
}