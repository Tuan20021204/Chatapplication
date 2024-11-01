package com.example.chatapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.adapter.ChatAdapter;
import com.example.chatapplication.model.Chat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerViewChatList;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerViewChatList = findViewById(R.id.recyclerViewChatList);
        recyclerViewChatList.setLayoutManager(new LinearLayoutManager(this));

        chatList = new ArrayList<>();

        // Thêm OnChatClickListener vào constructor của ChatAdapter
        chatAdapter = new ChatAdapter(this, chatList, chat -> {
            // Khi người dùng nhấn vào một cuộc trò chuyện, mở ChatActivity
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("recipientName", chat.getChatName());
            intent.putExtra("recipientProfileUrl", chat.getProfileImageUrl());
            startActivity(intent);
        });

        recyclerViewChatList.setAdapter(chatAdapter);
    }
}
