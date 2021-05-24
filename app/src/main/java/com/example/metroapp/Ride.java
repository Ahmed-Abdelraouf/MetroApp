package com.example.metroapp;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "rides")
public class Ride {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String start,end,note;
    public  byte price;


    public Ride() {
    }

    public Ride(String start, String end, String note,byte price) {
        this.start = start;
        this.end = end;
        this.note = note;
        this.price = price;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ride ride = (Ride) o;
        return price == ride.price &&
                Objects.equals(start, ride.start) &&
                Objects.equals(end, ride.end) &&
                Objects.equals(note, ride.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, note, price);
    }
}
