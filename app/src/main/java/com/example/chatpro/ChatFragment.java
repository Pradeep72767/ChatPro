package com.example.chatpro;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragment extends Fragment {

    private View privateChatView;
    private RecyclerView ChatList;

    private DatabaseReference chatRef, UserRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private FirebaseUser user;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatView =  inflater.inflate(R.layout.fragment_chat, container, false);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        if (user != null){
            currentUserID = user.getUid();
            chatRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        }

        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");

        ChatList = privateChatView.findViewById(R.id.chat_list);
        ChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  privateChatView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef, Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts, ChatViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatViewHolder chatViewHolder, int i, @NonNull Contacts contacts)
                    {
                        final String userIDs = getRef(i).getKey();
                        final String[] retImage = {"default_image"};

                        UserRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    if (dataSnapshot.hasChild("images"))
                                    {
                                        retImage[0] = dataSnapshot.child("images").getValue().toString();
                                        Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(chatViewHolder.profileImage);
                                    }

                                    final String retName  = dataSnapshot.child("name").getValue().toString();
                                    String retStatus =  dataSnapshot.child("status").getValue().toString();

                                    chatViewHolder.userName.setText(retName);
                                    chatViewHolder.userStatus.setText("Last seen :" + "\n" + "Date" + "Time");

                                    chatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view)
                                        {
                                            Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                            chatIntent.putExtra("visit_user_id", userIDs);
                                            chatIntent.putExtra("visit_user_name", retName);
                                            chatIntent.putExtra("visit_user_image", retImage[0]);
                                            startActivity(chatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError)
                            {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        return new ChatViewHolder(view);
                    }
                };

        ChatList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userName, userStatus;

        public ChatViewHolder(@NonNull View itemView)

        {
            super(itemView);

            profileImage = itemView.findViewById(R.id.user_profile_images);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
        }
    }
}