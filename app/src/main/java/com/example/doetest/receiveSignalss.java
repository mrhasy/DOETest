package com.example.doetest;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.widget.ArrayAdapter;

import java.util.ArrayList;


public class receiveSignalss {
    public BluetoothDevice onReceive(Context context, Intent intent) {
        ArrayList<String> arrayDeviceList = new ArrayList<String>();

        //Intent sig gives option such as sig.getData() <-- might be something that I am looking for
        String intentString = intent.getAction();
        if (BluetoothDevice.ACTION_FOUND.equals(intentString)) {
            BluetoothDevice extraDeviceList = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            System.out.println(extraDeviceList.getName());
            if (extraDeviceList.getName() != null && !arrayDeviceList.contains(extraDeviceList.getName())) {
                arrayDeviceList.add(extraDeviceList.getName());
                //System.out.println(arrayDeviceList);
               // Listdeviceall.notifyDataSetChanged();
                return extraDeviceList;
            }
        }
        return null;
    }
}
