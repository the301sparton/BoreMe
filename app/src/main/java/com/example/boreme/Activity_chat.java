package com.example.boreme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.Date;

public class Activity_chat extends AppCompatActivity {

    MessagesList messagesList;
    MessageInput inputView;
    MessagesListAdapter<Message> adapter;
    Author author;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    Author thatAuthor;
    int i=0;

    String thatUserId;

    @Override
    public void onBackPressed() {
        //startActivity(new Intent(Activity_chat.this, Activity_newConvoPicker.class));
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat);


        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                thatUserId= null;
            } else {
                thatUserId= extras.getString("inFrontUser");
            }
        } else {
            thatUserId= (String) savedInstanceState.getSerializable("STRING_I_NEED");
        }

        messagesList = findViewById(R.id.messagesList);
        inputView = findViewById(R.id.msgBox);

        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        author = new Author();
        author.setName(currentUser.getDisplayName());
        author.setId(currentUser.getUid());
        author.setAvatar(String.valueOf(currentUser.getPhotoUrl()));

        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.get().load(url).into(imageView);
            }
        };


        adapter = new MessagesListAdapter<>(author.getId(), imageLoader);
        messagesList.setAdapter(adapter);

        inputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message
                Message message = new Message();
                message.setId(String.valueOf(i));
                message.setText(String.valueOf(input));
                message.setCreatedAt(new Date());
                message.setAuthor(author);
                adapter.addToStart(message, true);
                DatabaseReference reference = database.getReference("messages").child(thatUserId).child(author.getId());
                String nxtMsg = reference.push().getKey();
                if (nxtMsg != null) {
                    reference.child("messages").child(nxtMsg).setValue(message);
                    reference.child("author").child("lol").setValue(author);
                }
                i++;
                return true;
            }
        });

        final DatabaseReference myMsgs = database.getReference("messages").child(author.getId()).child(thatUserId);
        DatabaseReference authorRef = myMsgs.child("author");
        authorRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                thatAuthor = dataSnapshot.child("lol").getValue(Author.class);
                myMsgs.child("messages").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot dataSnap : dataSnapshot.getChildren()){
                            Message message = dataSnap.getValue(Message.class);
                            if (message != null) {
                                message.setAuthor(thatAuthor);
                                adapter.addToStart(message,true);
                            }
                        }
                       dataSnapshot.getRef().removeValue();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
}

