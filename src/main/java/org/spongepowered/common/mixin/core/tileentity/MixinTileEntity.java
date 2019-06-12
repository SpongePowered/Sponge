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

import org.spongepowered.common.bridge.world.ChunkBridge;
import org.spongepowered.common.relocate.co.aikar.timings.SpongeTimings;
import co.aikar.timings.Timing;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.block.tileentity.TileEntityArchetype;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.api.world.LocatableBlock;
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
import org.spongepowered.common.block.SpongeTileEntityArchetypeBuilder;
import org.spongepowered.common.data.nbt.CustomDataNbtUtil;
import org.spongepowered.common.data.type.SpongeTileEntityType;
import org.spongepowered.common.bridge.tileentity.TileEntityBridge;
import org.spongepowered.common.bridge.data.CustomDataHolderBridge;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.world.SpongeLocatableBlockBuilder;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

@NonnullByDefault
@Mixin(net.minecraft.tileentity.TileEntity.class)
@Implements(@Interface(iface = TileEntityBridge.class, prefix = "tile$"))
abstract class MixinTileEntity implements TileEntityBridge {

    // uses different name to not clash with SpongeForge
    private final boolean isTileVanilla = getClass().getName().startsWith("net.minecraft.");
    @Nullable private Timing timing;
    @Nullable private LocatableBlock locatableBlock;
    // caches owner and notifier to avoid constant lookups in chunk
    @Nullable private User spongeOwner;
    @Nullable private User spongeNotifier;
    private boolean isTicking = false;
    private WeakReference<ChunkBridge> activeChunk = new WeakReference<>(null);
    // Used by tracker config
    private boolean allowsBlockBulkCapture = true;
    private boolean allowsEntityBulkCapture = true;
    private boolean allowsBlockEventCreation = true;
    private boolean allowsEntityEventCreation = true;
    private boolean isCaptured = false;

    @Shadow protected boolean tileEntityInvalid;
    @Shadow protected net.minecraft.world.World world;
    @Shadow private int blockMetadata;
    @Shadow protected BlockPos pos;

    @Shadow public abstract BlockPos getPos();
    @Shadow public abstract Block getBlockType();
    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    @Override @Shadow public abstract void markDirty();

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void impl$RefreshTrackerStates(CallbackInfo ci) {
        this.refreshTrackerStates();
    }

    @Intrinsic
    public void tile$markDirty() {
        this.markDirty();
    }


    @SuppressWarnings({"rawtypes"})
    @Inject(method = "register(Ljava/lang/String;Ljava/lang/Class;)V", at = @At(value = "RETURN"))
    private static void impl$registerTileEntityClassWithSpongeRegistry(String name, Class clazz, CallbackInfo callbackInfo) {
        if (clazz != null) {
            TileEntityTypeRegistryModule.getInstance().doTileEntityRegistration(clazz, name);
        }
    }

    @Inject(method = "invalidate", at = @At("RETURN"))
    private void impl$RemoveActiveChunkOnInvalidate(CallbackInfo ci) {
        this.setActiveChunk(null);
    }

    /**
     * Hooks into vanilla's writeToNBT to call {@link #bridge$writeToSpongeCompound}.
     * <p>
     * <p> This makes it easier for other entity mixins to override writeToNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla writes to (unused because we write to SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/tileentity/TileEntity;writeToNBT(Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/nbt/NBTTagCompound;", at = @At("HEAD"))
    private void impl$WriteSpongeDataToCompound(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> ci) {
        if (!((CustomDataHolderBridge) this).getCustomManipulators().isEmpty()) {
            this.bridge$writeToSpongeCompound(this.getSpongeData());
        }
    }

    /**
     * Hooks into vanilla's readFromNBT to call {@link #bridge$readFromSpongeCompound}.
     * <p>
     * <p> This makes it easier for other entity mixins to override readSpongeNBT without having to specify the <code>@Inject</code> annotation. </p>
     *
     * @param compound The compound vanilla reads from (unused because we read from SpongeData)
     * @param ci (Unused) callback info
     */
    @Inject(method = "Lnet/minecraft/tileentity/TileEntity;readFromNBT(Lnet/minecraft/nbt/NBTTagCompound;)V", at = @At("RETURN"))
    private void impl$ReadSpongeDataFromCompound(NBTTagCompound compound, CallbackInfo ci) {
        if (this.hasTileDataCompound()) {
            this.bridge$readFromSpongeCompound(this.getSpongeData());
        }
    }

    /**
     * Read extra data (SpongeData) from the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to read from
     */
    protected void bridge$readFromSpongeCompound(NBTTagCompound compound) {
        CustomDataNbtUtil.readCustomData(compound, (TileEntity) this);
    }

    /**
     * Write extra data (SpongeData) to the tile entity's NBT tag.
     *
     * @param compound The SpongeData compound to write to
     */
    protected void bridge$writeToSpongeCompound(NBTTagCompound compound) {
        CustomDataNbtUtil.writeCustomData(compound, (TileEntity) this);
    }

    public void supplyVanillaManipulators(List<DataManipulator<?, ?>> manipulators) {

    }

    @Override
    public Collection<DataManipulator<?, ?>> getContainers() {
        final List<DataManipulator<?, ?>> list = Lists.newArrayList();
        this.supplyVanillaManipulators(list);
        if (this instanceof CustomDataHolderBridge) {
            list.addAll(((CustomDataHolderBridge) this).getCustomManipulators());
        }
        return list;
    }

    @Override
    public boolean isVanilla() {
        return this.isTileVanilla;
    }

    @Override
    public Timing spongeImpl$getTimingHandler() {
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
            this.locatableBlock = new SpongeLocatableBlockBuilder()
                .world((World) this.world)
                .position(this.pos.getX(), this.pos.getY(), this.pos.getZ())
                .state(blockState)
                .build();
        }

        return this.locatableBlock;
    }

    @Override
    public void setSpongeOwner(@Nullable User owner) {
        this.spongeOwner = owner;
    }

    @Override
    public User getSpongeOwner() {
        return this.spongeOwner;
    }

    @Override
    public void setSpongeNotifier(@Nullable User notifier) {
        this.spongeNotifier = notifier;
    }

    @Nullable
    @Override
    public User getSpongeNotifier() {
        return this.spongeNotifier;
    }

    @Override
    @Nullable
    public ChunkBridge getActiveChunk() {
        return this.activeChunk.get();
    }

    @Override
    public void setActiveChunk(@Nullable ChunkBridge chunk) {
        if (chunk == null && this.world != null && !this.world.isRemote && !this.isValid()) {
            if (this.isTicking) {
                // If a TE is currently ticking and has been invalidated, delay clearing active chunk until finished
                // This is done to avoid issues during unwind when calling getActiveChunk
                // Note: This occurs with TE's such as pistons that invalidate during movement
                return;
            }
        }
        this.activeChunk = new WeakReference<ChunkBridge>(chunk);
    }

    @Override
    public boolean shouldTick() {
        final ChunkBridge chunk = this.getActiveChunk();
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
    public boolean isCaptured() {
        return this.isCaptured;
    }

    @Override
    public void setCaptured(boolean captured) {
        this.isCaptured = captured;
    }

    @Override
    public void refreshTrackerStates() {
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
            .add("tileType", this.tileType.getId())
            .add("world", this.world)
            .add("pos", this.pos)
            .add("blockMetadata", this.blockMetadata)
            .toString();
    }

    protected MoreObjects.ToStringHelper getPrettyPrinterStringHelper() {
        return MoreObjects.toStringHelper(this)
            .add("type", this.tileType.getId())
            .add("world", this.world.getWorldInfo().getWorldName())
            .add("pos", this.pos);
    }

    @Override
    public String getPrettyPrinterString() {
        return getPrettyPrinterStringHelper().toString();
    }
}
