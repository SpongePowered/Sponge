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
package org.spongepowered.common.mixin.core.server.level;

import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.PlayerSpawnFinder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.annotation.Nullable;

@Mixin(PlayerSpawnFinder.class)
public abstract class PlayerSpawnFinderMixin {

    @Shadow @Final private CompletableFuture<Vec3> finishedFuture;
    @Shadow @Final private ServerLevel level;
    @Shadow @Final private BlockPos spawnSuggestion;
    @Shadow @Final private int radius;
    @Shadow @Final private int candidateCount;
    @Shadow private int nextCandidateIndex;
    @Shadow @Final private int offset;
    @Shadow @Final private int coprime;

    @Shadow private static Vec3 fixupSpawnHeight(CollisionGetter level, BlockPos pos) { return null; }
    @Shadow @Nullable protected static BlockPos getOverworldRespawnPos(ServerLevel level, int x, int z) { return null; }
    @Shadow private static boolean noCollisionNoLiquid(CollisionGetter level, BlockPos pos) { return false; }

    /**
     * A mix of loop and recursion.
     * Loop is preferred when possible.
     *
     * @author Yeregorix
     * @reason Fix <a href="https://bugs.mojang.com/browse/MC/issues/MC-304426">MC-304426</a>.
     */
    @Overwrite
    private void scheduleNext() {
        int i;
        while ((i = this.nextCandidateIndex++) < this.candidateCount) {
            if (this.finishedFuture.isDone()) {
                return;
            }

            final int candidate = (this.offset + this.coprime * i) % this.candidateCount;
            final int relX = candidate % (this.radius * 2 + 1);
            final int relZ = candidate / (this.radius * 2 + 1);
            final int x = this.spawnSuggestion.getX() + relX - this.radius;
            final int z = this.spawnSuggestion.getZ() + relZ - this.radius;

            if (this.scheduleCandidate(x, z, i, () -> {
                final BlockPos pos = getOverworldRespawnPos(this.level, x, z);
                return pos != null && noCollisionNoLiquid(this.level, pos) ? Optional.of(Vec3.atBottomCenterOf(pos)) : Optional.empty();
            })) {
                return;
            }
        }

        if (this.finishedFuture.isDone()) {
            return;
        }

        scheduleCandidate(this.spawnSuggestion.getX(), this.spawnSuggestion.getZ(), i, () -> Optional.of(fixupSpawnHeight(this.level, this.spawnSuggestion)));
    }

    private boolean scheduleCandidate(final int x, final int z, final int index, final Supplier<Optional<Vec3>> supplier) {
        final int chunkX = SectionPos.blockToSectionCoord(x);
        final int chunkZ = SectionPos.blockToSectionCoord(z);
        final CompletableFuture<?> future = this.level.getChunkSource().addTicketAndLoadWithRadius(TicketType.SPAWN_SEARCH, new ChunkPos(chunkX, chunkZ), 0);
        final MinecraftServer server = this.level.getServer();

        // We exit either the loop or the recursion, depending on what happens first.
        final AtomicBoolean exit = new AtomicBoolean(true);

        future.whenCompleteAsync((chunk, error) -> {
            if (error == null) {
                try {
                    Optional<Vec3> result = supplier.get();
                    if (result.isPresent()) {
                        this.finishedFuture.complete(result.get());
                    } else if (!exit.getAndSet(false)) {
                        this.scheduleNext();
                    }
                } catch (Exception exception) {
                    error = exception;
                }
            }

            if (error != null) {
                CrashReport report = CrashReport.forThrowable(error, "Searching for spawn");
                CrashReportCategory category = report.addCategory("Spawn Lookup");
                category.setDetail("Origin", this.spawnSuggestion::toString);
                category.setDetail("Radius", () -> Integer.toString(this.radius));
                category.setDetail("Candidate", () -> "[" + x + "," + z + "]");
                category.setDetail("Progress", () -> index + " out of " + this.candidateCount);
                this.finishedFuture.completeExceptionally(new ReportedException(report));
            }
        }, server);

        return exit.getAndSet(false);
    }
}
