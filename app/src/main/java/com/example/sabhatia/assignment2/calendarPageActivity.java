package com.example.sabhatia.assignment2;



import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


public class calendarPageActivity extends AppCompatActivity {


    private EditText title;
    private EditText desc, location, loc2, time;
    private Button lookup, add;
    public static List<String> nameOfEvent = new ArrayList<String>();
    public static List<String> startDates = new ArrayList<String>();
    public static List<String> endDates = new ArrayList<String>();
    public static List<String> descriptions = new ArrayList<String>();
    public static ArrayList<String> locations = new ArrayList<String>();
    private ListView results;
    private ArrayList<String> result = new ArrayList<String>();
    private ArrayList<String> resultids = new ArrayList<String>();
    private ArrayList<String> ids = new ArrayList<String>();
    private ArrayAdapter adapter;
    private static final int PERMISSION_REQUEST_CODE = 1;

    private final Calendar myCalendar = Calendar.getInstance();
    private String event_Location, event_time;
    private static Tracker tracker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_calendar_page );

        MyApp myApp = (MyApp)getApplication();
        tracker = myApp.getDefaultTracker();
        Log.i("SCREEN NAME", "Setting screen name: " + this.getLocalClassName());
        tracker.setScreenName("Activity~" + this.getLocalClassName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());

        title = (EditText) findViewById( R.id.editText );
        desc = (EditText) findViewById( R.id.editText2 );
        location = (EditText) findViewById( R.id.editText4 );
        lookup = (Button) findViewById( R.id.button2 );
        add = (Button) findViewById( R.id.button );
        loc2 = (EditText) findViewById( R.id.editText3 );
        time = (EditText) findViewById( R.id.editText6 );

        adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, result );

        results =  findViewById( R.id.listView );
        results.setAdapter( adapter );

        lookup.setOnClickListener( new View.OnClickListener() {
            public void onClick(View v) {

                event_Location = loc2.getText().toString();

                if (event_Location.isEmpty()) {
                    Toast.makeText( calendarPageActivity.this, "Please enter a location", Toast.LENGTH_SHORT ).show();

                } else {
                    lookupEvent( v );
                    HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
                    eventBuilder.setAction("Calendar - location lookup ").setCategory("Click");
                    MyApp.tracker().send(eventBuilder.build());
                }

            }
        } );


        results.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {


                HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
                eventBuilder.setAction("calender- Set date+time").setCategory("Click");
                MyApp.tracker().send(eventBuilder.build());

                String entry = parent.getItemAtPosition( position ).toString();

                Intent intent = new Intent( Intent.ACTION_VIEW );


                int index = result.indexOf( entry );
                //ids.get( index );

                intent.setData( Uri.parse( "content://com.android.calendar/events/" + resultids.get( index ) ) );

                //intent.setData(Uri.parse("content://calendar/events/" + String.valueOf(calendarEventID)));
                intent.setFlags( Intent.FLAG_ACTIVITY_NEW_TASK );
                startActivity( intent );
            }
        } );



/*

        time.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;

                mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(hourOfDay, minute);
                        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
                        String timeString = format.format(calendar.getTime());
                        r_time.setText((timeString));
                    }
                });



                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });*/
    }

    private void updateLabel() {
        String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat( myFormat, Locale.US );

        time.setText( sdf.format( myCalendar.getTime() ) );
    }

    public void addEvent(View view) {


        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction("calender- add Event").setCategory("Click");
        MyApp.tracker().send(eventBuilder.build());


        Calendar calendar = Calendar.getInstance();

        Intent intent = new Intent( Intent.ACTION_INSERT )
                .setData( CalendarContract.Events.CONTENT_URI )
                .setType( "vnd.android.cursor.item/event" )
                .putExtra( CalendarContract.Events.TITLE, title.getText().toString() )
                .putExtra( CalendarContract.Events.DESCRIPTION, desc.getText().toString() )
                .putExtra( CalendarContract.Events.EVENT_LOCATION, location.getText().toString() )
                .putExtra( CalendarContract.Events.RRULE, "FREQ=WEEKLY;BYDAY=Tu;UNTIL=20181101" )
                .putExtra( CalendarContract.Events.DTSTART, calendar.getTimeInMillis() )
                .putExtra( CalendarContract.EXTRA_EVENT_ALL_DAY, true )
                .putExtra( CalendarContract.Events.HAS_ALARM, 1 )
                .putExtra( CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY );
        startActivity( intent );


    }

    public void lookupEvent(View view) {


        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction("calender- Lookup event ").setCategory("Click");
        MyApp.tracker().send(eventBuilder.build());


        adapter.clear();
        resultids.clear();
        result.clear();

        //must have the location and time / date

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission( this,
                Manifest.permission.READ_CALENDAR )
                != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText( this, "inside if ", Toast.LENGTH_SHORT ).show();
            ActivityCompat.requestPermissions( this,
                    new String[]{Manifest.permission.READ_CALENDAR},
                    PERMISSION_REQUEST_CODE );
        }


        // Permission has already been granted

        String projection[] = {"_id", "calendar_displayName"};
        Uri calendars;

        ContentResolver contentResolver = getApplicationContext().getContentResolver();
        Cursor managedCursor = contentResolver.query( Uri.parse( "content://com.android.calendar/events" ),
                new String[]{"_id", "title", "description",
                        "dtstart", "dtend", "eventLocation"}, null, null, null, null );


        managedCursor.moveToFirst();
        // fetching calendars name
        String CNames[] = new String[managedCursor.getCount()];

        // fetching calendars id
        nameOfEvent.clear();
        startDates.clear();
        endDates.clear();
        descriptions.clear();
        locations.clear();

        //debug
        //String date = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
        // String date = "04/08/2019";
        // Set<Integer> indices = new HashSet<Integer>();


        for (int i = 0; i < CNames.length; i++) {


            ids.add( managedCursor.getString( 0 ) );
            nameOfEvent.add( managedCursor.getString( 1 ) );
            String sDate = getDate( Long.parseLong( managedCursor.getString( 3 ) ) );
            startDates.add( sDate );
            //System.out.println(">>>>>>>>>"+sDate + " " + sDate.indexOf("04/08/2019"));
            //  endDates.add(getDate(Long.parseLong(managedCursor.getString(4))));
            descriptions.add( managedCursor.getString( 2 ) );
            locations.add( managedCursor.getString( 5 ) );

            if (managedCursor.getString( 5 ).contains( event_Location )) {
                //System.out.println("######" + managedCursor.getString( 0 ) );
                resultids.add(  managedCursor.getString( 0 ) );
                result.add(managedCursor.getString( 5 )  );
            }

            //System.out.println( locations.get( i ) );
            CNames[i] = managedCursor.getString( 1 );
            managedCursor.moveToNext();
        }
        adapter.notifyDataSetChanged();


        // System.out.println(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));

        //System.out.println("you said " + event_Location);


       /*for (int i = 0; i < locations.size(); i++) {

            //System.out.println(s);
            //System.out.println(s.contains( event_Location ));
            if (locations.get( i ).contains( event_Location )) {
                // System.out.println( "we have a location!##@@##!" );
                result.add( locations.get( i ) );
                //ids.add( Integer.toString( i ) );
                adapter.notifyDataSetChanged();
            }
        }*/


        if (result.size() == 0) {
            Toast.makeText( this, "No event with this location found", Toast.LENGTH_SHORT ).show();
        } else {

            //some results were found - display results
        }


    }


    public static String getDate(long milliSeconds) {
        SimpleDateFormat formatter = new SimpleDateFormat(
                "dd/MM/yyyy hh:mm:ss a" );
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( milliSeconds );
        return formatter.format( calendar.getTime() );
    }

    public void showDate(View view) {

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                myCalendar.set( Calendar.YEAR, year );
                myCalendar.set( Calendar.MONTH, month );
                myCalendar.set( Calendar.DAY_OF_MONTH, dayOfMonth );
                String myFormat = "dd MMMM yyyy"; // your format
                SimpleDateFormat sdf = new SimpleDateFormat( myFormat, Locale.getDefault() );
                time.setText( sdf.format( myCalendar.getTime() ) );

            }
        };


        new DatePickerDialog( this, date, myCalendar.get( Calendar.YEAR ), myCalendar.get( Calendar.MONTH ), myCalendar.get( Calendar.YEAR ) ).show();

    }

}

