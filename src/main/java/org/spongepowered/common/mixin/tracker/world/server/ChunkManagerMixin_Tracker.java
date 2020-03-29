package org.spongepowered.common.mixin.tracker.world.server;

import net.minecraft.entity.Entity;
import net.minecraft.world.server.ChunkManager;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.event.tracking.PhasePrinter;
import org.spongepowered.common.event.tracking.PhaseTracker;

@Mixin(ChunkManager.class)
public class ChunkManagerMixin_Tracker {

    @Shadow @Final private ServerWorld world;

    @Redirect(method = "track(Lnet/minecraft/entity/Entity;)V",
        at = @At(value = "NEW", args = "class=java/lang/IllegalStateException", remap = false))
    private IllegalStateException impl$reportEntityAlreadyTrackedWithWorld(final String string, final Entity entityIn) {
        final IllegalStateException exception = new IllegalStateException(String.format("Entity %s is already tracked for world: %s", entityIn, this.world.getWorldInfo().getWorldName()));
        if (SpongeImpl.getGlobalConfigAdapter().getConfig().getPhaseTracker().verboseErrors()) {
            PhasePrinter.printMessageWithCaughtException(PhaseTracker.getInstance(), "Exception tracking entity", "An entity that was already tracked was added to the tracker!", exception);
        }
        return exception;
    }

}
