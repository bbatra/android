package com.example.pranav.vibeblue2;

        import android.Manifest;
        import android.app.Activity;
        import android.bluetooth.BluetoothAdapter;
        import android.bluetooth.BluetoothDevice;

        import android.bluetooth.BluetoothSocket;
        import android.content.BroadcastReceiver;
        import android.content.Context;
        import android.content.Intent;
        import android.content.IntentFilter;

        import android.content.pm.PackageManager;
        import android.graphics.Color;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.net.wifi.ScanResult;
        import android.net.wifi.WifiManager;

        import android.os.Bundle;
        import android.view.Menu;
        import android.view.MenuItem;
        import android.view.View;

        import android.widget.ArrayAdapter;
        import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
        import android.widget.TextView;
        import android.widget.Toast;

        import java.io.IOException;
        import java.io.InputStream;
        import java.io.OutputStream;
        import java.util.ArrayList;
        import java.util.Iterator;
        import java.util.List;
        import java.util.Set;
        import java.util.UUID;

public class MainActivity extends Activity  {
    Button b1,b2,b3;
    public OutputStream output;
    private BluetoothAdapter BA;
    private Set<BluetoothDevice>pairedDevices;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private long maxVibeDistance=5;//in meters
    private long midVibeDistance=10;
    private long minVibeDistance=20;
    private ArrayList<Direction> directionList;
    private static String RIGHT="right";
    private static String LEFT="left";
    private int directionCount=0;
    public ArrayList<String> GPSList;
    public ArrayAdapter GPSadapter;
    ListView lv;
    ListView GPSView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        b1 = (Button) findViewById(R.id.button);
        b2 = (Button) findViewById(R.id.button2);
        b3 = (Button) findViewById(R.id.button3);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        BA = BluetoothAdapter.getDefaultAdapter();
        lv = (ListView) findViewById(R.id.listView);

        //setting up GPS Live Updating
        GPSView=(ListView) findViewById(R.id.listView2);
        GPSList=new ArrayList<String>();
        //GPSList.add("The GPS Locations");
        GPSadapter= new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, GPSList);
        GPSView.setAdapter(GPSadapter);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if(location!=null)
                setDirections(location);
                GPSadapter.add("Latitude: " + location.getLatitude() +"\nLongitude: "+ location.getLongitude());
                GPSadapter.notifyDataSetChanged();



            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }

        };


        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }


        //Constructing Direction ArrayList
        directionList = new ArrayList<Direction>();
        ConstructArrayDirectionsExample();
    }

    //distance between coordiantes in meters
    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 6371; // change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist/1000; // output distance, in meters
    }


    //Example Class
    public void ConstructArrayDirectionsExample(){

        Location loc = new Location("");
        String turn;
        loc.setLatitude(32.8864783);
        loc.setLongitude(-117.2413015);
        turn = "straight";
        Direction direction = new Direction(loc, turn);
        directionList.add(direction);

        loc.setLatitude(32.880997);
        loc.setLongitude(-117.2407446);
        turn = "right";
        direction = new Direction(loc, turn);
        directionList.add(direction);


        loc.setLatitude(32.8797576);
        loc.setLongitude(-117.2407483);
        turn = "straight";
        direction = new Direction(loc, turn);
        directionList.add(direction);


        loc.setLatitude(32.8797565);
        loc.setLongitude(-117.2408391);
        turn = "right";
        direction = new Direction(loc, turn);
        directionList.add(direction);

        loc.setLatitude(32.8788594);
        loc.setLongitude(-117.2405978);
        turn = "left";
        direction = new Direction(loc, turn);
        directionList.add(direction);



    }

    //Method called when new GPS Location is discovered,
    //will find if distance is close enough to send new
    //command to Arduino
    public void setDirections(Location loc){
        Direction myDirection=null;
        if(directionCount<=directionList.size()) {
            myDirection = directionList.get(directionCount);
        }
        else return;
        String direction=myDirection.getTurn();
        Location myLoc=myDirection.getMyLoc();
        double d=distance(loc.getLatitude(),loc.getLongitude()
                ,myLoc.getLatitude(),myLoc.getLongitude());
        if(d<=maxVibeDistance){

            if(direction.equals(RIGHT)){
                Right("3");



                directionCount++;
            }
            else if(direction.equals(LEFT)){
                Left("-3");
                directionCount++;
            }
        }
        else if(d<=midVibeDistance){
            if(direction.equals(RIGHT)){
                Right("2");
            }
            else if(direction.equals(LEFT)){
                Left("-2");
            }
        }
        else if(d<=minVibeDistance){
            if(direction.equals(RIGHT)){
                Right("1");
            }
            else if(direction.equals(LEFT)){
                Left("-1");
            }
        }
    }

    //Turning Bluetooth On
    public void on(View v){
        if (!BA.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
            Toast.makeText(getApplicationContext(),"Turned on",Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(),"Already on", Toast.LENGTH_LONG).show();
        }
    }






    //Arduino Left and Right Commands
    public void Right(String Right) {
        if (Right != null) {
            try {
                output.write(Right.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Right = null;

        }
    }
    public void Left(String Left) {
        if (Left != null) {
            try {
                output.write(Left.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Left = null;

        }
    }
    //Code to connect to Arduino
    public void connect(View v){

        Set<BluetoothDevice> pairedDevices= BA.getBondedDevices();
        BluetoothDevice arduino=null;
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device :  pairedDevices)
            {
                if(device.getName().equals("HC-06")) //Note, you will need to change this to match the name of your device
                {
                    arduino = device;
                    break;
                }
            }
        }
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        if(arduino!=null) {
            try {
                BluetoothSocket bluesock = arduino.createRfcommSocketToServiceRecord(uuid);
                bluesock.connect();
                output = bluesock.getOutputStream();
                 InputStream input= bluesock.getInputStream();
                }
                catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}