 package com.example.boreme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

 public class Activity_Profile extends AppCompatActivity {
     String thatUserId;String key;String finality = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_profile);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                thatUserId= null;
            } else {
                thatUserId= extras.getString("hisKey");

            }
        }

        else {
            thatUserId= (String) savedInstanceState.getSerializable("STRING_I_NEED");
        }
        Log.i("uouo",thatUserId);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference hisRef = database.getReference("users").child(thatUserId);

        hisRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                CircleImageView imageView = findViewById(R.id.hisProfilePic);
                Picasso.get().load(dataSnapshot.child("photoURI").getValue(String.class)).into(imageView);
                TextView hisName = findViewById(R.id.hisProfileName);
                hisName.setText(dataSnapshot.child("displayName").getValue(String.class));
                TextView hisKey = findViewById(R.id.hisProfileKey);
                TextView hisStatus = findViewById(R.id.hisProfileStatus);
                hisStatus.setText(dataSnapshot.child("status").getValue(String.class));

                key = dataSnapshot.child("key").getValue(String.class);


                byte[] toDecode = Base64.decode(key,0);
                int i = 1;
                for(byte b : toDecode){
                    if(i != 256){
                        finality += getStrFromByte(b).concat(" : ");
                    }
                    if(i%3 == 0){
                        finality +="\n";
                    }
                    i++;
                }

                hisKey.setText(finality);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

     String getStrFromByte(byte b1){
         String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
         return s1;
     }
}
