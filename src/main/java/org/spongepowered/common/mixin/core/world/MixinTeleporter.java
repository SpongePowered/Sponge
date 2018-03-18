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
package org.spongepowered.common.mixin.core.world;

import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.PortalAgent;
import org.spongepowered.api.world.PortalAgentType;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.interfaces.world.IMixinLocation;
import org.spongepowered.common.interfaces.world.IMixinTeleporter;
import org.spongepowered.common.interfaces.world.IMixinWorldServer;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;
import java.util.Random;

@Mixin(Teleporter.class)
public class MixinTeleporter implements PortalAgent, IMixinTeleporter {

    private boolean isVanilla;
    private int searchRadius = 128;
    private int creationRadius = 16;
    private boolean createNetherPortal = true;
    private PortalAgentType portalAgentType = PortalAgentRegistryModule.getInstance().validatePortalAgent((Teleporter) (Object) this);

    @Shadow @Final private WorldServer world;
    @Shadow @Final private Random random;
    @Shadow @Final private Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(WorldServer worldIn, CallbackInfo ci) {
        this.isVanilla = this.getClass().getName().startsWith("net.minecraft.");
    }

    @Override
    public int getSearchRadius() {
        return this.searchRadius;
    }

    @Override
    public PortalAgent setSearchRadius(int radius) {
        this.searchRadius = radius;
        return this;
    }

    @Override
    public int getCreationRadius() {
        return this.creationRadius;
    }

    @Override
    public PortalAgent setCreationRadius(int radius) {
        this.creationRadius = radius;
        return this;
    }

    /**
     * @author blood - May 21st, 2016
     *
     * @reason - rewritten to handle {@link MoveEntityEvent.Teleport.Portal}
     *
     * @param entityIn The entity being placed into the portal
     * @param rotationYaw The yaw of entity
     */
    @Overwrite
    public void placeInPortal(Entity entityIn, float rotationYaw) {
        Location<World> targetLocation = ((org.spongepowered.api.entity.Entity) entityIn).getLocation();
        // Sponge - remove hardcode to support any world using end or nether providers
        if (this.createNetherPortal) {
            if (!this.placeInExistingPortal(entityIn, rotationYaw)) {
                this.makePortal(entityIn);
                this.placeInExistingPortal(entityIn, rotationYaw);
            }
        } else {
            this.createEndPortal(targetLocation); // Sponge - move end portal create logic to its own method
            entityIn.setLocationAndAngles(targetLocation.getX(), targetLocation.getY() - 1, targetLocation.getZ(), entityIn.rotationYaw, 0.0F);
            entityIn.motionX = entityIn.motionY = entityIn.motionZ = 0.0D;
        }
    }

    private void createEndPortal(Location<World> targetLocation) {
        int xTarget = targetLocation.getBlockX();
        int yTarget = targetLocation.getBlockY() - 1;
        int zTarget = targetLocation.getBlockZ();
        int l = 1;
        int i1 = 0;

        for (int j1 = -2; j1 <= 2; ++j1) {
            for (int k1 = -2; k1 <= 2; ++k1) {
                for (int l1 = -1; l1 < 3; ++l1) {
                    int x = xTarget + k1 * l + j1 * i1;
                    int y = yTarget + l1;
                    int z = zTarget + k1 * i1 - j1 * l;
                    boolean flag = l1 < 0;
                    this.world.setBlockState(new BlockPos(x, y, z),
                            flag ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
                }
            }
        }
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

    /**
     * @author blood - May 21st, 2016
     *
     * @reason - rewritten to handle {@link MoveEntityEvent.Teleport.Portal}
     *
     * @param entityIn The entity being placed into the portal
     * @param rotationYaw The yaw of entity
     */
    @Overwrite
    public boolean placeInExistingPortal(Entity entityIn, float rotationYaw) {
        if (entityIn == null) {
            return false;
        }

        org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) entityIn;
        Optional<Location<World>> location = findPortal(spongeEntity.getLocation());
        if (location.isPresent()) {
            // last minute adjustments for portal exit
            this.handleEntityPortalExit(entityIn, location.get(), rotationYaw);
            return true;
        }

        return false;
    }

    @Override
    public Optional<Location<World>> findPortal(Location<World> searchLocation) {
        double closest = -1.0D;
        boolean addToCache = true;
        BlockPos portalPosition = BlockPos.ORIGIN;
        // Sponge - use the chunk coords instead of block coords
        Vector3i chunkPosition = searchLocation.getChunkPosition();
        long targetPosition = ChunkPos.asLong(chunkPosition.getX(), chunkPosition.getZ());

        if (this.destinationCoordinateCache.containsKey(targetPosition)) {
            Teleporter.PortalPosition teleporter$portalposition = this.destinationCoordinateCache.get(targetPosition);
            closest = 0.0D;
            portalPosition = teleporter$portalposition;
            teleporter$portalposition.lastUpdateTime = this.world.getTotalWorldTime();
            addToCache = false;
        } else {
            BlockPos blockSearchPosition = ((IMixinLocation) (Object) searchLocation).getBlockPos();

            for (int i1 = -this.searchRadius; i1 <= this.searchRadius; ++i1) {
                BlockPos blockpos2;

                for (int j1 = -this.searchRadius; j1 <= this.searchRadius; ++j1) {
                    for (BlockPos blockpos1 =
                         blockSearchPosition.add(i1, this.world.getActualHeight() - 1 - blockSearchPosition.getY(), j1); blockpos1
                                    .getY() >= 0; blockpos1 = blockpos2) {
                        blockpos2 = blockpos1.down();

                        if (this.world.getBlockState(blockpos1).getBlock() == Blocks.PORTAL) {
                            while (this.world.getBlockState(blockpos2 = blockpos1.down()).getBlock() == Blocks.PORTAL) {
                                blockpos1 = blockpos2;
                            }

                            double distance = blockpos1.distanceSq(blockSearchPosition);

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
                this.destinationCoordinateCache.put(targetPosition,
                        ((Teleporter) (Object) this).new PortalPosition(portalPosition, this.world.getTotalWorldTime()));
            }

            return Optional.of(new Location<World>(searchLocation.getExtent(), VecHelper.toVector3d(portalPosition)));
        }
        return Optional.empty();
    }

    private void handleEntityPortalExit(Entity entityIn, Location<World> portalLocation, float rotationYaw) {
        BlockPos blockPos = ((IMixinLocation) (Object) portalLocation).getBlockPos();
        double xTarget = portalLocation.getX() + 0.5D;
        double yTarget = portalLocation.getY() + 0.5D;
        double zTarget = portalLocation.getZ() + 0.5D;
        BlockPattern.PatternHelper blockpattern$patternhelper = Blocks.PORTAL.createPatternHelper(this.world, blockPos);
        boolean flag1 = blockpattern$patternhelper.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
        double d2 = blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X ? (double) blockpattern$patternhelper.getFrontTopLeft().getZ()
                : (double) blockpattern$patternhelper.getFrontTopLeft().getX();
        yTarget = blockpattern$patternhelper.getFrontTopLeft().getY() + 1
                - entityIn.getLastPortalVec().y * blockpattern$patternhelper.getHeight();

        if (flag1) {
            ++d2;
        }

        if (blockpattern$patternhelper.getForwards().getAxis() == EnumFacing.Axis.X) {
            zTarget = d2 + (1.0D - entityIn.getLastPortalVec().x) * blockpattern$patternhelper.getWidth()
                    * blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
        } else {
            xTarget = d2 + (1.0D - entityIn.getLastPortalVec().x) * blockpattern$patternhelper.getWidth()
                    * blockpattern$patternhelper.getForwards().rotateY().getAxisDirection().getOffset();
        }

        float f = 0.0F;
        float f1 = 0.0F;
        float f2 = 0.0F;
        float f3 = 0.0F;

        if (blockpattern$patternhelper.getForwards().getOpposite() == entityIn.getTeleportDirection()) {
            f = 1.0F;
            f1 = 1.0F;
        } else if (blockpattern$patternhelper.getForwards().getOpposite() == entityIn.getTeleportDirection().getOpposite()) {
            f = -1.0F;
            f1 = -1.0F;
        } else if (blockpattern$patternhelper.getForwards().getOpposite() == entityIn.getTeleportDirection().rotateY()) {
            f2 = 1.0F;
            f3 = -1.0F;
        } else {
            f2 = -1.0F;
            f3 = 1.0F;
        }

        double d3 = entityIn.motionX;
        double d4 = entityIn.motionZ;
        entityIn.motionX = d3 * f + d4 * f3;
        entityIn.motionZ = d3 * f2 + d4 * f1;
        entityIn.rotationYaw = rotationYaw - entityIn.getTeleportDirection().getOpposite().getHorizontalIndex() * 90
                + blockpattern$patternhelper.getForwards().getHorizontalIndex() * 90;
        entityIn.setLocationAndAngles(xTarget, yTarget, zTarget, entityIn.rotationYaw, entityIn.rotationPitch);
    }

    /**
     * @author blood - May 21st, 2016
     *
     * @reason - rewritten to handle {@link MoveEntityEvent.Teleport.Portal}
     *
     * @param entityIn The entity being placed into the portal
     */
    @Overwrite
    public boolean makePortal(Entity entityIn) {
        if (createPortal(((org.spongepowered.api.entity.Entity) entityIn).getLocation()).isPresent()) {
            return true;
        }

        return false;
    }

    @Override
    public Optional<Location<World>> createPortal(Location<World> toLocation) {
        return createTeleporter(toLocation, false);
    }

    // Adds boolean to turn on special tracking if called from API
    public Optional<Location<World>> createTeleporter(Location<World> nearLocation, boolean plugin) {
//        IMixinWorldServer spongeWorld = (IMixinWorldServer) nearLocation.getExtent();
//        final PhaseTracker causeTracker = PhaseTracker.getInstance();
//        if (plugin) {
//            Cause teleportCause = Cause.of(NamedCause.source(this));
//            if (causeTracker.getCurrentCause() != null) {
//                teleportCause = teleportCause.merge(causeTracker.getCurrentCause());
//            }
//            causeTracker.addCause(teleportCause);
//            causeTracker.setSpecificCapture(true);
//        }
        double closest = -1.0D;
        int xNearTarget = nearLocation.getBlockX();
        int yNearTarget = nearLocation.getBlockY();
        int zNearTarget = nearLocation.getBlockZ();
        int xAdjustedTarget = xNearTarget;
        int yAdjustedTarget = yNearTarget;
        int zAdjustedTarget = zNearTarget;
        int direction = 0;
        int dirOffset = this.random.nextInt(4);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int j2 = xNearTarget - this.creationRadius; j2 <= xNearTarget + this.creationRadius; ++j2) {
            double d1 = j2 + 0.5D - nearLocation.getBlockX();

            for (int l2 = zNearTarget - this.creationRadius; l2 <= zNearTarget + this.creationRadius; ++l2) {
                double d2 = l2 + 0.5D - nearLocation.getBlockZ();
                label142:

                for (int j3 = this.world.getActualHeight() - 1; j3 >= 0; --j3) {
                    if (this.world.isAirBlock(blockpos$mutableblockpos.setPos(j2, j3, l2))) {
                        while (j3 > 0 && this.world.isAirBlock(blockpos$mutableblockpos.setPos(j2, j3 - 1, l2))) {
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
                                        blockpos$mutableblockpos.setPos(i5, j5, k5);

                                        if (l4 < 0 && !this.world.getBlockState(blockpos$mutableblockpos).getMaterial()
                                                .isSolid() || l4 >= 0 && !this.world.isAirBlock(blockpos$mutableblockpos)) {
                                            continue label142;
                                        }
                                    }
                                }
                            }

                            double d5 = j3 + 0.5D - nearLocation.getBlockY();
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
            for (int l5 = xNearTarget - this.creationRadius; l5 <= xNearTarget + this.creationRadius; ++l5) {
                double d3 = l5 + 0.5D - nearLocation.getBlockX();

                for (int j6 = zNearTarget - this.creationRadius; j6 <= zNearTarget + this.creationRadius; ++j6) {
                    double d4 = j6 + 0.5D - nearLocation.getBlockZ();
                    label562:

                    for (int i7 = this.world.getActualHeight() - 1; i7 >= 0; --i7) {
                        if (this.world.isAirBlock(blockpos$mutableblockpos.setPos(l5, i7, j6))) {
                            while (i7 > 0 && this.world.isAirBlock(blockpos$mutableblockpos.setPos(l5, i7 - 1, j6))) {
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
                                        blockpos$mutableblockpos.setPos(j12, i13, j13);

                                        if (j11 < 0 && !this.world.getBlockState(blockpos$mutableblockpos).getMaterial()
                                                .isSolid() || j11 >= 0 && !this.world.isAirBlock(blockpos$mutableblockpos)) {
                                            continue label562;
                                        }
                                    }
                                }

                                double d6 = i7 + 0.5D - nearLocation.getBlockY();
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
            yAdjustedTarget = MathHelper.clamp(yAdjustedTarget, 70, this.world.getActualHeight() - 10);
            yFinalTarget = yAdjustedTarget;

            for (int j7 = -1; j7 <= 1; ++j7) {
                for (int l7 = 1; l7 < 3; ++l7) {
                    for (int k8 = -1; k8 < 3; ++k8) {
                        int k9 = xFinalTarget + (l7 - 1) * targetDirection + j7 * targetDirOffset;
                        int k10 = yFinalTarget + k8;
                        int k11 = zFinalTarget + (l7 - 1) * targetDirOffset - j7 * targetDirection;
                        boolean flag = k8 < 0;
                        this.world.setBlockState(new BlockPos(k9, k10, k11),
                                flag ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
                    }
                }
            }
        }

        IBlockState iblockstate = Blocks.PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, targetDirection != 0 ? EnumFacing.Axis.X : EnumFacing.Axis.Z);

        for (int i8 = 0; i8 < 4; ++i8) {
            for (int l8 = 0; l8 < 4; ++l8) {
                for (int l9 = -1; l9 < 4; ++l9) {
                    int l10 = xFinalTarget + (l8 - 1) * targetDirection;
                    int l11 = yFinalTarget + l9;
                    int k12 = zFinalTarget + (l8 - 1) * targetDirOffset;
                    boolean flag1 = l8 == 0 || l8 == 3 || l9 == -1 || l9 == 3;
                    this.world.setBlockState(new BlockPos(l10, l11, k12), flag1 ? Blocks.OBSIDIAN.getDefaultState() : iblockstate, 2);
                }
            }

            for (int i9 = 0; i9 < 4; ++i9) {
                for (int i10 = -1; i10 < 4; ++i10) {
                    int i11 = xFinalTarget + (i9 - 1) * targetDirection;
                    int i12 = yFinalTarget + i10;
                    int l12 = zFinalTarget + (i9 - 1) * targetDirOffset;
                    BlockPos blockpos = new BlockPos(i11, i12, l12);
                    this.world.neighborChanged(blockpos, this.world.getBlockState(blockpos).getBlock(), blockpos);
                }
            }
        }

        if (plugin) {
//            boolean portalResult = causeTracker.handleBlockCaptures();
//            // if portal ChangeBlockEvent was allowed, return the start location
//            if (portalResult) {
//                causeTracker.handleDroppedItems();
//                causeTracker.handleSpawnedEntities();
//                causeTracker.removeCurrentCause();
//                causeTracker.setSpecificCapture(false);
//                return Optional.of(new Location<World>((World) this.world, new Vector3i(xFinalTarget, yFinalTarget, zFinalTarget)));
//            } else { // portal ChangeBlockEvent was cancelled
//                causeTracker.getCapturedSpawnedEntities().clear();
//                causeTracker.getCapturedSpawnedEntityItems().clear();
//                causeTracker.removeCurrentCause();
//                causeTracker.setSpecificCapture(false);
//                // update cache
//                this.removePortalPositionFromCache(ChunkPos.asLong(xFinalTarget, zFinalTarget));
//                return Optional.empty();
//            }
        }

        return Optional.of(new Location<World>((World) this.world, new Vector3i(xFinalTarget, yFinalTarget, zFinalTarget)));
    }

    @Override
    public void removePortalPositionFromCache(Long portalPosition) {
        this.destinationCoordinateCache.remove(portalPosition);
    }

    @Override
    public void setPortalAgentType(PortalAgentType type) {
        this.portalAgentType = type;
    }

    @Override
    public PortalAgentType getType() {
        return this.portalAgentType;
    }

    @Override
    public void setNetherPortalType(boolean isNetherPortal) {
        if (isNetherPortal) {
            this.createNetherPortal = true;
        } else {
            this.createNetherPortal = false;
        }
    }

    @Override
    public boolean isVanilla() {
        return this.isVanilla;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("PortalAgent")
                .add("PortalAgentType", this.portalAgentType)
                .add("SearchRadius", this.searchRadius)
                .add("CreationRadius", this.creationRadius)
                .add("World", this.world.getWorldInfo().getWorldName())
                .add("DimensionId", ((IMixinWorldServer) this.world).getDimensionId())
                .toString();
    }
}
