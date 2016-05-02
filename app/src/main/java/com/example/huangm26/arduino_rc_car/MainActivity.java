package com.example.huangm26.arduino_rc_car;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    BluetoothAdapter btAdapter = null;
    BluetoothDevice btDevice = null;
    BluetoothSocket btSocket = null;
    OutputStream btOutputStream = null;
    InputStream btInputStream = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialize the bluetooth adapter
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //get instances of the buttons and set on click listener
        Button upButton = (Button)findViewById(R.id.upButton);
        Button downButton = (Button) findViewById(R.id.downButton);
        Button leftButton = (Button) findViewById(R.id.leftButton);
        Button rightButton = (Button) findViewById(R.id.rightButton);
        Button exitButton = (Button) findViewById(R.id.exitButton);
        upButton.setOnClickListener(this);
        downButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        exitButton.setOnClickListener(this);

        //set up the bluetooth and open the connection
        setupConnection();
        try {
            openConnection();
        } catch (IOException e) {
            e.printStackTrace();
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

    //set up the bluetooth connection with arduino
    private void setupConnection(){
        //if the bluetooth is not available, finish the program
        if(!checkBluetooth())
            finish();

        if(!btAdapter.isEnabled()){
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();
        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                Log.d("Device name:", device.getName());
                //potentially check bluetooth device name and get the matching device, but assume one device for now
                btDevice = device;
            }
        }
    }

    //open connection for the device, setting up input and output stream.
    private void openConnection() throws IOException {
        if(btDevice == null)
            return;
        btSocket = btDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
        btSocket.connect();
        btInputStream = btSocket.getInputStream();
        btOutputStream = btSocket.getOutputStream();
    }

    //check if the android device is bluetooth available
    private boolean checkBluetooth()
    {
        if(btAdapter == null) {
            Toast.makeText(this, "Your device doesn't support bluetooth", Toast.LENGTH_SHORT).show();
            return false;
        }
        else {
            return true;
        }
    }

    //send the control through the output stream
    private void sendControl(String controlBit){
        if(btOutputStream != null) {
            try {
                btOutputStream.write(controlBit.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //close the bluetooth socket connection
    private void closeConnection() {
        if (btSocket == null)
            return;
        try {
            btOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            btInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.upButton:
                sendControl("1");
                break;
            case R.id.downButton:
                sendControl("2");
                break;
            case R.id.leftButton:
                sendControl("3");
                break;
            case R.id.rightButton:
                sendControl("4");
                break;
            case R.id.exitButton:
                closeConnection();
                finish();
            default:
                break;
        }
    }
}
