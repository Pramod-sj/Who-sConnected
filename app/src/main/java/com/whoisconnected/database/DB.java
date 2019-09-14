package com.whoisconnected.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.whoisconnected.util.Convertor;
import com.whoisconnected.database.Model.Device;
import com.whoisconnected.database.Model.Scan;

@Database(entities = {Device.class, Scan.class},version = 2,exportSchema = false)

@TypeConverters(Convertor.class)
public abstract class DB extends RoomDatabase {

    public abstract DeviceDAO dbDAO();


    public abstract ScanDAO scanDAO();


}
