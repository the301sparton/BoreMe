package com.example.boreme;

import com.google.firebase.database.Exclude;
import com.stfalcon.chatkit.commons.models.IUser;

public class Author implements IUser {

    /*...*/

    private String id,name,avatar;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
