package com.mycompany.calc;

import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatMessage> messageList;

    public ChatAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage chat = messageList.get(position);
        holder.textMessage.setText(chat.getMessage());

        if (chat.getMediaUrl() != null && !chat.getMediaUrl().isEmpty()) {
            holder.textMedia.setVisibility(View.VISIBLE);
            holder.textMedia.setText("Attachment: " + chat.getMediaUrl());
        } else {
            holder.textMedia.setVisibility(View.GONE);
        }

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.bubbleLayout.getLayoutParams();

        if (chat.isSentByUser()) {
            params.gravity = Gravity.END;
            holder.bubbleLayout.setBackgroundColor(Color.parseColor("#EEFFDE"));
            holder.textSender.setVisibility(View.GONE);
        } else {
            params.gravity = Gravity.START;
            holder.bubbleLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            holder.textSender.setVisibility(View.VISIBLE);
            holder.textSender.setText(chat.getSender());
        }
        holder.bubbleLayout.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView textMessage, textSender, textMedia;
        LinearLayout bubbleLayout;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.textMessage);
            textSender = itemView.findViewById(R.id.textSender);
            textMedia = itemView.findViewById(R.id.textMedia);
            bubbleLayout = itemView.findViewById(R.id.bubbleLayout);
        }
    }
}
