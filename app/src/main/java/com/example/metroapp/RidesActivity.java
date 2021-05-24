package com.example.metroapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RidesActivity extends AppCompatActivity {
    ListView ridesLv;
    List<Ride> rides;
    RideDAO rideDAO;
    RidesAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        setContentView(R.layout.activity_rides);
        ridesLv = findViewById(R.id.ridesLv);
        rideDAO = MetroDatabase.getInstance(this).rideDAO();

        rides = rideDAO.allRides();
        adapter = new RidesAdapter(this,rides);

        ridesLv.setAdapter(adapter);


    }

    class RidesAdapter extends ArrayAdapter<Ride>{

        public RidesAdapter(@NonNull Context context,List<Ride> rides ) {
            super(context, 0,rides);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            viewHolder holder;

            // we made this if .. to reuse convertView and viewHolder
            if(convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.ride_layout,parent,false);
                 holder = new viewHolder(convertView);
                convertView.setTag(holder);
            }else
            {
                holder = (viewHolder) convertView.getTag();
            }

            // set data into the views
            holder.rl_StartStation.setText(getItem(position).start);
            holder.rl_EndStation.setText(getItem(position).end);
            holder.rl_Notes.setText(getItem(position).note);
            holder.rl_Price.setText(getItem(position).price + " LE");

            // lucky code xD
            holder.rl_button.setOnClickListener(v -> {
                rideDAO.removeRide((byte) getItem(position).id);
                rides.remove(position);
                adapter.notifyDataSetChanged();
                Toast.makeText(RidesActivity.this, "has removed", Toast.LENGTH_SHORT).show();
            });
            return  convertView;
        }

    }


    //we made this class to reuse the TextView id's because findViewById is expensive operation
    class viewHolder {
        TextView rl_StartStation,rl_EndStation,rl_Notes,rl_Price;
        Button rl_button;
        public  viewHolder (View convertView)
        {
             rl_StartStation = convertView.findViewById(R.id.rl_StartStation);
             rl_EndStation = convertView.findViewById(R.id.rl_EndStation);
             rl_Price = convertView.findViewById(R.id.rl_Price);
             rl_Notes = convertView.findViewById(R.id.rl_Notes);
             rl_button = convertView.findViewById(R.id.rl_button);
        }
    }

}