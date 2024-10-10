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
package org.spongepowered.test.portal;

import com.google.inject.Inject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.MovementTypes;
import org.spongepowered.api.event.entity.ChangeEntityWorldEvent;
import org.spongepowered.api.event.entity.InvokePortalEvent;
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.world.portal.Portal;
import org.spongepowered.api.world.portal.PortalLogic;
import org.spongepowered.api.world.server.ServerLocation;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;
import org.spongepowered.test.LoadableModule;

import java.util.Optional;

@Plugin("portaltest")
public final class PortalTest implements LoadableModule {

    public final static class Holder {
        public final static PortalTestListener INSTANCE = new PortalTestListener();
    }

    private final PluginContainer pluginContainer;

    @Inject
    public PortalTest(final PluginContainer pluginContainer) {
        this.pluginContainer = pluginContainer;
    }

    @Override
    public void enable(final CommandContext ctx) {
        Sponge.eventManager().registerListeners(this.pluginContainer, PortalTest.Holder.INSTANCE);
        ctx.cause().first(ServerPlayer.class).ifPresent(player -> {
            final var portalONE = PortalLogic.builder().addSimplePortal((from, fromPos, entity) -> Optional.of(entity.serverLocation().add(Vector3i.ONE))).build();
            player.offer(Keys.PORTAL_LOGIC, portalONE);
        });
    }

    @Override
    public void disable(final CommandContext ctx) {
        Sponge.eventManager().unregisterListeners(PortalTest.Holder.INSTANCE);
    }

    /**
     * A portal that
     * <p> - calculates to the exit to be 6 blocks up from the entity position</p>
     * <p> - tries to find an existing nether-portal at the exit location</p>
     * <p> - generates an end platform at the exit location if no portal was found</p>
     */
    public static class ExanplePortalLogic implements PortalLogic.PortalExitCalculator, PortalLogic.PortalFinder, PortalLogic.PortalGenerator {

        @Override
        public Optional<ServerLocation> calculatePortalExit(final ServerWorld from, final Vector3i fromPos, final Entity entity) {
            return Optional.of(entity.serverLocation().add(Vector3i.UP.mul(6)));
        }

        @Override
        public Optional<Portal> findPortal(final ServerLocation location, final int searchRange) {
            return Optional.empty();
//            return PortalLogic.factory().netherPortalFinder().findPortal(location, 16);
        }

        @Override
        public Optional<Portal> generatePortal(final ServerLocation location, final Axis axis) {
            return PortalLogic.factory().endPlatformGenerator().generatePortal(location, axis);
        }
    }

    public static final class PortalTestListener {

        @Listener
        private void onChangeWorldPre(final ChangeEntityWorldEvent.Pre event) {
            if (PortalTestListener.filter(event)) {
                event.setDestinationWorld(event.originalWorld());
            }
        }

        @Listener
        private void onChangeWorldReposition(final ChangeEntityWorldEvent.Reposition event) {
            if (PortalTestListener.filter(event)) {
                event.setDestinationPosition(event.destinationPosition().add(0, 2, 0));
            }
        }

        @Listener
        private void onPortalEnter(final InvokePortalEvent.Enter event) {
            System.out.println("InvokePortalEvent.Enter");
        }

        @Listener
        private void onPortalPrepare(final InvokePortalEvent.Prepare event) {
            System.out.println("InvokePortalEvent.Prepare");
            event.setPortalLogic(PortalLogic.builder().addPortal(new ExanplePortalLogic()).build());
        }

        @Listener
        private void onPortalExecute(final InvokePortalEvent.Execute event) {
            event.setCancelled(true);
            System.out.println("InvokePortalEvent.Execute");
            if (event.entity() instanceof ServerPlayer p) {
                p.showTitle(Title.title(Component.text("No nether for you!"), Component.text(":(")));
            }
        }

        private static boolean filter(final Event event) {
            return event.cause().context().get(EventContextKeys.MOVEMENT_TYPE).filter(x -> x == MovementTypes.PORTAL.get()).isPresent();
        }

    }
}
