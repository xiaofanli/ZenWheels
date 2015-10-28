package com.example.command;

public class Command {
	int carid, cmd, param;

	public final static int STOP = 0;
	public final static int FORWARD = 1;
	public final static int BACKWARD = 2;
	public final static int LEFT = 3;
	public final static int RIGHT = 4;
	public final static int NO_STEER = 5;
	public final static int HORN = 6;
	
	public Command(int cmd, int param) {
		this.cmd = cmd;
		this.param = param;
	}
	
	public Command(int carid, int cmd, int param) {
		this.carid = carid;
		this.cmd = cmd;
		this.param = param;
	}
}
