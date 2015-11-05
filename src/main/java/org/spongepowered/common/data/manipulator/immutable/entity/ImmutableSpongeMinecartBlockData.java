package org.spongepowered.common.data.manipulator.immutable.entity;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import net.minecraft.init.Blocks;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableMinecartBlockData;
import org.spongepowered.api.data.manipulator.mutable.entity.MinecartBlockData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableData;
import org.spongepowered.common.data.manipulator.mutable.entity.SpongeMinecartBlockData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

import java.util.Map;

public class ImmutableSpongeMinecartBlockData extends AbstractImmutableData<ImmutableMinecartBlockData, MinecartBlockData> implements ImmutableMinecartBlockData {

    private final BlockState block;
    private final int offset;

    private final ImmutableValue<BlockState> blockValue;
    private final ImmutableValue<Integer> offsetValue;

    public ImmutableSpongeMinecartBlockData() {
        this((BlockState) Blocks.air.getDefaultState(), 6);
    }

    public ImmutableSpongeMinecartBlockData(BlockState block, int offset) {
        super(ImmutableMinecartBlockData.class);
        this.block = Preconditions.checkNotNull(block);
        this.offset = offset;
        this.blockValue = new ImmutableSpongeValue<>(Keys.REPRESENTED_BLOCK, (BlockState) Blocks.air.getDefaultState(), block);
        this.offsetValue = new ImmutableSpongeValue<>(Keys.OFFSET, 6, offset);
        registerGetters();
    }

    @Override
    public ImmutableValue<BlockState> block() {
        return blockValue;
    }

    @Override
    public ImmutableValue<Integer> offset() {
        return offsetValue;
    }

    @Override
    public MinecartBlockData asMutable() {
        return new SpongeMinecartBlockData(this.block, this.offset);
    }

    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(ImmutableMinecartBlockData o) {
        Map oTraits = o.block().get().getTraitMap();
        Map traits = this.block.getTraitMap();
        return ComparisonChain.start()
                .compare(oTraits.entrySet().containsAll(traits.entrySet()), traits.entrySet().containsAll(oTraits.entrySet()))
                .compare((Integer) this.offset, o.offset().get())
                .result();
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Keys.REPRESENTED_BLOCK, this.block)
                .set(Keys.OFFSET, this.offset);
    }

    public BlockState getBlock() {
        return block;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    protected void registerGetters() {
        registerKeyValue(Keys.REPRESENTED_BLOCK, ImmutableSpongeMinecartBlockData.this::block);
        registerKeyValue(Keys.OFFSET, ImmutableSpongeMinecartBlockData.this::offset);

        registerFieldGetter(Keys.REPRESENTED_BLOCK, ImmutableSpongeMinecartBlockData.this::getBlock);
        registerFieldGetter(Keys.OFFSET, ImmutableSpongeMinecartBlockData.this::getOffset);
    }
}
