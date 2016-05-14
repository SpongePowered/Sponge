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
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.S2EPacketCloseWindow;
import net.minecraft.util.BlockPos;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.gui.window.Window;
import org.spongepowered.common.interfaces.entity.player.IMixinEntityPlayerMP;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public abstract class AbstractSpongeWindow implements Window {

    protected static final BlockPos VIRTUAL_POS = new BlockPos(-1, -1, -1);

    protected final Set<EntityPlayerMP> players = Sets.newHashSet();

    @Override
    @SuppressWarnings("unchecked")
    public Set<Player> getPlayersShowing() {
        return Collections.unmodifiableSet((Set<Player>) (Object) this.players);
    }

    @Override
    public boolean show(Player player) {
        Optional<Window> active = checkNotNull(player, "player").getActiveWindow();
        if ((active.isPresent() && (active.get() instanceof AbstractSpongeWindow && ((AbstractSpongeWindow) active.get()).canDetectClientClose()))
                || !this.show((EntityPlayerMP) player)) {
            return false;
        }
        this.players.add((EntityPlayerMP) player);
        ((IMixinEntityPlayerMP) player).setWindow(this);
        return true;
    }

    protected abstract boolean show(EntityPlayerMP player);

    @Override
    public void close(Player player) {
        this.sendClose((EntityPlayerMP) player);
        this.onClosed((EntityPlayerMP) player);
    }

    protected void onClosed(EntityPlayerMP player) {
        this.players.remove(player);
        ((IMixinEntityPlayerMP) player).setWindow(null);
    }

    protected void sendClose(EntityPlayerMP player) {
        // Calls displayGuiScreen(null) on the client
        // May cause problems with containers, be sure to override if it will
        player.playerNetServerHandler.sendPacket(new S2EPacketCloseWindow(-1));
    }

    protected void checkNotOpen() {
        checkState(this.players.isEmpty(), "Window is currently shown to one or more players");
    }

    public boolean canDetectClientClose() {
        return false;
    }

}
