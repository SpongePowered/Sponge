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
package org.spongepowered.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.apache.logging.log4j.Level;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.data.DataHolder;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.data.persistence.Queries;
import org.spongepowered.api.world.BlockChangeFlag;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.data.DataCompoundHolder;
import org.spongepowered.common.bridge.data.DataContainerHolder;
import org.spongepowered.common.data.holder.SpongeImmutableDataHolder;
import org.spongepowered.common.data.persistence.NBTTranslator;
import org.spongepowered.common.data.provider.nbt.NBTDataType;
import org.spongepowered.common.data.provider.nbt.NBTDataTypes;
import org.spongepowered.common.event.tracking.BlockChangeFlagManager;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.block.BlockPhase;
import org.spongepowered.common.util.Constants;
import org.spongepowered.common.util.PrettyPrinter;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.common.world.BlockChange;
import org.spongepowered.common.world.SpongeBlockChangeFlag;
import org.spongepowered.math.vector.Vector3i;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

@DefaultQualifier(NonNull.class)
public final class SpongeBlockSnapshot implements BlockSnapshot, SpongeImmutableDataHolder<BlockSnapshot>, DataContainerHolder.Immutable<BlockSnapshot>, DataCompoundHolder {

    private final BlockState blockState;
    private final ResourceKey worldKey;
    private final Vector3i pos;
    final @Nullable CompoundTag compound;
    // Internal use only
    private final BlockPos blockPos;
    private final SpongeBlockChangeFlag changeFlag;
    @Nullable WeakReference<ServerLevel> world;
    @MonotonicNonNull public BlockChange blockChange; // used for post event

    SpongeBlockSnapshot(final SpongeBlockSnapshotBuilder builder) {
        this.blockState = Objects.requireNonNull(builder.blockState);
        this.worldKey = Objects.requireNonNull(builder.worldKey);
        this.pos = Objects.requireNonNull(builder.coordinates);
        this.blockPos = VecHelper.toBlockPos(this.pos);
        this.compound = builder.compound;
        this.changeFlag = builder.flag;
        this.world = builder.worldRef;
        builder.worldRef = null;
    }

    @Override
    public BlockState state() {
        return this.blockState;
    }

    @Override
    public BlockSnapshot withState(final BlockState blockState) {
        return this.createBuilder().blockState(blockState).build();
    }

    @Override
    public BlockSnapshot withContainer(final DataContainer container) {
        return SpongeBlockSnapshotBuilder.pooled().build(container).get();
    }

    @Override
    public ResourceKey world() {
        return this.worldKey;
    }

    @Override
    public Vector3i position() {
        return this.pos;
    }

    @Override
    public Optional<ServerLocation> location() {
        return this.getServerWorld()
                .map(world -> ServerLocation.of((org.spongepowered.api.world.server.ServerWorld) world, this.pos));
    }

    @Override
    public BlockSnapshot withLocation(final ServerLocation location) {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public boolean restore(final boolean force, final BlockChangeFlag flag) {
        // TODO - rewrite with the PhaseTracker being the hook or use SpongeImplHooks to do the restore.

        final Optional<ServerLevel> optionalWorld = Optional.ofNullable(this.world.get());
        if (!optionalWorld.isPresent()) {
            return false;
        }

        final ServerLevel world = optionalWorld.get();
        // We need to deterministically define the context as nullable if we don't need to enter.
        // this way we guarantee an exit.
        try (final PhaseContext<?> context = BlockPhase.State.RESTORING_BLOCKS.createPhaseContext(PhaseTracker.SERVER)) {
            context.buildAndSwitch();
            final BlockPos pos = VecHelper.toBlockPos(this.pos);
            if (!net.minecraft.world.level.Level.isInWorldBounds(pos)) { // Invalid position. Inline this check
                return false;
            }
            final net.minecraft.world.level.block.state.BlockState current = world.getBlockState(pos);
            final net.minecraft.world.level.block.state.BlockState replaced = (net.minecraft.world.level.block.state.BlockState) this.blockState;
            if (!force && (current.getBlock() != replaced.getBlock() || current != replaced)) {
                return false;
            }

            // Prevent Shulker Boxes from dropping when restoring BlockSnapshot
//            if (current.getBlock().getClass() == BlockShulkerBox.class) {
//                world.bridge$removeTileEntity(pos);
//            }
            world.removeBlockEntity(pos);
            world.setBlock(pos, replaced, BlockChangeFlagManager.andNotifyClients(flag).getRawFlag());
            if (this.compound != null) {
                @Nullable BlockEntity te = world.getBlockEntity(pos);
                if (te != null) {
                    te.load((net.minecraft.world.level.block.state.BlockState) this.blockState, this.compound);
                } else {
                    // Because, some mods will "unintentionally" only obey some of the rules but not all.
                    // In cases like this, we need to directly just say "fuck it" and deserialize from the compound directly.
                    try {
                        te = BlockEntity.loadStatic((net.minecraft.world.level.block.state.BlockState) this.blockState, this.compound);
                        if (te != null) {
                            world.getChunk(pos).setBlockEntity(pos, te);
                        }
                    } catch (Exception e) {
                        // Seriously? The mod should be broken then.
                        final PrettyPrinter printer = new PrettyPrinter(60).add("Unable to restore").centre().hr()
                            .add("A mod is not correctly deserializing a TileEntity that is being restored. ")
                            .addWrapped(60, "Note that this is not the fault of Sponge. Sponge is understanding that "
                                + "a block is supposed to have a TileEntity, but the mod is breaking the contract"
                                + "on how to re-create the tile entity. Please open an issue with the offending mod.")
                            .add("Here's the provided compound:");
                        printer.add();
                        try {
                            printer.addWrapped(80, "%s : %s", "This compound", this.compound);
                        } catch (Throwable error) {
                            printer.addWrapped(
                                80,
                                "Unable to get the string of this compound. Printing out some of the entries to better assist"
                            );

                        }
                        printer.add()
                            .add("Desired World: " + this.worldKey)
                            .add("Position: " + this.pos)
                            .add("Desired BlockState: " + this.blockState);
                        printer.add();
                        printer.log(SpongeCommon.logger(), Level.ERROR);
                        return true; // I mean, I guess. the block was set up, but not the tile entity.
                    }

                }

                if (te != null) {
                    te.setChanged();
                }

            }
            // Finally, mark the location as being updated.
            world.getChunkSource().blockChanged(pos);
            return true;
        }
    }

    @Override
    public Optional<UUID> creator() {
        return Optional.empty();
    }

    @Override
    public Optional<UUID> notifier() {
        return Optional.empty();
    }

    @Override
    public Optional<BlockEntityArchetype> createArchetype() {
        throw new UnsupportedOperationException("Not implemented yet, please fix when this is called");
    }

    @Override
    public BlockSnapshot withRawData(final DataView container) throws InvalidDataException {
        return SpongeBlockSnapshotBuilder.pooled().buildContent(container).orElseThrow(InvalidDataException::new);
    }

    @Override
    public boolean validateRawData(final DataView container) {
        return SpongeBlockSnapshotBuilder.pooled().buildContent(container).isPresent();
    }

    @Override
    public BlockSnapshot copy() {
        return this;
    }

    @Override
    public int contentVersion() {
        return 1;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = DataContainer.createNew()
                .set(Queries.CONTENT_VERSION, contentVersion())
                .set(Queries.WORLD_KEY, this.worldKey.asString())
                .createView(Constants.Sponge.SNAPSHOT_WORLD_POSITION)
                .set(Queries.POSITION_X, this.pos.x())
                .set(Queries.POSITION_Y, this.pos.y())
                .set(Queries.POSITION_Z, this.pos.z())
                .container()
                .set(Constants.Block.BLOCK_STATE, this.blockState);
        if (this.compound != null) {
            container.set(Constants.Sponge.UNSAFE_NBT, NBTTranslator.INSTANCE.translateFrom(this.compound));
        }
        return container;
    }

    public Optional<ServerLevel> getServerWorld() {
        @Nullable ServerLevel world = this.world != null ? this.world.get() : null;
        if (world == null) {
            world = (ServerLevel) Sponge.server().worldManager().world(this.worldKey).orElse(null);
            if (world != null) {
                this.world = new WeakReference<>(world);
            }
        }
        return Optional.ofNullable(world);
    }

    public Optional<CompoundTag> getCompound() {
        return this.compound == null ? Optional.empty() : Optional.of(this.compound.copy());
    }

    public SpongeBlockSnapshotBuilder createBuilder() {
        final SpongeBlockSnapshotBuilder builder = SpongeBlockSnapshotBuilder.pooled();
        builder.blockState(this.blockState)
               .position(this.pos);
        if (this.world != null && this.world.get() != null) {
            builder.world(this.world.get());
        } else {
            builder.world(this.worldKey);
        }
        if (this.compound != null) {
            builder.addUnsafeCompound(this.compound);
        }
        return builder;
    }

    @Override
    public DataContainer data$getDataContainer() {
        if (this.compound == null) {
            return DataContainer.createNew();
        }
        return NBTTranslator.INSTANCE.translate(this.compound);
    }

    @Override
    public BlockSnapshot data$withDataContainer(DataContainer container) {
        final SpongeBlockSnapshotBuilder builder = this.createBuilder();
        builder.compound = NBTTranslator.INSTANCE.translate(container);;
        return builder.build();
    }

    @Override
    public CompoundTag data$getCompound() {
        return this.compound == null ? new CompoundTag() : this.compound.copy();
    }

    @Override
    public void data$setCompound(CompoundTag nbt) {
        // do nothing this is immutable
    }

    @Override
    public List<DataHolder> impl$delegateDataHolder() {
        return Arrays.asList(this, this.state(), this.state().type());
    }

    @Override
    public NBTDataType data$getNBTDataType() {
        return NBTDataTypes.BLOCK_ENTITY;
    }

    // Used internally for restores

    public SpongeBlockChangeFlag getChangeFlag() {
        return this.changeFlag;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SpongeBlockSnapshot that = (SpongeBlockSnapshot) o;
        return this.blockState.equals(that.blockState) &&
               this.changeFlag == that.changeFlag &&
               Objects.equals(this.worldKey, that.worldKey) &&
               Objects.equals(this.pos, that.pos) &&
               Objects.equals(this.compound, that.compound);
    }

    @Override
    public int hashCode() {
        return Objects
            .hash(this.blockState,
                this.worldKey,
                this.pos,
                this.changeFlag,
                this.compound);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SpongeBlockSnapshot.class.getSimpleName() + "[", "]")
            .add("world=" + this.worldKey)
            .add("position=" + this.blockPos)
            .add("blockState=" + this.blockState)
            .toString();
    }
}
