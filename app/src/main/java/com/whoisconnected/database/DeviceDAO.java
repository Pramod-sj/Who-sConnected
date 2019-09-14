package com.whoisconnected.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.whoisconnected.database.Model.Device;
import com.whoisconnected.database.Model.Macvender;

import java.util.List;

@Dao
public interface DeviceDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Long insertDevice(Device device);

    @Query("Select * from device")
    List<Device> getAllDevices();

    @Query("SELECT * FROM device WHERE macAddress IN ( :macIds ) ORDER BY lastIPNumber")
    List<Device> getDevicesByMacIds(List<String> macIds);

    @Query("Select * from device where macAddress=:macAddress")
    Device getDeviceByMac(String macAddress);

    @Query("UPDATE device SET ipAddress=:ipaddress where macAddress=:macaddress ")
    int updateIpAddress(String ipaddress,String macaddress);


    @Query("UPDATE device SET hostName=:hostName where macAddress=:macaddress ")
    int updateHostName(String hostName,String macaddress);


    @Query("UPDATE device SET macvender=:macvender where macAddress=:macaddress ")
    int updateHostName(Macvender macvender,String macaddress);

}
