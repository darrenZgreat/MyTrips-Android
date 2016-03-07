package com.regis.darren.mytrips;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.regis.darren.mytrips.domain.ActivityItem;
import com.regis.darren.mytrips.domain.Location;
import com.regis.darren.mytrips.domain.Trip;

import java.util.Calendar;

public class LocationActivity extends AppCompatActivity {

    static Location location;
    static String tripStartDate;
    static String tripEndDate;
    private Context context = null;
    private ListView listView = null;
    private ListAdapter adapter = null;

    static boolean settingArrive;
    static Button arriveButton;
    static Button departButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        Intent intent = getIntent();
        EditText cityField = (EditText) findViewById(R.id.city);
        EditText stateCountryField = (EditText) findViewById(R.id.stateCountry);
        arriveButton = (Button) findViewById(R.id.arrive);
        departButton = (Button) findViewById(R.id.depart);

        Trip trip = (Trip) intent.getSerializableExtra("trip");
        tripStartDate = trip.getStartDate();
        tripEndDate = trip.getEndDate();
        int locationIndex = intent.getIntExtra("locationIndex", -1);

        if(locationIndex != -1) {
            location = trip.getLocations().get(locationIndex);
            cityField.setText(location.getCity());
            stateCountryField.setText(location.getStateCountry());
            arriveButton.setText(location.getArrive());
            departButton.setText(location.getDepart());

            initWithActivityItems();
        }
        else
        {
            location = new Location();
        }


    }

    private void initWithActivityItems() {
        context = this;
        listView = (ListView) findViewById(R.id.activitiesListView);

        adapter = new ArrayAdapter<ActivityItem>(context, android.R.layout.simple_list_item_1, location.getActivityItems());
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(context, ActivityItemActivity.class);
                intent.putExtra("location", location);
                intent.putExtra("activityItemIndex", position);
                startActivity(intent);
            }
        });
    }

    public void addActivity(View view) {
        Intent intent = new Intent(context, ActivityItemActivity.class);
        intent.putExtra("location", location);
        startActivity(intent);
    }

    public void selectArrive(View view) {
        settingArrive = true;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void selectDepart(View view) {
        settingArrive = false;
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String[] match, matchOther;
            int month, day, year;
            long cTripStartDateTimeInMillis, cTripEndDateTimeInMillis;
            final Calendar c = Calendar.getInstance();
            final Calendar cTripStartDate = Calendar.getInstance();
            final Calendar cTripEndDate = Calendar.getInstance();

            String[] matchTripStartDate = tripStartDate.split("-");
            cTripStartDate.set(Integer.parseInt(matchTripStartDate[2]), Integer.parseInt(matchTripStartDate[0]) - 1, Integer.parseInt(matchTripStartDate[1]));
            cTripStartDateTimeInMillis = cTripStartDate.getTimeInMillis();

            String[] matchTripEndDate = tripEndDate.split("-");
            cTripEndDate.set(Integer.parseInt(matchTripEndDate[2]), Integer.parseInt(matchTripEndDate[0]) - 1, Integer.parseInt(matchTripEndDate[1]));
            cTripEndDateTimeInMillis = cTripEndDate.getTimeInMillis();

            if (settingArrive) {
                match = location.getArrive().split("-");
                matchOther = location.getDepart().split("-");
            } else {
                match = location.getDepart().split("-");
                matchOther = location.getArrive().split("-");
            }

            if (match.length == 3) {
                month = Integer.parseInt(match[0]) - 1;
                day = Integer.parseInt(match[1]);
                year = Integer.parseInt(match[2]);
            } else {
                month = Integer.parseInt(matchTripStartDate[0]) - 1;
                day = Integer.parseInt(matchTripStartDate[1]);
                year = Integer.parseInt(matchTripStartDate[2]);
            }

            DatePickerDialog datePicker = new DatePickerDialog(getActivity(), android.R.style.Theme_Material_Dialog, this, year, month, day);
            if (matchOther.length == 3) {
                c.set(Integer.parseInt(matchOther[2]), Integer.parseInt(matchOther[0]) - 1, Integer.parseInt(matchOther[1]));
                long cTimeInMillis = c.getTimeInMillis();
                if (settingArrive) {
                    datePicker.getDatePicker().setMinDate(cTripStartDateTimeInMillis);
                    datePicker.getDatePicker().setMaxDate(cTimeInMillis);

                } else {
                    datePicker.getDatePicker().setMinDate(cTimeInMillis);
                    datePicker.getDatePicker().setMaxDate(cTripEndDateTimeInMillis);
                }
            }
            else {
                datePicker.getDatePicker().setMinDate(cTripStartDateTimeInMillis);
                datePicker.getDatePicker().setMaxDate(cTripEndDateTimeInMillis);
            }
            return datePicker;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            String date = (month + 1) + "-" + day + "-" + year;
            if (settingArrive) {
                location.setArrive(date);
                arriveButton.setText(date);
            } else {
                location.setDepart(date);
                departButton.setText(date);
            }

        }
    }

}
