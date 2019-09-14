package com.whoisconnected.database.Model;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;

@Keep
@Entity
public class Scan implements Serializable {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    int id;

    String dateTime;
    float timeTakenInMins;
    ArrayList<String> foundMacAddress;


    public ArrayList<String> getFoundMacAddress() {
        return foundMacAddress;
    }

    public void setFoundMacAddress(ArrayList<String> foundMacAddress) {
        this.foundMacAddress = foundMacAddress;
    }

    public float getTimeTakenInMins() {
        return timeTakenInMins;
    }

    public void setTimeTakenInMins(float timeTakenInMins) {
        this.timeTakenInMins = timeTakenInMins;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

}
