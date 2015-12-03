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
package org.spongepowered.common.entity.player.tab;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.world.GameType;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.entity.context.store.HumanoidContextStore;
import org.spongepowered.common.entity.context.store.PlayerContextStore;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.interfaces.entity.IMixinEntityContext;
import org.spongepowered.common.interfaces.network.play.server.IMixinSPacketPlayerListItem;
import org.spongepowered.common.util.TextureUtil;

import java.util.Optional;

import javax.annotation.Nullable;

public final class TabListEntryAdapter {

    public static SPacketPlayerListItem addPacket(EntityPlayerMP source, EntityPlayerMP viewer) {
        GameProfile profile = source.getGameProfile();
        HumanoidContextStore store = (HumanoidContextStore) ((IMixinEntityContext) source).getContextStore();
        String name = store.getName((ContextViewer) viewer);
        Optional<ProfileProperty> skin = store.getTextures((ContextViewer) viewer);

        if (!name.equals(source.getName()) || !skin.equals(((PlayerContextStore) store).getRealTextures())) {
            profile = new GameProfile(source.getUniqueID(), name);

            if (skin.isPresent()) {
                TextureUtil.toPropertyMap(profile.getProperties(), skin.get());
            }
        }

        SPacketPlayerListItem packet = new SPacketPlayerListItem();
        packet.action = SPacketPlayerListItem.Action.ADD_PLAYER;
        ((IMixinSPacketPlayerListItem) packet).addEntry(
                profile,
                source.ping,
                source.interactionManager.getGameType(),
                null // This field is ignored when ADD_PLAYER is the action
        );

        return packet;
    }

    public static SPacketPlayerListItem human(EntityHuman human, @Nullable EntityPlayerMP viewer, SPacketPlayerListItem.Action action) {
        SPacketPlayerListItem packet = new SPacketPlayerListItem();
        packet.action = action;
        if (viewer == null) {
            ((IMixinSPacketPlayerListItem) packet).addEntry(
                    human.getProfile(),
                    0,
                    GameType.NOT_SET,
                    null
            );
        } else {
            GameProfile profile = human.getProfile();

            HumanoidContextStore store = (HumanoidContextStore) ((IMixinEntityContext) human).getContextStore();
            String name = store.getName((ContextViewer) viewer);
            Optional<ProfileProperty> skin = store.getTextures((ContextViewer) viewer);

            if (!name.equals(human.getName()) || !skin.equals(((PlayerContextStore) store).getRealTextures())) {
                profile = new GameProfile(human.getUniqueID(), name);

                if (skin.isPresent()) {
                    TextureUtil.toPropertyMap(profile.getProperties(), skin.get());
                }
            }

            ((IMixinSPacketPlayerListItem) packet).addEntry(
                    profile,
                    0,
                    GameType.NOT_SET,
                    null // This field is ignored when ADD_PLAYER is the action
            );
        }

        return packet;
    }

}
