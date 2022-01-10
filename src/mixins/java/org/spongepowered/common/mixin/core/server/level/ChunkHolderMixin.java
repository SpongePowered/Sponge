package org.spongepowered.common.mixin.core.server.level;

import net.minecraft.server.level.ChunkHolder;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.world.chunk.ChunkEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.event.ShouldFire;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

@Mixin(ChunkHolder.class)
abstract class ChunkHolderMixin {
    @Inject(
        method = "replaceProtoChunk(Lnet/minecraft/world/level/chunk/ImposterProtoChunk;)V",
        at = @At("TAIL")
    )
    private void impl$throwChunkGeneratedEvent(final ImposterProtoChunk imposter, final CallbackInfo ci) {
        if (!ShouldFire.CHUNK_EVENT_GENERATED) {
            return;
        }
        final LevelChunk chunk = imposter.getWrapped();
        final Vector3i chunkPos = VecHelper.toVector3i(chunk.getPos());
        final ChunkEvent.Generated event = SpongeEventFactory.createChunkEventGenerated(
            PhaseTracker.getInstance().currentCause(), chunkPos,
            (ResourceKey) (Object) chunk.getLevel().dimension().location()
        );
        SpongeCommon.post(event);
    }
}
