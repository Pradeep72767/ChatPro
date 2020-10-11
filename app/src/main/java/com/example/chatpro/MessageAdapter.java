package com.example.chatpro;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{

    private List<Message> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;

    public MessageAdapter(List<Message> userMessageList)
    {
        this.userMessageList = userMessageList;
    }


    public class MessageViewHolder extends  RecyclerView.ViewHolder
    {
        TextView senderMessageText, receiverMessageText;
        CircleImageView receiverProfileImage;
        public ImageView messageSenderImage, messageReceiverImage;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receive_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageReceiverImage = itemView.findViewById(R.id.message_receiver_image_file);
            messageSenderImage = itemView.findViewById(R.id.message_sender_image_file);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_message_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Message message = userMessageList.get(position);

        String fromUserID  = message.getFrom();
        String fromMessageType = message.getType();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("images"))
                {
                    String receiverImage = dataSnapshot.child("images").getValue().toString();

                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });



        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderImage.setVisibility(View.GONE);
        holder.messageReceiverImage.setVisibility(View.GONE);

        if (fromMessageType.equals("text"))
        {



            if (fromUserID.equals(messageSenderID))
            {
                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(message.getMessage() + "\n \n" + message.getTime());

            }
            else
            {


                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(message.getMessage() + "\n \n" + message.getTime());
            }
        }
        else if (fromMessageType.equals("image"))
        {
            if (fromUserID.equals(messageSenderID))
            {
                holder.messageSenderImage.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).into(holder.messageSenderImage);
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverImage.setVisibility(View.VISIBLE);

                Picasso.get().load(message.getMessage()).into(holder.messageReceiverImage);
            }
        }
        else
        {
            if (fromUserID.equals(messageSenderID))
            {
                holder.messageSenderImage.setVisibility(View.VISIBLE);

                holder.messageSenderImage.setBackgroundResource(R.drawable.file);


            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverImage.setVisibility(View.VISIBLE);

                holder.messageReceiverImage.setBackgroundResource(R.drawable.file);


            }
        }


        if(fromUserID.equals(messageSenderID))
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download",
                                        "Cancel",
                                        "Delete for everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteSentMessage(position, holder);
                                }
                                else if (i == 1)
                                {
                                    Uri uri = Uri.parse(userMessageList.get(position).getMessage());
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                                else if (i == 3)
                                {
                                    deleteMessageForEveryone(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }


                    else if (userMessageList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel",
                                        "Delete for everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteSentMessage(position, holder);
                                }
                                else if (i == 2)
                                {
                                    deleteMessageForEveryone(position, holder);
                                }

                            }
                        });
                        builder.show();
                    }

                    else if (userMessageList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "View",
                                        "Cancel",
                                        "Delete for everyone"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteSentMessage(position, holder);
                                }
                                else if (i == 1)
                                {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }
                                else if(i == 3)
                                {
                                    deleteMessageForEveryone(position, holder);
                                }
                            }
                        });
                        builder.show();
                    }


                }
            });
        }

        else
        {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Download",
                                        "Cancel"
                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteReceiveMessage(position, holder);
                                }
                                else if (i == 1)
                                {
                                    Uri uri = Uri.parse(userMessageList.get(position).getMessage());
                                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }


                    else if (userMessageList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "Cancel"

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteReceiveMessage(position, holder);
                                }


                            }
                        });
                        builder.show();
                    }

                    if (userMessageList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        "Delete for me",
                                        "View",
                                        "Cancel"

                                };
                        AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Delete Message...");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteReceiveMessage(position, holder);
                                }
                                else if (i == 1)
                                {
                                    Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                    intent.putExtra("url", userMessageList.get(position).getMessage());
                                    holder.itemView.getContext().startActivity(intent);
                                }

                            }
                        });
                        builder.show();
                    }


                }
            });
        }


    }




    @Override
    public int getItemCount()
    {
        return userMessageList.size();
    }


    private void deleteSentMessage(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef =  FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void deleteReceiveMessage(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef =  FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Delete successfully", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }


    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder)
    {
        final DatabaseReference rootRef =  FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    rootRef.child("Messages")
                            .child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getTo())
                            .child(userMessageList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Message Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }




}
