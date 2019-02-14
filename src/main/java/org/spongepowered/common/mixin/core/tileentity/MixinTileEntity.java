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
package org.spongepowered.common.mixin.core.tileentity;

import co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.Queries;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.data.nbt.CustomDataNbtUtil;
import org.spongepowered.common.data.persistence.NbtTranslator;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.data.util.DataQueries;
import org.spongepowered.common.data.util.DataUtil;
import org.spongepowered.common.data.util.NbtDataUtil;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.interfaces.IMixinChunk;
import org.spongepowered.common.interfaces.block.tile.IMixinTileEntity;
import org.spongepowered.common.interfaces.data.IMixinCustomDataHolder;
import org.spongepowered.common.util.VecHelper;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.tileentity.TileEntity.class)
@Implements(@Interface(iface = IMixinTileEntity.class, prefix = "tile$"))
public abstract class MixinTileEntity implements TileEntity, IMixinTileEntity {

    private final TileEntityType tileType = SpongeImplHooks.getTileEntityType(this.getClass());
    // uses different name to not clash with SpongeForge
    private final boolean isTileVanilla = getClass().getName().startsWith("net.minecraft.");
    @Nullable private Timing timing;
    @Nullable private LocatableBlock locatableBlock;
    // caches owner and notifier to avoid constant lookups in chunk
    @Nullable private User spongeOwner;
    @Nullable private User spongeNotifier;
    private boolean isTicking = false;
    private boolean hasSetOwner = false;
    private boolean hasSetNotifier = false;
    private WeakReference<IMixinChunk> activeChunk = new WeakReference<>(null);
    // Used by tracker config
    private boolean allowsBlockBulkCapture = true;
    private boolean allowsEntityBulkCapture = true;
    private boolean allowsBlockEventCreation = true;
    private boolean allowsEntityEventCreation = true;

    @Shadow protected net.minecraft.world.World world;
    @Shadow protected BlockPos pos;
    @Shadow private boolean removed;

    @Shadow public abstract BlockPos getPos();
    @Override @Shadow public abstract void markDirty();

    @Inject(method = "<init>*", at = @At("RETURN"))
    public void onConstruction(CallbackInfo ci) {
        this.refreshCache();
    }

    @Intrinsic
    public void tile$markDirty() {
        this.markDirty();
    }

    @Inject(method = "markDirty", at = @At(value = "HEAD"))
    public void onMarkDirty(CallbackInfo ci) {
        if (this.world != null && !this.world.isRemote) {
            // This handles transfers to this TE from a source such as a Hopper
            PhaseTracker.getInstance().getCurrentPhaseData().context.getSource(TileEntity.class).ifPresent(currentTick -> {
                if (currentTick != this) {
                    net.minecraft.tileentity.TileEntity te = (net.minecraft.tileentity.TileEntity) currentTick;
//                    world.getCauseTracker().trackTargetBlockFromSource(te, te.getPos(), this.getBlockType(), this.pos, PlayerTracker.Type.NOTIFIER);
                }
            });
        }
    }

    @Override
    public Location getLocation() {
        return new Location((World) this.world, VecHelper.toVector3i(this.getPos()));
    }

    @Override
    public int getContentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, getContentVersion())
            .set(Queries.WORLD_ID, ((World) this.world).getUniqueId().toString())
            .set(Queries.POSITION_X, this.getPos().getX())
            .set(Queries.POSITION_Y, this.getPos().getY())
            .set(Queries.POSITION_Z, this.getPos().getZ())
            .set(DataQueries.BLOCK_ENTITY_TILE_TYPE, this.tileType.getKey());
        final NBTTagCompound compound = new NBTTagCompound();
        this.writeToNbt(compound);
        NbtDataUtil.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
        container.set(DataQueries.UNSAFE_NBT, NbtTranslator.getInstance().translateFrom(compound));
        final Collection<DataManipulator<?, ?>> manipulators = ((IMixinCustomDataHolder) this).getCustomManipulators();
        if (!manipulators.isEmpty()) {
            container.set(DataQueries.DATA_MANIPULATORS, DataUtil.getSerializedManipulatorList(manipulators));
        }
        return container;
    }

    @Override
    public boolean validateRawData(DataView container) {
        return container.contains(Queries.WORLD_ID)
            && container.contains(Queries.POSITION_X)
            && container.contains(Queries.POSITION_Y)
            && container.contains(Queries.POSITION_Z)
            && container.contains(DataQueries.BLOCK_ENTITY_TILE_TYPE)
            && container.contains(DataQueries.UNSAFE_NBT);
    }

    @Override
    public void setRawData(DataView container) throws InvalidDataException {

    }

    @Override
    public boolean isValid() {
        return !this.removed;
    }

    @Override
    public void setValid(boolean valid) {
        this.removed = valid;
    }

    @Override
    public final TileEntityType getType() {
        return this.tileType;
    }

    @Override
    public BlockState getBlock() {
        return (BlockState) this.world.getBlockState(this.getPos());
    }

    @Inject(method = "remove", at = @At("RETURN"))
    public void onSpongeInvalidate(CallbackInfo ci) {
        this.setActiveChunk(null);
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #writeToNbt}.
     * <p>
     * <p> This makes it easier for other entity mixins to override writeToNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "write", at = @At("HEAD"))
    public void onWriteToNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> ci) {
        this.writeToNbt(this.getSpongeData());
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #readFromNbt}.
     * <p>
     * <p> This makes it easier for other entity mixins to override readFromNbt without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "read", at = @At("RETURN"))
    public void onReadFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        this.readFromNbt(this.getSpongeData());
    }

    /**
     * Read extra data (SpongeData) from the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    @Override
    public void readFromNbt(NBTTagCompound compound) {
        CustomDataNbtUtil.readCustomData(compound, this);
    }

    /**
     * Write extra data (SpongeData) to the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    @Override
    public void writeToNbt(NBTTagCompound compound) {
        CustomDataNbtUtil.writeCustomData(compound, this);
    }

    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {

    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        final List<DataManipulator<?, ?>> list = Lists.newArrayList();
        this.supplyVanillaManipulators(list);
        if (this instanceof IMixinCustomDataHolder) {
            list.addAll(((IMixinCustomDataHolder) this).getCustomManipulators());
        }
        return list;
    }

    @Override
    public boolean isVanilla() {
        return this.isTileVanilla;
    }

    @Override
    public Timing getTimingsHandler() {
        if (this.timing == null) {
            this.timing = SpongeTimings.getTileEntityTiming(this);
        }
        return this.timing;
    }

    @Override
    public TileEntityArchetype createArchetype() {
        return new SpongeTileEntityArchetypeBuilder().tile(this).build();
    }

    @Override
    public LocatableBlock getLocatableBlock() {
        if (this.locatableBlock == null) {
            final Chunk chunk = (Chunk) this.activeChunk.get();
            BlockState blockState = null;
            if (chunk != null) {
                blockState = (BlockState) chunk.getBlockState(this.pos);
            } else {
                blockState = this.getBlock();
            }
            this.locatableBlock = LocatableBlock.builder()
                    .location(new Location((World) this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ()))
                    .state(blockState)
                    .build();
        }

        return this.locatableBlock;
    }

    @Override
    public void setSpongeOwner(@Nullable User owner) {
        if (owner == null) {
            this.spongeOwner = null;
            this.hasSetOwner = false;
            return;
        }
        this.spongeOwner = owner;
        this.hasSetOwner = true;
    }

    @Override
    public User getSpongeOwner() {
        if (!this.hasSetOwner()) {
            IMixinChunk activeChunk = this.getActiveChunk();
            if (activeChunk != null) {
                this.setSpongeOwner(activeChunk.getBlockOwner(pos).orElse(null));
            }
        }
        return this.spongeOwner;
    }

    @Override
    public boolean hasSetOwner() {
        return this.hasSetOwner;
    }

    @Override
    public void setSpongeNotifier(@Nullable User notifier) {
        if (notifier == null) {
            this.spongeNotifier = null;
            this.hasSetNotifier = false;
            return;
        }
        this.spongeNotifier = notifier;
        this.hasSetNotifier = true;
    }

    @Nullable
    @Override
    public User getSpongeNotifier() {
        if (!this.hasSetNotifier()) {
            IMixinChunk activeChunk = this.getActiveChunk();
            if (activeChunk != null) {
                this.setSpongeNotifier(activeChunk.getBlockNotifier(pos).orElse(null));
            }
        }
        return this.spongeNotifier;
    }

    @Override
    public boolean hasSetNotifier() {
        return this.hasSetNotifier;
    }

    @Override
    @Nullable
    public IMixinChunk getActiveChunk() {
        return this.activeChunk.get();
    }

    @Override
    public void setActiveChunk(@Nullable IMixinChunk chunk) {
        if (chunk == null && this.world != null && !this.world.isRemote && !this.isValid()) {
            if (this.isTicking) {
                // If a TE is currently ticking and has been invalidated, delay clearing active chunk until finished
                // This is done to avoid issues during unwind when calling getActiveChunk
                // Note: This occurs with TE's such as pistons that invalidate during movement
                return;
            }
        }
        this.activeChunk = new WeakReference<IMixinChunk>(chunk);
    }

    @Override
    public boolean shouldTick() {
        final IMixinChunk chunk = this.getActiveChunk();
        // Don't tick if chunk is queued for unload or is in progress of being scheduled for unload
        // See https://github.com/SpongePowered/SpongeVanilla/issues/344
        if (chunk == null) {
            return false;
        }
        if (!chunk.isActive()) {
            return false;
        }

        return true;
    }

    @Override
    public boolean isTicking() {
        return this.isTicking;
    }

    @Override
    public void setIsTicking(boolean ticking) {
        this.isTicking = ticking;
    }

    @Override
    public boolean allowsBlockBulkCapture() {
        return this.allowsBlockBulkCapture;
    }

    @Override
    public boolean allowsEntityBulkCapture() {
        return this.allowsEntityBulkCapture;
    }

    @Override
    public boolean allowsBlockEventCreation() {
        return this.allowsBlockEventCreation;
    }

    @Override
    public boolean allowsEntityEventCreation() {
        return this.allowsEntityEventCreation;
    }

    @Override
    public void refreshCache() {
        if (this.tileType != null) {
            this.allowsBlockBulkCapture = ((SpongeTileEntityType) this.tileType).allowsBlockBulkCapture;
            this.allowsEntityBulkCapture = ((SpongeTileEntityType) this.tileType).allowsEntityBulkCapture;
            this.allowsBlockEventCreation = ((SpongeTileEntityType) this.tileType).allowsBlockEventCreation;
            this.allowsEntityEventCreation = ((SpongeTileEntityType) this.tileType).allowsEntityEventCreation;
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tileType", this.tileType)
            .add("world", this.world)
            .add("pos", this.pos)
            .toString();
    }
}
