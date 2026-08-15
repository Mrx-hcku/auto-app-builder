package com.mycompany.calc;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import io.socket.client.IO;
import io.socket.client.Socket;

public class MainActivity extends AppCompatActivity {
    private Socket socket;
    private EditText messageInput;
    private RecyclerView recyclerViewChat;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList = new ArrayList<>();
    private String currentUsername = "GuestUser";
    private AppDatabase db;
    private final String CURRENT_ROOM = "global_room";
    private final String SERVER_URL = "https://messgram-k4vn.onrender.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("MessgramPrefs", MODE_PRIVATE);
        currentUsername = prefs.getString("username", "GuestUser");

        db = AppDatabase.getInstance(this);

        messageInput = findViewById(R.id.messageInput);
        Button sendButton = findViewById(R.id.sendButton);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewChat.setLayoutManager(layoutManager);

        chatAdapter = new ChatAdapter(messageList);
        recyclerViewChat.setAdapter(chatAdapter);

        // Load cached offline messages instantly
        loadLocalCachedMessages();

        try {
            socket = IO.socket(SERVER_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.connect();
        socket.emit("join_room", CURRENT_ROOM);

        socket.on("load_history", args -> {
            try {
                org.json.JSONArray history = (org.json.JSONArray) args[0];
                List<MessageEntity> entities = new ArrayList<>();
                
                runOnUiThread(() -> messageList.clear());

                for (int i = 0; i < history.length(); i++) {
                    JSONObject obj = history.getJSONObject(i);
                    String msgId = obj.optString("_id", UUID.randomUUID().toString());
                    String sender = obj.getString("sender");
                    String msg = obj.optString("message", "");
                    String mediaUrl = obj.optString("mediaUrl", "");
                    long timestamp = obj.optLong("timestamp", System.currentTimeMillis());

                    entities.add(new MessageEntity(msgId, CURRENT_ROOM, sender, msg, mediaUrl, "", false, timestamp));

                    boolean isMine = sender.equals(currentUsername);
                    runOnUiThread(() -> messageList.add(new ChatMessage(sender, msg, mediaUrl, isMine)));
                }

                db.messageDao().insertAll(entities);
                runOnUiThread(() -> {
                    chatAdapter.notifyDataSetChanged();
                    if (!messageList.isEmpty()) {
                        recyclerViewChat.scrollToPosition(messageList.size() - 1);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        socket.on("receive_message", args -> {
            JSONObject data = (JSONObject) args[0];
            try {
                String msgId = data.optString("_id", UUID.randomUUID().toString());
                String sender = data.getString("sender");
                String msg = data.optString("message", "");
                String mediaUrl = data.optString("mediaUrl", "");
                long timestamp = System.currentTimeMillis();

                db.messageDao().insertMessage(new MessageEntity(msgId, CURRENT_ROOM, sender, msg, mediaUrl, "", false, timestamp));

                boolean isMine = sender.equals(currentUsername);
                runOnUiThread(() -> {
                    messageList.add(new ChatMessage(sender, msg, mediaUrl, isMine));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerViewChat.scrollToPosition(messageList.size() - 1);
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        sendButton.setOnClickListener(v -> {
            String msg = messageInput.getText().toString().trim();
            if (!msg.isEmpty() && socket != null) {
                try {
                    JSONObject data = new JSONObject();
                    data.put("chatId", CURRENT_ROOM);
                    data.put("type", "chat");
                    data.put("sender", currentUsername);
                    data.put("message", msg);
                    data.put("mediaUrl", "");
                    socket.emit("send_message", data);
                    messageInput.setText("");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadLocalCachedMessages() {
        List<MessageEntity> cached = db.messageDao().getMessagesForRoom(CURRENT_ROOM);
        if (cached != null && !cached.isEmpty()) {
            messageList.clear();
            for (MessageEntity entity : cached) {
                boolean isMine = entity.getSender().equals(currentUsername);
                messageList.add(new ChatMessage(entity.getSender(), entity.getMessage(), entity.getMediaUrl(), isMine));
            }
            chatAdapter.notifyDataSetChanged();
            recyclerViewChat.scrollToPosition(messageList.size() - 1);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) {
            socket.disconnect();
        }
    }
                     }
    
