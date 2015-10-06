package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.block.BlockRailBase;
import org.spongepowered.api.data.type.RailDirection;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockRailBase.EnumRailDirection.class)
@Implements(@Interface(iface = RailDirection.class, prefix = "rail$"))
public abstract class MixinRailDirection implements RailDirection {

    @Shadow private String name;
    @Shadow private int meta;

    public String rail$getId() {
        return this.name;
    }

    public String rail$getName() {
        return this.name;
    }

    public RailDirection rail$cycleNext() {
        return (RailDirection) (Object) BlockRailBase.EnumRailDirection.byMetadata((this.meta + 1) % 9);
    }
}
