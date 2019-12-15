package org.spongepowered.common.data.provider.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.RedstoneSide;
import org.spongepowered.api.data.Key;
import org.spongepowered.api.data.type.WireAttachmentType;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.util.Direction;
import org.spongepowered.common.data.provider.BlockStateDataProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("ConstantConditions")
public class RedstoneWireBlockWireAttachmentsProvider extends BlockStateDataProvider<Map<Direction, WireAttachmentType>> {

    private final Map<Direction, EnumProperty<RedstoneSide>> sides;

    RedstoneWireBlockWireAttachmentsProvider(Key<? extends Value<Map<Direction, WireAttachmentType>>> key,
            Class<? extends Block> blockType, Map<Direction, EnumProperty<RedstoneSide>> sides) {
        super(key, blockType);
        this.sides = sides;
    }

    @Override
    protected Optional<Map<Direction, WireAttachmentType>> getFrom(BlockState dataHolder) {
        final Map<Direction, WireAttachmentType> attachments = new HashMap<>();
        for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : this.sides.entrySet()) {
            attachments.put(entry.getKey(), (WireAttachmentType) (Object) dataHolder.get(entry.getValue()));
        }
        return Optional.of(attachments);
    }

    @Override
    protected Optional<BlockState> set(BlockState dataHolder, Map<Direction, WireAttachmentType> value) {
        for (final Map.Entry<Direction, EnumProperty<RedstoneSide>> entry : this.sides.entrySet()) {
            RedstoneSide type = (RedstoneSide) (Object) value.get(entry.getKey());
            if (type == null) {
                type = RedstoneSide.NONE;
            }
            dataHolder = dataHolder.with(entry.getValue(), type);
        }
        return Optional.of(dataHolder);
    }
}
