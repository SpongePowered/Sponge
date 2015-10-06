package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.block.BlockQuartz;
import org.spongepowered.api.data.type.QuartzType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockQuartz.EnumType.class)
@Implements(@Interface(iface = QuartzType.class, prefix = "type$"))
public abstract class MixinQuartsEnumType implements QuartzType {

    @Shadow private String unlocalizedName;

    public String type$getId() {
        return this.unlocalizedName;
    }

    public String type$getName() {
        return this.unlocalizedName;
    }
}
