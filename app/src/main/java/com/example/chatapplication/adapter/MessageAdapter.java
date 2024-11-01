package com.example.chatapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.model.Message;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private Context context;
    private List<Message> messageList;
    private String currentUserId; // ID của người dùng hiện tại

    // Constructor
    public MessageAdapter(Context context, List<Message> messageList, String currentUserId) {
        this.context = context;
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        // Hiển thị tin nhắn ở bên phải nếu người gửi là người dùng hiện tại
        if (message.getSenderId().equals(currentUserId)) {
            holder.textMessageRight.setText(message.getMessageText());
            holder.textMessageRight.setVisibility(View.VISIBLE);
            holder.textMessageLeft.setVisibility(View.GONE);
        } else {
            // Hiển thị tin nhắn ở bên trái nếu là tin nhắn của người khác
            holder.textMessageLeft.setText(message.getMessageText());
            holder.textMessageLeft.setVisibility(View.VISIBLE);
            holder.textMessageRight.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textMessageRight, textMessageLeft;

        MessageViewHolder(View itemView) {
            super(itemView);
            textMessageRight = itemView.findViewById(R.id.text_message_right);
            textMessageLeft = itemView.findViewById(R.id.text_message_left);
        }
    }
}
