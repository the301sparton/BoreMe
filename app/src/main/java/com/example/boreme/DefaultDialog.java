package com.example.boreme;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;

 class DefaultDialog implements IDialog {

    /*...*/
    private String id, dialogPhoto, dialogName;
    private IMessage lastMessage;
    private int unreadCount;
    private ArrayList<IUser> users;

    DefaultDialog(String id, String dialogName, String dialogPhoto, IMessage lastMessage, ArrayList<IUser> users, int unreadCount) {
        this.id = id;
        this.dialogName = dialogName;
        this.dialogPhoto = dialogPhoto;
        this.lastMessage = lastMessage;
        this.users = users;
        this.unreadCount = unreadCount;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDialogPhoto() {
        return dialogPhoto;
    }

    @Override
    public String getDialogName() {
        return dialogName;
    }

    @Override
    public ArrayList<IUser> getUsers() {
        return users;
    }

    @Override
    public IMessage getLastMessage() {
        return lastMessage;
    }

    @Override
    public void setLastMessage(IMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    @Override
    public int getUnreadCount() {
        return unreadCount;
    }
}