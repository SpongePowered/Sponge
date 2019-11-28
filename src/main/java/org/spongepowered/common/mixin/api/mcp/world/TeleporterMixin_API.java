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
package org.spongepowered.common.mixin.api.mcp.world;

import com.flowpowered.math.vector.Vector3i;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.Teleporter.PortalPosition;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;
import java.util.Random;

@Mixin(Teleporter.class)
public class TeleporterMixin_API implements PortalAgent {

    @Shadow @Final private ServerWorld world;
    @Shadow @Final private Random random;
    @Shadow @Final private Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache;

    private int api$searchRadius = Constants.World.Teleporter.DEFAULT_SEARCH_RADIUS;
    private int api$creationRadius = Constants.World.Teleporter.DEFAULT_CREATION_RADIUS;

    @Override
    public PortalAgentType getType() {
        return ((TeleporterBridge) this).bridge$getPortalAgentType();
    }

    @Override
    public int getSearchRadius() {
        return this.api$searchRadius;
    }

    @Override
    public PortalAgent setSearchRadius(int radius) {
        this.api$searchRadius = radius;
        return this;
    }

    @Override
    public int getCreationRadius() {
        return this.api$creationRadius;
    }

    @Override
    public PortalAgent setCreationRadius(int radius) {
        this.api$creationRadius = radius;
        return this;
    }

    @Override
    public Optional<Location<World>> findOrCreatePortal(Location<World> targetLocation) {
        Optional<Location<World>> foundTeleporter = this.findPortal(targetLocation);
        if (!foundTeleporter.isPresent()) {
            if (this.createPortal(targetLocation).isPresent()) {
                return this.findPortal(targetLocation);
            }
            return Optional.empty();
        }

        return foundTeleporter;
    }

    @Override
    public Optional<Location<World>> findPortal(Location<World> searchLocation) {
        double closest = -1.0D;
        boolean addToCache = true;
        BlockPos portalPosition = BlockPos.field_177992_a;
        // Sponge - use the chunk coords instead of block coords
        Vector3i chunkPosition = searchLocation.getChunkPosition();
        long targetPosition = ChunkPos.func_77272_a(chunkPosition.getX(), chunkPosition.getZ());

        if (this.destinationCoordinateCache.containsKey(targetPosition)) {
            Teleporter.PortalPosition teleporter$portalposition = this.destinationCoordinateCache.get(targetPosition);
            closest = 0.0D;
            portalPosition = teleporter$portalposition;
            teleporter$portalposition.field_85087_d = this.world.func_82737_E();
            addToCache = false;
        } else {
            BlockPos blockSearchPosition = VecHelper.toBlockPos(searchLocation);

            for (int i1 = -this.api$searchRadius; i1 <= this.api$searchRadius; ++i1) {
                BlockPos blockpos2;

                for (int j1 = -this.api$searchRadius; j1 <= this.api$searchRadius; ++j1) {
                    for (BlockPos blockpos1 =
                         blockSearchPosition.func_177982_a(i1, this.world.func_72940_L() - 1 - blockSearchPosition.func_177956_o(), j1); blockpos1
                                    .func_177956_o() >= 0; blockpos1 = blockpos2) {
                        blockpos2 = blockpos1.func_177977_b();

                        if (this.world.func_180495_p(blockpos1).func_177230_c() == Blocks.field_150427_aO) {
                            while (this.world.func_180495_p(blockpos2 = blockpos1.func_177977_b()).func_177230_c() == Blocks.field_150427_aO) {
                                blockpos1 = blockpos2;
                            }

                            double distance = blockpos1.func_177951_i(blockSearchPosition);

                            if (closest < 0.0D || distance < closest) {
                                closest = distance;
                                portalPosition = blockpos1;
                            }
                        }
                    }
                }
            }
        }

        if (closest >= 0.0D) {
            if (addToCache) {
                this.destinationCoordinateCache.put(targetPosition, ((Teleporter) (Object) this).new PortalPosition(portalPosition, this.world.func_82737_E()));
            }

            return Optional.of(new Location<>(searchLocation.getExtent(), VecHelper.toVector3d(portalPosition)));
        }
        return Optional.empty();
    }


    @Override
    public Optional<Location<World>> createPortal(Location<World> toLocation) {
        double closest = -1.0D;
        int xNearTarget = toLocation.getBlockX();
        int yNearTarget = toLocation.getBlockY();
        int zNearTarget = toLocation.getBlockZ();
        int xAdjustedTarget = xNearTarget;
        int yAdjustedTarget = yNearTarget;
        int zAdjustedTarget = zNearTarget;
        int direction = 0;
        int dirOffset = this.random.nextInt(4);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j2 = xNearTarget - this.api$creationRadius; j2 <= xNearTarget + this.api$creationRadius; ++j2) {
            double d1 = j2 + 0.5D - toLocation.getBlockX();

            for (int l2 = zNearTarget - this.api$creationRadius; l2 <= zNearTarget + this.api$creationRadius; ++l2) {
                double d2 = l2 + 0.5D - toLocation.getBlockZ();
                label142:

                for (int j3 = this.world.func_72940_L() - 1; j3 >= 0; --j3) {
                    if (this.world.func_175623_d(blockpos$mutableblockpos.func_181079_c(j2, j3, l2))) {
                        while (j3 > 0 && this.world.func_175623_d(blockpos$mutableblockpos.func_181079_c(j2, j3 - 1, l2))) {
                            --j3;
                        }

                        for (int k3 = dirOffset; k3 < dirOffset + 4; ++k3) {
                            int l3 = k3 % 2;
                            int i4 = 1 - l3;

                            if (k3 % 4 >= 2) {
                                l3 = -l3;
                                i4 = -i4;
                            }

                            for (int j4 = 0; j4 < 3; ++j4) {
                                for (int k4 = 0; k4 < 4; ++k4) {
                                    for (int l4 = -1; l4 < 4; ++l4) {
                                        int i5 = j2 + (k4 - 1) * l3 + j4 * i4;
                                        int j5 = j3 + l4;
                                        int k5 = l2 + (k4 - 1) * i4 - j4 * l3;
                                        blockpos$mutableblockpos.func_181079_c(i5, j5, k5);

                                        if (l4 < 0 && !this.world.func_180495_p(blockpos$mutableblockpos).func_185904_a()
                                                .func_76220_a() || l4 >= 0 && !this.world.func_175623_d(blockpos$mutableblockpos)) {
                                            continue label142;
                                        }
                                    }
                                }
                            }

                            double d5 = j3 + 0.5D - toLocation.getBlockY();
                            double distance = d1 * d1 + d5 * d5 + d2 * d2;

                            if (closest < 0.0D || distance < closest) {
                                closest = distance;
                                xAdjustedTarget = j2;
                                yAdjustedTarget = j3;
                                zAdjustedTarget = l2;
                                direction = k3 % 4;
                            }
                        }
                    }
                }
            }
        }

        if (closest < 0.0D) {
            for (int l5 = xNearTarget - this.api$creationRadius; l5 <= xNearTarget + this.api$creationRadius; ++l5) {
                double d3 = l5 + 0.5D - toLocation.getBlockX();

                for (int j6 = zNearTarget - this.api$creationRadius; j6 <= zNearTarget + this.api$creationRadius; ++j6) {
                    double d4 = j6 + 0.5D - toLocation.getBlockZ();
                    label562:

                    for (int i7 = this.world.func_72940_L() - 1; i7 >= 0; --i7) {
                        if (this.world.func_175623_d(blockpos$mutableblockpos.func_181079_c(l5, i7, j6))) {
                            while (i7 > 0 && this.world.func_175623_d(blockpos$mutableblockpos.func_181079_c(l5, i7 - 1, j6))) {
                                --i7;
                            }

                            for (int k7 = dirOffset; k7 < dirOffset + 2; ++k7) {
                                int j8 = k7 % 2;
                                int j9 = 1 - j8;

                                for (int j10 = 0; j10 < 4; ++j10) {
                                    for (int j11 = -1; j11 < 4; ++j11) {
                                        int j12 = l5 + (j10 - 1) * j8;
                                        int i13 = i7 + j11;
                                        int j13 = j6 + (j10 - 1) * j9;
                                        blockpos$mutableblockpos.func_181079_c(j12, i13, j13);

                                        if (j11 < 0 && !this.world.func_180495_p(blockpos$mutableblockpos).func_185904_a()
                                                .func_76220_a() || j11 >= 0 && !this.world.func_175623_d(blockpos$mutableblockpos)) {
                                            continue label562;
                                        }
                                    }
                                }

                                double d6 = i7 + 0.5D - toLocation.getBlockY();
                                double distance = d3 * d3 + d6 * d6 + d4 * d4;

                                if (closest < 0.0D || distance < closest) {
                                    closest = distance;
                                    xAdjustedTarget = l5;
                                    yAdjustedTarget = i7;
                                    zAdjustedTarget = j6;
                                    direction = k7 % 2;
                                }
                            }
                        }
                    }
                }
            }
        }

        int xFinalTarget = xAdjustedTarget;
        int yFinalTarget = yAdjustedTarget;
        int zFinalTarget = zAdjustedTarget;
        int targetDirection = direction % 2;
        int targetDirOffset = 1 - targetDirection;

        if (direction % 4 >= 2) {
            targetDirection = -targetDirection;
            targetDirOffset = -targetDirOffset;
        }

        if (closest < 0.0D) {
            yAdjustedTarget = MathHelper.func_76125_a(yAdjustedTarget, 70, this.world.func_72940_L() - 10);
            yFinalTarget = yAdjustedTarget;

            for (int j7 = -1; j7 <= 1; ++j7) {
                for (int l7 = 1; l7 < 3; ++l7) {
                    for (int k8 = -1; k8 < 3; ++k8) {
                        int k9 = xFinalTarget + (l7 - 1) * targetDirection + j7 * targetDirOffset;
                        int k10 = yFinalTarget + k8;
                        int k11 = zFinalTarget + (l7 - 1) * targetDirOffset - j7 * targetDirection;
                        boolean flag = k8 < 0;
                        this.world.func_175656_a(new BlockPos(k9, k10, k11),
                                flag ? Blocks.field_150343_Z.func_176223_P() : Blocks.field_150350_a.func_176223_P());
                    }
                }
            }
        }

        BlockState iblockstate = Blocks.field_150427_aO.func_176223_P().func_177226_a(NetherPortalBlock.field_176550_a, targetDirection != 0 ? Direction.Axis.X : Direction.Axis.Z);

        for (int i8 = 0; i8 < 4; ++i8) {
            for (int l8 = 0; l8 < 4; ++l8) {
                for (int l9 = -1; l9 < 4; ++l9) {
                    int l10 = xFinalTarget + (l8 - 1) * targetDirection;
                    int l11 = yFinalTarget + l9;
                    int k12 = zFinalTarget + (l8 - 1) * targetDirOffset;
                    boolean flag1 = l8 == 0 || l8 == 3 || l9 == -1 || l9 == 3;
                    this.world.func_180501_a(new BlockPos(l10, l11, k12), flag1 ? Blocks.field_150343_Z.func_176223_P() : iblockstate, 2);
                }
            }

            for (int i9 = 0; i9 < 4; ++i9) {
                for (int i10 = -1; i10 < 4; ++i10) {
                    int i11 = xFinalTarget + (i9 - 1) * targetDirection;
                    int i12 = yFinalTarget + i10;
                    int l12 = zFinalTarget + (i9 - 1) * targetDirOffset;
                    BlockPos blockpos = new BlockPos(i11, i12, l12);
                    this.world.func_190524_a(blockpos, this.world.func_180495_p(blockpos).func_177230_c(), blockpos);
                }
            }
        }

        return Optional.of(new Location<>((World) this.world, new Vector3i(xFinalTarget, yFinalTarget, zFinalTarget)));
    }

}
