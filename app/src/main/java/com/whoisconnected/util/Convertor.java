package com.whoisconnected.util;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.whoisconnected.database.Model.Macvender;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class Convertor {

    @TypeConverter
    public static String getStringFromMacvendorObject(Macvender macvender){
        return new Gson().toJson(macvender);
    }

    @TypeConverter
    public static Macvender getMacvendorObjectFromString(String string){
        return new Gson().fromJson(string, Macvender.class);
    }

    @TypeConverter
    public static String getStringFromArrayList(ArrayList<String> strings){
        return new Gson().toJson(strings);
    }

    @TypeConverter
    public static ArrayList<String> getArrayListFromString(String string){
        Type type=new TypeToken<ArrayList<String>>(){}.getType();
        return new Gson().fromJson(string, type);
    }

}
