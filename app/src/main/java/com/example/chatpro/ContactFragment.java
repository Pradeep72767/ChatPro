package com.example.chatpro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class ContactFragment extends Fragment {

    private View ContactView;
    private RecyclerView myContactList;

    private DatabaseReference ContactsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    FirebaseUser user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        user = FirebaseAuth.getInstance().getCurrentUser();
        ContactView = inflater.inflate(R.layout.fragment_contact, container, false);
        if (user == null){
            Intent loginIntent = new Intent(getContext(), LoginActivity.class);
            loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(loginIntent);
        } else {


            myContactList = ContactView.findViewById(R.id.contact_list);
            myContactList.setLayoutManager(new LinearLayoutManager(getContext()));


            mAuth = FirebaseAuth.getInstance();
            currentUserID = mAuth.getCurrentUser().getUid();
            ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
            UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");


        }
        return ContactView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ContactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ContactViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactViewHolder contactViewHolder, int i, @NonNull Contacts contacts)
                    {
                        String UsersIDs = getRef(i).getKey();

                        UsersRef.child(UsersIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    if (dataSnapshot.child("userStatus").hasChild("state"))
                                    {
                                        String state = dataSnapshot.child("userStatus").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userStatus").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userStatus").child("time").getValue().toString();

                                        if (state.equals("online"))
                                        {
                                            contactViewHolder.onlineIcon.setVisibility(View.VISIBLE);
                                        }
                                        else if (state.equals("offline"))
                                        {
                                            contactViewHolder.onlineIcon.setVisibility(View.INVISIBLE);
                                        }
                                    }
                                    else
                                    {
                                        contactViewHolder.onlineIcon.setVisibility(View.INVISIBLE);
                                    }

                                    if (dataSnapshot.hasChild("images"))
                                    {
                                        String profileImage = dataSnapshot.child("images").getValue().toString();
                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();


                                        contactViewHolder.userName.setText(profileName);
                                        contactViewHolder.userStatus.setText(profileStatus);
                                        Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(contactViewHolder.profileImage);
                                    }
                                    else
                                    {
                                        String profileName = dataSnapshot.child("name").getValue().toString();
                                        String profileStatus = dataSnapshot.child("status").getValue().toString();


                                        contactViewHolder.userName.setText(profileName);
                                        contactViewHolder.userStatus.setText(profileStatus);
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }

                        });

//                        contactViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
////                                Snackbar.make(view, "Item is clicked", BaseTransientBottomBar.LENGTH_SHORT).show();
//                            }
//                        });
                    }

                    @NonNull
                    @Override
                    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        ContactViewHolder viewHolder = new ContactViewHolder(view);
                        return viewHolder;
                    }
                };

        myContactList.setAdapter(adapter);
        adapter.startListening();
    }


    public static class ContactViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;


        public ContactViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.user_profile_images);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}