package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.block.BlockRedstoneComparator;
import org.spongepowered.api.data.type.ComparatorType;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockRedstoneComparator.Mode.class)
@Implements(@Interface(iface = ComparatorType.class, prefix = "mode$"))
public abstract class MixinComparatorMode implements ComparatorType {

    @Shadow private String name;

    public String mode$getId() {
        return this.name;
    }

    public String mode$getName() {
        return this.name;
    }
}
