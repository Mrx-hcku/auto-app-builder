package com.mycompany.calc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private EditText etMessage;
    private Button btnSend,btnLogout;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    private MessageDao messageDao;
    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Components Initialization
        tvWelcome = findViewById(R.id.tvWelcome);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnLogout = findViewById(R.id.btnLogout);
        recyclerView = findViewById(R.id.recyclerView);

        // Room Database Initialization using MessageDao
        appDatabase = AppDatabase.getInstance(this);
        messageDao = appDatabase.messageDao();

        // SharedPreferences se logged-in user ka naam fetch karna
        SharedPreferences prefs = getSharedPreferences("MessgramPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "User");
        tvWelcome.setText("Welcome, " + username + "!");

        // RecyclerView Setup
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Load offline messages from Room Database
        loadLocalMessages();

        // Send Button Click Listener
        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                ChatMessage chatMessage = new ChatMessage(username, messageText, "Just now");
                messageList.add(chatMessage);
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);

                // Save to Room Database as MessageEntity
                saveMessageLocally(username, messageText);

                etMessage.setText("");
            } else {
                Toast.makeText(MainActivity.this, "Message cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        // Logout Button Click Listener
        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Redirect back to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void loadLocalMessages() {
        new Thread(() -> {
            List<MessageEntity> savedEntities = messageDao.getAllMessages();
            runOnUiThread(() -> {
                for (MessageEntity entity : savedEntities) {
                    messageList.add(new ChatMessage(entity.sender, entity.message, "Saved"));
                }
                chatAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void saveMessageLocally(String sender, String message) {
        new Thread(() -> {
            MessageEntity entity = new MessageEntity();
            entity.sender = sender;
            entity.message = message;
            messageDao.insertMessage(entity);
        }).start();
    }
}
