package org.spongepowered.common.mixin.core.util.datafix;

import net.minecraft.util.datafix.DataFixer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(DataFixer.class)
public abstract class MixinDataFixer {

    @Shadow @Final public int version;

    @ModifyConstant(
            method = "process(Lnet/minecraft/util/datafix/IFixType;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;",
            constant = @Constant(intValue = 169)
    )
    private int modifyVersion(int versionComingIn) {
        return version;
    }

}
