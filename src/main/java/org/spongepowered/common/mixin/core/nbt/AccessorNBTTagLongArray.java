package org.spongepowered.common.mixin.core.nbt;

import net.minecraft.nbt.NBTTagLongArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NBTTagLongArray.class)
public interface AccessorNBTTagLongArray {

    @Accessor("data") long[] accessor$getLongArray();

}
