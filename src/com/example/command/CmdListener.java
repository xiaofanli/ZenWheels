package com.example.command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import com.example.bluetooth.BluetoothSerialService;
import com.example.car.MainActivity;
import com.example.zenwheels.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

public class CmdListener extends Thread{
	private BluetoothAdapter mBluetoothAdapter = null;
	private Queue<Command> queue = new LinkedList<Command>();
	private Socket socket = null;
	private ServerSocket server = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private String ip = null;
	private Context context;
    private Handler handler;
	public Object wakeObj = new Object();
	public boolean wakemeup = true;
//	public static boolean connected = false;
	
	public CmdListener(String ip, Button conn, BluetoothAdapter mBluetoothAdapter, Context context, Handler handler) {
		this.ip = ip;
		this.mBluetoothAdapter = mBluetoothAdapter;
		this.context = context;
		this.handler = handler;
	}
	
	private Thread instrHandler = new Thread(){
//		public Object wakemeup = new Object();
		public void run() {
			while(true){
				while(queue.isEmpty()){
					synchronized (queue) {
						try {
							queue.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				
				synchronized (queue) {
					Command cmd = queue.poll();
					if(cmd != null){
						BluetoothSerialService btss = MainActivity.mBtSS.get(cmd.car);
						switch(cmd.cmd){
						case Command.STOP:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.NO_SPEED).array();
			            		btss.write(send);
			            	}
							break;
						case Command.FORWARD:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.SPEED_FRONT[cmd.param]).array();
			            		btss.write(send);
			            		System.out.println("forward sent");
			            	}
							break;
						case Command.BACKWARD:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.SPEED_BACK[cmd.param]).array();
			            		btss.write(send);
			            	}
							break;
						case Command.LEFT:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.STEER_LEFT[cmd.param]).array();
			            		btss.write(send);
//			            		System.out.println("steer left");
			            	}
							break;
						case Command.RIGHT:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.STEER_RIGHT[cmd.param]).array();
			            		btss.write(send);
//			            		System.out.println("steer right");
			            	}
							break;
						case Command.NO_STEER:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.NO_STEER).array();
			            		btss.write(send);
			            	}
							break;
						case Command.HORN:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		btss.write(ByteBuffer.allocate(4).putInt(MainActivity.codes.HORN_ON).array());
			            		MainActivity.threadPool.execute(new HornTask(btss, cmd.param));
			            	}
							break;
						case Command.CONNECT:
							if(btss == null){
								btss = new BluetoothSerialService(context, handler);
								MainActivity.mBtSS.put(cmd.car, btss);
							}
							BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(MainActivity.carAddrMap.get(cmd.car));
							if(btss.getState() == BluetoothSerialService.STATE_CONNECTED)
								handler.obtainMessage(MainActivity.MESSAGE_DEVICE_CONNECTED, device).sendToTarget();
							else{
								if(!btss.waiting4Listen)
									MainActivity.threadPool.execute(btss.new Wait4ListenTask(device, true));
//								btss.connect(device, true);
							}
							break;
						case Command.DISCONNECT:
							if(btss != null)
								btss.stop();
							break;
						}
//						System.out.println(cmd.cmd+" "+cmd.param);
					}
				}
			}
		}
		
	};
	
	private class HornTask implements Runnable{
		private BluetoothSerialService btss = null;
		private int time = 0;
		
		public HornTask(BluetoothSerialService btss, int time) {
			this.btss = btss;
			this.time = time;
		}
		
		public void run() {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED)
        		btss.write(ByteBuffer.allocate(4).putInt(MainActivity.codes.HORN_OFF).array());
		}
	}

	public void run() {
		instrHandler.setDaemon(true);
		instrHandler.start();
		try {
			System.out.println("here");
			server = new ServerSocket(MainActivity.PORT);
			System.out.println("there");
		} catch (IOException e2) {
			System.out.println("there2");
			e2.printStackTrace();
		}
		if(server == null)
			System.err.println("server is null");
		while(true){
			try {
//				socket = new Socket(ip, MainActivity.PORT);
				socket = server.accept();
				socket.setTcpNoDelay(true);
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				sendConnectedCarInfo();
				handler.obtainMessage(R.string.pc_connected).sendToTarget();
			}catch (IOException e) {
				e.printStackTrace();
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				handler.obtainMessage(R.string.pc_disconnected).sendToTarget();
			}
			
			while(true){
				try {
					String s = in.readUTF();
					System.out.println(s);
					String[] cmd = s.split("_");
//					System.out.println("NEW CMD");
					int cw = Integer.parseInt(cmd[1]);
					int param = Integer.parseInt(cmd[2]);
					synchronized (queue) {
						queue.add(new Command(cmd[0], cw, param, socket));
						queue.notify();
					}
				} catch (IOException e) {
					e.printStackTrace();
					try {
						System.out.println("close socket");
						socket.close();
						System.out.println("close socket2");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					handler.obtainMessage(R.string.pc_disconnected).sendToTarget();
					break; 
				}
			}
			
			if(wakemeup){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else{
				synchronized (wakeObj) {
					try {
						System.out.println("wait");
						wakeObj.wait();
						System.out.println("wake up");
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
	
	public void write(String str){
		if(!isConnected())
			return;
		synchronized (out) {
			try {
				out.writeUTF(str);
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void sendConnectedCarInfo(){
		if(isConnected() && !MainActivity.connectedCars.isEmpty()){
			String str = "ADD";
			for(String car : MainActivity.connectedCars)
				str += "_" + car;
			write(str);
		}
	}
	
	public void sendLostCarInfo(String car){
		System.out.println(car+" lost sending");
		if(isConnected()){
			write("REMOVE_" + car);
			System.out.println(car+" lost sent");
		}
	}
	
	public boolean isConnected(){
		return socket != null && socket.isConnected() && !socket.isClosed();
	}
	
	public void closeSocket(){
		if(isConnected()){
			try {
				in.close();
				socket.close();
//				connected = false;
				handler.obtainMessage(R.string.pc_disconnected).sendToTarget();
//				new LongTimeTask().execute("Connect");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
}
