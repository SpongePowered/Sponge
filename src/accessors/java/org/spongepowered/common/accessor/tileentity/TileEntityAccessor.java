package org.spongepowered.common.accessor.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TileEntity.class)
public interface TileEntityAccessor {

    @Accessor("world") void accessor$setWorld(World world);
}
