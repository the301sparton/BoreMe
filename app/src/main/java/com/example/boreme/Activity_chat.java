package com.example.boreme;

// Class file for chat Activity (The Page In Which You Chat)
// Responsible for send and receiving Messages.
// Also Responsible for encrypting and decrypting messages.
// Please keep your contributions modular.

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Date;
import java.util.ResourceBundle;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_chat extends AppCompatActivity {


    //Declare Global objects / variables here
    SharedPreferences preferences;
    MessagesList messagesList;
    MessageInput inputView;
    MessagesListAdapter<Message> adapter;
    Author author;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    Author thatAuthor;
    SecretKey secretKey;
    TextView hisTypingState;
    int i=0;
    String thatUserId;



    // This function is triggered when the user presses the back button
    @Override
    public void onBackPressed() {
        finish();       //finish the activity and deallocate the memory
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_chat);

        //Initialize objects here
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String keyStr = preferences.getString("myKey","");
        byte[] decodedKey = Base64.decode(keyStr,0);
        secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        messagesList = findViewById(R.id.messagesList);
        inputView = findViewById(R.id.msgBox);
        hisTypingState = findViewById(R.id.hisTypingState);


        setStatusTapListener();     //Function responsible to display profile when status bar is clicked


        //get data passed from previous activity
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



        //Start Typing Listener
        final DatabaseReference typingRef = database.getReference("messages").child(thatUserId).child(currentUser.getUid()).child("typingState");
        inputView.setTypingListener(new MessageInput.TypingListener() {
            @Override
            public void onStartTyping() {
               typingRef.setValue("true");
            }

            @Override
            public void onStopTyping() {
                typingRef.setValue("false");
            }
        });
        //End Typing Listener



        //Start Monitor Typing state of other user
        DatabaseReference hisTypingRef = database.getReference("messages").child(currentUser.getUid()).child(thatUserId).child("typingState");
        hisTypingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue(String.class) != null) {
                    if (dataSnapshot.getValue(String.class).contentEquals("true")) {
                        hisTypingState.setText("Typing..");
                    } else {
                        hisTypingState.setText("");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //End Tying monitor for other user



        //Define Image loading process
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.get().load(url).into(imageView);        //use Picasso to load image
            }
        };


        //Create a new author object and set all fields
        author = new Author();
        author.setName(currentUser.getDisplayName());
        author.setId(currentUser.getUid());
        author.setAvatar(String.valueOf(currentUser.getPhotoUrl()));
        adapter = new MessagesListAdapter<>(author.getId(), imageLoader);


        // set adapter to message list
        messagesList.setAdapter(adapter);


        //set listener for message send event
        inputView.setInputListener(new MessageInput.InputListener() {
            @Override
            public boolean onSubmit(CharSequence input) {
                //validate and send message

                byte[] cyperText = new byte[0];
                try {
                    //Encrypted Message
                    cyperText = encryptMsg(String.valueOf(input), secretKey);
                    Message message = new Message();    //Create new Message objects
                    message.setId(String.valueOf(i));
                    message.setText(Base64.encodeToString(cyperText,0));
                    message.setCreatedAt(new Date());
                    message.setAuthor(author);

                    //create new key for the message;
                    DatabaseReference reference = database.getReference("messages").child(thatUserId).child(author.getId());
                    String nxtMsg = reference.push().getKey();
                    if (nxtMsg != null) {
                        //save encrypted message to database
                        reference.child("messages").child(nxtMsg).setValue(message);
                        reference.child("author").child("lol").setValue(author);
                    }
                    message.setText(String.valueOf(input));
                    adapter.addToStart(message, true);

                    i++;     //increment id

                    //catch all possible exceptions
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidParameterSpecException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                return true;
            }
        });


        //Get His Key
        final DatabaseReference hisKey = database.getReference("users").child(thatUserId);


        //Add listener for their messages
        hisKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot hisKeySnap) {

                CircleImageView thatUserImg = findViewById(R.id.hisImg);
                Picasso.get().load(hisKeySnap.child("photoURI").getValue(String.class)).into(thatUserImg);
                TextView hisName = findViewById(R.id.hisName);
                hisName.setText(hisKeySnap.child("displayName").getValue(String.class));

                //get other users key
                byte[] decodedKey = Base64.decode(hisKeySnap.child("key").getValue(String.class),0);
                final SecretKey hisSecretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

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

                                    try {
                                        Date dateOfMsg = dataSnap.child("createdAt").getValue(Date.class);
                                        Message message = new Message();
                                        message.setCreatedAt(dateOfMsg);
                                        message.setId(dataSnap.child("id").getValue(String.class));
                                        String cypherMsg = dataSnap.child("text").getValue(String.class);  //Encrypted message received
                                        message.setText(decryptMsg(Base64.decode(cypherMsg,0),hisSecretKey)); //decrypt message and add to message object
                                        message.setAuthor(thatAuthor);  //set details of the user who sent the message
                                        adapter.addToStart(message,true);   // add received message to the UI


                                        //catch all possible decryption exceptions
                                    } catch (NoSuchPaddingException e) {
                                        e.printStackTrace();
                                    } catch (NoSuchAlgorithmException e) {
                                        e.printStackTrace();
                                    } catch (InvalidParameterSpecException e) {
                                        e.printStackTrace();
                                    } catch (InvalidAlgorithmParameterException e) {
                                        e.printStackTrace();
                                    } catch (InvalidKeyException e) {
                                        e.printStackTrace();
                                    } catch (BadPaddingException e) {
                                        e.printStackTrace();
                                    } catch (IllegalBlockSizeException e) {
                                        e.printStackTrace();
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                                }
                                //remove messages from database when received;
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    // Start activity profile when status-bar is clicked
    void setStatusTapListener(){
        LinearLayout statusBar = findViewById(R.id.titleBar);
        statusBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Activity_chat.this, Activity_Profile.class).putExtra("hisKey",thatUserId));
            }
        });
    }


    // Function responsible to create encrypted message
    public static byte[] encryptMsg(String message, SecretKey secret)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException
    {
        /* Encrypt the message. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        return cipherText;
    }


    //Function responsible to decrypt a given message
    public static String decryptMsg(byte[] cipherText, SecretKey secret)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException
    {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);
        String decryptString = new String(cipher.doFinal(cipherText), "UTF-8");
        return decryptString;
    }


}

