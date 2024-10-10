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
package org.spongepowered.common.mixin.api.minecraft.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntity;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.block.entity.BlockEntityType;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.block.entity.SpongeBlockEntityArchetypeBuilder;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.server.SpongeLocatableBlockBuilder;
import org.spongepowered.math.vector.Vector3i;

import java.util.HashSet;
import java.util.Set;

@Mixin(net.minecraft.world.level.block.entity.BlockEntity.class)
public abstract class BlockEntityMixin_API implements BlockEntity {

    //@formatter:off
    @Shadow @Final private net.minecraft.world.level.block.entity.BlockEntityType<?> type;
    @Shadow protected net.minecraft.world.level.Level level;
    @Shadow protected boolean remove;
    @Shadow public abstract BlockPos shadow$getBlockPos();
    @Shadow public abstract CompoundTag shadow$saveWithFullMetadata(HolderLookup.Provider $$0);
    @Shadow @Final protected BlockPos worldPosition;
    @Shadow public abstract net.minecraft.world.level.block.state.BlockState shadow$getBlockState();
    @Shadow public abstract void shadow$setChanged();
    //@formatter:on

    @Nullable private LocatableBlock api$LocatableBlock;
    private boolean api$canTickRequested = false;
    private boolean api$canTick;
    private boolean api$isTicking = true;

    public ServerLocation location() {
        return ServerLocation.of((ServerWorld) this.level, VecHelper.toVector3i(this.shadow$getBlockPos()));
    }

    @Override
    public ServerLocation serverLocation() {
        if (this.level == null) {
            throw new RuntimeException("The TileEntity has not been spawned in a world yet!");
        }

        if (this.level.isClientSide) {
            throw new RuntimeException("You should not attempt to make a server-side location on the client!");
        }

        final BlockPos pos = this.shadow$getBlockPos();
        return ServerLocation.of((ServerWorld) this.level, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public World<?, ?> world() {
        return (World<?, ?>) this.level;
    }

    @Override
    public Vector3i blockPosition() {
        return VecHelper.toVector3i(this.shadow$getBlockPos());
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final ResourceKey key = (ResourceKey) (Object) SpongeCommon.vanillaRegistry(Registries.BLOCK_ENTITY_TYPE).getKey(this.type);

        final DataContainer container = DataContainer.createNew()
            .set(Queries.CONTENT_VERSION, this.contentVersion())
            .set(Queries.WORLD_KEY, ((ServerWorld) this.level).key())
            .set(Queries.POSITION_X, this.shadow$getBlockPos().getX())
            .set(Queries.POSITION_Y, this.shadow$getBlockPos().getY())
            .set(Queries.POSITION_Z, this.shadow$getBlockPos().getZ())
            .set(Constants.TileEntity.TILE_TYPE, key);
        final CompoundTag compound = this.shadow$saveWithFullMetadata(this.level.registryAccess());
        Constants.NBT.filterSpongeCustomData(compound); // We must filter the custom data so it isn't stored twice
        container.set(Constants.Sponge.UNSAFE_NBT, NBTTranslator.INSTANCE.translateFrom(compound));
        return container;
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return container.contains(Queries.WORLD_KEY)
            && container.contains(Queries.POSITION_X)
            && container.contains(Queries.POSITION_Y)
            && container.contains(Queries.POSITION_Z)
            && container.contains(Constants.TileEntity.TILE_TYPE)
            && container.contains(Constants.Sponge.UNSAFE_NBT);
    }

    @Override
    public boolean isRemoved() {
        return this.remove;
    }

    @Override
    public void remove() {
        if (!this.remove) {
            this.world().removeBlockEntity(this.blockPosition());
        }
    }

    @Override
    public boolean canTick() {
        if (!this.api$canTickRequested) {
            this.api$canTick = this.shadow$getBlockState().getTicker(this.level, this.type) != null;
            this.api$canTickRequested = true;
        }

        return api$canTick;
    }

    @Override
    public boolean isTicking() {
        return !this.remove && this.canTick() && this.api$isTicking;
    }

    @Override
    public boolean setTicking(final boolean ticking) {
        if (this.isRemoved() || !this.canTick()) {
            return false;
        }

        this.api$isTicking = ticking;
        return true;
    }

    @Override
    public final BlockEntityType type() {
        return (BlockEntityType) this.type;
    }

    @Override
    public BlockState block() {
        return (BlockState) this.level.getBlockState(this.shadow$getBlockPos());
    }

    @Override
    public BlockEntityArchetype createArchetype() {
        return SpongeBlockEntityArchetypeBuilder.pooled()
            .blockEntity(this)
            .build();
    }

    @Override
    public LocatableBlock locatableBlock() {
        if (this.api$LocatableBlock == null) {
            final BlockState blockState = this.block();
            this.api$LocatableBlock = new SpongeLocatableBlockBuilder()
                .world((ServerWorld) this.level)
                .position(this.shadow$getBlockPos().getX(), this.shadow$getBlockPos().getY(), this.shadow$getBlockPos().getZ())
                .state(blockState)
                .build();
        }

        return this.api$LocatableBlock;
    }

    @Override
    public Set<Value.Immutable<?>> getValues() {
        // TODO: Minecraft 1.1 - Merge custom and Vanilla values and return the merged result.
        return this.api$getVanillaValues();
    }

    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        return new HashSet<>();
    }

}
