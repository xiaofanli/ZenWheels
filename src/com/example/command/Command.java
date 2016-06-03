package com.example.command;

import java.net.Socket;

public class Command {
	final String car;
	final int cmd, param;
	final Socket socket;

	public final static int STOP = 0;
	public final static int FORWARD = 1;
	public final static int BACKWARD = 2;
	public final static int LEFT = 3;
	public final static int RIGHT = 4;
	public final static int NO_STEER = 5;
	public final static int HORN = 6;
	public final static int CONNECT = 7;
	public final static int DISCONNECT = 8;
	
//	public Command(int cmd, int param) {
//		this.cmd = cmd;
//		this.param = param;
//	}
	
	public Command(String car, int cmd, int param, Socket socket) {
		this.car = car;
		this.cmd = cmd;
		this.param = param;
		this.socket = socket;
	}
}
