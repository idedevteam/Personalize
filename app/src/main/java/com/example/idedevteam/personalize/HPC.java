package com.example.idedevteam.personalize;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by idedevteam on 7/10/18.
 */

public class HPC extends Activity {

    final String TAG = "PERSONALIZE";
    public final String ACTION_USB_PERMISSION = "com.procodecg.codingmom.ehealth.USB_PERMISSION";

    UsbManager usbManager;
    UsbDevice usbDevice;
    UsbDeviceConnection usbConn;
    UsbSerialDevice serialPort;

    ByteBuffer respondData;

    IntentFilter filter;

    String data;

    Button simpanBtn, pinBtn, pinVerifyBtn;
    EditText pin, pinVerify, nik, nama, sip;
    Spinner role;
    int i, roleID;

    byte[] setPassword, checkPassword, setHPData, setCert, selectResponseHPC, responseSetPass, responseAuth, responseSetCert, responseSetHPData;

    byte[] APDU_select_hpc = {0x00, (byte) 0xA4, 0x04, 0x00, 0x08, 0x48, 0x50, 0x43, 0x44, 0x55, 0x4D, 0x4D, 0x59};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hpc);

        role = (Spinner) findViewById(R.id.role);
        ArrayAdapter<CharSequence> adapterRole = ArrayAdapter.createFromResource(this,
                R.array.role, android.R.layout.simple_spinner_item);
        adapterRole.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        role.setPrompt("Pilih role");
        role.setAdapter(adapterRole);
        role.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selection = (String) adapterView.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals("DOKTER")) {
                        roleID = 1;
                    } else if (selection.equals("PENDAFTARAN")) {
                        roleID = 2;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                roleID = 1;
            }
        });
        role.setVisibility(View.GONE);

        pin = (EditText) findViewById(R.id.pin);
        pinVerify = (EditText) findViewById(R.id.pinVerify);
        pinVerify.setVisibility(View.GONE);
        nik = (EditText) findViewById(R.id.nik);
        nik.setVisibility(View.GONE);
        nama = (EditText) findViewById(R.id.nama);
        nama.setVisibility(View.GONE);
        sip = (EditText) findViewById(R.id.sip);
        sip.setVisibility(View.GONE);

        simpanBtn = (Button) findViewById(R.id.simpanBtn);
        simpanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command1 = "80f1000000";
                command1 += Util.intToHex2(1+nik.getText().length());
                command1 += "01";
                command1 += Util.stringToHex(nik.getText().toString());
                Log.i(TAG, command1);
                setCert = Util.hexStringToByteArray(command1);

                String command2 = "80f2000000";
                int length = nik.getText().length() + 1 + nama.getText().length() + 1 + sip.getText().length();
                command2 += Util.intToHex2(length);
                command2 += Util.stringToHex(nik.getText().toString());
                command2 += Util.intToHex4(nama.getText().length());
                command2 += Util.stringToHex(nama.getText().toString());
                command2 += Util.intToHex4(sip.getText().length());
                command2 += Util.stringToHex(sip.getText().toString());
                Log.i(TAG, command2);
                setHPData = Util.hexStringToByteArray(command2);

                send();
            }
        });
        simpanBtn.setVisibility(View.GONE);

        pinBtn = (Button) findViewById(R.id.pinBtn);
        pinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = "80c20000000005";
                command += Util.stringToHex(pin.getText().toString());
                setPassword = Util.hexStringToByteArray(command);

                send();
            }
        });

        pinVerifyBtn = (Button) findViewById(R.id.pinVerifyBtn);
        pinVerifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String command = "80c30000000005";
                command += Util.stringToHex(pinVerify.getText().toString());
                checkPassword = Util.hexStringToByteArray(command);

                send();
            }
        });
        pinVerifyBtn.setVisibility(View.GONE);

        respondData = ByteBuffer.allocate(2);
        i = 0;
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        // connect usb device
        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                usbDevice = entry.getValue();
                int deviceID = usbDevice.getVendorId();
                if (deviceID == 1027 || deviceID == 9025) {
                    Log.d(TAG, "Device ID " + deviceID);
                    PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(usbDevice, pi);
                    keep = false;
                } else {
                    usbConn = null;
                    usbDevice = null;
                }

                if (!keep)
                    break;
            }
        }
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
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                i=0;
                Intent activity = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(activity);
                finish();
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
                        Log.i(TAG, "HPC Detected");
                    } else {
                        Log.i(TAG, "HPC Not Detected");
                    }
                }
            } else if (i == 2) {
                respondData.put(bytes);

                if (respondData.position() == 2) {
                    responseSetPass = new byte[2];
                    respondData.rewind();
                    respondData.get(responseSetPass);
                    respondData.position(0);

                    Log.i(TAG, "Select response string: " + Util.bytesToHex(responseSetPass));
                    if (Util.bytesToHex(responseSetPass).toString().equals("9000")) {
                        Log.i(TAG, "PIN berhasil dimasukan");
                        verifyPin();
                    } else {
                        Log.i(TAG, "PIN gagal");
                    }
                }
            } else if (i == 3) {
                respondData.put(bytes);

                if (respondData.position() == 2) {
                    responseAuth = new byte[2];
                    respondData.rewind();
                    respondData.get(responseAuth);
                    respondData.position(0);

                    Log.i(TAG, "Select response string: " + Util.bytesToHex(responseAuth));
                    if (Util.bytesToHex(responseAuth).toString().equals("9000")) {
                        Log.i(TAG, "PIN authenticated");
                        showForm();
                    } else {
                        Log.i(TAG, "PIN not authenticated");
                    }
                }
            } else if (i == 4) {
                respondData.put(bytes);

                if (respondData.position() == 2) {
                    responseSetCert = new byte[2];
                    respondData.rewind();
                    respondData.get(responseSetCert);
                    respondData.position(0);

                    Log.i(TAG, "Set Cert response string: " + Util.bytesToHex(responseSetCert));
                    if (Util.bytesToHex(responseSetCert).toString().equals("9000")) {
                        send();
                    }

                }
            } else if (i == 5) {
                respondData.put(bytes);

                if (respondData.position() == 2) {
                    responseSetHPData = new byte[2];
                    respondData.rewind();
                    respondData.get(responseSetHPData);
                    respondData.position(0);

                    Log.i(TAG, "Set HP Data response string: " + Util.bytesToHex(responseSetHPData));
                    if (Util.bytesToHex(responseSetHPData).toString().equals("9000")) {
                        send();
                    }
                }
            }
        }
    };

    public void send() {
        if (i == 0) {
            serialPort.write(APDU_select_hpc);
            i++;
        } else if(i == 1) {
            serialPort.write(setPassword);
            i++;
        } else if(i == 2) {
            serialPort.write(checkPassword);
            i++;
        } else if(i == 3) {
            serialPort.write(setCert);
            i++;
        } else if(i == 4) {
            serialPort.write(setHPData);
            i++;
        } else {
            serialPort.close();
            unregisterReceiver(broadcastReceiver);
            Intent activity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(activity);
            finish();
        }
    }

    public void verifyPin(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pin.setVisibility(View.GONE);
                pinBtn.setVisibility(View.GONE);
                pinVerify.setVisibility(View.VISIBLE);
                pinVerifyBtn.setVisibility(View.VISIBLE);
            }
        });
    }

    public void showForm(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pinVerifyBtn.setVisibility(View.GONE);
                pinVerify.setVisibility(View.GONE);
                role.setVisibility(View.VISIBLE);
                nik.setVisibility(View.VISIBLE);
                nama.setVisibility(View.VISIBLE);
                sip.setVisibility(View.VISIBLE);
                simpanBtn.setVisibility(View.VISIBLE);
            }
        });
    }
}
