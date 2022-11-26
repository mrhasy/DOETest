package com.example.doetest;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.os.Handler;


public class MainActivity extends AppCompatActivity {
    Button ButtonOff;
    Button ButtonOn;
    Button ButtonShowDevices;
    Button send;
    EditText writeMsg;
    BluetoothAdapter BTadapter = BluetoothAdapter.getDefaultAdapter();
    Intent BtEnableIntent;
    int BTrequestCode;
    ListView deviceListLayout;
    TextView status;
    TextView msg_box;
    SendReceive sendReceive;
    ArrayList<String> arrayDeviceList = new ArrayList<String>();
    ArrayAdapter<String> Listdeviceall;
    BluetoothDevice[] btArr;
    //server use
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final String APP_NAME = "DOEChat";
    private static final UUID MY_UUID = UUID.fromString("3bab976d-b40a-4b6e-9a1c-3a596e8d94e0");


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButtonOff = findViewById(R.id.BluetoothOff);
        ButtonOn = findViewById(R.id.BluetoothOn);
        ButtonShowDevices = findViewById(R.id.ShowDevices);
        BtEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        deviceListLayout = findViewById(R.id.deviceListLayout);
        send = findViewById(R.id.send);
        writeMsg = findViewById(R.id.writeMsg);
        BTrequestCode = 1;
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }


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
                btArr=new BluetoothDevice[BT.size()];
                //String[] DeviceStrings= new String[BT.size()];
                int a = 0;
                //Listdeviceall = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, arrayDeviceList);

                if (BT.size() > 0 && arrayDeviceList.isEmpty()) {
                    for (BluetoothDevice eachDevice : BT) {
                        arrayDeviceList.add(eachDevice.getName());

                        //System.out.println(BluetoothDevice.EXTRA_DEVICE+ " Extra Devices");
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
                ClientClass clientClass = new ClientClass(btArr[position]);
                clientClass.start();

                status.setText("connecting");
            }
        });
        send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String string = String.valueOf(writeMsg.getText());
                sendReceive.write(string.getBytes());

            }
        });

    }


    BroadcastReceiver receiveSignals = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {

            //Intent sig gives option such as sig.getData() <-- might be something that I am looking for
            String intentString = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(intentString)) {
                BluetoothDevice extraDeviceList = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (extraDeviceList.getName() != null && !arrayDeviceList.contains(extraDeviceList.getName())) {
                    arrayDeviceList.add(extraDeviceList.getName());
                    System.out.println(arrayDeviceList);
                    Listdeviceall.notifyDataSetChanged();
                }
            }
            System.out.println("HEREEEEEEEE in Receiver");


        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BTrequestCode) {
            if (resultCode == RESULT_OK) {
                System.out.println("Bluetooth is Enable");
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("Bluetooth enabling canceled");
            }
        }
    }

    private class ServerClass extends Thread {
        private BluetoothServerSocket ServerSocket;

        public ServerClass() {
            try {
                ServerSocket = BTadapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case STATE_LISTENING:
                        status.setText("Listening..");
                        break;
                    case STATE_CONNECTING:
                        status.setText("Connecting..");
                        break;
                    case STATE_CONNECTED:
                        status.setText("Connected!");
                        break;
                    case STATE_CONNECTION_FAILED:
                        status.setText("Failed Connection!");
                        break;
                    case STATE_MESSAGE_RECEIVED:
                        status.setText("Message Received!");
                        byte[] readBuff= (byte[]) msg.obj;
                        String tempMsg = new String(readBuff,0,msg.arg1);
                        msg_box.setText(tempMsg);
                        break;
                }
                return false;
            }
        });

        public void run() {
            BluetoothSocket socket = null;
            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = ServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }

    }
    private class ClientClass extends Thread
    {

        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device1)
        {
            device=device1;

            try{
                socket=device.createInsecureRfcommSocketToServiceRecord(MY_UUID);

            } catch (IOException e){
                e.printStackTrace();
            }
        }
        public void run()
        {
            try{
                socket.connect();
                Message message= Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e){
                e.printStackTrace();
                Message message = Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);

            }


        }
        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case STATE_LISTENING:
                        status.setText("Listening..");
                        break;
                    case STATE_CONNECTING:
                        status.setText("Connecting..");
                        break;
                    case STATE_CONNECTED:
                        status.setText("Connected!");
                        break;
                    case STATE_CONNECTION_FAILED:
                        status.setText("Failed Connection!");
                        break;
                    case STATE_MESSAGE_RECEIVED:
                        status.setText("Message Received!");
                        byte[] readBuff= (byte[]) msg.obj;
                        String tempMsg = new String(readBuff,0,msg.arg1);
                        msg_box.setText(tempMsg);
                        break;
                }
                return false;
            }
        });
    }
    private class SendReceive extends Thread
    {
        private BluetoothSocket bluetoothSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive (BluetoothSocket socket)
        {
            bluetoothSocket = socket;
            InputStream tempIn= null;
            OutputStream tempOut = null;

            try{
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }


        public void run()
        {
            byte[] buffer=new byte[1024];
            int bytes;

            while (true)
            {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {

            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case STATE_LISTENING:
                        status.setText("Listening..");
                        break;
                    case STATE_CONNECTING:
                        status.setText("Connecting..");
                        break;
                    case STATE_CONNECTED:
                        status.setText("Connected!");
                        break;
                    case STATE_CONNECTION_FAILED:
                        status.setText("Failed Connection!");
                        break;
                    case STATE_MESSAGE_RECEIVED:
                        status.setText("Message Received!");
                        break;
                }
                return false;
            }
        });
    }
}