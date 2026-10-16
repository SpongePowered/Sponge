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
package org.spongepowered.common.test.gametest;

import io.leangen.geantyref.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.event.Cancellable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.block.ScheduleBlockUpdateEvent;
import org.spongepowered.api.event.block.TickBlockEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.common.util.VecHelper;
import org.spongepowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Records, in the order Sponge throws them, the block and entity spawn events that touch one
 * test's structure. Positions are relative to the test, so entries read the same
 * wherever the test was placed, and listeners run at {@link Order#POST} to see
 * each event's final cancellation.
 *
 * <p>Listeners are global and tests in a batch share ticks, so anything outside
 * this test's bounds is ignored.</p>
 */
public final class EventRecorder {

    /** @param positions relative to the test; for neighbor notifications these are the targets */
    public record Entry(long tick, String event, boolean cancelled, List<BlockPos> positions, String cause) {

        @Override
        public String toString() {
            final String positions = this.positions.stream()
                .map(pos -> "(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")")
                .toList()
                .toString();
            return "t" + this.tick + " " + this.event + (this.cancelled ? " [cancelled]" : "") + " " + positions + " <- " + this.cause;
        }
    }

    private final GameTestHelper helper;
    private final AABB bounds;
    private final List<Entry> entries = new ArrayList<>();
    private final List<EventListener<?>> listeners = new ArrayList<>();

    private EventRecorder(final GameTestHelper helper) {
        this.helper = helper;
        this.bounds = helper.getBounds();
    }

    public static EventRecorder start(final GameTestHelper helper) {
        final EventRecorder recorder = new EventRecorder(helper);
        recorder.record(TypeToken.get(ChangeBlockEvent.Pre.class), "Pre",
            event -> event.locations().stream().map(Location::blockPosition));
        recorder.record(TypeToken.get(ChangeBlockEvent.All.class), "All",
            event -> event.transactions().stream().map(transaction -> transaction.original().position()));
        recorder.record(TypeToken.get(ChangeBlockEvent.Post.class), "Post",
            event -> event.receipts().stream().map(receipt -> receipt.originalBlock().position()));
        recorder.record(TypeToken.get(NotifyNeighborBlockEvent.class), "NotifyNeighbor",
            event -> event.tickets().stream().map(ticket -> ticket.targetPosition()));
        recorder.record(new TypeToken<ScheduleBlockUpdateEvent<BlockType>>() {}, "ScheduleBlockUpdate",
            event -> event.tickets().stream().map(ticket -> ticket.block().blockPosition()));
        recorder.record(TypeToken.get(TickBlockEvent.Scheduled.class), "TickScheduled",
            event -> Stream.of(event.block().position()));
        recorder.record(TypeToken.get(TickBlockEvent.Random.class), "TickRandom",
            event -> Stream.of(event.block().position()));
        // Named by the API type thrown, e.g. DropItemEvent.Destruct for block drops
        recorder.record(TypeToken.get(SpawnEntityEvent.class), EventRecorder::apiName,
            event -> event.entities().stream().map(entity -> entity.location().blockPosition()));
        return recorder;
    }

    public List<Entry> entries() {
        return List.copyOf(this.entries);
    }

    /** The event names in order, e.g. {@code [All, NotifyNeighbor, All]}, for asserting a sequence */
    public List<String> events() {
        return this.entries.stream().map(Entry::event).toList();
    }

    public void stop() {
        this.listeners.forEach(Sponge.eventManager()::unregisterListeners);
        this.listeners.clear();
    }

    private <E extends Event> void record(final TypeToken<E> type, final String name, final Function<E, Stream<Vector3i>> positions) {
        this.record(type, event -> name, positions);
    }

    private <E extends Event> void record(final TypeToken<E> type, final Function<E, String> name, final Function<E, Stream<Vector3i>> positions) {
        final EventListener<E> listener = event -> {
            final List<BlockPos> inside = positions.apply(event)
                .map(VecHelper::toBlockPos)
                .filter(pos -> this.bounds.contains(Vec3.atCenterOf(pos)))
                .map(this::relative)
                .distinct()
                .toList();
            if (!inside.isEmpty()) {
                this.entries.add(new Entry(this.helper.getTick(), name.apply(event), event instanceof final Cancellable c && c.isCancelled(), inside,
                    this.describeCause(event)));
            }
        };
        this.listeners.add(listener);
        SpongeGameTests.listen(type, Order.POST, listener);
    }

    // Not GameTestHelper#relativePos: it undoes the rotation by adding 180 degrees, which mirrors unrotated tests
    private BlockPos relative(final BlockPos absolute) {
        final BlockPos origin = this.helper.absolutePos(BlockPos.ZERO);
        final Rotation inverse = Rotation.values()[(4 - this.helper.getTestRotation().ordinal()) % 4];
        return StructureTemplate.transform(absolute, Mirror.NONE, inverse, origin).subtract(origin);
    }

    private static String apiName(final Event event) {
        final Class<?> api = event.getClass().getInterfaces()[0];
        return api.getName().substring(api.getPackageName().length() + 1).replace('$', '.');
    }

    private String describeCause(final Event event) {
        return event.cause().first(LocatableBlock.class)
            .map(block -> {
                final BlockPos pos = this.relative(VecHelper.toBlockPos(block.blockPosition()));
                return block.blockState().type().key(RegistryTypes.BLOCK_TYPE).value()
                    + "(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";
            })
            .orElseGet(() -> event.cause().root().getClass().getSimpleName());
    }
}
