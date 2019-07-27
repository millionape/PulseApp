package com.example.kittaphornj.pulseapp;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.harrysoft.androidbluetoothserial.BluetoothManager;
import com.harrysoft.androidbluetoothserial.BluetoothSerialDevice;
import com.harrysoft.androidbluetoothserial.SimpleBluetoothDeviceInterface;


import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button f10,f11,f12,f20,f21,f22,f30,f31,f32,start,stop;
    private final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION=1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;
    private SimpleBluetoothDeviceInterface deviceInterface;
    BluetoothManager bluetoothManager = BluetoothManager.getInstance();
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothSerialDevice  ble;
    public int speedBPM = 60;
    static final int ENABLE_BT_REQUEST_CODE = 1;
    private ProgressDialog dialog;
    boolean connectTag = false;

    TextView text1,bpmText;


    String bleID;
    String resultTextForm[] = new String[]{"ปิตตะแทรกวาตะ","ปิตตะแทรกเสมหะ","วาตะแทรกเสมหะ",
                                            "วาตะแทรกปิตตะ","ปิตตะแทรกเสมหะ","เสมหะแทรกปิตตะ",
                                            "เสมหะแทรกวาตะ"};
    boolean arr[][] = new boolean[][]{{true,false,false},{true,false,false},{true,false,false}};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle bundle = getIntent().getExtras();
        String dname = bundle.getString("device_name");
        String dmac = bundle.getString("device_mac");
        Toast.makeText(MainActivity.this,dname+dmac ,Toast.LENGTH_SHORT).show();
        //bleID = getIntent().getStringExtra("device_id");
        //bleID = "30:AE:A4:4F:43:B6";
        bleID = dmac;
        dialog = new ProgressDialog(MainActivity.this);
        dialog.setCancelable(false);
        text1 = (TextView) findViewById(R.id.textView);
        bpmText = (TextView) findViewById(R.id.textView2);
        SeekBar seek = (SeekBar) findViewById(R.id.seekBar);
        seek.setProgress(60);
        seek.incrementProgressBy(10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seek.setMin(60);
        }
        seek.setMax(120);
        text1.setMovementMethod(new ScrollingMovementMethod());
        f10 = (Button) findViewById(R.id.b10);
        f11 = (Button) findViewById(R.id.b11);
        f12 = (Button) findViewById(R.id.b12);
        f20 = (Button) findViewById(R.id.b20);
        f21 = (Button) findViewById(R.id.b21);
        f22 = (Button) findViewById(R.id.b22);
        f30 = (Button) findViewById(R.id.b30);
        f31 = (Button) findViewById(R.id.b31);
        f32 = (Button) findViewById(R.id.b32);
        start = (Button) findViewById(R.id.button20);
        stop = (Button) findViewById(R.id.button21);
        f10.setOnClickListener(this);
        f11.setOnClickListener(this);
        f12.setOnClickListener(this);
        f20.setOnClickListener(this);
        f21.setOnClickListener(this);
        f22.setOnClickListener(this);
        f30.setOnClickListener(this);
        f31.setOnClickListener(this);
        f32.setOnClickListener(this);
        setResultText(arr);
        requestPermission();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, ENABLE_BT_REQUEST_CODE);
            Toast.makeText(getApplicationContext(), "Enabling Bluetooth!", Toast.LENGTH_SHORT).show();
        }
        dialog.setMessage("Connecting.....");
        connectDevice(bleID);

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i<=60 ){
                    speedBPM = 60;
                    bpmText.setText(String.valueOf(60)+"bpm");
                }else if(i>60 && i<=70){
                    speedBPM = 70;
                    bpmText.setText(String.valueOf(70)+"bpm");
                }else if(i>70 && i<=80){
                    speedBPM = 80;
                    bpmText.setText(String.valueOf(80)+"bpm");
                }else if(i>80 && i<=90){
                    speedBPM = 90;
                    bpmText.setText(String.valueOf(90)+"bpm");
                }else if(i>90 && i<=100){
                    speedBPM = 100;
                    bpmText.setText(String.valueOf(100)+"bpm");
                }else if(i>100 && i<=110){
                    speedBPM = 110;
                    bpmText.setText(String.valueOf(110)+"bpm");
                }else if(i>110 && i<=120){
                    speedBPM = 120;
                    bpmText.setText(String.valueOf(120)+"bpm");
                }
                else{
                    speedBPM = i;
                    bpmText.setText(String.valueOf(i)+"bpm");
                }



            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(connectTag == true){
                    sendMSG();
                }
            }
        });
        bluetoothManager = BluetoothManager.getInstance();
        if (bluetoothManager == null) {
            // Bluetooth unavailable on this device :( tell the user
            Toast.makeText(MainActivity.this, "Bluetooth not available.", Toast.LENGTH_SHORT).show(); // Replace context with your context instance.
            finish();
        }
        List<BluetoothDevice> pairedDevices = bluetoothManager.getPairedDevicesList();
        for (BluetoothDevice device : pairedDevices) {
            Log.d("My Bluetooth App", "Device name: " + device.getName());
            Log.d("My Bluetooth App", "Device MAC Address: " + device.getAddress());
        }
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectTag == false) {
                    dialog.setMessage("Connecting please wait....");
                    dialog.show();
                    connectDevice(bleID);
                }else{
                    sendMSG();
                }
            }
        });
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectTag == true) {
                    //connectTag = false;
                    deviceInterface.sendMessage("OFF");
                    //bluetoothManager.closeDevice(bleID);
                    //start.setEnabled(true);
                    //start.setBackgroundColor(Color.GRAY);
                }else{
                    Toast.makeText(MainActivity.this, "No device is connected yet.", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void connectDevice(String mac) {
        bluetoothManager.openSerialDevice(mac)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onConnected, this::onError);
    }

    private void onConnected(BluetoothSerialDevice connectedDevice) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        ble = connectedDevice;
        // You are now connected to this device!
        // Here you may want to retain an instance to your device:
        deviceInterface = connectedDevice.toSimpleDeviceInterface();
        connectTag = true;
        // Listen to bluetooth events
        deviceInterface.setListeners(this::onMessageReceived, this::onMessageSent, this::onError);
//        start.setEnabled(false);
//        start.setBackgroundColor(Color.GREEN);

        // Let's send a message:
//        if(connectTag == true){
//            sendMSG();
//        }
    }

    private void onMessageSent(String message) {
        // We sent a message! Handle it here.
        //Toast.makeText(MainActivity.this, "Sent a message! Message was: " + message, Toast.LENGTH_LONG).show(); // Replace context with your context instance.
    }

    private void onMessageReceived(String message) {
        // We received a message! Handle it here.
        //Toast.makeText(MainActivity.this, "Received a message! Message was: " + message, Toast.LENGTH_LONG).show(); // Replace context with your context instance.
    }

    private void onError(Throwable error) {
        connectTag = false;
        start.setEnabled(true);
        //start.setBackgroundColor(Color.RED);
        // Handle the error
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
        if(!((Activity) MainActivity.this).isFinishing()) {
            //show dialog
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
            alertDialog.setTitle("Error");
            alertDialog.setMessage("Unable to connect device.");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b10:
                if(arr[0][0] == false){
                    f10.setBackgroundResource(R.drawable.round_button_press);
                    f11.setBackgroundResource(R.drawable.round_button);
                    f12.setBackgroundResource(R.drawable.round_button);
                    arr[0][0] = true;
                    arr[0][1] = false;
                    arr[0][2] = false;
                }
                break;
            case R.id.b11:
                if(arr[0][1] == false){
                    f11.setBackgroundResource(R.drawable.round_button_press);
                    f10.setBackgroundResource(R.drawable.round_button);
                    f12.setBackgroundResource(R.drawable.round_button);
                    arr[0][0] = false;
                    arr[0][1] = true;
                    arr[0][2] = false;
                }
                break;
            case R.id.b12:
                if(arr[0][2] == false){
                    f12.setBackgroundResource(R.drawable.round_button_press);
                    f10.setBackgroundResource(R.drawable.round_button);
                    f11.setBackgroundResource(R.drawable.round_button);
                    arr[0][0] = false;
                    arr[0][2] = true;
                    arr[0][1] = false;
                }
                break;
                ////// f1 finish
            case R.id.b20:
                if(arr[1][0] == false){
                    f20.setBackgroundResource(R.drawable.round_button_press);
                    f21.setBackgroundResource(R.drawable.round_button);
                    f22.setBackgroundResource(R.drawable.round_button);
                    arr[1][0] = true;
                    arr[1][1] = false;
                    arr[1][2] = false;
                }
                break;
            case R.id.b21:
                if(arr[1][1] == false){
                    f21.setBackgroundResource(R.drawable.round_button_press);
                    f20.setBackgroundResource(R.drawable.round_button);
                    f22.setBackgroundResource(R.drawable.round_button);
                    arr[1][0] = false;
                    arr[1][1] = true;
                    arr[1][2] = false;
                }
                break;
            case R.id.b22:
                if(arr[1][2] == false){
                    f22.setBackgroundResource(R.drawable.round_button_press);
                    f20.setBackgroundResource(R.drawable.round_button);
                    f21.setBackgroundResource(R.drawable.round_button);
                    arr[1][0] = false;
                    arr[1][2] = true;
                    arr[1][1] = false;
                }
                break;
                //// f2 finish
            case R.id.b30:
                if(arr[2][0] == false){
                    f30.setBackgroundResource(R.drawable.round_button_press);
                    f31.setBackgroundResource(R.drawable.round_button);
                    f32.setBackgroundResource(R.drawable.round_button);
                    arr[2][0] = true;
                    arr[2][1] = false;
                    arr[2][2] = false;
                }
                break;
            case R.id.b31:
                if(arr[2][1] == false){
                    f31.setBackgroundResource(R.drawable.round_button_press);
                    f30.setBackgroundResource(R.drawable.round_button);
                    f32.setBackgroundResource(R.drawable.round_button);
                    arr[2][0] = false;
                    arr[2][1] = true;
                    arr[2][2] = false;
                }
                break;
            case R.id.b32:
                if(arr[2][2] == false){
                    f32.setBackgroundResource(R.drawable.round_button_press);
                    f30.setBackgroundResource(R.drawable.round_button);
                    f31.setBackgroundResource(R.drawable.round_button);
                    arr[2][0] = false;
                    arr[2][2] = true;
                    arr[2][1] = false;
                }
                break;

        }
        setResultText(arr);
    }
    int getDistinc(boolean finger[]){
        for(int i=0;i<3;i++){
            if(finger[i] == true){
                return i+1;
            }
        }
        return -1;
    }
    void sendMSG(){
        int finger1 = getDistinc(arr[0]);
        int finger2 = getDistinc(arr[1]);
        int finger3 = getDistinc(arr[2]);

        String msg = "$"+String.valueOf(speedBPM)+"/"+String.valueOf(finger1)+"/"+String.valueOf(finger2)+"/"+String.valueOf(finger3)+"\r\n";
        deviceInterface.sendMessage(msg);
    }
    void setResultText(boolean x[][]){
        String str = "";
        int position1,position2,position3;
        position1 = getDistinc(x[0]);
        position2 = getDistinc(x[1]);
        position3 = getDistinc(x[2]);
        if(position1 == -1 || position2 == -1 || position3 == -1){
            text1.setText("unknown");
            return;
        }
        if(position1 == 1 && position2 == 1 && position3 ==1){
            str += "ปิตตะแทรกวาตะ"+'\n';
            str += "ปิตตะแทรกเสมหะ"+'\n'+"------------------------\n"; ///1
            str += "ลมร้อนพัดขึ้น อาจปวดศีรษะ มีไข้ ไมเกรน ปวดตา ความดันเลือดสูง \nความร้อนค้างในเส้น ร้อนใน";
            str += "ไข้หวัด เสมหะร้อน";

        }else if(position1 == 1 && position2 == 1 && position3 ==2){ ///2
            str += "ปิตตะแทรกวาตะ"+'\n';
            str += "วาตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมร้อนพัดขึ้น อาจปวดศีรษะ มีไข้ ไมเกรน ปวดตา ความดันเลือดสูง ความร้อนค้างในเส้น ร้อนใน";
            str += "คอแห้ง เสมหะแห้ง  ท้องผูก";
        }else if(position1 == 1 && position2 == 1 && position3 ==3){ /// 3
            str += "ปิตตะแทรกวาตะ"+'\n'+"------------------------\n";
            str += "ลมร้อนพัดขึ้น อาจปวดศีรษะ มีไข้ ไมเกรน ปวดตา ความดันเลือดสูง ความร้อนค้างในเส้น ร้อนใน\n";
        }else if(position1 == 1 && position2 == 2 && position3 == 1){ /// 4
            str += "ปิตตะแทรกวาตะ"+'\n';
            str += "วาตะแทรกปิตตะ"+'\n';
            str += "ปิตตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมร้อนพัดขึ้น อาจปวดศีรษะ มีไข้ ไมเกรน ปวดตา ความดันเลือดสูง ความร้อนค้างในเส้น ร้อนใน";
            str += "ไฟย่อยอ่อน มีแก๊สในท้องมาก  ท้องอืดแน่น  ระบบย่อยแปรปรวน อ่อนกำลัง";
            str += "ไข้หวัด เสมหะร้อน";
        }else if(position1 == 1 && position2 == 2 && position3 == 2){ /// 5
            str += "ปิตตะแทรกวาตะ"+'\n';
            str += "วาตะแทรกปิตตะ"+'\n';
            str += "วาตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมร้อนพัดขึ้น อาจปวดศีรษะ มีไข้ ไมเกรน ปวดตา ความดันเลือดสูง ความร้อนค้างในเส้น ร้อนใน";
            str += "ไฟย่อยอ่อน มีแก๊สในท้องมาก  ท้องอืดแน่น  ระบบย่อยแปรปรวน อ่อนกำลัง";
            str += "คอแห้ง เสมหะแห้ง  ท้องผูก";
        }else if(position1 == 1 && position2 == 2 && position3 == 3){  // 6
            str += "ปิตตะแทรกวาตะ"+'\n';
            str += "วาตะแทรกปิตตะ"+'\n'+"------------------------\n";
            str += "ลมร้อนพัดขึ้น อาจปวดศีรษะ มีไข้ ไมเกรน ปวดตา ความดันเลือดสูง ความร้อนค้างในเส้น ร้อนใน";
            str += "ไฟย่อยอ่อน มีแก๊สในท้องมาก  ท้องอืดแน่น  ระบบย่อยแปรปรวน อ่อนกำลัง";
        }else if(position1 == 1 && position2 == 3 && position3 == 1){ /// 7
            str += "ปิตตะแทรกวาตะ"+'\n';
            str += "เสมหะแทรกปิตตะ"+'\n';
            str += "ปิตตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมร้อนพัดขึ้น อาจปวดศีรษะ มีไข้ ไมเกรน ปวดตา ความดันเลือดสูง ความร้อนค้างในเส้น ร้อนใน";
            str += "เบื่ออาหาร  ความอยากอาหารลดลง  มีมีเสลดค้างในระบบย่อย";
            str += "ไข้หวัด เสมหะร้อน";

        }else if(position1 == 1 && position2 == 3 && position3 == 2){ // 8
            str += "ปิตตะแทรกวาตะ"+'\n';
            str += "เสมหะแทรกปิตตะ"+'\n';
            str += "วาตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมร้อนพัดขึ้น อาจปวดศีรษะ มีไข้ ไมเกรน ปวดตา ความดันเลือดสูง ความร้อนค้างในเส้น ร้อนใน";
            str += "เบื่ออาหาร  ความอยากอาหารลดลง  มีมีเสลดค้างในระบบย่อย";
            str += "คอแห้ง เสมหะแห้ง  ท้องผูก";
        }else if(position1 == 1 && position2 == 3 && position3 == 3){ /// 9
            str += "ปิตตะแทรกวาตะ"+'\n';
            str += "เสมหะแทรกปิตตะ"+'\n'+"------------------------\n";
            str += "ลมร้อนพัดขึ้น อาจปวดศีรษะ มีไข้ ไมเกรน ปวดตา ความดันเลือดสูง ความร้อนค้างในเส้น ร้อนใน";
            str += "เบื่ออาหาร  ความอยากอาหารลดลง  มีมีเสลดค้างในระบบย่อย";
        }else if(position1 == 2 && position2 == 1 && position3 == 1){ /// 10
            str += "ปิตตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ไข้หวัด เสมหะร้อน";
        }else if(position1 == 2 && position2 == 1 && position3 == 2){ /// 11
            str += "วาตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "คอแห้ง เสมหะแห้ง  ท้องผูก";
        }else if(position1 == 2 && position2 == 1 && position3 == 3) { //12
            str += "ปกติ";
        }else if(position1 == 2 && position2 == 2 && position3 == 1){ //13
            str += "วาตะแทรกปิตตะ"+'\n';
            str += "ปิตตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ไฟย่อยอ่อน มีแก๊สในท้องมาก  ท้องอืดแน่น ระบบย่อยแปรปรวน อ่อนกำลัง";
            str += "ไข้หวัด เสมหะร้อน";
        }else if(position1 == 2 && position2 == 2 && position3 == 2){///14
            str += "วาตะแทรกปิตตะ"+'\n';
            str += "วาตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ไฟย่อยอ่อน มีแก๊สในท้องมาก  ท้องอืดแน่น ระบบย่อยแปรปรวน อ่อนกำลัง";
            str += "คอแห้ง เสมหะแห้ง  ท้องผูก";
        }else if(position1 == 2 && position2 == 2 && position3 == 3){ /// 15
            str += "วาตะแทรกปิตตะ"+'\n'+"------------------------\n";
            str += "ไฟย่อยอ่อน มีแก๊สในท้องมาก  ท้องอืดแน่น ระบบย่อยแปรปรวน อ่อนกำลัง";
        }else if(position1 == 2 && position2 == 3 && position3 == 1){ /// 16
            str += "เสมหะแทรกปิตตะ"+'\n';
            str += "ปิตตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "เบื่ออาหาร  ความอยากอาหารลดลง  มีมีเสลดค้างในระบบย่อย";
            str += "ไข้หวัด เสมหะร้อน";
        }else if(position1 == 2 && position2 == 3 && position3 == 2){ /// 17
            str += "เสมหะแทรกปิตตะ"+'\n';
            str += "วาตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "เบื่ออาหาร  ความอยากอาหารลดลง  มีมีเสลดค้างในระบบย่อย ";
            str += "คอแห้ง เสมหะแห้ง  ท้องผูก";
        }else if(position1 == 2 && position2 == 3 && position3 == 3){ //18
            str += "เสมหะแทรกปิตตะ"+'\n'+"------------------------\n";
            str += "เบื่ออาหาร  ความอยากอาหารลดลง  มีมีเสลดค้างในระบบย่อย ";
        }else if(position1 == 3 && position2 == 1 && position3 == 1){ //19
            str += "เสมหะแทรกวาตะ"+'\n';
            str += "ปิตตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมเฉื่อย มึนศีรษะ หนักศรีษะ ง่วงนอนซึมเซา มีเสมหะค้างในทางเดินลม";
            str += "ไข้หวัด เสมหะร้อน ";
        }else if(position1 == 3 && position2 == 1 && position3 == 2){ //20
            str += "เสมหะแทรกวาตะ"+'\n';
            str += "วาตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมเฉื่อย มึนศีรษะ หนักศรีษะ ง่วงนอนซึมเซา มีเสมหะค้างในทางเดินลม";
            str += "คอแห้ง เสมหะแห้ง  ท้องผูก";
        }else if(position1 == 3 && position2 == 1 && position3 == 3){ //21
            str += "เสมหะแทรกวาตะ"+'\n'+"------------------------\n";
            str += "ลมเฉื่อย มึนศีรษะ หนักศรีษะ ง่วงนอนซึมเซา มีเสมหะค้างในทางเดินลม";
        }else if(position1 == 3 && position2 == 2 && position3 == 1){ //22
            str += "เสมหะแทรกวาตะ"+'\n';
            str += "วาตะแทรกปิตตะ"+'\n';
            str += "ปิตตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมเฉื่อย มึนศีรษะ หนักศรีษะ ง่วงนอนซึมเซา มีเสมหะค้างในทางเดินลม";
            str += "ไฟย่อยอ่อน มีแก๊สในท้องมาก  ท้องอืดแน่น  ระบบย่อยแปรปรวน อ่อนกำลัง";
            str += "ไข้หวัด เสมหะร้อน ";
        }else if(position1 == 3 && position2 == 2 && position3 == 2){ //23
            str += "เสมหะแทรกวาตะ"+'\n';
            str += "วาตะแทรกปิตตะ"+'\n';
            str += "วาตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมเฉื่อย มึนศีรษะ หนักศรีษะ ง่วงนอนซึมเซา มีเสมหะค้างในทางเดินลม";
            str += "ไฟย่อยอ่อน มีแก๊สในท้องมาก  ท้องอืดแน่น  ระบบย่อยแปรปรวน อ่อนกำลัง";
            str += "คอแห้ง เสมหะแห้ง  ท้องผูก ";
        }else if(position1 == 3 && position2 == 2 && position3 == 3){ //24
            str += "เสมหะแทรกวาตะ"+'\n';
            str += "วาตะแทรกปิตตะ"+'\n'+"------------------------\n";
            str += "ลมเฉื่อย มึนศีรษะ หนักศรีษะ ง่วงนอนซึมเซา มีเสมหะค้างในทางเดินลม";
            str += "ไฟย่อยอ่อน มีแก๊สในท้องมาก  ท้องอืดแน่น  ระบบย่อยแปรปรวน อ่อนกำลัง";
        }else if(position1 == 3 && position2 == 3 && position3 == 1){ //25
            str += "เสมหะแทรกวาตะ"+'\n';
            str += "เสมหะแทรกปิตตะ"+'\n';
            str += "ปิตตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมเฉื่อย มึนศีรษะ หนักศรีษะ ง่วงนอนซึมเซา มีเสมหะค้างในทางเดินลม ";
            str += "เบื่ออาหาร  ความอยากอาหารลดลง  มีมีเสลดค้างในระบบย่อย  ";
            str += "ไข้หวัด เสมหะร้อน  ";
        }else if(position1 == 3 && position2 == 3 && position3 == 2){ //26
            str += "เสมหะแทรกวาตะ"+'\n';
            str += "เสมหะแทรกปิตตะ"+'\n';
            str += "วาตะแทรกเสมหะ"+'\n'+"------------------------\n";
            str += "ลมเฉื่อย มึนศีรษะ หนักศรีษะ ง่วงนอนซึมเซา มีเสมหะค้างในทางเดินลม ";
            str += "เบื่ออาหาร  ความอยากอาหารลดลง  มีมีเสลดค้างในระบบย่อย ";
            str += "คอแห้ง เสมหะแห้ง  ท้องผูก";
        }else if(position1 == 3 && position2 == 3 && position3 == 3){
            str += "เสมหะแทรกวาตะ"+'\n';
            str += "เสมหะแทรกปิตตะ"+'\n'+"------------------------\n";
            str += "ลมเฉื่อย มึนศีรษะ หนักศรีษะ ง่วงนอนซึมเซา มีเสมหะค้างในทางเดินลม ";
            str += "เบื่ออาหาร  ความอยากอาหารลดลง  มีมีเสลดค้างในระบบย่อย ";
        }
        text1.setText(str);
        if(connectTag == true){
            sendMSG();
        }else{
            Toast.makeText(MainActivity.this, "Press \"START\" to send the data." , Toast.LENGTH_LONG).show();
        }
    }


    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check.
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //Check what request we’re responding to//
        if (requestCode == ENABLE_BT_REQUEST_CODE) {

            //If the request was successful…//
            if (resultCode == Activity.RESULT_OK) {

                //...then display the following toast.//
                Toast.makeText(MainActivity.this,"Bluetooth has been enabled" ,Toast.LENGTH_SHORT).show();
            }

            //If the request was unsuccessful...//
            if(resultCode == RESULT_CANCELED){

                //...then display this alternative toast.//
                Toast.makeText(MainActivity.this, "An error occurred while attempting to enable Bluetooth", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
