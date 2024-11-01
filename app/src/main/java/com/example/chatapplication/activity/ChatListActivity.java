package com.example.chatapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chatapplication.R;
import com.example.chatapplication.adapter.ChatAdapter;
import com.example.chatapplication.model.Chat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChatList;
    private ChatAdapter chatAdapter;
    private List<Chat> chatList;
    private DatabaseReference chatListRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        // Khởi tạo RecyclerView cho danh sách cuộc trò chuyện
        recyclerViewChatList = findViewById(R.id.recyclerViewChatList);
        recyclerViewChatList.setLayoutManager(new LinearLayoutManager(this));
        chatList = new ArrayList<>();

        // Lấy ID người dùng hiện tại từ FirebaseAuth
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Khởi tạo Adapter cho RecyclerView với OnChatClickListener
        chatAdapter = new ChatAdapter(this, chatList, chat -> {
            // Mở ChatActivity khi chọn một cuộc trò chuyện
            Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
            intent.putExtra("chatId", chat.getChatId());
            intent.putExtra("recipientName", chat.getChatName());
            intent.putExtra("recipientProfileUrl", chat.getProfileImageUrl());
            startActivity(intent);
        });
        recyclerViewChatList.setAdapter(chatAdapter);

        // Tham chiếu đến danh sách cuộc trò chuyện của người dùng trong Firebase
        chatListRef = FirebaseDatabase.getInstance().getReference("user_chats").child(currentUserId);

        // Lấy danh sách cuộc trò chuyện từ Firebase
        loadChatList();
    }

    private void loadChatList() {
        chatListRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    // Giả sử mỗi `chatSnapshot` chứa thông tin về cuộc trò chuyện
                    Chat chat = chatSnapshot.getValue(Chat.class);
                    chatList.add(chat);
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi
            }
        });
    }
}
