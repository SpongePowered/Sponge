package org.spongepowered.common.mixin.core.world.gen.feature;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldGenDungeons.class)
public interface WorldGenDungeonsAccessor {

    @Accessor("SPAWNERTYPES") ResourceLocation[] accessor$getSpawnerTypes();

}
