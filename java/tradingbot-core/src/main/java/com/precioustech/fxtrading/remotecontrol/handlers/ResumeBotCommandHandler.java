package com.precioustech.fxtrading.remotecontrol.handlers;

import com.precioustech.fxtrading.remotecontrol.RemoteControl;

public class ResumeBotCommandHandler<M, N, L> implements CommandHandler {

	private final RemoteControl<M, N, L> remote;

	public ResumeBotCommandHandler(RemoteControl<M, N, L> remote) {
		this.remote = remote;
	}

	@Override
	public void handleCommand(String[] args) {
		this.remote.resumeTrading();
	}

}
