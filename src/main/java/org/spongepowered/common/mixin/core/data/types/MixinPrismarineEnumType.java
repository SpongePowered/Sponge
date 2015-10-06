package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.block.BlockPrismarine;
import org.spongepowered.api.data.type.PrismarineType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockPrismarine.EnumType.class)
@Implements(@Interface(iface = PrismarineType.class, prefix = "type$"))
public abstract class MixinPrismarineEnumType implements PrismarineType {

    @Shadow private String unlocalizedName;

    public String type$getId() {
        return this.unlocalizedName;
    }

    public String type$getName() {
        return this.unlocalizedName;
    }
}
