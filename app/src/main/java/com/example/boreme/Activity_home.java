package com.example.boreme;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_home extends AppCompatActivity {
    Message lastMsg;
    CircleImageView meImg;
    TextView displayName, my_status;
    FirebaseDatabase database;
    SharedPreferences preferences;
    FirebaseUser currentUser;
    DialogsList dialogsList;

    BoundService mBoundService;
    boolean mServiceBound = false;


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BoundService.MyBinder myBinder = (BoundService.MyBinder) service;
            mBoundService = myBinder.getService();
            mServiceBound = true;
        }
    };
    @Override
    protected void onStart() {

        Log.v("loginState", String.valueOf(preferences.getBoolean("isSignedIn",false)));
        if(preferences.getBoolean("isSignedIn",false) && !mServiceBound) {
            Intent intent = new Intent(this, BoundService.class);
            startService(intent);
            bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            preferences.edit().putBoolean("isServiceConnected",true).apply();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        meImg = findViewById(R.id.profile_image);
        displayName = findViewById(R.id.displayName);
        my_status = findViewById(R.id.my_statusText);
        dialogsList = findViewById(R.id.dialogsList);

        database = FirebaseDatabase.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        getViews();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Activity_home.this, Activity_newConvoPicker.class));
            }
        });
    }

    void getViews(){
        if (!preferences.getBoolean("isSignedIn", false)) {
            List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    200);
        } else {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            displayName.setText(currentUser.getDisplayName());
            my_status.setText(preferences.getString("myStatus","I am dumb enough not to change status yet :("));
            Picasso.get().load(currentUser.getPhotoUrl()).into(meImg);
            setDialogAdaptor();
        }

    }

    @Override
    protected void onRestart() {
        lastMsg = null;
        setDialogAdaptor();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        getViews();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.encryption){
            startActivity(new Intent(Activity_home.this, Activity_Encrpt.class));
        }
        else if(item.getItemId() == R.id.about){
            startActivity(new Intent(Activity_home.this, activity_about.class));
        }
        else if(item.getItemId() == R.id.my_status){
            startActivity(new Intent(Activity_home.this, Activity_status.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 200) {

            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                preferences.edit().putBoolean("isSignedIn", true).apply();

                if (user != null) {
                    onStart();
                    Log.i("url", String.valueOf(user.getPhotoUrl()));
                    displayName.setText(user.getDisplayName());
                    my_status.setText(user.getEmail());

                    DatabaseReference meAsUser = database.getReference("users").child(user.getUid());
                    meAsUser.child("displayName").setValue(user.getDisplayName());
                    meAsUser.child("photoURI").setValue(String.valueOf(user.getPhotoUrl()));
                    meAsUser.child("emailId").setValue(user.getEmail());
                    meAsUser.child("my_status").setValue(preferences.getString("myStatus","I am dumb enough not to change status yet :("));
                    Picasso.get().load(user.getPhotoUrl()).into(meImg);
                }
                currentUser = user;
                genereteNewKey();
                setDialogAdaptor();
                Toast.makeText(getApplicationContext(), "Sign In successful :)", Toast.LENGTH_SHORT).show();
                // ...
            } else {
                Toast.makeText(getApplicationContext(), "Failed To Sign In :(", Toast.LENGTH_LONG).show();
            }
        }
    }



    private void setDialogAdaptor() {

        List<DefaultDialog> ConvoList;

        final DialogsListAdapter dialogsListAdapter = new DialogsListAdapter<>(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.get().load(url).into(imageView);
            }
        });

        dialogsList.setAdapter(dialogsListAdapter);

        DatabaseReference myDialog = database.getReference("messages").child(currentUser.getUid());
        final List<DefaultDialog> finalConvoList = new ArrayList<DefaultDialog>();
        myDialog.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userConvo : dataSnapshot.getChildren()){
                    String id = userConvo.getKey();
                    Author convoAuthor = userConvo.child("author").child("lol").getValue(Author.class);
                    if(convoAuthor != null) {
                        String convoName = convoAuthor.getName();
                        String convoImg = convoAuthor.getAvatar();
                        ArrayList<IUser> convoUser = new ArrayList<>();
                        convoUser.add(convoAuthor);
                        int unReadCount = (int) userConvo.child("messages").getChildrenCount();

                        for (DataSnapshot thisConvoMsg : userConvo.child("messages").getChildren()) {
                            if (thisConvoMsg != null) {
                                lastMsg = thisConvoMsg.getValue(Message.class);
                                lastMsg.setAuthor(convoAuthor);
                            }
                        }

                        if (lastMsg == null) {
                            lastMsg = new Message();
                            lastMsg.setCreatedAt(new Date());
                            lastMsg.setId("1");
                            lastMsg.setAuthor(convoAuthor);
                            lastMsg.setText("No new Message.");
                            unReadCount = 0;
                        } else {
                            lastMsg.setText("Encrypted MSG: " + lastMsg.getText());
                        }


                        DefaultDialog thisDialog = new DefaultDialog(id, convoName, convoImg, lastMsg, convoUser, unReadCount);
                        // Log.i("convoName", lastMsg.getText());
                        finalConvoList.add(thisDialog);
                    }
                }

                if(finalConvoList.isEmpty()){
                    //You are lonely
                    dialogsList.setVisibility(View.GONE);
                    TextView tv= findViewById(R.id.lonelyText);
                    tv.setVisibility(View.VISIBLE);
                }
                else{
                    TextView tv= findViewById(R.id.lonelyText);
                    tv.setVisibility(View.GONE);
                    dialogsList.setVisibility(View.VISIBLE);
                    dialogsListAdapter.setItems(finalConvoList);
                }
                LinearLayout progressBar = findViewById(R.id.holder);
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<IDialog>() {
            @Override
            public void onDialogClick(IDialog dialog) {
                startActivity(new Intent(Activity_home.this, Activity_chat.class).putExtra("inFrontUser",dialog.getId()));
            }


        });

    }


    private void genereteNewKey(){

        if(preferences.getString("myKey","").contentEquals("")){
            try {
              SecretKey secretKey = generateKey();
                String keyStr = Base64.encodeToString(secretKey.getEncoded(),0);
                database.getReference("users").child(currentUser.getUid()).child("key").setValue(keyStr);
                preferences.edit().putString("myKey", keyStr).apply();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeySpecException e) {
                e.printStackTrace();
            }
        }
    }

    public static SecretKey generateKey()
            throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        SecretKey secret;
        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        keygen.init(256);
        return secret = keygen.generateKey();
    }

}
