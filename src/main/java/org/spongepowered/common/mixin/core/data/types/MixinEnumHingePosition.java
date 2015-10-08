package org.spongepowered.common.mixin.core.data.types;

import net.minecraft.block.BlockDoor;
import org.spongepowered.api.data.type.Hinge;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockDoor.EnumHingePosition.class)
@Implements(@Interface(iface = Hinge.class, prefix = "hinge$"))
public abstract class MixinEnumHingePosition implements Hinge {

    @Shadow public abstract String getName();

    public String hinge$getId() {
        return this.getName();
    }

    @Intrinsic
    public String hinge$getName() {
        return this.getName();
    }

    public Hinge hinge$cycleNext() {
        return this.getName().equals("left") ? (Hinge) (Object) BlockDoor.EnumHingePosition.RIGHT : (Hinge) (Object) BlockDoor.EnumHingePosition.LEFT;
    }
}
