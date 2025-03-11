package com.simibubi.create.infrastructure.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

import net.minecraft.util.Unit;

public class TransactionSuccessCallback extends SnapshotParticipant<Unit> {
	private final Runnable callback;

	private TransactionSuccessCallback(Runnable callback) {
		this.callback = callback;
	}

	@Override
	protected Unit createSnapshot() {
		return Unit.INSTANCE;
	}

	@Override
	protected void readSnapshot(Unit snapshot) {
	}

	@Override
	protected void onFinalCommit() {
		this.callback.run();
	}

	public static void register(TransactionContext ctx, Runnable callback) {
		new TransactionSuccessCallback(callback).updateSnapshots(ctx);
	}
}
