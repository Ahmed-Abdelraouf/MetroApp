package com.example.metroapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class RoutActivity extends AppCompatActivity {
    StationDAO stationDAO;
    StationLocation startStationLocation;
    ArrayList<String> rout = new ArrayList<>();
    ArrayList<String> direction = new ArrayList<>();
    ArrayList<String> convert = new ArrayList<>();
    StringBuilder tmp = new StringBuilder();
    float latitude,longitude;

    TextView titleText,convertStationText;
    ListView directionListView, routListView;
    ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_rout);

        //views
        titleText = findViewById(R.id.titleText);
        convertStationText = findViewById(R.id.convertStationText);
        directionListView = findViewById(R.id.directionListView);
        routListView = findViewById(R.id.routListView);


        //setting
        stationDAO = MetroDatabase.getInstance(this).stationDAO();
        Intent intent = getIntent();
        convert = intent.getStringArrayListExtra("convert");
        rout = intent.getStringArrayListExtra("finalRout");
        direction = intent.getStringArrayListExtra("direction");
        String start =  intent.getStringExtra("start");
        String end =  intent.getStringExtra("end");



        startStationLocation =  stationDAO.stationLocation(start);
        latitude = (float) startStationLocation.latitude;
        longitude = (float) startStationLocation.longitude;

        tmp = new StringBuilder("Start Station : "+start + "\n" + "End Station : " + end+ "\n" +
                "Price :" + intent.getStringExtra("price") + "LE"+ "\n" +
                "Estimated time :" + rout.size()*2 );
        titleText.setText(tmp);


        tmp.setLength(0);
        for (byte i = 0; i < convert.size(); i++) {
            tmp.append(convert.get(i));
            if(i == convert.size()-1)
            {
                tmp.append(".");
                break;
            }
            tmp.append("-> ");
        }
        convertStationText.setText(tmp);


        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,direction);
        directionListView.setAdapter(adapter);
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,rout);
        routListView.setAdapter(adapter);
    }

    public void navigate(View view) {

        Intent in = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="+latitude +","+ longitude));

        startActivity(in);
    }
}