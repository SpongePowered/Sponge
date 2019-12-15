package org.spongepowered.common.data.provider.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.RedstoneSide;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class RedstoneWireBlockWireAttachmentProvider extends BlockStateDataProvider<WireAttachmentType> {

    private final EnumProperty<RedstoneSide> property;

    RedstoneWireBlockWireAttachmentProvider(Key<? extends Value<WireAttachmentType>> key,
            EnumProperty<RedstoneSide> property) {
        super(key, RedstoneWireBlock.class);
        this.property = property;
    }

    @Override
    protected Optional<WireAttachmentType> getFrom(BlockState dataHolder) {
        return Optional.of((WireAttachmentType) (Object) dataHolder.get(this.property));
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, WireAttachmentType value) {
        return Optional.of(dataHolder.with(this.property, (RedstoneSide) (Object) value));
    }
}
