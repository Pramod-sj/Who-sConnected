package com.whoisconnected.database.Model;

import androidx.annotation.Keep;

import java.io.Serializable;

@Keep
public class Macvender implements Serializable {
    private String company;
    private String mac_prefix;
    private String address;
    private String start_hex;
    private String end_hex;
    private String country;
    private String type;

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getMac_prefix() {
        return mac_prefix;
    }

    public void setMac_prefix(String mac_prefix) {
        this.mac_prefix = mac_prefix;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStart_hex() {
        return start_hex;
    }

    public void setStart_hex(String start_hex) {
        this.start_hex = start_hex;
    }

    public String getEnd_hex() {
        return end_hex;
    }

    public void setEnd_hex(String end_hex) {
        this.end_hex = end_hex;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
