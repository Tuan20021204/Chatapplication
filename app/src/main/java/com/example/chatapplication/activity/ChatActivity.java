package com.example.chatapplication.activity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapplication.R;
import com.example.chatapplication.adapter.MessageAdapter;
import com.example.chatapplication.model.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private ImageView recipientProfileImage;
    private TextView recipientName;
    private EditText editTextMessage;
    private Button buttonSend;

    private DatabaseReference chatRef;
    private String currentUserId;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        recipientProfileImage = findViewById(R.id.recipientProfileImage);
        recipientName = findViewById(R.id.recipientName);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);


        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        messageList = new ArrayList<>();


        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        messageAdapter = new MessageAdapter(this, messageList, currentUserId);
        recyclerViewMessages.setAdapter(messageAdapter);


        String recipientNameText = getIntent().getStringExtra("recipientName");
        String recipientProfileUrl = getIntent().getStringExtra("recipientProfileUrl");
        chatId = getIntent().getStringExtra("chatId");


        recipientName.setText(recipientNameText);
        Glide.with(this).load(recipientProfileUrl).placeholder(R.drawable.profile_placeholder).into(recipientProfileImage);


        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);


        loadMessages();


        buttonSend.setOnClickListener(v -> sendMessage());
    }


    private void sendMessage() {
        String messageText = editTextMessage.getText().toString().trim();
        if (!messageText.isEmpty()) {

            String messageId = chatRef.push().getKey();


            Message message = new Message(messageText, currentUserId, System.currentTimeMillis());


            chatRef.child(messageId).setValue(message);
            editTextMessage.setText("");
        }
    }


    private void loadMessages() {
        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Message message = dataSnapshot.getValue(Message.class);
                messageList.add(message);
                messageAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
