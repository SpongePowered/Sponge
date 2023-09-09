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
package org.spongepowered.forge.mixin.core.minecraftforge.event;

import net.minecraft.network.chat.Component;
import net.minecraftforge.event.ServerChatEvent;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.forge.launch.bridge.event.ForgeEventBridge_Forge;

@Mixin(value = ServerChatEvent.class, remap = false)
public abstract class ServerChatEventMixin_Forge implements ForgeEventBridge_Forge {

    // @formatter:off
    @Shadow public abstract void shadow$setMessage(Component e);
    @Shadow public abstract Component shadow$getMessage();
    @Shadow public abstract net.minecraft.server.level.ServerPlayer shadow$getPlayer();
    // @formatter:on

    @Override
    public void bridge$syncFrom(final Event event) {
        final PlayerChatEvent.Submit spongeEvent = (PlayerChatEvent.Submit) event;
        ((net.minecraftforge.eventbus.api.Event) (Object) this).setCanceled(spongeEvent.isCancelled());
        this.shadow$setMessage(SpongeAdventure.asVanilla(spongeEvent.message()));
    }

    @Override
    public void bridge$syncTo(final Event event) {
        final PlayerChatEvent.Submit spongeEvent = (PlayerChatEvent.Submit) event;
        spongeEvent.setCancelled(((net.minecraftforge.eventbus.api.Event) (Object) this).isCanceled());
        spongeEvent.setMessage(SpongeAdventure.asAdventure(this.shadow$getMessage()));
    }

    @Override
    public @Nullable Event bridge$createSpongeEvent() {
        // TODO SF 1.19.4
        /* final Audience audience = (Audience) this.shadow$getPlayer().server;
        final PlayerChatFormatter chatFormatter = ((ServerPlayer) this.shadow$getPlayer()).chatFormatter();
        final net.kyori.adventure.text.Component originalMessage = SpongeAdventure.asAdventure(this.shadow$getMessage());
        return SpongeEventFactory.createPlayerChatEventSubmit(
                PhaseTracker.getCauseStackManager().currentCause(),
                originalMessage,
                originalMessage,
                audience,
                Optional.of(audience),
                chatFormatter,
                Optional.of(chatFormatter)
        ); */
        return null;
    }
}
