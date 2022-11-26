package com.example.doetest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import com.r0adkll.slidr.Slidr;
import androidx.fragment.app.FragmentTransaction;
import com.r0adkll.slidr.model.SlidrInterface;


public class MainActivity extends AppCompatActivity {
    Button ButtonOff;
    Button ButtonOn;
    Button ButtonShowDevices;
    Button send;
    Button btnConnect;
    EditText writeMsg;

    ConnectEachOther mConnectEachOther;
    BluetoothAdapter BTadapter = BluetoothAdapter.getDefaultAdapter();

    Intent BtEnableIntent;
    int BTrequestCode;
    int btArrLength;
    ListView deviceListLayout;
    TextView StatusTxtView;
    TextView msg_box;
    static TextView ChatBox;
    TextView status;
    ArrayList<String> arrayDeviceList = new ArrayList<String>();
    ArrayAdapter<String> Listdeviceall;
    BluetoothDevice btArrTotal[];
    BluetoothDevice btArrTotal1[];
    BluetoothDevice thisDeviceConnect;

    int checking;
    BluetoothDevice btArrDevice[];
    BluetoothDevice btArrExtraDevice[];
    int x;
    int i;
    int t;
    //server use
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String APP_NAME = "DOETest";
    private static final UUID MY_UUID = UUID.fromString("3bab976d-b40a-4b6e-9a1c-3a596e8d94e0");


    //private BroadcastReceiver receiveSignalss;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButtonOff = findViewById(R.id.BluetoothOff);
        ButtonOn = findViewById(R.id.BluetoothOn);
        msg_box=findViewById(R.id.msg_box);
        btnConnect = findViewById(R.id.btnConnect);
        ButtonShowDevices = findViewById(R.id.ShowDevices);
        BtEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        deviceListLayout = findViewById(R.id.deviceListLayout);
        send = findViewById(R.id.send);
        writeMsg = findViewById(R.id.writeMsg);
        ChatBox = findViewById(R.id.ChatBox);
        BTrequestCode = 1;
        mConnectEachOther = new ConnectEachOther(MainActivity.this);
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        //SlidrInterface slidr = Slidr.attach(this);
        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
        //final Set<BluetoothDevice> BT = BTadapter.getBondedDevices();
        //btArr=new BluetoothDevice[BT.size()];

        ButtonOff.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View vadf) {
                if (BTadapter.isEnabled()) {
                    BTadapter.disable();
                }
            }
        });
        ButtonOn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View vadf) {
                if (BTadapter == null) {
                    System.out.println("Bluetooth doesn't support on this device");
                } else {
                    if (!BTadapter.isEnabled()) {
                        startActivityForResult(BtEnableIntent, BTrequestCode);
                    }
                }
            }
        });
        ButtonShowDevices.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View vadf) {
                Set<BluetoothDevice> BT = BTadapter.getBondedDevices();
                //System.out.println("blutoothdevice lengths, btArr:  " + BT.size());
                btArrDevice=new BluetoothDevice[BT.size()];
                int i = 0;
                //System.out.println("what is i: ?????????????????????????????????????????????????" +i);
                //System.out.println(BT.toArray(new BluetoothDevice[0]).toString());
                //String[] DeviceStrings= new String[BT.size()];

                //Listdeviceall = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayDeviceList);

                if (BT.size() > 0) {
                    for (BluetoothDevice eachDevice : BT) {
                        btArrDevice[i]=eachDevice;
                        i++;
                        if (!arrayDeviceList.contains(eachDevice.getName())) {
                            arrayDeviceList.add(eachDevice.getName());
                        }
                        //deviceListLayout.setAdapter(Listdeviceall);
                    }

                }
                BTadapter.startDiscovery();
                IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiveSignals, intentFilter);
                Listdeviceall = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayDeviceList);
                deviceListLayout.setAdapter(Listdeviceall);
                
            }

        });

        deviceListLayout.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (btArrExtraDevice==null){
                    thisDeviceConnect = btArrDevice[position];
                    //msg_box.setText(btArrDevice[position].getName());
                } else if(btArrTotal!=null){
                    thisDeviceConnect = btArrTotal[position];
                    //msg_box.setText(btArrTotal[position].getName());
                }
                if (thisDeviceConnect!=null){
                    msg_box.setText(thisDeviceConnect.getName());
                    System.out.println("this device is: " + thisDeviceConnect.getName());
                    System.out.println("UUID is: " + thisDeviceConnect.getUuids());
                }
            }
        });
        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                byte[] byteArray = writeMsg.getText().toString().getBytes(Charset.defaultCharset());
                //System.out.println("Byte value: " + byteArray + " Actual string: " + writeMsg.getText().toString());
                //mConnectEachOther.startSending(thisDeviceConnect, MY_UUID); //try doing it again same as how we do for connect thread and connect with connectedthread
                if (mConnectEachOther != null) {
                    mConnectEachOther.writes(byteArray);
                }
                if (mConnectEachOther.ChatBoxMSG != null) {
                    System.out.println("ChatBox is: " + " " + mConnectEachOther.ChatBoxMSG);
                    ChatBox.setText(mConnectEachOther.ChatBoxMSG);
                }
            }
        });
        btnConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View vadf) {
                if (thisDeviceConnect!=null) {
                    mConnectEachOther.startClient(thisDeviceConnect, MY_UUID);
                }
                //thisDeviceConnect.setPin()
            }
        });



    }

    BroadcastReceiver receiveSignals = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {

            //Intent sig gives option such as sig.getData() <-- might be something that I am looking for
            //StatusTxtView.setText("Searching for Nearby Devices");
            String intentString = intent.getAction();
            StatusTxtView = findViewById(R.id.StatusTxtView);
            StatusTxtView.setText("Searching for near by devices");
            if (BluetoothDevice.ACTION_FOUND.equals(intentString)) {
                BluetoothDevice extraDeviceList = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //System.out.println(extraDeviceList.getName() + " receiver with the onReceiver000000000000000000");
                if (extraDeviceList.getName() != null && !arrayDeviceList.contains(extraDeviceList.getName())) {
                    //i=x;
                    //i=0;
                    StatusTxtView.setText("Extra Devices found!");
                    if (i == 0){
                        btArrExtraDevice = new BluetoothDevice[100];
                        btArrExtraDevice[i] = extraDeviceList;
                        //System.out.println("btArrExtraDevice[0]: " + btArrExtraDevice[i].getName());
                    } else if (!Arrays.asList(btArrExtraDevice).contains(extraDeviceList)){
                        btArrExtraDevice[i] = extraDeviceList;
                    }
                    btArrTotal = add(btArrDevice, btArrExtraDevice);
                    System.out.println(extraDeviceList.getName());
                    arrayDeviceList.add(extraDeviceList.getName());
                    Listdeviceall.notifyDataSetChanged();
                    i++;
                }

            }
        }

    };
    public BluetoothDevice[] add(BluetoothDevice[] first, BluetoothDevice[] second){
        btArrTotal1 = new BluetoothDevice[first.length + second.length];
        int x=0;
        //System.out.println("LENGHTTTTT: " + first.length + second.length);
        for (int i=0; i<btArrTotal1.length;){
            if (i<first.length){
                //System.out.println("first[i]: "+ first[i]);
                btArrTotal1[i] = first[i];
                if (first[i] !=null){
                    //System.out.print("first[i] : " + first[i].getName());
                }
            }else if(second[x]!=null){
                //System.out.println("i: " + i + "x: " + x + "total length: " + btArrTotal1.length);
                btArrTotal1[i] = second[x];
                //System.out.print("second[i] : " + second[x].getName());
                x++;
            }
            i++;
        }
        //System.out.println("btArrTotal1 length: " + (first.length + second.length));
        return btArrTotal1;
        //return btArrTotal[first.length + second.length];
    }
    float x1,y1,x2,y2;

    public boolean onTouchEvent(MotionEvent touchEvent){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if(x1 < x2){
                    //transaction.replace(R.id.constraintlayout,Fragment1);
                    //transaction.commit();
                    System.out.println("Print x1: " +x1);
                    System.out.println("print x2: " + x2);
                    System.out.println("print y1: " + y1);
                    System.out.println("print y2: " + y2);
                    Intent i = new Intent(MainActivity.this, SubPage1.class);
                    startActivity(i);
                }//else if(x1> x2){
                    //transaction.replace(R.layout.fragment_sub_page2, Fragment2);
                    //transaction.addToBackStack(null);
                    //Intent i = new Intent(MainActivity.this, SubPage2.class);
                    //startActivity(i);
               // }
                break;
        }
        return false;
    }


}