package org.spongepowered.common.accessor.tileentity;

import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignTileEntity.class)
public interface SignTileEntityAccessor {

    @Accessor("messages") ITextComponent[] accessor$messages();
}
