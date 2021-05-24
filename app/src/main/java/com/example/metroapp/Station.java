package com.example.metroapp;


import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "stations")
public class Station {

    @PrimaryKey(autoGenerate = true)
    public int id;


    public String name;
    public double latitude;
    public double  longitude;
    public byte line;





}


