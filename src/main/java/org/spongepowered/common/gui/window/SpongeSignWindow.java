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
package org.spongepowered.common.gui.window;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Lists;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;
import org.spongepowered.api.block.tileentity.TileEntity;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableSignData;
import org.spongepowered.api.gui.window.SignWindow;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeSignData;
import org.spongepowered.common.text.SpongeTexts;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class SpongeSignWindow extends AbstractSpongeWindow implements SignWindow {

    private final List<Consumer<ImmutableSignData>> consumers = Lists.newArrayList();
    private Location<World> boundLocation;

    @Override
    protected boolean show() {
        if (this.boundLocation == null) {
            TileEntitySign fake = new TileEntitySign();
            fake.setPos(VIRTUAL_POS);
            this.player.openEditSign(fake);
            return true;
        } else {
            Optional<TileEntity> te = this.boundLocation.getTileEntity();
            if (te.isPresent() && te.get() instanceof TileEntitySign) {
                this.player.openEditSign((TileEntitySign) te.get());
                return true;
            }
            return false;
        }
    }

    @Override
    public Optional<Location<World>> getLocation() {
        return Optional.ofNullable(this.boundLocation);
    }

    @Override
    public void setLocation(Location<World> location) {
        checkNotOpen();
        this.boundLocation = location;
    }

    @Override
    public void onFinishVirtualEdit(Consumer<ImmutableSignData> consumer) {
        this.consumers.add(checkNotNull(consumer, "consumer"));
    }

    @Override
    public boolean canDetectClientClose() {
        return true;
    }

    @Override
    public void onClientClose(Packet<INetHandlerPlayServer> packet) {
        if (((C12PacketUpdateSign) packet).getPosition().equals(SpongeSignWindow.VIRTUAL_POS)) {
            IChatComponent[] lines = ((C12PacketUpdateSign) packet).getLines();
            List<Text> text = Lists.newArrayList();
            for (IChatComponent line : lines) {
                text.add(line == null ? Text.EMPTY : SpongeTexts.toText(line));
            }
            ImmutableSpongeSignData data = new ImmutableSpongeSignData(text);
            for (Consumer<ImmutableSignData> c : this.consumers) {
                c.accept(data);
            }
        } else {
            // Handle real TileEntity update
            packet.processPacket(this.player.playerNetServerHandler);
        }
        super.onClientClose(packet);
    }

    public static class Builder extends SpongeWindowBuilder<SignWindow, SignWindow.Builder> implements SignWindow.Builder {

        @Override
        public SignWindow build() {
            return new SpongeSignWindow();
        }
    }

}
