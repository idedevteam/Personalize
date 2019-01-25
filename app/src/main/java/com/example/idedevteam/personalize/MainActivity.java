package com.example.idedevteam.personalize;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;

public class MainActivity extends AppCompatActivity {

    final String TAG = "PERSONALIZE";
    public final String ACTION_USB_PERMISSION = "com.procodecg.codingmom.ehealth.USB_PERMISSION";

    UsbManager usbManager;
    UsbDevice usbDevice;
    UsbDeviceConnection usbConn;
    UsbSerialDevice serialPort;

    ByteBuffer respondData;

    IntentFilter filter;

    String data, appletDetected;

    TextView text;
    int i;

    byte[] selectResponseHPC, cardCheckingHPC, selectResponsePDC;

    byte[] APDU_select_hpc = {0x00, (byte) 0xA4, 0x04, 0x00, 0x08, 0x48, 0x50, 0x43, 0x44, 0x55, 0x4D, 0x4D, 0x59};
    byte[] APDU_hpc_card_checking = {(byte)0x80, (byte)0xE1, 0x00, 0x00, 0x00, 0x00, 0x00};
    byte[] APDU_select_pdc = {0x00, (byte) 0xA4, 0x04, 0x00, 0x08, 0x50, 0x44, 0x43, 0x44, 0x55, 0x4D, 0x4D, 0x59};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text = (TextView) findViewById(R.id.textView);

        respondData = ByteBuffer.allocate(2469);
        i = 0;

        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "intent.getAction() " + intent.getAction());

            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {

                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    Log.d(TAG, "Permission granted");
                    usbConn = usbManager.openDevice(usbDevice);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(usbDevice, usbConn);
                    if (serialPort != null) {
                        if (serialPort.open()) {
                            // set serial connection parameters
                            serialPort.setBaudRate(9600);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);
                            Log.i(TAG, "Serial port opened");

                            i = 0;
                            send();
                        } else {
                            Log.w(TAG, "PORT NOT OPEN");
                        }
                    } else {
                        Log.w(TAG, "PORT IS NULL");
                    }
                } else {
                    Log.w(TAG, "PERMISSION NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                // connect usb device
                HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
                if (!usbDevices.isEmpty()) {
                    boolean keep = true;
                    for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                        usbDevice = entry.getValue();
                        int deviceID = usbDevice.getVendorId();
                        if (deviceID == 1027 || deviceID == 9025) {
                            if(!usbManager.hasPermission(usbDevice)) {
                                Log.d(TAG, "Device ID " + deviceID);
                                PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                                usbManager.requestPermission(usbDevice, pi);
                                keep = false;
                            }
                        } else {
                            usbConn = null;
                            usbDevice = null;
                        }

                        if (!keep)
                            break;
                    }
                } else {
                    Toast.makeText(context.getApplicationContext(), "Usb devices empty", Toast.LENGTH_SHORT).show();
                }

            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                Log.w(TAG, "DETACHED");
            } else {
                Log.w(TAG, "NO INTENT?");
            }
        }
    };

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() {
        // triggers whenever data is read
        @Override
        public void onReceivedData(byte[] bytes) {
            Log.d(TAG, "Received bytes");
            data = null;
            data = Util.bytesToHex(bytes);

            Log.d(TAG, "Data " + data);
            Log.d(TAG, "i: " + i);

            if (i == 1) {
                respondData.put(bytes);

                if (respondData.position() == 2) {
                    selectResponseHPC = new byte[2];
                    respondData.rewind();
                    respondData.get(selectResponseHPC);
                    respondData.position(0);

                    Log.i(TAG, "Select response string: " + Util.bytesToHex(selectResponseHPC));
                    if (Util.bytesToHex(selectResponseHPC).toString().equals("9000")) {
                        Log.i(TAG, "HPC DETECTED");
                        appletDetected = "HPC";
                        i = 2;
                    }
                    send();
                }
            } else if (i == 2) {
                respondData.put(bytes);

                if (respondData.position() == 2) {
                    selectResponsePDC = new byte[2];
                    respondData.rewind();
                    respondData.get(selectResponsePDC);
                    respondData.position(0);

                    Log.i(TAG, "Select response string: " + Util.bytesToHex(selectResponsePDC));
                    if (Util.bytesToHex(selectResponsePDC).toString().equals("9000")) {
                        Log.i(TAG, "PDC DETECTED");
                        appletDetected = "PDC";
                    } else {
                        appletDetected = "0";
                    }
                    i = 3;
                    send();
                }
//            } else if (i == 3){
//                respondData.put(bytes);
//
//                if (respondData.position() == 3) {
//                    cardCheckingHPC = new byte[3];
//                    respondData.rewind();
//                    respondData.get(cardCheckingHPC);
//                    respondData.position(0);
//
//                    Log.i(TAG, "Card checking response: " + Util.bytesToHex(cardCheckingHPC));
//                    if (Util.bytesToHex(Arrays.copyOfRange(cardCheckingHPC, 0, 1)).equals("00")){
//                        Log.i(TAG, "VIRGIN HPC DETECTED");
//                        send();
//                    }
//                }
//            } else if (i == 4){

            }
        }
    };

    public void send() {
        if (i == 0) {
            serialPort.write(APDU_select_hpc);
            i++;
        } else if(i == 1) {
            serialPort.write(APDU_select_pdc);
            i++;
//        } else if(i == 2) {
//            serialPort.write(APDU_hpc_card_checking);
//            i++;
        } else {
            serialPort.close();
            if(appletDetected != "0"){
                showForm(appletDetected);
            }
        }
    }

    private void showForm(String applet) {
        Log.i(TAG, applet);
        Intent intent = null;
        if(applet == "HPC"){
            intent = new Intent(MainActivity.this, HPC.class);
        } else if(applet == "PDC") {
            intent = new Intent(MainActivity.this, PDC.class);
        }

        startActivity(intent);
        unregisterReceiver(broadcastReceiver);
    }
}