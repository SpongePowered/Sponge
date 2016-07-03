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
package org.spongepowered.common.event.tracking;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Multimap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.common.event.InternalNamedCauses;
import org.spongepowered.common.event.tracking.phase.ItemDropData;
import org.spongepowered.common.event.tracking.phase.util.PhaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import javax.annotation.Nullable;

/**
 * Similar to {@link Cause} except it can be built continuously
 * and retains no real side effects. Strictly speaking this object
 * exists to avoid confusion between what is suggested to be a
 * {@link Cause} for an {@link Event} versus the context of which
 * a {@link IPhaseState} is being completed with.
 */
public class PhaseContext {

    private boolean isCompleted = false;
    private final ArrayList<NamedCause> contextObjects = new ArrayList<>(10);
    @Nullable private Cause cause = null;

    public static PhaseContext start() {
        return new PhaseContext();
    }

    public PhaseContext add(@Nullable NamedCause namedCause) {
        if (namedCause == null) {
            return this;
        }
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(namedCause);
        return this;
    }

    public PhaseContext addBlockCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, new CapturedBlocksSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCK_ITEM_DROPS, new BlockItemEntityDropsSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS, new BlockItemDropsSupplier()));
        return this;
    }

    public PhaseContext addCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, new CapturedBlocksSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS, new BlockItemDropsSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_BLOCK_ITEM_DROPS, new BlockItemEntityDropsSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEMS, new CapturedItemsSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, new CapturedEntitiesSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEM_STACKS, new CapturedItemStackSupplier()));
        return this;
    }

    public PhaseContext addEntityCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, new CapturedEntitiesSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEMS, new CapturedItemsSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ITEM_STACKS, new CapturedItemStackSupplier()));
        return this;
    }

    public PhaseContext addEntityDropCaptures() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITY_STACK_DROPS, new EntityItemDropsSupplier()));
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_ENTITY_ITEM_DROPS, new EntityItemEntityDropsSupplier()));
        return this;
    }

    public PhaseContext player() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_PLAYER, new CapturePlayer()));
        return this;
    }

    public PhaseContext player(@Nullable Player player) {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        this.contextObjects.add(NamedCause.of(InternalNamedCauses.Tracker.CAPTURED_PLAYER, new CapturePlayer(player)));
        return this;
    }

    public PhaseContext complete() {
        this.isCompleted = true;
        return this;
    }

    public boolean isComplete() {
        return this.isCompleted;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> first(Class<T> tClass) {
        for (NamedCause cause : this.contextObjects) {
            if (tClass.isInstance(cause.getCauseObject())) {
                return Optional.of((T) cause.getCauseObject());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> firstNamed(String name, Class<T> tClass) {
        for (NamedCause cause : this.contextObjects) {
            if (cause.getName().equalsIgnoreCase(name) && tClass.isInstance(cause.getCauseObject())) {
                return Optional.of((T) cause.getCauseObject());
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Entity> getCapturedEntities() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, CapturedEntitiesSupplier.class)
                .map(CapturedEntitiesSupplier::get)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to capture entity spawns!", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<Entity> getCapturedEntitySupplier() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITIES, (Class<CapturedSupplier<Entity>>) (Class<?>) CapturedEntitiesSupplier.class)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to capture entity spawns!", this));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<EntityItem> getCapturedItems() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ITEMS, CapturedItemsSupplier.class)
                .map(CapturedItemsSupplier::get)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to capture dropped item entities!", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<EntityItem> getCapturedItemsSupplier() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ITEMS, (Class<CapturedSupplier<EntityItem>>) (Class<?>) CapturedItemsSupplier.class)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to capture dropped item entities!", this));
    }

    @SuppressWarnings("unchecked")
    public List<BlockSnapshot> getCapturedBlocks() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, CapturedBlocksSupplier.class)
                .map(CapturedBlocksSupplier::get)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to capture block changes, but there is no list available!", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<BlockSnapshot> getCapturedBlockSupplier() throws IllegalStateException {
        return this.firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCKS, (Class<CapturedSupplier<BlockSnapshot>>) (Class<?>) CapturedBlocksSupplier.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing blocks, but we're not capturing them!", this));
    }

    public Multimap<BlockPos, ItemDropData> getCapturedBlockDrops() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS, BlockItemDropsSupplier.class)
                .map(BlockItemDropsSupplier::get)
                .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing block drops", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<BlockPos, ItemDropData> getBlockDropSupplier() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCK_DROPS,
                (Class<CapturedMultiMapSupplier<BlockPos, ItemDropData>>) (Class<?>) BlockItemDropsSupplier.class)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to track block item drops!", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<BlockPos, EntityItem> getBlockItemDropSupplier() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_BLOCK_ITEM_DROPS,
                (Class<CapturedMultiMapSupplier<BlockPos, EntityItem>>) (Class<?>) BlockItemEntityDropsSupplier.class)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to track block item drops!", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<UUID, ItemDropData> getCapturedEntityDropSupplier() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITY_STACK_DROPS,
                (Class<CapturedMultiMapSupplier<UUID, ItemDropData>>) (Class<?>) EntityItemDropsSupplier.class)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to capture entity drops!", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedMultiMapSupplier<UUID, EntityItem> getCapturedEntityItemDropSupplier() throws IllegalStateException {
        return firstNamed(InternalNamedCauses.Tracker.CAPTURED_ENTITY_ITEM_DROPS,
                (Class<CapturedMultiMapSupplier<UUID, EntityItem>>) (Class<?>) EntityItemEntityDropsSupplier.class)
                .orElseThrow(PhaseUtil.throwWithContext("Intended to capture entity drops!", this));
    }

    @SuppressWarnings("unchecked")
    public CapturedSupplier<ItemDropData> getCapturedItemStackSupplier() throws IllegalStateException {
        return this.firstNamed(InternalNamedCauses.Tracker.CAPTURED_ITEM_STACKS, (Class<CapturedSupplier<ItemDropData>>) (Class<?>) CapturedItemStackSupplier.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing ItemStack drops from entities, but we're not capturing them!", this));
    }

    public CapturePlayer getCapturedPlayerSupplier() throws IllegalStateException {
        return this.firstNamed(InternalNamedCauses.Tracker.CAPTURED_PLAYER, CapturePlayer.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a Player from an event listener, but we're not capturing them!", this));
    }

    public Optional<Player> getCapturedPlayer() throws IllegalStateException {
        return this.firstNamed(InternalNamedCauses.Tracker.CAPTURED_PLAYER, CapturePlayer.class)
                .orElseThrow(PhaseUtil.throwWithContext("Expected to be capturing a Player from an event listener, but we're not capturing them!", this))
                .getPlayer();
    }

    public void forEach(Consumer<NamedCause> consumer) {
        this.contextObjects.forEach(consumer);
    }

    PhaseContext() {
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.isCompleted, this.contextObjects, this.cause);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final PhaseContext other = (PhaseContext) obj;
        return Objects.equals(this.isCompleted, other.isCompleted)
               && Objects.equals(this.contextObjects, other.contextObjects)
               && Objects.equals(this.cause, other.cause);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("isCompleted", this.isCompleted)
                .add("contextObjects", this.contextObjects)
                .add("cause", this.cause)
                .toString();
    }

    static class BlockItemDropsSupplier extends CapturedMultiMapSupplier<BlockPos, ItemDropData> {

        BlockItemDropsSupplier() {
        }

    }

    static class EntityItemDropsSupplier extends CapturedMultiMapSupplier<UUID, ItemDropData> {

        EntityItemDropsSupplier() {
        }
    }

    static final class CapturedItemsSupplier extends CapturedSupplier<EntityItem> {

        CapturedItemsSupplier() {
        }
    }

    static final class CapturedItemStackSupplier extends CapturedSupplier<ItemDropData> {

        CapturedItemStackSupplier() {
        }
    }

    static final class CapturedBlocksSupplier extends CapturedSupplier<BlockSnapshot> {

        CapturedBlocksSupplier() {
        }
    }

    static final class CapturedEntitiesSupplier extends CapturedSupplier<Entity> {

        CapturedEntitiesSupplier() {
        }
    }

    static final class EntityItemEntityDropsSupplier extends CapturedMultiMapSupplier<UUID, EntityItem> {

        EntityItemEntityDropsSupplier() {
        }
    }

    static final class BlockItemEntityDropsSupplier extends CapturedMultiMapSupplier<BlockPos, EntityItem> {
        BlockItemEntityDropsSupplier() {
        }
    }

    public static final class CapturePlayer {

        @Nullable private Player player;

        CapturePlayer() {

        }

        CapturePlayer(@Nullable Player player) {
            this.player = player;
        }

        public Optional<Player> getPlayer() {
            return Optional.ofNullable(this.player);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CapturePlayer that = (CapturePlayer) o;
            return com.google.common.base.Objects.equal(player, that.player);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(player);
        }

        @Override
        public String toString() {
            return com.google.common.base.Objects.toStringHelper(this)
                    .add("player", player)
                    .toString();
        }

        public void addPlayer(EntityPlayerMP playerMP) {
            this.player = ((Player) playerMP);
        }
    }
}
