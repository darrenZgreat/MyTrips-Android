package com.regis.darren.mytrips;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.regis.darren.mytrips.domain.ActivityItem;
import com.regis.darren.mytrips.domain.Location;
import com.regis.darren.mytrips.domain.Trip;
import com.regis.darren.mytrips.service.ITripSvc;
import com.regis.darren.mytrips.service.TripSvcSIOImpl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ActivityItemActivity extends AppCompatActivity {

    private int tripIndex;
    private int locationIndex;
    private int activityItemIndex;
    private List<Trip> trips = new ArrayList<Trip>();
    static Trip trip;
    private Location location;
    static ActivityItem activityItem;
    static String locationArrive;
    static String locationDepart;
    static Button dateButton;
    static Button timeButton;
    private EditText activityItemNameField;
    private Button dynamicButton1;
    private Button dynamicButton2;
    private EditText descriptionField;

    private Boolean addingNew = false;
    private boolean readyToDelete = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_item);

        try {
            ITripSvc tripSvc = TripSvcSIOImpl.getInstance(this);
            trips = tripSvc.retrieveAll();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        Intent intent = getIntent();
        activityItemNameField = (EditText) findViewById(R.id.activityName);
        dateButton = (Button) findViewById(R.id.date);
        timeButton = (Button) findViewById(R.id.time);
        descriptionField = (EditText) findViewById(R.id.description);
        dynamicButton1 = (Button) findViewById(R.id.activityItemDynamicButton1);
        dynamicButton2 = (Button) findViewById(R.id.activityItemDynamicButton2);

        tripIndex = intent.getIntExtra("tripIndex", -1);
        trip = trips.get(tripIndex);
        locationIndex =  intent.getIntExtra("locationIndex", -1);
        location = trip.getLocations().get(locationIndex);
        locationArrive = location.getArrive();
        locationDepart = location.getDepart();
        activityItemIndex = intent.getIntExtra("activityItemIndex", -1);

        if(activityItemIndex != -1) {
            activityItem = location.getActivityItems().get(activityItemIndex);
            activityItemNameField.setText(activityItem.getActivityName());
            dateButton.setText(activityItem.getDate());
            timeButton.setText(activityItem.getTime());
            descriptionField.setText(activityItem.getDescription());
            dynamicButton1.setText("Save");
            dynamicButton2.setText("Delete");
        }
        else
        {
            activityItem = new ActivityItem();
            dynamicButton1.setText("Add");
            dynamicButton2.setText("Cancel");
            addingNew = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        readyToDelete = false;
        dynamicButton2.setText("Delete");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_itinerary) {
            if(activityItem.equals(new ActivityItem(activityItemNameField.getText().toString(), dateButton.getText().toString(), timeButton.getText().toString(), descriptionField.getText().toString()))) {
                Intent intent = new Intent(this, ItineraryActivity.class);
                intent.putExtra("tripIndex", tripIndex);
                startActivity(intent);
                return true;
            }
            else {
                Toast.makeText(this, "Please SAVE the activity first", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void onActivityItemDynamic1(View view) {

        try {
            ITripSvc tripSvc = TripSvcSIOImpl.getInstance(this);

            String name = activityItemNameField.getText().toString();
            String date = dateButton.getText().toString();
            String time = timeButton.getText().toString();
            String description = descriptionField.getText().toString();

            if(addingNew) {
                if(name.compareTo("Activity Name") == 0) {
                    Toast.makeText(this, "Please provide a Activity Name", Toast.LENGTH_SHORT).show();
                }
                else if(date.compareTo("Date") == 0) {
                    Toast.makeText(this, "Please provide a Date", Toast.LENGTH_SHORT).show();
                }
                else if(time.compareTo("Time") == 0) {
                    Toast.makeText(this, "Please provide a Time", Toast.LENGTH_SHORT).show();
                }
                else {
                    activityItem.setActivityName(name);
                    activityItem.setDate(date);
                    activityItem.setTime(time);
                    activityItem.setDescription(description);
                    location.getActivityItems().add(activityItem);
                    tripSvc.update(trip, getIntent().getIntExtra("tripIndex", -1));
                    finish();
                }
            }
            else {
                activityItem.setActivityName(name);
                activityItem.setDate(date);
                activityItem.setTime(time);
                activityItem.setDescription(description);
                tripSvc.update(trip, getIntent().getIntExtra("tripIndex", -1));
                finish();
            }
        }
        catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityItemDynamic2(View view) {
        if(addingNew) {
            finish();
        }
        else {
            if(readyToDelete) {
                try {
                    ITripSvc tripSvc = TripSvcSIOImpl.getInstance(this);
                    tripSvc.delete(trip, tripIndex, locationIndex, activityItemIndex);
                    finish();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            else {
                dynamicButton2.setText("Confirm Delete");
                readyToDelete = true;
            }
        }
    }

    public void selectDate(View view) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String[] match;
            int month, day, year;
            long cLocationArriveTimeInMillis, cLocationDepartTimeInMillis;
            final Calendar c = Calendar.getInstance();
            final Calendar cLocationArrive = Calendar.getInstance();
            final Calendar cLocationDepart = Calendar.getInstance();

            String[] matchLocationArrive = locationArrive.split("-");
            cLocationArrive.set(Integer.parseInt(matchLocationArrive[2]), Integer.parseInt(matchLocationArrive[0]) - 1, Integer.parseInt(matchLocationArrive[1]));
            cLocationArriveTimeInMillis = cLocationArrive.getTimeInMillis();

            String[] matchLocationDepart = locationDepart.split("-");
            cLocationDepart.set(Integer.parseInt(matchLocationDepart[2]), Integer.parseInt(matchLocationDepart[0]) - 1, Integer.parseInt(matchLocationDepart[1]));
            cLocationDepartTimeInMillis = cLocationDepart.getTimeInMillis();

            match = activityItem.getDate().split("-");

            if (match.length == 3) {
                month = Integer.parseInt(match[0]) - 1;
                day = Integer.parseInt(match[1]);
                year = Integer.parseInt(match[2]);
            } else {
                month = Integer.parseInt(matchLocationArrive[0]) - 1;
                day = Integer.parseInt(matchLocationArrive[1]);
                year = Integer.parseInt(matchLocationArrive[2]);
            }

            DatePickerDialog datePicker = new DatePickerDialog(getActivity(), android.R.style.Theme_Material_Dialog, this, year, month, day);
            datePicker.getDatePicker().setMinDate(cLocationArriveTimeInMillis);
            datePicker.getDatePicker().setMaxDate(cLocationDepartTimeInMillis);
            return datePicker;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            String date = (month + 1) + "-" + day + "-" + year;
            dateButton.setText(date);
        }
    }

    public void selectTime(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String[] match;
            int hour, minute;
            final Calendar c = Calendar.getInstance();

            match = activityItem.getTime().split(" |:");

            if (match.length == 3) {
                hour = Integer.parseInt(match[0]);
                minute = Integer.parseInt(match[1]);
                String ampm = match[2].toUpperCase();
                if(ampm.compareTo("AM")==0 && hour==12) {
                    hour=0;
                }
                else if(ampm.compareTo("PM")==0 && hour!=12) {
                    hour+=12;
                }
            }
            else {
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            }

            return new TimePickerDialog(getActivity(), android.R.style.Theme_Material_Dialog, this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

            String minuteString;
            String time;

            if(minute<10) {
                minuteString = "0"+minute;
            }
            else {
                minuteString = ""+minute;
            }

            if(hourOfDay==0) {
                time = "12:" + minuteString + " AM";
            }
            else if(hourOfDay<12) {
                time = hourOfDay + ":" + minuteString + " AM";
            }
            else if(hourOfDay==12) {
                time = hourOfDay + ":" + minuteString + " PM";
            }
            else {
                time = "" + (hourOfDay-12) + ":" + minuteString + " PM";
            }
            timeButton.setText(time);
        }
    }

}
