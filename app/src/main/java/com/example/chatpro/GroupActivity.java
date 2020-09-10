package com.example.chatpro;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupActivity extends AppCompatActivity {

    private ImageButton sendMessageButton;
    private Toolbar mtoolbar;

    private ScrollView mScrollView;
    private EditText userMessageInput;
    private TextView displayTextMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, GroupNameRef, GroupMessageKeyRef;
    private String currentGroupName, currentUserId, currentUserName, currentDate, currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        currentGroupName = getIntent().getExtras().get("groupName").toString();


        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);



        InitilizeFields();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                saveMessageToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if(dataSnapshot.exists())
                {
                    DisplayMessage(dataSnapshot);
                }

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
        });
    }




    private void InitilizeFields()
    {
        mtoolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupName);

        mScrollView = findViewById(R.id.group_scroll_view);
        userMessageInput = findViewById(R.id.input_group_message);
        sendMessageButton  = findViewById(R.id.send_message_button);
        displayTextMessage = findViewById(R.id.group_chat_text_display);
    }

    private void getUserInfo()
    {
        UsersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void saveMessageToDatabase()
    {

       String message = userMessageInput.getText().toString();
        String messageKey = GroupNameRef.push().getKey();

       if(TextUtils.isEmpty(message))
       {
           Toast.makeText(this,"Please Write message...", Toast.LENGTH_SHORT).show();
       }
       else
       {
           Calendar calForDate = Calendar.getInstance();
           SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM, yyyy");
           currentDate = currentDateFormat.format(calForDate.getTime());


           Calendar calForTime = Calendar.getInstance();
           SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm:ss a");
           currentTime = currentTimeFormat.format(calForTime.getTime());

           //HashMap<String, Object> groupMessageKey = new HashMap<>();
           HashMap<String, Object> groupMessageKey = new HashMap<>();
           GroupNameRef.updateChildren(groupMessageKey);

          //GroupNameRef = GroupNameRef.child(messageKey);
           GroupMessageKeyRef = GroupNameRef.child(messageKey);

           HashMap<String, Object> messageInfoMap = new HashMap<>();
               messageInfoMap.put("name", currentUserName);
               messageInfoMap.put("message", message);
               messageInfoMap.put("date", currentDate);
               messageInfoMap.put("time", currentTime);


           // GroupNameRef.updateChildren(messageInfoMap);
           GroupMessageKeyRef.updateChildren(messageInfoMap);




       }

    }

//    private void DisplayMessage(DataSnapshot dataSnapshot)
//    {
//        for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
//            displayTextMessage.append(dataSnapshot1.child("name").getValue(String.class) +
//                    ":\n" + dataSnapshot1.child("message").getValue(String.class) +
//                    ":\n" + dataSnapshot1.child("time").getValue(String.class) +
//                    "   "+ dataSnapshot1.child("date").getValue(String.class) + "\n\n\n");
//        }
//        Iterator iterator = dataSnapshot.getChildren().iterator();
//
//        while (iterator.hasNext())
//        {
//            String chatDate =  (String) ((DataSnapshot)iterator.next()).getValue();
//            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
//            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
//            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
//
//            displayTextMessage.append(chatName + ":\n" + chatMessage + ":\n" + chatTime + "   "+ chatDate + "\n\n\n");
//
//
//            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
//        }
   // }


    private void DisplayMessage(DataSnapshot dataSnapshot1)
    {
//        if(dataSnapshot.exists()){
//            for (DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){

//                displayTextMessage.append(dataSnapshot1.child("name").getValue(String.class)+":\n"+ dataSnapshot1.child("message").getValue(String.class)+":\n"+
//                        dataSnapshot1.child("time").getValue(String.class)+"  "+ dataSnapshot1.child("date").getValue(String.class)+"\n\n\n");
//                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);


//            }
//        }
        Iterator iterator = dataSnapshot1.getChildren().iterator();

        while (iterator.hasNext())
        {
            String chatDate =  (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append(chatName + ":\n" + chatMessage + ":\n" + chatTime + "   "+ chatDate + "\n\n\n");


            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }


    }
}