package com.precioustech.fxtrading;

public final class ObjectWrapper<T> {

	private final T innerObject;

	public ObjectWrapper(T objToWrap) {
		this.innerObject = objToWrap;
	}

	public T getWrappedObject() {
		return this.innerObject;
	}
}
