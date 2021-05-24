package com.example.metroapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements LocationListener {
    Geocoder geocoder ;
    StationDAO stationDAO;
    RideDAO rideDAO;
    LocationManager manager;
    Ride savedRide;
    EditText addressEditText,notesEditText;
    Spinner endStationSpinner, startStationSpinner;
    ImageView getEndStationImageView;



    byte ticketPrice;
    boolean fStart, fEnd, endFires, isChecked = true;
    byte[][] stationsIndexes = {
            {-2, -2},
            {-2, -2},
            {-2, -2},};


    //list of all stations line by line
    String startStation = "", endStation = "",savedNotes;
    List<String> allStations = new ArrayList<>(), lineOneStations, lineTwoStations, lineThreeStations;
    List<List<String>> stations = new ArrayList<>();
    ArrayList<String> rout = new ArrayList<>();
    ArrayList<String> convert = new ArrayList<>();
    ArrayList<String> direction = new ArrayList<>();
    ArrayList<String> tmp = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


        //setting
        stationDAO = MetroDatabase.getInstance(this).stationDAO();
        rideDAO = MetroDatabase.getInstance(this).rideDAO();
        geocoder = new Geocoder(this);


        // views
        addressEditText = findViewById(R.id.addressEditText);
        getEndStationImageView = findViewById(R.id.getEndStationImageView);
        startStationSpinner = findViewById(R.id.startStationSpinner);
        endStationSpinner = findViewById(R.id.endStationSpinner);
        notesEditText = findViewById(R.id.notesEditText);


        //get stations
        lineOneStations = stationDAO.lineStations((byte) 1);        //Helwan – El Marg
        lineTwoStations = stationDAO.lineStations((byte) 2);        // Shobra El Kheima – El Mounib
        lineThreeStations = stationDAO.lineStations((byte) 3);      // abbassiya - attaba

        // add all stations into allStations "Spinner adapter list"
        allStations.addAll(lineOneStations);
        allStations.addAll(lineTwoStations);
        allStations.addAll(lineThreeStations);


        // add stations into stations array
        stations.add(lineOneStations);
        stations.add(lineTwoStations);
        stations.add(lineThreeStations);

        // setup Spinner adapter and view
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, allStations);
        startStationSpinner.setAdapter(adapter);
        endStationSpinner.setAdapter(adapter);

        savedRide = rideDAO.lastRide();

        if (savedRide != null)
        {
            endStation = savedRide.end;
            startStation = savedRide.start;
            savedNotes = savedRide.note;
            startStationSpinner.setSelection(allStations.indexOf(startStation));
            endStationSpinner.setSelection(allStations.indexOf(endStation));
            notesEditText.setText(savedNotes);

        }else
        {
            startStationSpinner.setSelection(0);
            endStationSpinner.setSelection(0);
        }

        endStation = "";
        startStation = "";

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public void onBackPressed() {

        Ride ride = new Ride(startStation,endStation,notesEditText.getText().toString(),ticketPrice);

        // first time
        if(savedRide == null)
        {
            rideDAO.insertRide(ride);
            ride = null;
            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
        }

        if(savedRide != null && !savedRide.equals(ride))
        {

            rideDAO.insertRide(ride);
            ride = null;
            Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show();
        }

        nullifyAll();
        super.onBackPressed(); //close app
    }

    public void getRoute(View view) {

        startStation = startStationSpinner.getSelectedItem().toString();
        endStation = endStationSpinner.getSelectedItem().toString();

        if (startStation.equals(endStation)) {
            Toast.makeText(this, "please choose different stations", Toast.LENGTH_SHORT).show();
            return;
        }

        // its a new operation
         if(rout.size() == 0  || !(rout.get(0).equals(startStation) && rout.get(rout.size()-1).equals(endStation)))
        {
            metroHelper(startStation, endStation);
        }

        Intent intent = new Intent(this, RoutActivity.class);
        intent.putExtra("start", startStation);
        intent.putExtra("end", endStation);
        intent.putExtra("price", "" + ticketPrice);
        intent.putStringArrayListExtra("finalRout", rout);
        intent.putStringArrayListExtra("direction", direction);
        intent.putStringArrayListExtra("convert", convert);

        startActivity(intent);

        // nullify
        intent = null;

    }





    public void nearestStation(View view) {


        //getting view tag
        String tag = (String) view.getTag();


        if(tag.equals("endStation"))
        {
            Toast.makeText(this, "im here", Toast.LENGTH_SHORT).show();
            String address = addressEditText.getText().toString()+",Egypt"; // address from user
            try {
                Location location =new Location("");
                List<Address> locationName = geocoder.getFromLocationName(address, 1);
                if(locationName.size() != 0)
                {
                    location.setLatitude(locationName.get(0).getLatitude());
                    location.setLongitude(locationName.get(0).getLongitude());
                    byte minIndex = getMinIndex(location);
                    location = null;
                    endStationSpinner.setSelection(minIndex);
                }else
                {
                    Toast.makeText(this, "address not found", Toast.LENGTH_SHORT).show();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

            address = null;
        }
        else {
            manager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] perm = {Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(this,perm,1);
            }
            else {
                manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);

                //real device.... testing -> it works
                //manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
            }

        }
    }
    @Override
    public void onLocationChanged(Location location) {
        //get the nearest Station
        byte minIndex = getMinIndex(location);
        startStationSpinner.setSelection(minIndex);
    }


  /*
  while (subLocations.size() != 0)
        {
            for (StationLocation subLocation : subLocations) {
                location.setLongitude(subLocation.longitude);
                loc.setLatitude(subLocation.latitude);
                distance = location.distanceTo(loc);
                if(distance < min)
                {
                    min = distance;
                    minIndex = subLocation.id;
                }
                subLocations.clear();
            }
            offset+=5;
            subLocations.addAll(stationDAO.subStationsLocations(offset));
        }
   */


    private byte getMinIndex(Location location) {
      StationLocation stationLocation;
      Location loc = new Location("");
      float distance;
      float min = Float.MAX_VALUE;
      byte minIndex = 0, offset = 0;
      /*
      for (byte i = 0; i < allStations.size(); i++) {
          stationLocation = stationDAO.stationLocation(allStations.get(i));
          loc.setLongitude(stationLocation.longitude);
          loc.setLatitude(stationLocation.latitude);
          distance = location.distanceTo(loc);
          if(distance < min)
          {
              min = distance;
              minIndex = i;
          }
          stationLocation = null;
      }
       */

        List<StationLocation> subLocations = stationDAO.subStationsLocations(offset);

        while (subLocations.size() != 0)
        {

            for (StationLocation subLocation : subLocations) {
                loc.setLongitude(subLocation.longitude);
                loc.setLatitude(subLocation.latitude);
                distance = location.distanceTo(loc);
                if(distance < min)
                {
                    min = distance;
                    minIndex = (byte) (subLocation.id-1);
                }

            }
            subLocations.clear();
            offset+=5;
            subLocations.addAll(stationDAO.subStationsLocations(offset));
        }
      loc = null;
      location = null;
      subLocations = null;
      return minIndex;
  }
    public void showAddress(View view) {

        if(isChecked)
        {
            isChecked = false;

            //show
            addressEditText.setVisibility(View.VISIBLE);
            getEndStationImageView.setVisibility(View.VISIBLE);
            endStationSpinner.setEnabled(false);
        }
        else
        {
            isChecked = true;

            //hide
            addressEditText.setVisibility(View.INVISIBLE);
            getEndStationImageView.setVisibility(View.INVISIBLE);
            endStationSpinner.setEnabled(true);
        }


    }
    void metroHelper(String start, String end) {
        clean();
        byte i = 0;

        // find stations indexes
        for (List<String> line : stations) {

            if(!fStart)
            {
                fStart = line.contains(start);
                stationsIndexes[i][0] = (byte) line.indexOf(start);
            }
            if(!fEnd)
            {
                fEnd = line.contains(end);
                stationsIndexes[i][1] = (byte) line.indexOf(end);
            }


            if( fStart && fEnd)
            {
                if(!endFires) {
                    if(stationsIndexes[i][1] - stationsIndexes[i][0] > 0 )
                    {

                        rout.addAll(line.subList(stationsIndexes[i][0], stationsIndexes[i][1]+1));
                        direction.add("- From : " +line.get( stationsIndexes[i][0]) + " To : "  + line.get(stationsIndexes[i][1]) + " -> Direction :  " +line.get(line.size()-1));
                    }else
                    {
                        tmp.addAll(line.subList(stationsIndexes[i][1], stationsIndexes[i][0]+1));
                        Collections.reverse(tmp);
                        rout.addAll(tmp);
                        tmp.clear();
                        direction.add("- From : " + line.get(stationsIndexes[i][1]) + " To : "  + line.get(stationsIndexes[i][0]) + " -> Direction :  " +line.get(0));


                    }
                }
                else {
                    if(stationsIndexes[i][1] - stationsIndexes[i][0] > 0 ) {

                        tmp.addAll(line.subList(stationsIndexes[i][0], stationsIndexes[i][1]+1));
                        Collections.reverse(tmp);
                        rout.addAll(tmp);
                        tmp.clear();
                        direction.add("- From : " +line.get(stationsIndexes[i][0]) + " To : "  +line.get( stationsIndexes[i][1]) + " -> Direction :  " +line.get(line.size()-1));

                    }
                    else {
                        rout.addAll(line.subList(stationsIndexes[i][1], stationsIndexes[i][0]+1));
                        direction.add("- From : " + line.get(stationsIndexes[i][1]) + " To : "  + line.get(stationsIndexes[i][0]) + " -> Direction :  " +line.get(0));

                    }
                }

                break;

            }

            if( !fEnd && fStart  )
            {

                if(i == 0)
                {
                    stationsIndexes[0][1]  = (byte) line.indexOf("Al Shohadaa");
                    stationsIndexes[1][0]  = (byte) stations.get(1).indexOf("Al Shohadaa");

                    convert.add("Al Shohadaa");
                } else if(i == 1)
                {
                    stationsIndexes[1][1]  = (byte) line.indexOf("Ataba");
                    stationsIndexes[2][0]  = (byte) stations.get(2).indexOf("Ataba");
                    convert.add("Ataba");
                }else {
                    break;
                }


                if(stationsIndexes[i][1] - stationsIndexes[i][0] > 0 )
                {
                    rout.addAll(line.subList(stationsIndexes[i][0], stationsIndexes[i][1]+1));
                    direction.add("- From : " + line.get(stationsIndexes[i][0]) + "  To : "  + line.get(stationsIndexes[i][1]) + " -> Direction :  " +line.get(line.size()-1));

                }else
                {

                    tmp.addAll(line.subList(stationsIndexes[i][1], stationsIndexes[i][0]+1));
                    Collections.reverse(tmp);
                    rout.addAll(tmp);
                    tmp.clear();
                    direction.add("- From : " + line.get(stationsIndexes[i][1]) + "  To : "  + line.get(stationsIndexes[i][0]) + " -> Direction :  " +line.get(0));


                }


            }

            if( !fStart && fEnd)
            {
                endFires = true;

                if(i == 0)
                {
                    stationsIndexes[0][0]  = (byte) line.indexOf("Al Shohadaa");
                    stationsIndexes[1][1]  = (byte) stations.get(1).indexOf("Al Shohadaa");
                    convert.add("Al Shohadaa");
                }else if(i == 1)
                {
                    stationsIndexes[1][0]  = (byte) line.indexOf("Ataba");
                    stationsIndexes[2][1]  = (byte) stations.get(2).indexOf("Ataba");
                    convert.add("Ataba");
                }else {
                    break;
                }



                if(stationsIndexes[i][1] - stationsIndexes[i][0] > 0 )
                {
                    tmp.addAll(line.subList(stationsIndexes[i][0], stationsIndexes[i][1]+1));
                    Collections.reverse(tmp);
                    rout.addAll(tmp);
                    tmp.clear();
                    direction.add("- From : " + line.get(stationsIndexes[i][0]) + "  To : "  + line.get(stationsIndexes[i][1]) + " -> Direction :  " +line.get(line.size()-1));


                }else
                {
                    rout.addAll(line.subList(stationsIndexes[i][1], stationsIndexes[i][0]+1));
                    direction.add("- From : " + line.get(stationsIndexes[i][1]) + "  To : "  + line.get(stationsIndexes[i][0]) + " -> Direction :  " +line.get(0));


                }


            }



            i++;

        }

        if(endFires)
        {

            Collections.reverse(direction);
            Collections.reverse(rout);
            Collections.reverse(convert);
        }


        //ticket Price checker
        if(rout.size() <= 9) ticketPrice = 5;
        else if (rout.size() <= 16) ticketPrice = 7;
        else ticketPrice = 10;


    }
    void  clean(){
        direction.clear();
        convert.clear();
        rout.clear();
        tmp.clear();
        stationsIndexes = null;
        fStart = false;
        fEnd= false;
        endFires = false;
        stationsIndexes = new byte[][]{
                {-2, -2},
                {-2, -2},
                {-2, -2},};

    }
    void nullifyAll() {
        direction = null;
        convert = null ;
        rout = null;
        tmp = null;
        stationsIndexes = null;
    }
    public void showRides(MenuItem item) {
        Intent intent = new Intent(this, RidesActivity.class);
        startActivity(intent);

        // nullify
        intent = null;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 1 means location permission
        if(requestCode == 1)
        {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                try {

                    manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this, null);
                    //real device.... testing -> it works
                    //manager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this, null);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }else {
                Toast.makeText(this, "you have to allow Location permissions", Toast.LENGTH_SHORT).show();
            }
        }

    }




    //unused method but i have to Override it
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {


    }


    public void speak(View view) {
        Intent in = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        in.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        in.putExtra(RecognizerIntent.EXTRA_PROMPT,"text");
        startActivityForResult(in,1);
    }

    ArrayList<String> stringArrayExtra;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK)
        {
            stringArrayExtra = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            notesEditText.setText(stringArrayExtra.get(0));
            stringArrayExtra = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}