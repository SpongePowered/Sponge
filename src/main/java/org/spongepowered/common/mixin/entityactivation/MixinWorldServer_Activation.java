package org.spongepowered.common.mixin.entityactivation;

import net.minecraft.world.WorldServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.mixin.core.world.MixinWorld;
import org.spongepowered.common.mixin.plugin.entityactivation.EntityActivationRange;

@Mixin(value = WorldServer.class, priority = 1005)
public abstract class MixinWorldServer_Activation extends MixinWorld {

    @Override
    protected void entityActivationCheck() {
        EntityActivationRange.activateEntities(((net.minecraft.world.World) (Object) this));
    }

}
