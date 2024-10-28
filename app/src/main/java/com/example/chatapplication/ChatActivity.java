package com.example.chatapplication;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.Button;
import java.util.List;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import com.google.firebase.database.ChildEventListener;
import android.view.View;
import android.util.Log;


public class ChatActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference messagesRef;
    private ListView chatListView;
    private EditText messageField;
    private Button sendButton;
    private List<String> messagesList = new ArrayList<>();
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




        database = FirebaseDatabase.getInstance();
        messagesRef = database.getReference("messages");
        chatListView = findViewById(R.id.chat_list);
        messageField = findViewById(R.id.message_field);
        sendButton = findViewById(R.id.send_button);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messagesList);
        chatListView.setAdapter(adapter);

        // Listen for new messages
        messagesRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {
                String newMessage = dataSnapshot.getValue(String.class);
                messagesList.add(newMessage);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {

            }
            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) { }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("ChatActivity", "loadPost:onCancelled", databaseError.toException());
            }
        });


        // Send button logic
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = messageField.getText().toString();
                messagesRef.push().setValue(message);
                messageField.setText("");
            }
        });
    }
}

