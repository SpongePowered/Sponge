package org.spongepowered.vanilla.mixin.core.world.level;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.level.LevelBridge;

import java.util.Collection;
import java.util.List;

@Mixin(Level.class)
public abstract class LevelMixin_Vanilla implements LevelBridge {

    // @formatter:off
    @Shadow @Final protected List<BlockEntity> blockEntitiesToUnload;
    // @formatter:on

    @Override
    public Collection<BlockEntity> bridge$blockEntitiesToUnload() {
        return this.blockEntitiesToUnload;
    }
}
