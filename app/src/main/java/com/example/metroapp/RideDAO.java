package com.example.metroapp;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RideDAO {
    // select all the rides decreasing order .. then gave me the first one
    @Query("SELECT * FROM rides ORDER BY id DESC LIMIT 1")
    Ride lastRide();


    // select all the rides decreasing order -> it will call the full argument constructor
    @Query("SELECT * FROM rides ORDER BY id DESC")
    List<Ride> allRides();

    // insert ride -> it will call the 3 argument constructor
    @Insert
    void insertRide(Ride ride);


    //DELETE FROM rides WHERE id = 1;
    @Query("DELETE FROM rides WHERE id = :id")
    void removeRide(byte id);
}
