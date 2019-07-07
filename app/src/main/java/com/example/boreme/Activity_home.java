package com.example.boreme;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_home extends AppCompatActivity {
    Message lastMsg;
    CircleImageView meImg;
    TextView displayName, emailId;
    FirebaseDatabase database;
    SharedPreferences preferences;
    FirebaseUser currentUser;
    DialogsList dialogsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        meImg = findViewById(R.id.profile_image);
        displayName = findViewById(R.id.displayName);
        emailId = findViewById(R.id.emailId);
        dialogsList = findViewById(R.id.dialogsList);

        database = FirebaseDatabase.getInstance();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        List<AuthUI.IdpConfig> providers = Collections.singletonList(new AuthUI.IdpConfig.GoogleBuilder().build());

// Create and launch sign-in intent
        if (!preferences.getBoolean("isSignedIn", false)) {
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .build(),
                    200);
            preferences.edit().putBoolean("isSignedIn", true).apply();
        } else {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            displayName.setText(currentUser.getDisplayName());
            emailId.setText(currentUser.getEmail());
            Picasso.get().load(currentUser.getPhotoUrl()).into(meImg);
        }


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Activity_home.this, Activity_newConvoPicker.class));
            }
        });

        setDialogAdaptor();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.signOut) {
            signOutUser();
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


                if (user != null) {
                    Log.i("url", String.valueOf(user.getPhotoUrl()));
                    displayName.setText(user.getDisplayName());
                    emailId.setText(user.getEmail());

                    DatabaseReference meAsUser = database.getReference("users").child(user.getUid());
                    meAsUser.child("displayName").setValue(user.getDisplayName());
                    meAsUser.child("photoURI").setValue(String.valueOf(user.getPhotoUrl()));
                    meAsUser.child("emailId").setValue(user.getEmail());

                    Picasso.get().load(user.getPhotoUrl()).into(meImg);
                }
                currentUser = user;
                Toast.makeText(getApplicationContext(), "Sign In successful :)", Toast.LENGTH_SHORT).show();
                // ...
            } else {
                Toast.makeText(getApplicationContext(), "Failed To Sign In :(", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void signOutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        finish();
                    }
                });
        preferences.edit().putBoolean("isSignedIn", false).apply();
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
                    String convoName = convoAuthor.getName();
                    String convoImg = convoAuthor.getAvatar();
                    ArrayList<IUser> convoUser = new ArrayList<>();
                    convoUser.add(convoAuthor);
                    int unReadCount = (int) userConvo.child("messages").getChildrenCount();

                    for(DataSnapshot thisConvoMsg : userConvo.child("messages").getChildren()){
                            Log.i("message", String.valueOf(thisConvoMsg));
                            lastMsg = thisConvoMsg.getValue(Message.class);
                            lastMsg.setAuthor(convoAuthor);
                    }


                    DefaultDialog thisDialog = new DefaultDialog(id, convoName, convoImg, lastMsg, convoUser, unReadCount);
                   // Log.i("convoName", lastMsg.getText());
                    finalConvoList.add(thisDialog);
                }
                dialogsListAdapter.setItems(finalConvoList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
