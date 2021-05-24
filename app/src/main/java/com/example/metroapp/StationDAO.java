package com.example.metroapp;


import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Query;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface StationDAO {

    @Query("SELECT name FROM stations WHERE line =:lineNumber")
    List<String> lineStations(byte lineNumber);


    @Query("SELECT id,latitude,longitude FROM stations WHERE name =:stationName")
    StationLocation stationLocation(String stationName);



    @Query("SELECT id,latitude,longitude FROM stations LIMIT 5 OFFSET :offset")
    List<StationLocation> subStationsLocations(byte offset);


}

class StationLocation {

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "id")
    public byte id;

}