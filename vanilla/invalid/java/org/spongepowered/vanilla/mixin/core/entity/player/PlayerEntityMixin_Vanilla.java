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
package org.spongepowered.vanilla.mixin.core.entity.player;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.action.SleepingEvent;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.entity.player.PlayerEntityBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.mixin.core.entity.LivingEntityMixin;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin_Vanilla extends LivingEntityMixin implements PlayerEntityBridge {

    @Shadow protected boolean sleeping;
    @Shadow @Nullable public BlockPos bedLocation;
    @Shadow private int sleepTimer;
    @Shadow @Nullable private BlockPos spawnPos;
    @Shadow private boolean spawnForced;
    @Shadow public abstract void setSpawnPoint(BlockPos pos, boolean forced);

    Map<UUID, BlockPos> vanilla$spawnChunkMap = new ConcurrentHashMap<>();
    Set<UUID> vanilla$spawnForcedSet = new ConcurrentSet<>();

    /**
     * @author Minecrell
     * @reason Return the appropriate bed location for the current dimension
     */
    @Overwrite
    @Nullable
    public BlockPos getBedLocation() { // getBedLocation
        return bridge$getBedLocation(this.dimension);
    }

    @Override
    public BlockPos bridge$getBedLocation(int dimension) {
        return dimension == 0 ? this.spawnPos : this.vanilla$spawnChunkMap.get(dimension);
    }

    /**
     * @author Minecrell
     * @reason Return the appropriate spawn forced flag for the current dimension
     */
    @Overwrite
    public boolean isSpawnForced() { // isSpawnForced
        return bridge$isSpawnForced(this.dimension);
    }

    @Override
    public boolean bridge$isSpawnForced(DimensionType dimension) {
        return dimension != DimensionType.OVERWORLD ? this.spawnForced : this.vanilla$spawnForcedSet.contains(dimension);
    }

    @Inject(method = "setSpawnPoint", at = @At("HEAD"), cancellable = true)
    private void vanilla$onSetSpawnPoint(BlockPos pos, boolean forced, CallbackInfo ci) {
        if (this.dimension != DimensionType.OVERWORLD) {
            vanilla$setSpawnChunk(pos, forced, this.world);
            ci.cancel();
        }
    }

    private void vanilla$setSpawnChunk(@Nullable BlockPos pos, boolean forced, net.minecraft.world.World dimension) {
        final DimensionType dimensionId = ((WorldInfoBridge) dimension.getWorldInfo()).bridge$getDimensionType();
        if (dimensionId == null) {
            return;
        }
        final UUID id = ((World) dimension).getProperties().getUniqueId();
        if (pos != null) {
            this.vanilla$spawnChunkMap.put(id, pos);
            if (forced) {
                this.vanilla$spawnForcedSet.add(id);
            } else {
                this.vanilla$spawnForcedSet.remove(id);
            }
        } else {
            this.vanilla$spawnChunkMap.remove(id);
            this.vanilla$spawnForcedSet.remove(id);
        }
    }

    @Inject(method = "readAdditional", at = @At("RETURN"))
    private void onReadEntityFromNBT(CompoundNBT tagCompound, CallbackInfo ci) {
        final ListNBT spawnList = tagCompound.getList(Constants.Sponge.User.USER_SPAWN_LIST, Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < spawnList.size(); i++) {
            final CompoundNBT spawnData = spawnList.getCompound(i);
            UUID spawnDim = spawnData.getUniqueId(Constants.UUID);
            final int x = spawnData.getInt(Constants.Sponge.User.USER_SPAWN_X);
            final int y = spawnData.getInt(Constants.Sponge.User.USER_SPAWN_Y);
            final int z = spawnData.getInt(Constants.Sponge.User.USER_SPAWN_Z);
            this.vanilla$spawnChunkMap.put(spawnDim, new BlockPos(x, y, z));
            if (spawnData.getBoolean(Constants.Sponge.User.USER_SPAWN_FORCED)) {
                this.vanilla$spawnForcedSet.add(spawnDim);
            }
        }
    }

    @Inject(method = "writeAdditional", at = @At("RETURN"))
    private void vanilla$onWriteEntityToNBT(CompoundNBT tagCompound, CallbackInfo ci) {
        final ListNBT spawnList = new ListNBT();

        for (Map.Entry<UUID, BlockPos> entry : this.vanilla$spawnChunkMap.entrySet()) {
            UUID dim = entry.getKey();
            BlockPos spawn = entry.getValue();

            CompoundNBT spawnData = new CompoundNBT();
            spawnData.putUniqueId(Constants.UUID, dim);
            spawnData.putInt(Constants.Sponge.User.USER_SPAWN_X, spawn.getX());
            spawnData.putInt(Constants.Sponge.User.USER_SPAWN_Y, spawn.getY());
            spawnData.putInt(Constants.Sponge.User.USER_SPAWN_Z, spawn.getZ());
            spawnData.putBoolean(Constants.Sponge.User.USER_SPAWN_FORCED, this.vanilla$spawnForcedSet.contains(dim));
            spawnList.add(spawnData);
        }

        tagCompound.put(Constants.Sponge.User.USER_SPAWN_LIST, spawnList);
    }

    // Event injectors

    @Inject(method = "trySleep", at = @At("HEAD"), cancellable = true)
    private void vanilla$onTrySleep(BlockPos bedPos, CallbackInfoReturnable<PlayerEntity.SleepResult> ci) {
        Sponge.getCauseStackManager().pushCause(this);
        SleepingEvent.Pre event = SpongeEventFactory.createSleepingEventPre(Sponge.getCauseStackManager().getCurrentCause(),
                ((World) this.world).createSnapshot(bedPos.getX(), bedPos.getY(), bedPos.getZ()), (Player) this);
        if (SpongeCommon.postEvent(event)) {
            ci.setReturnValue(PlayerEntity.SleepResult.OTHER_PROBLEM);
        }
        Sponge.getCauseStackManager().popCause();
    }

    /**
     * @author Minecrell
     * @reason Implements the post sleeping events.
     */
    @Overwrite
    public void wakeUpPlayer(boolean immediately, boolean updateWorldFlag, boolean setSpawn) {
        // Sponge start (Set size after event call)
        //this.setSize(0.6F, 1.8F);
        Transform<World> newLocation = null;
        // Sponge end

        IBlockState iblockstate = this.world.getBlockState(this.bedLocation);

        if (this.bedLocation != null && iblockstate.getBlock() == Blocks.BED) {
            // Sponge start (Change block state after event call)
            //this.world.setBlockState(this.playerLocation, iblockstate.withProperty(BlockBed.OCCUPIED, Boolean.valueOf(false)), 4);
            // Sponge end
            BlockPos blockpos = BlockBed.getSafeExitLocation(this.world, this.bedLocation, 0);

            if (blockpos == null) {
                blockpos = this.bedLocation.up();
            }

            // Sponge start (Store position for later)
            /*this.setPosition((double) ((float) blockpos.getX() + 0.5F), (double) ((float) blockpos.getY() + 0.1F),
                    (double) ((float) blockpos.getZ() + 0.5F));*/
            newLocation = ((Player) this).getTransform().setPosition(new Vector3d(blockpos.getX() + 0.5F, blockpos.getY() + 0.1F, blockpos.getZ() + 0.5F));
            // Sponge end
        }

        // Sponge start
        BlockSnapshot bed = ((Player) this).getWorld().createSnapshot(VecHelper.toVector3i(this.bedLocation));
        try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
            frame.pushCause(this);
            SleepingEvent.Post event = SpongeEventFactory.createSleepingEventPost(frame.getCurrentCause(), bed,
                    Optional.ofNullable(newLocation), (Player) this, setSpawn);

            if (SpongeCommon.postEvent(event)) {
                return;
            }

            //  Moved from above
            this.setSize(0.6F, 1.8F);
            if (newLocation != null) {
                // Set property only if bed still existsthis.world.setBlockState(this.bedLocation, iblockstate.withProperty(BlockBed.OCCUPIED, false), 4);}

                // Teleport player
                event.getSpawnTransform().ifPresent(this::bridge$setLocationAndAngles);
                // Sponge end

                this.sleeping = false;

                if (!this.world.isRemote && updateWorldFlag) {
                    this.world.updateAllPlayersSleepingFlag();
                }

                this.sleepTimer = immediately ? 0 : 100;

                if (setSpawn) {
                    this.setSpawnPoint(this.bedLocation, false);
                }

                // Sponge start
                SpongeCommon.postEvent(SpongeEventFactory.createSleepingEventFinish(frame.getCurrentCause(), bed, (Player) this));
            }
            // Sponge end
        }

    }
}
