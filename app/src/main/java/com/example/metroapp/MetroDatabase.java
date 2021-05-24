package com.example.metroapp;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Station.class, Ride.class},version = 1)
public abstract class MetroDatabase extends RoomDatabase {

    public abstract StationDAO stationDAO();
    public abstract RideDAO rideDAO();
    private static MetroDatabase ourInterface;

    public static MetroDatabase getInstance(Context context){

        if(ourInterface == null)
        {
            ourInterface = Room.databaseBuilder(context,
                    MetroDatabase.class,"metro.db").
                    createFromAsset("databases/metro.db").
                    allowMainThreadQueries().build();

        }
        return  ourInterface;

    }

}
