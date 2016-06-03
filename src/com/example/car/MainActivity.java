package com.example.car;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.BluetoothSerialService;
import com.example.bluetooth.DeviceListActivity;
import com.example.command.CmdListener;
import com.example.zenwheels.R;

public class MainActivity extends Activity{
	private BluetoothAdapter mBluetoothAdapter = null;
//	public static BluetoothSerialService[] mBtSS = {null, null, null, null, null, null};
	public static Map<String, BluetoothSerialService> mBtSS = new HashMap<String, BluetoothSerialService>();
	public static final RaceCarCodes codes = new RaceCarCodes();
	public static final ExecutorService threadPool = Executors.newCachedThreadPool();
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    private Context context;
    private Handler handler = new Handler(){
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
			case MESSAGE_DEVICE_CONNECTED:{
				connectedCars.add(carAddrMap.get(((BluetoothDevice)msg.obj).getAddress()));
				if(cmdListener != null)
					cmdListener.sendConnectedCarInfo();
			}
				break;
			case MESSAGE_DEVICE_DISCONNECTED:{
				String car = carAddrMap.get(((BluetoothDevice)msg.obj).getAddress());
				connectedCars.remove(car);
				if(cmdListener != null)
					cmdListener.sendLostCarInfo(car);
			}
				break;
			case R.string.pc_connected:
				if(connectpc != null)
					connectpc.setText("Connected");
				break;
			case R.string.pc_disconnected:
				if(connectpc != null)
					connectpc.setText("Disconnected");
				break;	
			default:
				break;
			}
    	};
    };
    private Intent data;
    private int lightsCount = 0;
    private int blinkLeftFlag = 0;
    private int blinkRightFlag = 0;
    private int faultFlag = 0;
    // Key names received from the BluetoothSerialService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDR = "device_addr";
    public static final String TOAST = "toast";
    // Message types sent from the BluetoothSerialService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_DEVICE_CONNECTED = 6;
    public static final int MESSAGE_DEVICE_DISCONNECTED = 7;
    
	public static final String SILVER = "Silver SUV";//A
	public static final String GREEN = "Green Car";//B
	public static final String RED = "Red Car";//C
	public static final String WHITE = "White Car";//D
	public static final String BLACK = "Black Car";//E
	public static final String ORANGE = "Orange Car";//F
    
    private static String IP = "192.168.1.121";
    public static final int PORT = 8888;
    private static Button connectpc;
	private CmdListener cmdListener = null;
	private TextView addressText;
	private EditText steerParam;
	public static Map<String, String> mapping = new HashMap<String, String>();
	public static Map<String, String> carAddrMap = new HashMap<String, String>();
	public static Set<String> connectedCars = new HashSet<String>();
//	public static List<String> carName = new ArrayList<String>(), carAddr = new ArrayList<String>();
//	public static List<String> addr2pos = new ArrayList<String>();
//	public static List<String> name2pos = new ArrayList<String>();
	
	static{
		carAddrMap.put(SILVER, "00:06:66:61:A9:01");
		carAddrMap.put(WHITE, "00:06:66:61:AA:61");
		carAddrMap.put(GREEN, "00:06:66:45:9D:35");
		carAddrMap.put(RED, "00:06:66:61:9F:38");
		carAddrMap.put(BLACK, "00:06:66:49:A8:C4");
		carAddrMap.put(ORANGE, "00:06:66:49:96:0C");
		
		carAddrMap.put("00:06:66:61:A9:01", SILVER);
		carAddrMap.put("00:06:66:61:AA:61", WHITE);
		carAddrMap.put("00:06:66:45:9D:35", GREEN);
		carAddrMap.put("00:06:66:61:9F:38", RED);
		carAddrMap.put("00:06:66:49:A8:C4", BLACK);
		carAddrMap.put("00:06:66:49:96:0C", ORANGE);
		
		mapping.put("MicroCar-97", "White Car");
		mapping.put("MicroCar-53", "Green Car");
		mapping.put("MicroCar-56", "Red Car");
		mapping.put("MicroCar-96", "Black Car");
		mapping.put("MicroCar-1", "Silver SUV");
		mapping.put("MicroCar-12", "Orange Car");
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		System.out.println(Thread.currentThread());
		context = this;
		// Get local Bluetooth adapter
		if(mBluetoothAdapter == null)
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
		
//		addressText = (TextView) findViewById(R.id.address_text);
//		steerParam = (EditText) findViewById(R.id.steer_param);
		
		Button pairedDevicesBtn = (Button)findViewById(R.id.bluetooth_connect);
		pairedDevicesBtn.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		        data = new Intent(context,DeviceListActivity.class) ;
		        startActivityForResult(data,REQUEST_CONNECT_DEVICE_SECURE);
		    }
		});
		
//		addr2pos.add("00:06:66:61:AA:61");
//		name2pos.add("White Car");
//		addr2pos.add("00:06:66:45:9D:35");
//		name2pos.add("Green Car");
//		addr2pos.add("00:06:66:61:9F:38");
//		name2pos.add("Red Car");
//		addr2pos.add("00:06:66:49:A8:C4");
//		name2pos.add("Black Car");
//		addr2pos.add("00:06:66:61:A9:01");
//		name2pos.add("Silver SUV");
//		addr2pos.add("00:06:66:49:96:0C");
//		name2pos.add("Orange Car");
		
		connectpc = (Button) findViewById(R.id.pc_connect);
		connectpc.setText(R.string.pc_disconnected);
		if(cmdListener == null){
			cmdListener = new CmdListener(null, connectpc, mBluetoothAdapter, context, handler);
			new Thread(cmdListener).start();
		}
	}
	
	@Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
//        	for(int i = 0;i < mBtSS.length;i++)
//        		if(mBtSS[i] == null)
//        			mBtSS[i] = new BluetoothSerialService(context, handler);
        }
    }
	
	@Override
	protected void onPause() {
		super.onPause();
//		if(mBtSS != null) {
//			mBtSS.stop();
//		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
	    // See which child activity is calling us back.
	    switch (requestCode) {
	        case REQUEST_CONNECT_DEVICE_SECURE:
	            if (resultCode == Activity.RESULT_OK){
	                connectDevice(data, true);
	                Log.d("CONECTARE","m-am conectat");
	                Toast.makeText(context, "m-am conectat", Toast.LENGTH_SHORT).show();
	            } 
	            break;
	        case REQUEST_ENABLE_BT:
	            break;
	    }
	}
	
	private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
		String name = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        if(mBluetoothAdapter == null) {
        	return;
        }
//        if(!carName.contains(name)){
//        	System.out.println(name);
//	        carAddr.add(address);
//	        carName.add(name);
//	        if(cmdListener != null)
//	        	cmdListener.sendCarInfo();
//        }
//        addressText.setText(name+"\n"+address);
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
//        mBtSS.connect(device, secure);
//        mBtSS[addr2pos.indexOf(device.getAddress())].connect(device, secure);        
    }
	
	@Override
	protected void onResume() {
		  super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
