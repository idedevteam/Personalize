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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by idedevteam on 7/10/18.
 */

public class PDC extends Activity {

    final String TAG = "PERSONALIZE";
    public final String ACTION_USB_PERMISSION = "com.procodecg.codingmom.ehealth.USB_PERMISSION";

    UsbManager usbManager;
    UsbDevice usbDevice;
    UsbDeviceConnection usbConn;
    UsbSerialDevice serialPort;

    ByteBuffer respondData;

    IntentFilter filter;

    String data;

    Button simpanBtn;
    EditText nik, kategoriPasien, nomorAsuransi, namaPasien, namaKK, hubunganKeluarga, alamat, rt, rw,
            kelurahan, kecamatan, kabupaten, provinsi, kodePos, wilayahKerja, tempatLahir, tanggalLahir, telepon, hp,
            jenisKelamin, agama, pendidikan, pekerjaan, kelasPerawatan, alamatEmail, statusPerkawinan, kewarganegaraan, namaKerabat, hubunganKerabat,
            alamatKerabat, kelurahanKerabat, kecamatanKerabat, kabupatenKerabat, provinsiKerabat, kodePosKerabat, teleponKerabat, hpKerabat, namaKantor, alamatKantor,
            kabupatenKantor, teleponKantor, hpKantor, golonganDarah, alergi, riwayatOperasi, riwayatRawat, riwayatKronis, riwayatBawaan, faktorRisiko;
    TextView tanggalDaftar;
    int i;

    byte[] chunck1, chunck2, chunck3, chunck4, chunck5, chunck6, chunck7, chunck8, chunck9, chunck10, selectResponsePDC, responseAuth, responseChunck;

//    byte[] APDU_select_hpc = {0x00, (byte) 0xA4, 0x04, 0x00, 0x08, 0x48, 0x50, 0x43, 0x44, 0x55, 0x4D, 0x4D, 0x59};
    byte[] APDU_select_pdc = {0x00, (byte) 0xA4, 0x04, 0x00, 0x08, 0x50, 0x44, 0x43, 0x44, 0x55, 0x4D, 0x4D, 0x59};
    byte[] APDU_pdc_half_personalize = {(byte) 0x80, (byte) 0xC8, 0x00, 0x00, 0x00, 0x00, 0x00};
    byte[] APDU_pdc_final_personalize = {(byte) 0x80, (byte) 0xC9, 0x00, 0x00, 0x00, 0x00, 0x00};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdc);

        //setting format tanggal device yg baru
        Long tsLong = System.currentTimeMillis();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(tsLong);
        String mTanggalPeriksa = String.valueOf(formatter.format(calendar.getTime()));

        nik = (EditText) findViewById(R.id.nik);
        kategoriPasien = (EditText) findViewById(R.id.kategoriPasien);
        nomorAsuransi = (EditText) findViewById(R.id.nomorAsuransi);
        tanggalDaftar = (TextView) findViewById(R.id.tanggalDaftar);
        tanggalDaftar.setText(mTanggalPeriksa);
        namaPasien = (EditText) findViewById(R.id.namaPasien);
        namaKK = (EditText) findViewById(R.id.namaKK);
        hubunganKeluarga = (EditText) findViewById(R.id.hubunganKeluarga);
        alamat = (EditText) findViewById(R.id.alamat);
        rt = (EditText) findViewById(R.id.rt);
        rw = (EditText) findViewById(R.id.rw);
        kelurahan = (EditText) findViewById(R.id.kelurahan);
        kecamatan = (EditText) findViewById(R.id.kecamatan);
        kabupaten = (EditText) findViewById(R.id.kabupaten);
        provinsi = (EditText) findViewById(R.id.provinsi);
        kodePos = (EditText) findViewById(R.id.kodePos);
        wilayahKerja = (EditText) findViewById(R.id.wilayahKerja);
        tempatLahir = (EditText) findViewById(R.id.tempatLahir);
        tanggalLahir = (EditText) findViewById(R.id.tanggalLahir);
        telepon = (EditText) findViewById(R.id.telepon);
        hp = (EditText) findViewById(R.id.hp);
        jenisKelamin = (EditText) findViewById(R.id.jenisKelamin);
        agama = (EditText) findViewById(R.id.agama);
        pendidikan = (EditText) findViewById(R.id.pendidikan);
        pekerjaan = (EditText) findViewById(R.id.pekerjaan);
        kelasPerawatan = (EditText) findViewById(R.id.kelasPerawatan);
        alamatEmail = (EditText) findViewById(R.id.alamatEmail);
        statusPerkawinan = (EditText) findViewById(R.id.statusPerkawinan);
        kewarganegaraan = (EditText) findViewById(R.id.kewarganegaraan);
        namaKerabat = (EditText) findViewById(R.id.namaKerabat);
        hubunganKerabat = (EditText) findViewById(R.id.hubunganKerabat);
        alamatKerabat = (EditText) findViewById(R.id.alamatKerabat);
        kelurahanKerabat = (EditText) findViewById(R.id.kelurahanKerabat);
        kecamatanKerabat = (EditText) findViewById(R.id.kecamatanKerabat);
        kabupatenKerabat = (EditText) findViewById(R.id.kabupatenKerabat);
        provinsiKerabat = (EditText) findViewById(R.id.provinsiKerabat);
        kodePosKerabat = (EditText) findViewById(R.id.kodePosKerabat);
        teleponKerabat = (EditText) findViewById(R.id.teleponKerabat);
        hpKerabat = (EditText) findViewById(R.id.hpKerabat);
        namaKantor = (EditText) findViewById(R.id.namaKantor);
        alamatKantor = (EditText) findViewById(R.id.alamatKantor);
        kabupatenKantor = (EditText) findViewById(R.id.kabupatenKantor);
        teleponKantor = (EditText) findViewById(R.id.teleponKantor);
        hpKantor = (EditText) findViewById(R.id.hpKantor);
        golonganDarah = (EditText) findViewById(R.id.golonganDarah);
        alergi = (EditText) findViewById(R.id.alergi);
        riwayatOperasi = (EditText) findViewById(R.id.riwayatOperasi);
        riwayatRawat = (EditText) findViewById(R.id.riwayatRawat);
        riwayatKronis = (EditText) findViewById(R.id.riwayatKronis);
        riwayatBawaan = (EditText) findViewById(R.id.riwayatBawaan);
        faktorRisiko = (EditText) findViewById(R.id.faktorRisiko);

        simpanBtn = (Button) findViewById(R.id.simpanBtn);
        simpanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cmd1 = "80c3000000";
                cmd1 += "00D2"; //total length
                cmd1 += "0000"; //pointer
                cmd1 += "00CE"; //length data
                cmd1 += Util.padVariableText(nik.getText().toString(), 16);
                cmd1 += Util.padVariableText(kategoriPasien.getText().toString(), 52);
                cmd1 += Util.padVariableText(nomorAsuransi.getText().toString(), 82);
                cmd1 += Util.bytesToHex(Util.dateToBytes(Util.getCurrentDate()));
                cmd1 += Util.padVariableText(namaPasien.getText().toString(), 54);
                Log.i(TAG, cmd1);
                chunck1 = Util.hexStringToByteArray(cmd1);

                String cmd2 = "80c3000000";
                cmd2 += "00DE"; //total length
                cmd2 += "00CE"; //pointer
                cmd2 += "00DA"; //length data
                cmd2 += Util.padVariableText(namaKK.getText().toString(), 52);
                cmd2 += Util.padVariableText(hubunganKeluarga.getText().toString(), 1);
                cmd2 += Util.padVariableText(alamat.getText().toString(), 102);
                cmd2 += Util.padVariableText(rt.getText().toString(), 1);
                cmd2 += Util.padVariableText(rw.getText().toString(), 1);
                cmd2 += Util.padVariableText(kelurahan.getText().toString(), 2);
                cmd2 += Util.padVariableText(kecamatan.getText().toString(), 1);
                cmd2 += Util.padVariableText(kabupaten.getText().toString(), 1);
                cmd2 += Util.padVariableText(provinsi.getText().toString(), 1);
                cmd2 += Util.padVariableText(kodePos.getText().toString(), 3);
                cmd2 += Util.padVariableText(wilayahKerja.getText().toString(), 1);
                cmd2 += Util.padVariableText(tempatLahir.getText().toString(), 22);
                cmd2 += Util.padVariableText(tanggalLahir.getText().toString(), 4);
                cmd2 += Util.padVariableText(telepon.getText().toString(), 8);
                cmd2 += Util.padVariableText(hp.getText().toString(), 18);
                Log.i(TAG, cmd2);
                chunck2 = Util.hexStringToByteArray(cmd2);

                String cmd3 = "80c3000000";
                cmd3 += "00BD";
                cmd3 += "01AB";
                cmd3 += "00B9";
                cmd3 += Util.padVariableText(jenisKelamin.getText().toString(),1);
                cmd3 += Util.padVariableText(agama.getText().toString(),1);
                cmd3 += Util.padVariableText(pendidikan.getText().toString(), 1);
                cmd3 += Util.padVariableText(pekerjaan.getText().toString(), 1);
                cmd3 += Util.padVariableText(kelasPerawatan.getText().toString(), 2);
                cmd3 += Util.padVariableText(alamatEmail.getText().toString(), 22);
                cmd3 += Util.padVariableText(statusPerkawinan.getText().toString(), 1);
                cmd3 += Util.padVariableText(kewarganegaraan.getText().toString(), 1);
                cmd3 += Util.padVariableText(namaKerabat.getText().toString(), 52);
                cmd3 += Util.padVariableText(hubunganKerabat.getText().toString(),1 );
                cmd3 += Util.padVariableText(alamatKerabat.getText().toString(), 102);
                Log.i(TAG, cmd3);
                chunck3 = Util.hexStringToByteArray(cmd3);

                String cmd4 = "80c3000000";
                cmd4 += "00C9";
                cmd4 += "0264";
                cmd4 += "00C5";
                cmd4 += Util.padVariableText(kelurahanKerabat.getText().toString(), 2);
                cmd4 += Util.padVariableText(kecamatanKerabat.getText().toString(),1);
                cmd4 += Util.padVariableText(kabupatenKerabat.getText().toString(),1);
                cmd4 += Util.padVariableText(provinsiKerabat.getText().toString(),1);
                cmd4 += Util.padVariableText(kodePosKerabat.getText().toString(), 3);
                cmd4 += Util.padVariableText(teleponKerabat.getText().toString(), 18);
                cmd4 += Util.padVariableText(hpKerabat.getText().toString(), 18);
                cmd4 += Util.padVariableText(namaKantor.getText().toString(), 22);
                cmd4 += Util.padVariableText(alamatKantor.getText().toString(), 102);
                cmd4 += Util.padVariableText(kabupatenKantor.getText().toString(),1);
                cmd4 += Util.padVariableText(teleponKantor.getText().toString(), 18);
                cmd4 += Util.padVariableText(hpKantor.getText().toString(), 12);
                Log.i(TAG, cmd4);
                chunck4 = Util.hexStringToByteArray(cmd4);

                String cmd5 = "80c4000000";
                cmd5 += "006B";
                cmd5 += "0000";
                cmd5 += "0067";
                cmd5 += Util.padVariableText(golonganDarah.getText().toString(),1);
                cmd5 += Util.padVariableText(alergi.getText().toString(), 102);
                Log.i(TAG, cmd5);
                chunck5 = Util.hexStringToByteArray(cmd5);

                String cmd6 = "80c4000000";
                cmd6 += "00CC";
                cmd6 += "0067";
                cmd6 += "00C8";
                cmd6 += Util.padVariableText(riwayatOperasi.getText().toString(), 200);
                Log.i(TAG, cmd6);
                chunck6 = Util.hexStringToByteArray(cmd6);

                String cmd7 = "80c4000000";
                cmd7 += "00CC";
                cmd7 += "0161";
                cmd7 += "00C8";
                cmd7 += Util.padVariableText(riwayatRawat.getText().toString(), 200);
                Log.i(TAG, cmd7);
                chunck7 = Util.hexStringToByteArray(cmd7);

                String cmd8 = "80c4000000";
                cmd8 += "00CC";
                cmd8 += "025B";
                cmd8 += "00C8";
                cmd8 += Util.padVariableText(riwayatKronis.getText().toString(), 200);
                Log.i(TAG, cmd8);
                chunck8 = Util.hexStringToByteArray(cmd8);

                String cmd9 = "80c4000000";
                cmd9 += "00CC";
                cmd9 += "0355";
                cmd9 += "00C8";
                cmd9 += Util.padVariableText(riwayatBawaan.getText().toString(), 200);
                Log.i(TAG, cmd9);
                chunck9 = Util.hexStringToByteArray(cmd9);

                String cmd10 = "80c4000000";
                cmd10 += "00CC";
                cmd10 += "044F";
                cmd10 += "00C8";
                cmd10 += Util.padVariableText(faktorRisiko.getText().toString(), 200);
                Log.i(TAG, cmd10);
                chunck10 = Util.hexStringToByteArray(cmd10);

                send();
            }
        });

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
                    selectResponsePDC = new byte[2];
                    respondData.rewind();
                    respondData.get(selectResponsePDC);
                    respondData.position(0);

                    Log.i(TAG, "Select response string: " + Util.bytesToHex(selectResponsePDC));
                    if (Util.bytesToHex(selectResponsePDC).toString().equals("9000")) {
                        Log.i(TAG, "PDC Detected");
                    } else {
                        Log.i(TAG, "PDC Not Detected");
                    }
                }
            } else if (i > 1 && i < 14) {
                respondData.put(bytes);

                if (respondData.position() == 2) {
                    responseChunck = new byte[2];
                    respondData.rewind();
                    respondData.get(responseChunck);
                    respondData.position(0);

                    Log.i(TAG, "Chunck response string: " + Util.bytesToHex(responseChunck));
                    if (Util.bytesToHex(responseChunck).toString().equals("9000")) {
                        send();
                    }

                }
            }
        }
    };

    public void send() {
        if (i == 0) {
            serialPort.write(APDU_select_pdc);
            i++;
        } else if(i == 1) {
            Log.i(TAG,"Send chunck 1");
            serialPort.write(chunck1);
            i++;
        } else if(i == 2) {
            Log.i(TAG,"Send chunck 2");
            serialPort.write(chunck2);
            i++;
        } else if(i == 3) {
            Log.i(TAG,"Send chunck 3");
            serialPort.write(chunck3);
            i++;
        } else if(i == 4) {
            Log.i(TAG,"Send chunck 4");
            serialPort.write(chunck4);
            i++;
        } else if(i == 5) {
            Log.i(TAG,"Send half personalize");
            serialPort.write(APDU_pdc_half_personalize);
            i++;
        } else if(i == 6) {
            Log.i(TAG,"Send chunck 5");
            serialPort.write(chunck5);
            i++;
        } else if(i == 7) {
            Log.i(TAG,"Send chunck 6");
            serialPort.write(chunck6);
            i++;
        } else if(i == 8) {
            Log.i(TAG,"Send chunck 7");
            serialPort.write(chunck7);
            i++;
        } else if(i == 9) {
            Log.i(TAG,"Send chunck 8");
            serialPort.write(chunck8);
            i++;
        } else if(i == 10) {
            Log.i(TAG,"Send chunck 9");
            serialPort.write(chunck9);
            i++;
        } else if(i == 11) {
            Log.i(TAG,"Send chunck 10");
            serialPort.write(chunck10);
            i++;
        } else if(i == 12) {
            Log.i(TAG,"Send final personalize");
            serialPort.write(APDU_pdc_final_personalize);
            i++;
        }else {
            serialPort.close();
            unregisterReceiver(broadcastReceiver);
            Intent activity = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(activity);
            finish();
        }
    }
}
