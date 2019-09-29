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

import com.google.common.base.MoreObjects;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
import org.spongepowered.common.bridge.world.WorldServerBridge;
import org.spongepowered.common.bridge.world.TeleporterBridge;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.registry.type.world.PortalAgentRegistryModule;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.Optional;

@Mixin(Teleporter.class)
public abstract class TeleporterMixin implements TeleporterBridge {

    private boolean impl$createNetherPortal = true;
    private PortalAgentType impl$portalAgentType = PortalAgentRegistryModule.getInstance().validatePortalAgent(this);

    @Shadow @Final private WorldServer world;
    @Shadow @Final private Long2ObjectMap<Teleporter.PortalPosition> destinationCoordinateCache;

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
        if (this.impl$createNetherPortal) {
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

    @Override
    public void bridge$placeEntity(net.minecraft.world.World world, Entity entity, float yaw) {
        boolean didPort;
        if (entity instanceof EntityPlayerMP) {
            this.placeInPortal(entity, yaw);
            didPort = true;
        } else {
            if (((WorldServerBridge) this.world).bridge$getDimensionId() == Constants.World.END_DIMENSION_ID) {
                didPort = true;
            } else {
                didPort = this.placeInExistingPortal(entity, yaw);
            }
        }
        
        if (didPort) {
            ((IPhaseState) PhaseTracker.getInstance().getCurrentState()).markTeleported(PhaseTracker.getInstance().getCurrentContext());
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
        org.spongepowered.api.entity.Entity spongeEntity = (org.spongepowered.api.entity.Entity) entityIn;
        Optional<Location<World>> location = ((PortalAgent) this).findPortal(spongeEntity.getLocation());
        if (location.isPresent()) {
            // last minute adjustments for portal exit
            this.impl$handleEntityPortalExit(entityIn, location.get(), rotationYaw);
            return true;
        }

        return false;
    }

    private void impl$handleEntityPortalExit(Entity entityIn, Location<World> portalLocation, float rotationYaw) {
        BlockPos blockPos = VecHelper.toBlockPos(portalLocation);
        double xTarget = portalLocation.getX() + 0.5D;
        double yTarget;
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
        return ((PortalAgent) this).createPortal(((org.spongepowered.api.entity.Entity) entityIn).getLocation()).isPresent();
    }

    @Override
    public void bridge$removePortalPositionFromCache(Long portalPosition) {
        this.destinationCoordinateCache.remove(portalPosition);
    }

    @Override
    public void bridge$setPortalAgentType(PortalAgentType type) {
        this.impl$portalAgentType = type;
    }

    @Override
    public void bridge$setNetherPortalType(boolean isNetherPortal) {
        this.impl$createNetherPortal = isNetherPortal;
    }

    @Override
    public PortalAgentType bridge$getPortalAgentType() {
        return this.impl$portalAgentType;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper("PortalAgent")
                .add("type", this.impl$portalAgentType)
                .add("searchRadius", ((PortalAgent) this).getSearchRadius())
                .add("creationRadius", ((PortalAgent) this).getCreationRadius())
                .add("world", this.world.getWorldInfo().getWorldName())
                .add("dimensionId", ((WorldServerBridge) this.world).bridge$getDimensionId())
                .toString();
    }
}
