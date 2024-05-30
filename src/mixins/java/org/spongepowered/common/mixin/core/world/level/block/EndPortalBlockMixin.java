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
package org.spongepowered.common.mixin.core.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EndPortalBlock;
import net.minecraft.world.level.levelgen.feature.EndPlatformFeature;
import net.minecraft.world.level.portal.DimensionTransition;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.bridge.world.level.block.PortalBlockBridge;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.portal.SpongePortal;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;

@Mixin(EndPortalBlock.class)
public abstract class EndPortalBlockMixin implements PortalBlockBridge {

    @Override
    public Optional<ServerLocation> bridge$calculatePortalExit(final ServerWorld from, final Vector3i fromPos, final Entity entity) {
        final var fromLevel = (ServerLevel) from.world();
        final var toLevelKey = fromLevel.dimension() == Level.END ? Level.OVERWORLD : Level.END;
        final var toLevel = fromLevel.getServer().getLevel(toLevelKey);
        if (toLevel == null) {
            return Optional.empty();
        }
        return Optional.of(EndPortalBlockMixin.impl$calculateVanillaPortalExit(entity, toLevel));
    }

    private static ServerLocation impl$calculateVanillaPortalExit(final Entity entity, final ServerLevel toLevel) {
        final var toEnd = toLevel.dimension() == Level.END;
        if (toEnd) {
            return ServerLocation.of((ServerWorld) toLevel, VecHelper.toVector3i(ServerLevel.END_SPAWN_POINT));
        }

        if (entity instanceof ServerPlayer player) {
            var transition = player.findRespawnPositionAndUseSpawnBlock(false, DimensionTransition.DO_NOTHING);
            return ServerLocation.of((ServerWorld) transition.newLevel(), VecHelper.toVector3d(transition.pos()));
        }

        final var sharedSpawnPos = toLevel.getSharedSpawnPos();
        final var spawnPos = ((net.minecraft.world.entity.Entity) entity).adjustSpawnLocation(toLevel, sharedSpawnPos).getBottomCenter();
        return ServerLocation.of((ServerWorld) toLevel, VecHelper.toVector3d(spawnPos));
    }


    @Override
    public Optional<Portal> bridge$findPortal(final ServerLocation at, final int searchRange) {
        final var level = (ServerLevel) at.world();
        if (level.dimension() == Level.END) {
            return Optional.empty(); // End platform always generates
        }
        return Optional.of(new SpongePortal(at, (PortalLogic) this)); // this should be spawn - but the parameter takes precedence
    }

    @Override
    public Optional<Portal> bridge$generatePortal(final ServerLocation at, final Axis axis) {
        final var level = (ServerLevel) at.world();
        // If a target dimension is set assume we always want to generate a portal otherwise we could have used a spawn teleporter
        if (level.dimension() == Level.END) {
            final var bottomCenter = VecHelper.toBlockPos(at.blockPosition()).getBottomCenter();
            EndPlatformFeature.createEndPlatform(level, BlockPos.containing(bottomCenter).below(), true);
            return Optional.of(new SpongePortal(at, (PortalLogic) this));
        }
        return Optional.empty();
    }

    @Override
    public boolean bridge$teleport(final org.spongepowered.api.entity.Entity entity, final ServerLocation destination, final boolean generateDestinationPortal) {
        final var toLevel = (ServerLevel) destination.world();
        if (toLevel.dimension() == Level.END) {
            if (generateDestinationPortal) {
                this.bridge$generatePortal(destination, Axis.X);
            }
        }

        var exit = EndPortalBlockMixin.impl$calculateVanillaPortalExit(entity, toLevel);
        return entity.setLocation(exit);
    }

}
