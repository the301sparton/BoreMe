package com.example.boreme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class Activity_newConvoPicker extends AppCompatActivity {

    FirebaseDatabase database;
    LinearLayout pg_bar;
    ListView listView;
    ArrayList<DTO> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_new_convo_picker);

        list = new ArrayList<DTO>();
        pg_bar = findViewById(R.id.prog_loader);
        listView = findViewById(R.id.list);

        database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pg_bar.setVisibility(View.GONE);
                for (DataSnapshot user : dataSnapshot.getChildren()){
                    DTO currentUser = new DTO();
                    currentUser.setUid(Objects.requireNonNull(user.getKey()));
                    currentUser.setDisplayName(Objects.requireNonNull(user.child("displayName").getValue()).toString());
                    currentUser.setPhotoUri(Objects.requireNonNull(user.child("photoURI").getValue()).toString());
                    currentUser.setEmailId(Objects.requireNonNull(user.child("emailId").getValue()).toString());
                    list.add(currentUser);
                }
                ListAdapter listAdapter = new ListAdapter(list,Activity_newConvoPicker.this);
                listView.setAdapter(listAdapter);
                listViewClickListener();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

     void listViewClickListener(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(Activity_newConvoPicker.this, Activity_chat.class);
                intent.putExtra("inFrontUser",list.get(i).getUid());
                startActivity(intent);
            }
        });
    }
}

class DTO implements Serializable{
    private String displayName, photoUri, emailId, uid;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}

class ViewHolder {
    public TextView displayName, emailId;
    public CircleImageView displayImg;
}

class ListAdapter extends BaseAdapter {

    ArrayList<DTO> list = new ArrayList<>();
    Activity ctx;
    ViewHolder holder;
    ListAdapter(ArrayList<DTO> arr, Activity context){
        list.addAll(arr);
        ctx = context;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ctx.getLayoutInflater().inflate(R.layout.newconvopicker_listrow, null);
            holder = new ViewHolder();
            holder.displayName = convertView.findViewById(R.id.picker_displayName);
            holder.displayImg = convertView.findViewById(R.id.picker_displayImg);
            holder.emailId = convertView.findViewById(R.id.picker_emailId);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        Picasso.get().load(list.get(position).getPhotoUri()).into(holder.displayImg);
        holder.displayName.setText(list.get(position).getDisplayName());
        holder.emailId.setText(list.get(position).getEmailId());

        return convertView;
    }
}