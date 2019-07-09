package com.example.boreme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Activity_status extends AppCompatActivity {
    FirebaseDatabase database;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_status);


        database = FirebaseDatabase.getInstance();

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final TextView currentStatus = findViewById(R.id.currentStatus);
        currentStatus.setText(preferences.getString("myStatus","I am dumb enough not to change status yet :("));

        Button update = findViewById(R.id.statusUpdate);
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText newStatus = findViewById(R.id.newStatus);
                if(newStatus.getText().toString().contentEquals("")){
                    Toast.makeText(getApplicationContext(),"Status can not be empty",Toast.LENGTH_SHORT).show();
                }
                else{
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        database.getReference("users").child(currentUser.getUid()).child("status").setValue(newStatus.getText().toString());
                        preferences.edit().putString("myStatus",newStatus.getText().toString()).apply();
                        currentStatus.setText(newStatus.getText().toString());
                    }
                }
            }
        });

    }
}
