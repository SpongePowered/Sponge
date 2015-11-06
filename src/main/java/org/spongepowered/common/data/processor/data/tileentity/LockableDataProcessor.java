package org.spongepowered.common.data.processor.data.tileentity;

import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableLockableData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.LockableData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeLockableData;
import org.spongepowered.common.data.processor.common.AbstractSingleDataSingleTargetProcessor;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Optional;

public final class LockableDataProcessor extends AbstractSingleDataSingleTargetProcessor<ILockableContainer, String, Value<String>, LockableData, ImmutableLockableData> {

	public LockableDataProcessor() {
		super(Keys.LOCK_TOKEN, ILockableContainer.class);
	}

	@Override
	public DataTransactionResult remove(DataHolder dataHolder) {
		if (dataHolder instanceof ILockableContainer) {
			set((ILockableContainer) dataHolder, "");
			return DataTransactionBuilder.successNoData();
		}
		return DataTransactionBuilder.failNoData();
	}

	@Override
	protected boolean set(ILockableContainer entity, String value) {
		entity.setLockCode(value.length() == 0 ? LockCode.EMPTY_CODE : new LockCode(value));
		return true;
	}

	@Override
	protected Optional<String> getVal(ILockableContainer entity) {
		LockCode code = entity.getLockCode();
		if (code.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(code.getLock());
	}

	@Override
	protected ImmutableValue<String> constructImmutableValue(String value) {
		return new ImmutableSpongeValue<String>(Keys.LOCK_TOKEN, "", value);
	}

	@Override
	protected LockableData createManipulator() {
		return new SpongeLockableData();
	}

}
