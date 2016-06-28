package com.precioustech.fxtrading.remotecontrol.handlers;

public interface PingContentGenerator<T> {

	T generate(String args[]);
}
