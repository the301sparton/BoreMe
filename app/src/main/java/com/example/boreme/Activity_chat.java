package com.example.boreme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
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

public class Activity_chat extends AppCompatActivity {

    SharedPreferences preferences;
    MessagesList messagesList;
    MessageInput inputView;
    MessagesListAdapter<Message> adapter;
    Author author;
    FirebaseUser currentUser;
    FirebaseDatabase database;
    Author thatAuthor;
    SecretKey secretKey;
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

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        database = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();



            String keyStr = preferences.getString("myKey","");
            byte[] decodedKey = Base64.decode(keyStr,0);
            secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");


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

                byte[] cyperText = new byte[0];
                try {
                    //Encrypted Message
                    cyperText = encryptMsg(String.valueOf(input), secretKey);

                    Message message = new Message();
                    message.setId(String.valueOf(i));
                    message.setText(Base64.encodeToString(cyperText,0));
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
        final DatabaseReference hisKey = database.getReference("users").child(thatUserId).child("key");

        hisKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot hisKeySnap) {

                byte[] decodedKey = Base64.decode(hisKeySnap.getValue(String.class),0);
                final SecretKey hisSecretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                Log.i("HisKey", String.valueOf(decodedKey));

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
                                        String cypherMsg = dataSnap.child("text").getValue(String.class);
                                        Log.i("gotMsg",cypherMsg);
                                        message.setText(decryptMsg(Base64.decode(cypherMsg,0),hisSecretKey));
                                        message.setAuthor(thatAuthor);
                                        adapter.addToStart(message,true);

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

    public static SecretKey generateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        SecretKey secret;
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(256);
        return secret = keygen.generateKey();
    }

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

