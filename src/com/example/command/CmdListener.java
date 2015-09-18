package com.example.command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.Queue;

import com.example.bluetooth.BluetoothSerialService;
import com.example.car.MainActivity;
import android.os.AsyncTask;
import android.widget.Button;

public class CmdListener extends Thread{
	private Queue<Command> queue = new LinkedList<Command>();
	private Socket socket = null;
	private DataInputStream in = null;
	private DataOutputStream out = null;
	private String ip = null;
	public Object wakeObj = new Object(), obj = new Object();
	public boolean wakemeup = true;
	public static boolean connected = false;
	Button connButton = null;
	
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
						BluetoothSerialService btss = MainActivity.mBtSS[cmd.carid];
						switch(cmd.cmd){
						case 0:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.NO_SPEED).array();
			            		btss.write(send);
			            	}
							break;
						case 1:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.SPEED_FRONT[cmd.param]).array();
			            		btss.write(send);
			            	}
							break;
						//left
						case 3:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.STEER_LEFT[cmd.param]).array();
			            		btss.write(send);
			            		System.out.println("steer left");
			            	}
							break;
						//right	
						case 4:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.STEER_RIGHT[cmd.param]).array();
			            		btss.write(send);
			            		System.out.println("steer right");
			            	}
							break;
						//no steer
						case 5:
							if (btss != null && btss.getState() == BluetoothSerialService.STATE_CONNECTED) {
			            		byte[] send = ByteBuffer.allocate(4).putInt(MainActivity.codes.NO_STEER).array();
			            		btss.write(send);
			            	}
							break;
						}
						System.out.println(cmd.cmd+" "+cmd.param);
					}
				}
			}
		}
		
	};
	
	public CmdListener(String ip, Button conn) {
		this.ip = ip;
		connButton = conn;
//		new Thread(new Handler()).start();
	}
	@Override
	public void run() {
		instrHandler.setDaemon(true);
		instrHandler.start();
		while(true){
			try {
				socket = new Socket(ip, 8888);
				socket.setTcpNoDelay(true);
				in = new DataInputStream(socket.getInputStream());
				out = new DataOutputStream(socket.getOutputStream());
				connected = true;
				sendCarInfo();
				new LongTimeTask().execute("Disconnect");
				synchronized (obj) {
					obj.notify();
				}
			}catch (IOException e) {
				e.printStackTrace();
				connected = false;
				new LongTimeTask().execute("Connect");
			}
			
			while(connected){
				try {
					String[] cmd = in.readUTF().split("_");
//					System.out.println("NEW CMD");
					int carid = MainActivity.name2pos.indexOf(cmd[0]);
					int cw = Integer.parseInt(cmd[1]);
					int param = Integer.parseInt(cmd[2]);
					synchronized (queue) {
						queue.add(new Command(carid, cw, param));
						queue.notify();
					}
				} catch (IOException e) {
					e.printStackTrace();
					connected = false;
					new LongTimeTask().execute("Connect");
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
	
	public void sendCarInfo(){
		if(connected && MainActivity.carName != null && MainActivity.carAddr != null){
			String str = "zenwheels";
			for(int i = 0;i < MainActivity.carName.size();i++)
				str += "_"+ MainActivity.carName.get(i)+"_"+MainActivity.carAddr.get(i);
			try {
				out.writeUTF(str);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void closeSocket(){
		if(connected){
			try {
				in.close();
				socket.close();
				connected = false;
				new LongTimeTask().execute("Connect");
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	
	class LongTimeTask extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			return params[0];
		}
		
		@Override
		protected void onPostExecute(String result) {
			connButton.setText(result);
		}
	}
}
