package com.simibubi.create.infrastructure.fabric.transfer;

import com.simibubi.create.infrastructure.fabric.ProcessingIterator;

import com.simibubi.create.infrastructure.fabric.util.OneTimeRunnable;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Iterator;

public class ChangeListeningStorageWrapper<T> implements Storage<T> {
	private final Storage<T> wrapped;
	private final Runnable onChange;

	public ChangeListeningStorageWrapper(Storage<T> wrapped, Runnable onChange) {
		this.wrapped = wrapped;
		// wrap runnable, since many success callbacks might be registered
		this.onChange = new OneTimeRunnable(onChange);
	}

	@Override
	public boolean supportsInsertion() {
		return this.wrapped.supportsInsertion();
	}

	@Override
	public long insert(T resource, long maxAmount, TransactionContext transaction) {
		TransactionSuccessCallback.register(transaction, this.onChange);
		return this.wrapped.insert(resource, maxAmount, transaction);
	}

	@Override
	public boolean supportsExtraction() {
		return this.wrapped.supportsExtraction();
	}

	@Override
	public long extract(T resource, long maxAmount, TransactionContext transaction) {
		TransactionSuccessCallback.register(transaction, this.onChange);
		return this.wrapped.extract(resource, maxAmount, transaction);
	}

	@Override
	public Iterator<StorageView<T>> iterator() {
		return wrapIterator(this.wrapped.iterator(), this.onChange);
	}

	@Override
	public Iterator<StorageView<T>> nonEmptyIterator() {
		return wrapIterator(this.wrapped.nonEmptyIterator(), this.onChange);
	}

	@Override
	public Iterable<StorageView<T>> nonEmptyViews() {
		return this::nonEmptyIterator;
	}

	@Override
	public long getVersion() {
		return this.wrapped.getVersion();
	}

	public static <T> Iterator<StorageView<T>> wrapIterator(Iterator<StorageView<T>> iterator, Runnable onChange) {
		return new ProcessingIterator<>(iterator, view -> new ChangeListeningViewWrapper<>(view, onChange));
	}
}
