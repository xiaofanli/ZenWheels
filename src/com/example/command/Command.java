package com.example.command;

public class Command {
	int carid, cmd, param;

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
