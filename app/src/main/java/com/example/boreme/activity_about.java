package com.example.boreme;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import de.hdodenhof.circleimageview.CircleImageView;

public class activity_about extends AppCompatActivity {

//    This Class is responsible for controlling the UI part of the about activity.
//    It basically adds two onClickListeners to start custom chrome tabs showing my Github and LinkedIn pages.
//    Not really that important.







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_about);
        CircleImageView git,lin;


        git = findViewById(R.id.git);       //Initialize Object for github
        lin = findViewById(R.id.lin);       //Initialize Object for LinkedIn


        //set onclick even listener on github object.
        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Uri uri = Uri.parse("https://www.github.com/the301sparton");  //github profile URI
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().addDefaultShareMenuItem().enableUrlBarHiding().setCloseButtonIcon(BitmapFactory.decodeResource(
                        getResources(), R.drawable.ic_chevron_right_black_24dp)).setToolbarColor(Color.parseColor("#000000")).build();

                CustomTabActivityHelper.openCustomTab(activity_about.this, customTabsIntent, uri,
                        new CustomTabActivityHelper.CustomTabFallback() {
                            @Override
                            public void openUri(Activity activity, Uri uri) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                activity.startActivity(intent);  //start custom chrome tab
                            }
                        });

            }
        });



        //set onclick even listener on LinkedIn object.
        lin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.linkedin.com/in/chaitanya-deshpande-6bb71b153");  //LinkedIn profile URI
                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().addDefaultShareMenuItem().enableUrlBarHiding().setCloseButtonIcon(BitmapFactory.decodeResource(
                        getResources(), R.drawable.ic_chevron_right_black_24dp)).setToolbarColor(Color.parseColor("#000000")).build();

                CustomTabActivityHelper.openCustomTab(activity_about.this, customTabsIntent, uri,
                        new CustomTabActivityHelper.CustomTabFallback() {
                            @Override
                            public void openUri(Activity activity, Uri uri) {
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                activity.startActivity(intent);     //start custom chrome tab
                            }
                        });
            }
        });

    }
}
