package com.whoisconnected.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.whoisconnected.database.Model.Scan;

import java.util.List;

@Dao
public interface ScanDAO {
    @Insert
    Long insertScan(Scan scan);

    @Query("Select * from scan")
    List<Scan> getAllScans();

    @Query("Select * from scan where id= (select count(*) from scan)")
    Scan getLastScan();
}
