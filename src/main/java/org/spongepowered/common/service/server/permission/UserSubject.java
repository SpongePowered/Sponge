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
import net.minecraft.server.players.ServerOpListEntry;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.service.permission.PermissionService;
import org.spongepowered.api.service.permission.SubjectCollection;
import org.spongepowered.api.service.permission.SubjectReference;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.common.SpongeCommon;

import java.util.Optional;

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
            public SubjectReference parent() {
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
                    opLevel = ((OpLevelCollection.OpLevelSubject) parent).opLevel();
                }
                if (opLevel > 0) {
                    // TODO: Should bypassesPlayerLimit be true or false?
                    SpongePermissionService.getOps().add(new ServerOpListEntry(player, opLevel, false));
                } else {
                    SpongePermissionService.getOps().remove(player);
                }
            }
        };
        this.collection = users;
    }

    @Override
    public String identifier() {
        return this.player.getId().toString();
    }

    @Override
    public Optional<String> friendlyIdentifier() {
        return Optional.of(this.player.getName());
    }

    @Override
    public Optional<?> associatedObject() {
        if (!Sponge.isServerAvailable()) {
            return Optional.empty();
        }

        return Sponge.server().player(this.player.getId());
    }

    int getOpLevel() {
        Preconditions.checkState(Sponge.isServerAvailable(), "Server is not available!");

        // Query op level from server ops list based on player's game profile
        final ServerOpListEntry entry = SpongePermissionService.getOps().get(this.player);
        if (entry == null) {
            // Take care of singleplayer commands -- unless an op level is specified, this player follows global rules
            return SpongeCommon.server().getPlayerList().isOp(this.player) ? SpongeCommon.server().getOperatorUserPermissionLevel() : 0;
        } else {
            return entry.getLevel();
        }
    }

    @Override
    public SubjectCollection containingCollection() {
        return this.collection;
    }

    @Override
    public MemorySubjectData subjectData() {
        return this.data;
    }

    @Override
    public PermissionService service() {
        return this.collection.getService();
    }

    @Override
    public Tristate permissionValue(final String permission, final Cause cause) {
        Tristate ret = super.permissionValue(permission, cause);
        if (ret == Tristate.UNDEFINED) {
            ret = this.dataPermissionValue(this.collection.defaults().subjectData(), permission);
        }
        if (ret == Tristate.UNDEFINED) {
            ret = this.dataPermissionValue(this.collection.getService().defaults().subjectData(), permission);
        }
        if (ret == Tristate.UNDEFINED && this.getOpLevel() >= SpongePermissionService.getServerOpLevel()) {
            ret = Tristate.TRUE;
        }
        return ret;
    }

    @Override
    public Optional<String> option(final String option, final Cause cause) {
        Optional<String> ret = super.option(option, cause);
        if (!ret.isPresent()) {
            ret = this.dataOptionValue(this.collection.defaults().subjectData(), option);
        }
        if (!ret.isPresent()) {
            ret = this.dataOptionValue(this.collection.getService().defaults().subjectData(), option);
        }
        return ret;
    }
}
