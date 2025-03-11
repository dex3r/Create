package com.simibubi.create.infrastructure.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public record ChangeListeningViewWrapper<T>(StorageView<T> wrapped, Runnable onChange) implements StorageView<T> {
	@Override
	public long extract(T resource, long maxAmount, TransactionContext transaction) {
		TransactionSuccessCallback.register(transaction, this.onChange);
		return this.wrapped.extract(resource, maxAmount, transaction);
	}

	@Override
	public boolean isResourceBlank() {
		return this.wrapped.isResourceBlank();
	}

	@Override
	public T getResource() {
		return this.wrapped.getResource();
	}

	@Override
	public long getAmount() {
		return this.wrapped.getAmount();
	}

	@Override
	public long getCapacity() {
		return this.wrapped.getCapacity();
	}

	@Override
	public StorageView<T> getUnderlyingView() {
		return new ChangeListeningViewWrapper<>(this.wrapped.getUnderlyingView(), this.onChange);
	}
}
