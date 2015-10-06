package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.block.BlockStone;
import org.spongepowered.api.data.type.StoneType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockStone.EnumType.class)
@Implements(@Interface(iface = StoneType.class, prefix = "type$"))
public abstract class MixinStoneEnumType implements StoneType {

    @Shadow private String name;

    public String type$getId() {
        return this.name;
    }

    public String type$getName() {
        return this.name;
    }
}
