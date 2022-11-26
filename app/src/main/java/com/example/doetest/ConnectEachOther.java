package com.example.doetest;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.UUID;

public class ConnectEachOther {
    private final String TAG = "ConnectEachOther";
    private final String appName = "DOETest";
    private final UUID myUUID= UUID.fromString("3bab976d-b40a-4b6e-9a1c-3a596e8d94e0");
    private final BluetoothAdapter defaultBluetoothAdapter;
    Context context;
    private BluetoothDevice cdevice;
    private BluetoothDevice btDevice;
    private UUID cuuid;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    //private startReading mConnectedReading;
    private AcceptThread mAcceptThread;
    private BluetoothSocket Socket1;
    //ProgressBar mProgressBar;
    static String ChatBoxMSG;
    public ConnectEachOther(Context context1) {
        context= context1;
        defaultBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        System.out.println("STARTING ACCEPTING THREAD!!!");
        start();
    }


    private class AcceptThread extends Thread {
        //private final BluetoothServerSocket btServerSocket;
        private BluetoothServerSocket btServerSocket;
        public AcceptThread(){
            //BluetoothServerSocket tmp = null;
            try{
                btServerSocket = defaultBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName,myUUID);

                System.out.print("good serversocket connection done!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }catch (IOException e){
                System.out.println("FAILEDDD connecting serversocket!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            //btServerSocket= tmp;
        }
        public void run(){
            BluetoothSocket Socket= null;
            try{
                Socket = btServerSocket.accept();
                System.out.println(Socket.toString() + " is what the server socket is ******************************");
                System.out.println("server socket accepted!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }catch (IOException e){
                System.out.println("Failed accepting socket!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
            if (Socket!=null){
                //reads();
                connected(Socket);
                System.out.println("two servers CONNECTED WORK!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }
        }
        public void cancel(){
            try{
                btServerSocket.close();
                System.out.println("close out serversocket!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }catch (IOException e){
                System.out.println("error message for close statement: " + e.getMessage());
            }
        }
    }
    private class ConnectThread extends Thread{
        private BluetoothSocket connectSocket;
        public ConnectThread(BluetoothDevice device, UUID uuid){
            cdevice = device;
            cuuid = uuid;
        }
        public void run(){
            //BluetoothSocket tmp = null;
            try{
                Class<?>[] paramTypes = new Class<?>[] {Integer.TYPE};
                //connectSocket = cdevice.createRfcommSocketToServiceRecord(cuuid)
                //createRfcommSocket is hidden even though it is public, also you will get error with createRfcommsocketToserviceRecord because it will return -1
                //so you will need to convert it to 1 for android >4.2 and use createRfcommSocket
                //https://stackoverflow.com/questions/18657427/ioexception-read-failed-socket-might-closed-bluetooth-on-android-4-3
                Method connectm = cdevice.getClass().getMethod("createRfcommSocket", paramTypes);
                Object[] params = new Object[] {Integer.valueOf(1)};
                connectSocket = (BluetoothSocket) connectm.invoke(cdevice,params);
                System.out.println("connectsocket creation here!!!!!!!!!!!!!!!!!!!!");
            }catch (NoSuchMethodException e){
                System.out.println("connectserver error: " + e.getMessage());

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            //connectSocket = tmp;
            defaultBluetoothAdapter.cancelDiscovery();
            try {
                System.out.println("cdevice at connectsocket: " + cdevice.getName());
                System.out.println("cuuid at connectsocket: " + cuuid);
                System.out.println("cdevice's UUID at connectsocket: " + cdevice.getUuids());
                connectSocket.connect();
                System.out.println("Bluetooth device is connected to: " + cdevice.getName());
            }catch (IOException e) {
                try{
                    connectSocket.close();
                    System.out.println("Couldn't do connectsocket on this device: " + cdevice.getName());
                }catch (IOException e1){
                    System.out.println("error couldn't connect close: " + e1.getMessage());
                }
                System.out.println("error couldn't connect: " + e.getMessage());
            }
            connected(connectSocket);
        }
        public void cancel(){
            try{
                connectSocket.close();
                System.out.println("close out connectsocket!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            }catch (IOException e){
                System.out.println("error couldn't cancel connect socket: " + e.getMessage());
            }
        }
    }

    public synchronized void start(){
        if (mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mAcceptThread == null){
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }

        System.out.println("Starting Acceptthread");
    }
    //I think everything starts here?
    public void startClient(BluetoothDevice device, UUID uuid){
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
        System.out.println("STARTCLIENT AFTER MCONNECTTHREAD");
        System.out.println("startClient's UUID: " + uuid);
        System.out.println("startClient's Device: " + device.getName());
    }


    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        //prkioivate final InputStream mmInStream;
        //private final OutputStream mmOutStream;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            mmSocket = socket;
            try{
                mmInStream = mmSocket.getInputStream();
                mmOutStream = mmSocket.getOutputStream();
            } catch (IOException e){
                System.out.println("error: " + e);
            }
        }
        public void run(){
            byte[] buffer = new byte[1024];
            int bytes;
            while (true){
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);
                    //incomingMessage = String(bytes);
                    ChatBoxMSG = incomingMessage;
                    MainActivity.ChatBox.setText(incomingMessage);
                    System.out.println("InputStream msg: " + incomingMessage);

                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        public void writes(byte[] bytes){
            try{
                mmOutStream.write(bytes);
            }catch (IOException e){
                System.out.println("error: " + e.getMessage());
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            } catch (IOException e){
                System.out.println("error: " + e.getMessage());
            }
        }

    }
    public void connected(BluetoothSocket socket){
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        //mConnectedThread.write(out);
    }

    public void writes(byte[] out){
        //String text = new String(out, Charset.defaultCharset());
        mConnectedThread.writes(out);
        //System.out.println("Does text work? " + text);
        //System.out.println(out);
    }

}
