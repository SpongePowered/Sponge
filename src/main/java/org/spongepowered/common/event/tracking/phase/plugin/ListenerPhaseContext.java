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
package org.spongepowered.common.event.tracking.phase.plugin;

import static com.google.common.base.Preconditions.checkState;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.event.tracking.IPhaseState;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.TrackingUtil;

import java.util.Optional;

import javax.annotation.Nullable;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ListenerPhaseContext<L extends ListenerPhaseContext<L>> extends PluginPhaseContext<L> {


    Object object;
    private CapturePlayer capturePlayer;

    ListenerPhaseContext(final IPhaseState<L> state) {
        super(state);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected boolean isRunaway(final PhaseContext<?> phaseContext) {
        return phaseContext instanceof ListenerPhaseContext && ((ListenerPhaseContext) phaseContext).object == this.object;
    }

    @SuppressWarnings("unchecked")
    public L event(final Object obj) {
        this.object = obj;
        return (L) this;
    }

    @SuppressWarnings("unchecked")
    public L player() {
        checkState(!this.isCompleted, "Cannot add a new object to the context if it's already marked as completed!");
        checkState(this.capturePlayer == null, "Already capturing a player object!");
        this.capturePlayer = new CapturePlayer();
        return (L) this;
    }

    public Object getEvent() {
        return this.object;
    }

    public CapturePlayer getCapturedPlayerSupplier() throws IllegalStateException {
        if (this.capturePlayer == null) {
            throw TrackingUtil.throwWithContext("Expected to be capturing a Player from an event listener, but we're not capturing them!", this)
                .get();
        }
        return this.capturePlayer;
    }

    public Optional<Player> getCapturedPlayer() throws IllegalStateException {
        return getCapturedPlayerSupplier().getPlayer();
    }

    @Override
    public PrettyPrinter printCustom(final PrettyPrinter printer, final int indent) {
        final String s = String.format("%1$" + indent + "s", "");
        super.printCustom(printer, indent)
            .add(s + "- %s: %s", "Listener", this.object);
        if (this.capturePlayer != null && this.capturePlayer.player != null) {
            printer.add(s + "- %s: %s", "CapturedPlayer", this.capturePlayer.player);
        }
        return printer;
    }

    @Override
    protected void reset() {
        super.reset();
        this.object = null;
        if (this.capturePlayer != null) {
            this.capturePlayer.player = null;
        }
    }

    public static final class CapturePlayer {

        @Nullable Player player;

        CapturePlayer() {

        }

        public Optional<Player> getPlayer() {
            return Optional.ofNullable(this.player);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final CapturePlayer that = (CapturePlayer) o;
            return com.google.common.base.Objects.equal(this.player, that.player);
        }

        @Override
        public int hashCode() {
            return com.google.common.base.Objects.hashCode(this.player);
        }

        @Override
        public String toString() {
            return com.google.common.base.MoreObjects.toStringHelper(this)
                .add("player", this.player)
                .toString();
        }

        public void addPlayer(final ServerPlayerEntity playerMP) {
            this.player = ((Player) playerMP);
        }
    }
}
