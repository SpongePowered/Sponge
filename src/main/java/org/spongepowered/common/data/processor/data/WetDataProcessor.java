package org.spongepowered.common.data.processor.data;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.ImmutableWetData;
import org.spongepowered.api.data.manipulator.mutable.SkullData;
import org.spongepowered.api.data.manipulator.mutable.WetData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;

import com.google.common.base.Optional;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class WetDataProcessor extends AbstractSpongeDataProcessor<WetData, ImmutableWetData> {

	@Override
	public boolean supports(DataHolder dataHolder) {
		return dataHolder instanceof ItemStack && ((ItemStack) dataHolder).getItem().equals(Item.getItemFromBlock(Blocks.sponge));
	}

	@Override
	public Optional<WetData> from(DataHolder dataHolder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<WetData> createFrom(DataHolder dataHolder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<WetData> fill(DataHolder dataHolder, WetData manipulator, MergeFunction overlap) {
		if (this.supports(dataHolder)) {
            
        }
        return Optional.absent();
	}

	@Override
	public Optional<WetData> fill(DataContainer container, WetData m) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataTransactionResult set(DataHolder dataHolder, WetData manipulator, MergeFunction function) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ImmutableWetData> with(Key<? extends BaseValue<?>> key, Object value, ImmutableWetData immutable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataTransactionResult remove(DataHolder dataHolder) {
		// TODO Auto-generated method stub
		return null;
	}

}
