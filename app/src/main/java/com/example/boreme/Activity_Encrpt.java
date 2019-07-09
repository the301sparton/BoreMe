package com.example.boreme;

//This class is responsible to create display user information about the algorithms and process of encryption
//It also displays user his 256 bit Key so that he can double check that his messages are end to end encrypted


import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.TextView;

public class Activity_Encrpt extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout__encrpt);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String key = preferences.getString("myKey","");
        String finality = "";

        byte[] toDecode = Base64.decode(key,0);
        int i = 1;
        for(byte b : toDecode){
            if(i != 256){
                finality += getStrFromByte(b).concat(" : "); //Create key string in binary
            }
            if(i%3 == 0){
                finality +="\n";
            }
           i++;
        }

        TextView textView = findViewById(R.id.encrptKey);
        textView.setText(finality); //display key string
    }


    //convert byte to binary formatted String
    String getStrFromByte(byte b1){
        String s1 = String.format("%8s", Integer.toBinaryString(b1 & 0xFF)).replace(' ', '0');
        return s1;
    }
}
