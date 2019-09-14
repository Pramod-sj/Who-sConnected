package com.whoisconnected.database.Model;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Keep
@Entity
public class Device implements Serializable {

    @PrimaryKey
    @NonNull
    String macAddress;

    String ipAddress;

    String hostName;

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    Macvender macvender;

    public Macvender getMacvender() {
        return macvender;
    }

    public void setMacvender(Macvender macvender) {
        this.macvender = macvender;
    }

    int lastIPNumber;

    public int getLastIPNumber() {
        return lastIPNumber;
    }

    public void setLastIPNumber(int lastIPNumber) {
        this.lastIPNumber = lastIPNumber;
    }
}
