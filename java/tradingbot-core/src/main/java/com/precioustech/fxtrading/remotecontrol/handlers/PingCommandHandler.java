package com.precioustech.fxtrading.remotecontrol.handlers;

import com.precioustech.fxtrading.remotecontrol.RemoteControl;

public class PingCommandHandler<M, N, L> implements CommandHandler {

	private final RemoteControl<M, N, L> remote;
	private final PingContentGenerator<String> contentGenerator;
	// private final TradeInfoService<L, N, M> tradeInfoService;

	public PingCommandHandler(RemoteControl<M, N, L> remote, PingContentGenerator<String> contentGenerator) {
		this.remote = remote;
		this.contentGenerator = contentGenerator;
		// this.tradeInfoService = tradeInfoService;
	}

	@Override
	public void handleCommand(String[] args) {
		remote.ping(contentGenerator.generate(args));
	}

}
