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
package org.spongepowered.common.service.permission;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.service.permission.base.SingleParentMemorySubjectData;
import org.spongepowered.common.service.permission.base.SpongeSubject;

import java.util.Optional;
import java.util.Set;
import net.minecraft.server.management.OpEntry;

/**
 * An implementation of vanilla minecraft's 4 op groups.
 */
public class UserSubject extends SpongeSubject {
    private final GameProfile player;
    private final MemorySubjectData data;
    private final UserCollection collection;

    public UserSubject(final GameProfile player, final UserCollection users) {
        this.player = player;
        this.data = new SingleParentMemorySubjectData(users.getService()) {
            @Override
            public SubjectReference getParent() {
                return users.getService().getGroupForOpLevel(getOpLevel()).asSubjectReference();
            }

            @Override
            public void setParent(SubjectReference parent) {
                int opLevel;
                if (parent == null) {
                    opLevel = 0;
                } else {
                    if (!(parent.resolve().join() instanceof OpLevelCollection.OpLevelSubject)) {
                        return;
                    }
                    opLevel = ((OpLevelCollection.OpLevelSubject) parent).getOpLevel();
                }
                if (opLevel > 0) {
                    // TODO: Should bypassesPlayerLimit be true or false?
                    SpongePermissionService.getOps().addEntry(new OpEntry(player, opLevel, false));
                } else {
                    SpongePermissionService.getOps().removeEntry(player);
                }
            }
        };
        this.collection = users;
    }

    @Override
    public String getIdentifier() {
        return this.player.getId().toString();
    }

    @Override
    public Optional<String> getFriendlyIdentifier() {
        return Optional.of(player.getName());
    }

    @Override
    public Optional<CommandSource> getCommandSource() {
        if (Sponge.isServerAvailable()) {
            return Optional.ofNullable((CommandSource) SpongeImpl.getServer().getPlayerList().getPlayerByUUID(this.player.getId()));
        }
        return Optional.empty();
    }

    int getOpLevel() {
        Preconditions.checkState(Sponge.isServerAvailable(), "Server is not available!");

        // Query op level from server ops list based on player's game profile
        OpEntry entry = SpongePermissionService.getOps().getEntry(this.player);
        if (entry == null) {
            // Take care of singleplayer commands -- unless an op level is specified, this player follows global rules
            return SpongeImpl.getServer().getPlayerList().canSendCommands(this.player) ? SpongeImpl.getServer().getOpPermissionLevel() : 0;
        } else {
            return entry.getPermissionLevel();
        }
    }

    @Override
    public SubjectCollection getContainingCollection() {
        return this.collection;
    }

    @Override
    public MemorySubjectData getSubjectData() {
        return this.data;
    }

    @Override
    public PermissionService getService() {
        return collection.getService();
    }

    @Override
    public Tristate getPermissionValue(Set<Context> contexts, String permission) {
        Tristate ret = super.getPermissionValue(contexts, permission);
        if (ret == Tristate.UNDEFINED) {
            ret = getDataPermissionValue(this.collection.getDefaults().getSubjectData(), permission);
        }
        if (ret == Tristate.UNDEFINED) {
            ret = getDataPermissionValue(this.collection.getService().getDefaults().getSubjectData(), permission);
        }
        if (ret == Tristate.UNDEFINED && getOpLevel() >= SpongePermissionService.getServerOpLevel()) {
            ret = Tristate.TRUE;
        }
        return ret;
    }

    @Override
    public Optional<String> getOption(Set<Context> contexts, String option) {
        Optional<String> ret = super.getOption(contexts, option);
        if (!ret.isPresent()) {
            ret = getDataOptionValue(this.collection.getDefaults().getSubjectData(), option);
        }
        if (!ret.isPresent()) {
            ret = getDataOptionValue(this.collection.getService().getDefaults().getSubjectData(), option);
        }
        return ret;
    }
}
