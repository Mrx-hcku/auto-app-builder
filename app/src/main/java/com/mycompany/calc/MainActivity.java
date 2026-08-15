package com.mycompany.calc;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private EditText inputMessage;
    private Button btnSend;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList = new ArrayList<>();
    private String username, chatId = "global_room";
    private ChatDao chatDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- Session Guard Check ---
        SharedPreferences prefs = getSharedPreferences("MessgramPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);
        
        if (!isLoggedIn) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        username = prefs.getString("username", "Guest");

        inputMessage = findViewById(R.id.inputMessage);
        btnSend = findViewById(R.id.btnSend);
        recyclerView = findViewById(R.id.recyclerViewChat);

        chatDao = AppDatabase.getInstance(this).chatDao();

        // Load offline cached messages from Room DB first
        chatList.addAll(chatDao.getMessagesForRoom(chatId));
        chatAdapter = new ChatAdapter(chatList, username);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);
        if (!chatList.isEmpty()) {
            recyclerView.scrollToPosition(chatList.size() - 1);
        }

        // Connect Socket.io to Render Backend Server
        try {
            socket = IO.socket("https://messgram-k4vn.onrender.com");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.connect();
        socket.emit("join_room", chatId);

        // Listen for live incoming messages
        socket.on("receive_message", args -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String sender = data.getString("sender");
                String message = data.getString("message");
                String mediaUrl = data.optString("mediaUrl", "");
                long timestamp = data.optLong("timestamp", System.currentTimeMillis());

                ChatMessage chatMessage = new ChatMessage(chatId, sender, message, mediaUrl, timestamp);
                
                // Save to local Room Database
                chatDao.insertMessage(chatMessage);

                runOnUiThread(() -> {
                    chatList.add(chatMessage);
                    chatAdapter.notifyItemInserted(chatList.size() - 1);
                    recyclerView.scrollToPosition(chatList.size() - 1);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Send Message Event
        btnSend.setOnClickListener(v -> {
            String msgText = inputMessage.getText().toString().trim();
            if (!msgText.isEmpty()) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("chatId", chatId);
                    jsonObject.put("type", "chat");
                    jsonObject.put("sender", username);
                    jsonObject.put("message", msgText);
                    jsonObject.put("mediaUrl", "");

                    socket.emit("send_message", jsonObject);
                    inputMessage.setText("");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Add Logout option in Toolbar Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // Ensure menu file exists or create programmatically if needed
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout || item.getTitle() != null && item.getTitle().equals("Logout")) {
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutUser() {
        SharedPreferences.Editor editor = getSharedPreferences("MessgramPrefs", MODE_PRIVATE).edit();
        editor.clear();
        editor.apply();

        if (socket != null) {
            socket.disconnect();
        }

        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
        }
    }
                          }
            
