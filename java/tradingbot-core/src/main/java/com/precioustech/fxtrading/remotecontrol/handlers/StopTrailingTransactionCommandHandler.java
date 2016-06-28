package com.precioustech.fxtrading.remotecontrol.handlers;

import org.apache.log4j.Logger;

import com.precioustech.fxtrading.helper.ProviderHelper;
import com.precioustech.fxtrading.remotecontrol.RemoteControl;

public class StopTrailingTransactionCommandHandler<M, N, L, T> implements CommandHandler {

	private static final Logger LOG = Logger.getLogger(StopTrailingTransactionCommandHandler.class);
	private final RemoteControl<M, N, L> remote;
	private final ProviderHelper<T, L> providerHelper;

	public StopTrailingTransactionCommandHandler(RemoteControl<M, N, L> remote, ProviderHelper<T, L> providerHelper) {
		this.remote = remote;
		this.providerHelper = providerHelper;
	}

	@Override
	public void handleCommand(String[] args) {
		if (args.length == 0) {
			LOG.warn("Expecting a transaction id(s) but received 0 arguments.");
			return;
		}
		for (String arg : args) {
			L transactionId = this.providerHelper.toTransactionId(arg);
			if (transactionId != null) {
				this.remote.stopTrailingTransaction(transactionId);
			}
		}
	}

}
