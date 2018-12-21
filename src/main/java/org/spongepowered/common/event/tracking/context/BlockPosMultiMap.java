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
package org.spongepowered.common.event.tracking.context;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.World;
import org.spongepowered.common.block.BlockUtil;
import org.spongepowered.common.block.SpongeBlockSnapshot;
import org.spongepowered.common.block.SpongeBlockSnapshotBuilder;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.TrackingUtil;
import org.spongepowered.common.world.BlockChange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

public final class BlockPosMultiMap implements ICaptureSupplier {

    @Nullable private ListMultimap<BlockPos, SpongeBlockSnapshot> multimap;
    @Nullable private List<SpongeBlockSnapshot> snapshots;
    @Nullable private Set<BlockPos> usedBlocks;
    private boolean hasMulti = false;

    public BlockPosMultiMap() {
    }

    /**
     * Captures the provided {@link BlockSnapshot} into a {@link Multimap} backed collection.
     * The premise is that each {@link BlockPos} normally has a single {@link BlockChange},
     * with the exceptions of certain few cases where multiple changes can occur for the same
     * position. The larger issue is that while the multiple changes are tracked, the desired
     * flag of changes does not result in a valid {@link BlockChange}, and therefor an invalid
     * {@link ChangeBlockEvent} is generated, potentially leading to duplication bugs with
     * protection plugins. As a result, the consuming {@link BlockSnapshot} is placed into
     * a {@link ListMultimap} keyed by the {@link BlockPos}, and if there are multiple snapshots
     * per {@link BlockPos}, {@link #hasMultiChanges()} will be {@code true}, and this method
     * will return {@code true}.
     *
     * @param snapshot The snapshot being captured
     * @param newState The most current new IBlockState to calculate the BlockChange flag
     * @return True if the block position has previously not been modified or captured yet
     */
    public boolean put(BlockSnapshot snapshot, IBlockState newState) {
        // Start by figuring out the backing snapshot. In all likelyhood, we could just cast, but we want to be safe
        final SpongeBlockSnapshot backingSnapshot;
        if (!(snapshot instanceof SpongeBlockSnapshot)) {
            backingSnapshot = new SpongeBlockSnapshotBuilder().from(snapshot).build();
        } else {
            backingSnapshot = (SpongeBlockSnapshot) snapshot;
        }
        // Get the key of the block position, we know this is a pure block pos and not a mutable one too.
        final BlockPos blockPos = backingSnapshot.getBlockPos();
        if (this.usedBlocks == null) { // Means we have a first usage. All three fields are null
            // At this point, we know we have not captured anything and
            // can just populate the normal list.
            this.usedBlocks = new HashSet<>();
            this.snapshots = new ArrayList<>();

            this.usedBlocks.add(blockPos);
            this.snapshots.add(backingSnapshot);
            return true;
        }
        // This isn't our first rodeo...
        final boolean added = this.usedBlocks.add(blockPos); // add it to the set of positions already used and use the boolean
        if (this.hasMulti) {
            // Means we've already got multiple changes per position once before.
            // Likewise, the used blocks, snapshots and multimap will NOT be null.
            // more fasts, we know we have multiple block positions.
            // And we can find out if this is the first time we
            if (added) {
                // If the position hasn't been captured yet, that means we need to add it as an original
                // snapshot being changed, for the list usage.
                this.snapshots.add(backingSnapshot);
            }
            // we don't have to
            this.multimap.put(blockPos, backingSnapshot);

            // If the position is duplicated, we need to update the original snapshot of the now incoming block change
            // in relation to the original state (so if a block was set to air, then afterwards set to piston head, it should go from break to modify)
            if (!added) {
                final List<SpongeBlockSnapshot> list = this.multimap.get(blockPos);
                if (list != null && !list.isEmpty()) {
                    final SpongeBlockSnapshot originalSnapshot = list.get(0);
                    TrackingUtil.associateBlockChangeWithSnapshot(PhaseTracker.getInstance().getCurrentState(), newState.getBlock(), BlockUtil.toNative(originalSnapshot.getState()), originalSnapshot );
                }
            }
            return added;
        }
        // We have not yet checked if this incoming snapshot is a duplicate position
        if (!added) {
            // Ok, means we have a multi change on a same position, now to use the multimap
            // for probably the first time.
            if (this.multimap == null) {
                // If the multimap hasn't been populated yet, well, now we have to populate the multimap and eliminate the list.
                this.multimap = LinkedListMultimap.create(); // LinkedListMultimap is insertion order respective, so the backed lists per
                // Now to populate it from the previously used list of snapshots...
                for (SpongeBlockSnapshot existing : this.snapshots) { // Ignore snapshots potentially being null, it will never be null at this point.
                    this.multimap.put(existing.getBlockPos(), existing);
                }
            }
            // And place the snapshot into the multimap.
            this.multimap.put(blockPos, backingSnapshot);
            // Now we can re-evaluate the modified block position
            // If the position is duplicated, we need to update the original snapshot of the now incoming block change
            // in relation to the original state (so if a block was set to air, then afterwards set to piston head, it should go from break to modify)
            final List<SpongeBlockSnapshot> list = this.multimap.get(blockPos);
            if (list != null && !list.isEmpty()) {
                final SpongeBlockSnapshot originalSnapshot = list.get(0);
                TrackingUtil.associateBlockChangeWithSnapshot(PhaseTracker.getInstance().getCurrentState(), newState.getBlock(), BlockUtil.toNative(originalSnapshot.getState()), originalSnapshot );
            }
            // Flip the boolean to have fasts for next entry
            this.hasMulti = true;
            return false;
        }
        // At this point, we haven't captured the block position yet.
        // and we can check if the list is null.
        if (this.snapshots == null) {
            this.snapshots = new ArrayList<>();
        }
        this.snapshots.add(backingSnapshot);
        // And this is the only time that we return true, if we have not caught multiple transactions per position before.
        return true;
    }

    public boolean hasMultiChanges() {
        return this.hasMulti;
    }

    /**
     * Gets an <b>unmodifiable</b> {@link List} of the original
     * {@link BlockSnapshot}s being changed for their respective
     * {@link BlockPos Block Positions}. The list is self updating
     * and the {@link BlockSnapshot}s themselves are self updating
     * based on the current processes within the PhaseTracker's
     * {@link IPhaseState} the game is processing. The reasons for
     * this list to be unmodifiable except by this object are as follows:
     * <ul>
     *     <li>Submitted {@link BlockSnapshot}s are to be added by the
     *     {@link #put(BlockSnapshot, IBlockState)} method.</li>
     *     <li>Adding multiple {@link BlockSnapshot}s per {@link BlockPos}
     *     results in an internal restructuring of storage such that a
     *     {@link Multimap} is created to keep track of intermediary
     *     {@link BlockSnapshot}s. By this nature, the list cannot be modified
     *     except by this capture object.</li>
     *     <li>Removing a {@link BlockSnapshot} is only applicable via
     *     {@link #prune(BlockSnapshot)} or {@link #clear()}. This is to
     *     allow sanity checking for multimap purposes and garbage cleanup
     *     when necessary.</li>
     *     <li>The creation of {@link ChangeBlockEvent}s requires a
     *     {@link Transaction} to be created, and plugins are only
     *     exposed the {@link Transaction#getOriginal()} as the first
     *     {@link BlockSnapshot} that would exist in this list. Intermediary
     *     {@link BlockSnapshot} changes for that postiion are internally
     *     utilized to process physics, but are not exposed to the event.</li>
     * </ul>
     *
     * @return An unmodifiable list of first block originals being changed
     */
    public final List<SpongeBlockSnapshot> get() {
        return Collections.unmodifiableList(this.snapshots == null ? Collections.emptyList() : this.snapshots);
    }

    public final void prune(BlockSnapshot snapshot) {
        // TODO - We need to not only prune the snapshot from the list of snapshots, but may also need to revert the multimap usage.
    }

    /**
     * Returns {@code true} if there are no captured objects.
     *
     * @return {@code true} if empty
     */
    @Override
    public final boolean isEmpty() {
        return this.snapshots != null && !this.snapshots.isEmpty();
    }

    /**
     * If not empty, activates the {@link BiConsumer} then clears all captures.
     * The catch with this is that as the underlying list is guaranteed
     * to be the first {@link BlockSnapshot} change, multiple changes can exist
     * for an individual {@link BlockPos}, such that the multi-map needs to
     * provide said information to the consumer.
     *
     * <p>The first {@link List} parameter is identical to having been
     * built from the first elements of each list from the second parameter
     * of {@link Map} where {@link Map#values()} returns the insertion order
     * preserved {@link List} of {@link BlockSnapshot}s for the backed
     * {@link BlockPos}. In simpler terms, the {@link List} is built from
     * the first elements of each list from the {@link Map}s values.</p>
     *
     * @param consumer The consumer to activate
     */
    @SuppressWarnings("UnstableApiUsage") // Guava marks Multimaps.asMap as Beta features
    public final void acceptAndClearIfNotEmpty(BiConsumer<List<? extends BlockSnapshot>, Map<BlockPos, List<BlockSnapshot>>> consumer) {
        if (this.multimap != null) {
            final List<? extends BlockSnapshot> blockSnapshots = get();
            // Since multimaps provide a view when asMap is called, we need to recreate the collection
            // of the map to pass into the consumer
            final Map<BlockPos, List<SpongeBlockSnapshot>> view = Multimaps.asMap(this.multimap);
            final Map<BlockPos, List<BlockSnapshot>> map = new LinkedHashMap<>(view.size());
            for (Map.Entry<BlockPos, List<SpongeBlockSnapshot>> entryView : view.entrySet()) {
                map.put(entryView.getKey(), new ArrayList<>(entryView.getValue()));
            }
            this.multimap.clear(); // Clean captured lists before they get potentially contaminated by processing.
            consumer.accept(blockSnapshots, map); // Accept the list and map
        }
    }

    /**
     * Similar to {@link #get()}, but instead of creating the underlying collections,
     * this will fail fast. Utilize for checks when lists are needed regardless whether
     * they are empty or not.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<SpongeBlockSnapshot> orEmptyList() {
        if (this.snapshots == null) {
            return Collections.emptyList();
        }
        if (this.snapshots.isEmpty()) {
            return Collections.emptyList();
        }
        return get();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.snapshots);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final BlockPosMultiMap other = (BlockPosMultiMap) obj;
        return Objects.equals(this.multimap, other.multimap);
    }


    @Override
    public String toString() {
        return com.google.common.base.MoreObjects.toStringHelper(this)
            .add("Captured", this.multimap == null ? 0 : this.multimap.size())
            .toString();
    }

    public void clear() {
        this.hasMulti = false;
        if (this.multimap != null) {
            this.multimap.clear();
            this.multimap = null;
        }
        if (this.snapshots != null) {
            this.snapshots.clear();
            this.snapshots = null;
        }
        if (this.usedBlocks != null) {
            this.usedBlocks.clear();
        }
    }

    public void restoreOriginals() {
        if (this.snapshots != null && !this.snapshots.isEmpty()) {
            for (SpongeBlockSnapshot original : Lists.reverse(this.snapshots)) {
                original.restore(true, BlockChangeFlags.NONE);
            }
            this.clear();
        }
    }
}
