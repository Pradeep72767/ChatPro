package com.example.chatpro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiveID, messageReceiveName, messageReceiveImage, messageSenderID;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;

    private Toolbar ChatToolbar;
    private ImageButton sendMessage;
    private EditText messageInputText;

    private List<Message> messageList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessageList;

    ChildEventListener showMessageChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiveID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiveName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiveImage = getIntent().getExtras().get("visit_user_image").toString();

        InitilizeControllers();

        userName.setText(messageReceiveName);
        Picasso.get().load(messageReceiveImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendMessage();
            }
        });

    }



    private void InitilizeControllers()
    {

        ChatToolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);


        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_profile_last_seen);
        userImage = findViewById(R.id.custom_profile_image);

        sendMessage = findViewById(R.id.send_message_button);
        messageInputText =findViewById(R.id.input_mesage);

        messageAdapter = new MessageAdapter(messageList);
        userMessageList = findViewById(R.id.private_chat_message_list);
        linearLayoutManager = new LinearLayoutManager(this);

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        showMessageChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                Message message = dataSnapshot.getValue(Message.class);

                messageList.add(message);

                messageAdapter.notifyDataSetChanged();

                userMessageList.smoothScrollToPosition(userMessageList.getAdapter().getItemCount());

               // messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        };
        RootRef.child("Messages").child(messageSenderID).child(messageReceiveID)
                .addChildEventListener(showMessageChildEventListener);


        userMessageList.setLayoutManager(linearLayoutManager);
        userMessageList.setAdapter(messageAdapter);
    }

    private void SendMessage()
    {
        String messageText = messageInputText.getText().toString();

        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this,"Please Write message...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiveID;
            String messageReceiverRef = "Messages/" + messageReceiveID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                    .child(messageSenderID).child(messageReceiveID).push();

            String messagePusID  = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();

            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);


            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(messageSenderRef + "/" + messagePusID, messageTextBody);
            messageBodyDetail.put(messageReceiverRef + "/" + messagePusID, messageTextBody);

            RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {

                    messageInputText.setText("");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            RootRef.child("Messages").child(messageSenderID).child(messageReceiveID)
                    .removeEventListener(showMessageChildEventListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}