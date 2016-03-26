/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.common.mixin.core.world.gen;

import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.common.event.tracking.CauseTracker;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.event.tracking.phase.TrackingPhases;
import org.spongepowered.common.event.tracking.phase.WorldPhase;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.world.gen.SpongeChunkProvider;

@Mixin(ChunkProviderServer.class)
public abstract class MixinChunkProviderServer {

    private static final String CHUNK_PROVIDER_POPULATE = "Lnet/minecraft/world/chunk/IChunkProvider;populate(Lnet/minecraft/world/chunk/IChunkProvider;II)V";
    @Shadow public WorldServer worldObj;

    /**
     * @author blood, updated gabizou
     *
     * This redirect is *very* important. This enables the cause tracker to at least track that we're in
     * a terrain generation phase. The finer grained events thrown (like populators) are thrown within
     * {@link SpongeChunkProvider} itself. This is geared more towards modded servers where mods can
     * break population logic and cause re-entrance to populating chunks.
     *
     * <p>Note that in 1.9, this logic must be moved to Chunk.</p>
     *
     * @param serverChunkGenerator
     * @param chunkProvider
     * @param x
     * @param z
     */
    @Redirect(method = "populate", at = @At(value = "INVOKE", target = CHUNK_PROVIDER_POPULATE))
    public void onChunkPopulate(IChunkProvider serverChunkGenerator, IChunkProvider chunkProvider, int x, int z) {
        final CauseTracker causeTracker = ((IMixinWorldServer) this.worldObj).getCauseTracker();
        final NamedCause sourceCause = NamedCause.source(this);
        final NamedCause chunkProviderCause = NamedCause.of(TrackingUtil.CHUNK_PROVIDER, chunkProvider);
        causeTracker.switchToPhase(TrackingPhases.WORLD, WorldPhase.State.TERRAIN_GENERATION, PhaseContext.start()
                .add(sourceCause)
                .add(chunkProviderCause)
                .addCaptures()
                .complete());
        serverChunkGenerator.populate(chunkProvider, x, z);
        causeTracker.completePhase();
    }

}
