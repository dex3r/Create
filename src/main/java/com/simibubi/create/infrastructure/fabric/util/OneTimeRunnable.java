package com.simibubi.create.infrastructure.fabric.util;

public class OneTimeRunnable implements Runnable {
	private final Runnable wrapped;
	private boolean hasRan;

	public OneTimeRunnable(Runnable wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void run() {
		if (!this.hasRan) {
			this.hasRan = true;
			this.wrapped.run();
		}
	}
}
