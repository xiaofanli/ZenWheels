package com.example.car;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
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
	public static BluetoothSerialService[] mBtSS = {null, null, null, null, null, null};
	public static final raceCarCodes codes = new raceCarCodes();
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    private static Context context;
    private static Handler handler = new Handler();
    private Intent data;
    private int lightsCount = 0;
    private int blinkLeftFlag = 0;
    private int blinkRightFlag = 0;
    private int faultFlag = 0;
    // Key names received from the BluetoothSerialService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    // Message types sent from the BluetoothSerialService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    
    private String serverIP = "192.168.1.100";
    private Button connectpc;
	private CmdListener cmdListener = null;
	private TextView addressText;
	private EditText steerParam;
	public static Map<String, String> mapping = new HashMap<String, String>();
	public static List<String> carName = new ArrayList<String>(), carAddr = new ArrayList<String>();
	public static List<String> addr2pos = new ArrayList<String>();
	public static List<String> name2pos = new ArrayList<String>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		context = this;
		// Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
		
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		
		addressText = (TextView) findViewById(R.id.address_text);
		steerParam = (EditText) findViewById(R.id.steer_param);
		
		Button pairedDevicesBtn = (Button)findViewById(R.id.bluetooth_connect);
		pairedDevicesBtn.setOnClickListener(new Button.OnClickListener() {
		    public void onClick(View v) {
		        data = new Intent(context,DeviceListActivity.class) ;
		        startActivityForResult(data,REQUEST_CONNECT_DEVICE_SECURE);
		    }
		});
		
//		mBtSS = new BluetoothSerialService(context, handler);
		for(int i = 0;i < mBtSS.length;i++)
			mBtSS[i] = new BluetoothSerialService(context, handler);
		
		addr2pos.add("00:06:66:61:AA:61");
		name2pos.add("White Car");
		addr2pos.add("00:06:66:45:9D:35");
		name2pos.add("Green Car");
		addr2pos.add("00:06:66:61:9F:38");
		name2pos.add("Red Car");
		addr2pos.add("00:06:66:49:A8:C4");
		name2pos.add("Black Car");
		addr2pos.add("00:06:66:61:A9:01");
		name2pos.add("Silver SUV");
		addr2pos.add("00:06:66:49:96:0C");
		name2pos.add("Orange Car");
		
		mapping.put("MicroCar-97", "White Car");
		mapping.put("MicroCar-53", "Green Car");
		mapping.put("MicroCar-56", "Red Car");
		mapping.put("MicroCar-96", "Black Car");
		mapping.put("MicroCar-1", "Silver SUV");
		mapping.put("MicroCar-12", "Orange Car");
		
		Button steerStop = (Button)findViewById(R.id.steer_stop);
		steerStop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				for(String name : carName){
					BluetoothSerialService btss = mBtSS[name2pos.indexOf(name)];
					if (btss == null || btss.getState() != BluetoothSerialService.STATE_CONNECTED) {
						Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
					}
	            	else {
	            		byte[] send = ByteBuffer.allocate(4).putInt(codes.NO_SPEED).array();
	            		btss.write(send);
	            	}
				}
			}
		});
/*		
		Button forward = (Button)findViewById(R.id.steer_front);
		forward.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		byte[] send = ByteBuffer.allocate(4).putInt(codes.SPEED_FRONT[31]).array();
    				mBtSS.write(send);
            	}
			}
		});
		
		Button backward = (Button)findViewById(R.id.steer_back);
		backward.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		byte[] send = ByteBuffer.allocate(4).putInt(codes.SPEED_BACK[31]).array();
    				mBtSS.write(send);
            	}
			}
		});
		
		Button steerLeft = (Button)findViewById(R.id.steer_left);
		steerLeft.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		String s = steerParam.getText().toString();
            		int param = 31;
            		if(s != null && !s.equals(""))
            			param = Integer.parseInt(s);
            		System.out.println("param: "+param);
            		
            		byte[] send = ByteBuffer.allocate(4).putInt(codes.STEER_LEFT[param]).array();
    				mBtSS.write(send);
            	}
			}
		});
		
		Button steerRight = (Button)findViewById(R.id.steer_right);
		steerRight.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		String s = steerParam.getText().toString();
            		int param = 31;
            		if(s != null && !s.equals(""))
            			param = Integer.parseInt(s);
            		System.out.println("param: "+param);
            		
            		byte[] send = ByteBuffer.allocate(4).putInt(codes.STEER_RIGHT[param]).array();
    				mBtSS.write(send);
            	}
			}
		});
		
		Button nosteer = (Button)findViewById(R.id.no_steer);
		nosteer.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		byte[] send = ByteBuffer.allocate(4).putInt(codes.NO_STEER).array();
    				mBtSS.write(send);
            	}
			}
		});
		
		Button horn = (Button)findViewById(R.id.horn);
		horn.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override			
		    public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN) {
					// Check that we're actually connected before trying anything
					if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
						Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
					}
					else {
						byte[] send = ByteBuffer.allocate(4).putInt(codes.HORN_ON).array();
						mBtSS.write(send);
					}
				}
				if(event.getAction() == MotionEvent.ACTION_UP){
					byte[] send = ByteBuffer.allocate(4).putInt(codes.HORN_OFF).array();
					mBtSS.write(send);
	            }
	            return true;
		    }
		});
		
		Button lights = (Button)findViewById(R.id.lights);
		lights.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		byte[] send = ByteBuffer.allocate(4).putInt(codes.LIGHTS_OFF).array();
            		if(lightsCount == 0) {
            			send = ByteBuffer.allocate(4).putInt(codes.LIGHTS_SOFT).array();
            			lightsCount++;
            		}
            		else if(lightsCount == 1) {
            			send = ByteBuffer.allocate(4).putInt(codes.LIGHTS).array();
            			lightsCount++;
            		}
            		else {
            			send = ByteBuffer.allocate(4).putInt(codes.LIGHTS_OFF).array();
            			lightsCount = 0;
            		}
            		mBtSS.write(send);
            	}
            }
        });
		
		
		Button onoffBtn = (Button)findViewById(R.id.onoff);
		onoffBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		mBtSS.stop();
            		mBtSS = new BluetoothSerialService(context, handler);
            	}
            }
        });
		
		Runnable blinkRunnable = new Runnable() {
			@Override
			public void run() {
				byte[] send = null;
				while(true) {
					if(blinkLeftFlag == 1) {
						//back
						send = ByteBuffer.allocate(4).putInt(codes.BLINK_LEFT[0]).array();
						//front
						mBtSS.write(send);
						send = ByteBuffer.allocate(4).putInt(codes.BLINK_LEFT[1]).array();
						mBtSS.write(send);
						try {
								Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						send = ByteBuffer.allocate(4).putInt(codes.BLINK_LEFT_OFF).array();
						mBtSS.write(send);
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else if(blinkRightFlag == 1) {
						//back
						send = ByteBuffer.allocate(4).putInt(codes.BLINK_RIGHT[0]).array();
						//front
						mBtSS.write(send);
						send = ByteBuffer.allocate(4).putInt(codes.BLINK_RIGHT[1]).array();
						mBtSS.write(send);
						try {
								Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						send = ByteBuffer.allocate(4).putInt(codes.BLINK_RIGHT_OFF).array();
						mBtSS.write(send);
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else if(faultFlag == 1) {
						for(int signal : codes.FAULT) {
							send = ByteBuffer.allocate(4).putInt(signal).array();
							mBtSS.write(send);
						}
						try {
								Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						for(int signal : codes.FAULT_OFF) {
							send = ByteBuffer.allocate(4).putInt(signal).array();
							mBtSS.write(send);
						}
						try {
							Thread.sleep(400);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					else {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
			}
		};
		
		final Thread blinkRunnableThread = new Thread(blinkRunnable);
		blinkRunnableThread.start();
		
		Button blinkLeft = (Button)findViewById(R.id.blink_left);
		blinkLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		if(blinkLeftFlag == 0) {
            			blinkLeftFlag = 1;
            		}
            		else if(blinkLeftFlag == 1) {
            			blinkLeftFlag = 0;
            		}
            	}
            }
        });
		
		Button blinkRight = (Button)findViewById(R.id.blink_right);
		blinkRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		if(blinkRightFlag == 0) {
            			blinkRightFlag = 1;
            		}
            		else if(blinkRightFlag == 1) {
            			blinkRightFlag = 0;
            		}
            	}
            }
        });
		
		Button fault = (Button)findViewById(R.id.fault);
		fault.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (mBtSS == null || mBtSS.getState() != BluetoothSerialService.STATE_CONNECTED) {
					Toast.makeText(context, R.string.not_connected, Toast.LENGTH_SHORT).show();
				}
            	else {
            		if(faultFlag == 0) {
            			faultFlag = 1;
            		}
            		else if(faultFlag == 1) {
            			faultFlag = 0;
            		}
            	}
            }
        });
*/		
		connectpc = (Button) findViewById(R.id.pc_connect);
		connectpc.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(CmdListener.connected){
					cmdListener.wakemeup = false;
					cmdListener.closeSocket();
//					connect_button.setText("Connect");
				}
				else{
//				else if(mBtSS != null && mBtSS.getState() == BluetoothSerialService.STATE_CONNECTED){
//					connect_button.setText("Connect");
					final EditText input = new EditText(context);
					input.setText(serverIP);
					new AlertDialog.Builder(context).setTitle("请输入IP").setIcon(android.R.drawable.ic_dialog_info)
							.setView(input).setNegativeButton("取消", null).setPositiveButton("确定",new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,int which) {
									if(cmdListener == null){
										cmdListener = new CmdListener(input.getText().toString(), connectpc);
										new Thread(cmdListener).start();
									}
									else{
										cmdListener.wakemeup = true;
										synchronized (cmdListener.wakeObj) {
											System.out.println("notify");
											cmdListener.wakeObj.notify();
										}
									}
									synchronized (cmdListener.obj) {
										try {
											cmdListener.obj.wait();
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									if (CmdListener.connected) {
//										connect_button.setText("Disconnect");
										Toast.makeText(getApplicationContext(),"Connection Successful",Toast.LENGTH_LONG).show();
									} else {
//										connect_button.setText("Connect");
										Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_LONG).show();
									}
								}
							}).show();
				}
			}
		});
	}
	
	@Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        } else {
//            if (mBtSS == null) 
//            	mBtSS = new BluetoothSerialService(context, handler);
        	for(BluetoothSerialService bt : mBtSS)
        		if(bt == null)
        			bt = new BluetoothSerialService(context, handler);
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
        if(!carName.contains(name)){
        	System.out.println(name);
	        carAddr.add(address);
	        carName.add(name);
	        if(cmdListener != null)
	        	cmdListener.sendCarInfo();
        }
//        addressText.setText(name+"\n"+address);
        
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
//        mBtSS.connect(device, secure);
        mBtSS[addr2pos.indexOf(device.getAddress())].connect(device, secure);
        
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
