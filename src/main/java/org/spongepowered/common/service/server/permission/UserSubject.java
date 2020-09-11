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
package org.spongepowered.common.service.server.permission;

import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.OpEntry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.permission.MemorySubjectData;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.SpongeCommon;

import java.util.Optional;
import java.util.Set;

/**
 * An implementation of vanilla minecraft's 4 op groups.
 */
public class UserSubject extends SpongeSubject {
    private final GameProfile player;
    private final MemorySubjectData data;
    private final UserCollection collection;

    public UserSubject(final GameProfile player, final UserCollection users) {
        this.player = Preconditions.checkNotNull(player);
        this.data = new SingleParentMemorySubjectData(this) {
            @Override
            public SubjectReference getParent() {
                return users.getService().getGroupForOpLevel(UserSubject.this.getOpLevel()).asSubjectReference();
            }

            @Override
            public void setParent(final SubjectReference parent) {
                final int opLevel;
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
        return Optional.of(this.player.getName());
    }

    int getOpLevel() {
        Preconditions.checkState(Sponge.isServerAvailable(), "Server is not available!");

        // Query op level from server ops list based on player's game profile
        final OpEntry entry = SpongePermissionService.getOps().getEntry(this.player);
        if (entry == null) {
            // Take care of singleplayer commands -- unless an op level is specified, this player follows global rules
            return SpongeCommon.getServer().getPlayerList().canSendCommands(this.player) ? SpongeCommon.getServer().getOpPermissionLevel() : 0;
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
        return this.collection.getService();
    }

    @Override
    public Tristate getPermissionValue(final Set<Context> contexts, final String permission) {
        Tristate ret = super.getPermissionValue(contexts, permission);
        if (ret == Tristate.UNDEFINED) {
            ret = this.getDataPermissionValue(this.collection.getDefaults().getSubjectData(), permission);
        }
        if (ret == Tristate.UNDEFINED) {
            ret = this.getDataPermissionValue(this.collection.getService().getDefaults().getSubjectData(), permission);
        }
        if (ret == Tristate.UNDEFINED && this.getOpLevel() >= SpongePermissionService.getServerOpLevel()) {
            ret = Tristate.TRUE;
        }
        return ret;
    }

    @Override
    public Optional<String> getOption(final Set<Context> contexts, final String option) {
        Optional<String> ret = super.getOption(contexts, option);
        if (!ret.isPresent()) {
            ret = this.getDataOptionValue(this.collection.getDefaults().getSubjectData(), option);
        }
        if (!ret.isPresent()) {
            ret = this.getDataOptionValue(this.collection.getService().getDefaults().getSubjectData(), option);
        }
        return ret;
    }
}
