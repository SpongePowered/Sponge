package org.spongepowered.common.data.processor.data.tileentity;

import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntitySign;

import org.spongepowered.api.block.tileentity.Banner;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBannerData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BannerData;
import org.spongepowered.api.data.merge.MergeFunction;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.service.persistence.InvalidDataException;
import org.spongepowered.common.data.DataProcessor;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBannerData;
import org.spongepowered.common.data.manipulator.mutable.tileentity.SpongeBannerData;
import org.spongepowered.common.data.processor.common.AbstractSpongeDataProcessor;

import com.google.common.base.Optional;

public class BannerDataProcessor extends AbstractSpongeDataProcessor<BannerData, ImmutableBannerData> {

	@Override
	public Optional<BannerData> createFrom(DataHolder dataHolder) {
		if (dataHolder instanceof TileEntityBanner) {
			((TileEntityBanner) dataHolder).getBaseColor();
			((TileEntityBanner) dataHolder).getPatternList();
			
			
			final Optional<BannerData> oldData = ((Banner) dataHolder).getData();
			
		}
		return null;
	}

	@Override
	public boolean supports(DataHolder dataHolder) {
		return dataHolder instanceof TileEntityBanner;
	}

	@Override
	public Optional<BannerData> from(DataHolder dataHolder) {
		if (dataHolder instanceof TileEntitySign) {
			((TileEntityBanner) dataHolder).getBaseColor();
			((TileEntityBanner) dataHolder).getPatternList();
			final BannerData signData = new SpongeBannerData();
			
		}
		return null;
	}

	@Override
	public Optional<BannerData> fill(DataHolder dataHolder,
			BannerData manipulator, MergeFunction overlap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<BannerData> fill(DataContainer container, BannerData m) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataTransactionResult set(DataHolder dataHolder,
			BannerData manipulator, MergeFunction function) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ImmutableBannerData> with(Key<? extends BaseValue<?>> key,
			Object value, ImmutableBannerData immutable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DataTransactionResult remove(DataHolder dataHolder) {
		// TODO Auto-generated method stub
		return null;
	}

}
