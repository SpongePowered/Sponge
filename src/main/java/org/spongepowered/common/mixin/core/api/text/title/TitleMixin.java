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
package org.spongepowered.common.mixin.core.api.text.title;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.title.Title;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.api.text.TextBridge;
import org.spongepowered.common.bridge.text.TitleBridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.STitlePacket;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(value = Title.class, remap = false)
public abstract class TitleMixin implements TitleBridge {

    @Shadow @Final Optional<Text> title;
    @Shadow @Final Optional<Text> subtitle;
    @Shadow @Final Optional<Text> actionBar;
    @Shadow @Final Optional<Integer> fadeIn;
    @Shadow @Final Optional<Integer> stay;
    @Shadow @Final Optional<Integer> fadeOut;
    @Shadow @Final boolean clear;
    @Shadow @Final boolean reset;

    private List<STitlePacket> packets;

    @Override
    public void bridge$send(ServerPlayerEntity player) {
        for (STitlePacket packet : this.getPackets()) {
            player.field_71135_a.func_147359_a(packet);
        }
    }

    private List<STitlePacket> getPackets() {
        if (this.packets == null) {
            this.packets = new ArrayList<>();
            if (this.clear) {
                this.packets.add(new STitlePacket(STitlePacket.Type.RESET, null)); // SPacketTitle.Type.RESET is actually CLEAR
            }
            if (this.reset) {
                this.packets.add(new STitlePacket(STitlePacket.Type.RESET, null));
            }
            if (this.fadeIn.isPresent() || this.stay.isPresent() || this.fadeOut.isPresent()) {
                this.packets.add(new STitlePacket(this.fadeIn.orElse(20), this.stay.orElse(60), this.fadeOut.orElse(20)));
            }
            if (this.subtitle.isPresent()) {
                this.packets.add(new STitlePacket(STitlePacket.Type.SUBTITLE, ((TextBridge) this.subtitle.get()).bridge$toComponent()));
            }
            if (this.actionBar.isPresent()) {
                this.packets.add(new STitlePacket(STitlePacket.Type.ACTIONBAR, ((TextBridge) this.actionBar.get()).bridge$toComponent()));
            }
            if (this.title.isPresent()) {
                this.packets.add(new STitlePacket(STitlePacket.Type.TITLE, ((TextBridge) this.title.get()).bridge$toComponent()));
            }
        }

        return this.packets;
    }
}
