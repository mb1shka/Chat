package com.javarush.task.task30.task3008.client;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class ClientGuiModel {

    private final Set allUserNames = new TreeSet();
    //список всех участников чата

    private String newMessage;
    //новое сообщение, которое получил клиент


    public Set getAllUserNames() {
        return Collections.unmodifiableSet(allUserNames);
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }

    public void addUser(String newUserName) {
        //добавляет имя участника во множество, хранящее всех участников
        allUserNames.add(newUserName);
    }

    public void deleteUser(String userName) {
        allUserNames.remove(userName);
    }
}
