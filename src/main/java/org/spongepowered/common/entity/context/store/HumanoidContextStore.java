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
package org.spongepowered.common.entity.context.store;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketTeams;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeOptionalValue;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.entity.player.tab.TabListEntryAdapter;
import org.spongepowered.common.util.TextureUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

/**
 * A context store for a {@link Humanoid}.
 */
public class HumanoidContextStore extends EntityContextStore {

    protected final Map<ContextViewer, String> fakeNames = new WeakHashMap<>();
    protected final Map<ContextViewer, ProfileProperty> fakeSkins = new WeakHashMap<>();
    protected final Map<ContextViewer, UUID> textureIds = new WeakHashMap<>();
    @Nullable protected ProfileProperty fakeTexture;
    @Nullable protected UUID fakeTextureId;
    protected String fakeName;

    public HumanoidContextStore(Entity entity) {
        super(entity);
    }

    // --------------
    // ---- Root ----
    // --------------

    public boolean hasSomethingFake(@Nullable ContextViewer viewer) {
        return this.hasFakeName(viewer) || this.hasTextures(viewer);
    }

    // -------------------
    // ---- Fake Name ----
    // -------------------

    public boolean hasFakeName(@Nullable ContextViewer viewer) {
        return viewer != null && this.fakeNames.containsKey(viewer);
    }

    public String getFakeName() {
        return this.fakeName;
    }

    public void setFakeName(@Nullable String name) {
        this.setFakeNameAndTextures(null, name, null, true, false);
    }

    @Nullable
    public String getFakeName(ContextViewer viewer) {
        return this.fakeNames.get(viewer);
    }

    public String getName(@Nullable ContextViewer viewer) {
        @Nullable String name = viewer == null ? this.entity.getName() : this.fakeNames.get(viewer);
        if (name == null) {
            name = this.entity.getName();
        }

        return name;
    }

    public void setFakeName(ContextViewer viewer, @Nullable String name) {
        this.setFakeNameAndTextures(viewer, name, null, true, false);
    }

    public void clearFakeNames() {
        Set<ContextViewer> viewers = Sets.newHashSet(this.fakeNames.keySet());

        viewers.forEach(this::removeFromClient);
        this.fakeNames.clear();
        viewers.forEach(this::addToClient);
    }

    // ------------------
    // ---- Textures ----
    // ------------------

    public boolean hasTextures(@Nullable ContextViewer viewer) {
        return viewer != null && this.fakeSkins.containsKey(viewer);
    }

    public Optional<ProfileProperty> getTextures() {
        if (this instanceof PlayerContextStore) {
            return this.fakeTexture != null ? Optional.of(this.fakeTexture) : ((PlayerContextStore) this).getRealTextures();
        } else {
            return Optional.ofNullable(this.fakeTexture);
        }
    }

    public Optional<ProfileProperty> getTextures(@Nullable ContextViewer viewer) {
        if (viewer == null) {
            return this.getTextures();
        } else {
            @Nullable ProfileProperty textures = this.fakeSkins.get(viewer);
            return textures != null ? Optional.of(textures) : this.getTextures();
        }
    }

    public void clearTextures() {
        Set<ContextViewer> viewers = Sets.newHashSet(this.fakeSkins.keySet());

        viewers.forEach(this::removeFromClient);
        this.fakeSkins.clear();
        viewers.forEach(this::addToClient);
    }

    @Nullable
    public ProfileProperty getTextureProperty(ContextViewer viewer) {
        return this.fakeSkins.get(viewer);
    }

    public void setTextureProperty(ContextViewer viewer, @Nullable ProfileProperty property) {
        this.setFakeNameAndTextures(viewer, null, property, false, true);
    }

    @Nullable
    public UUID getTextureId(ContextViewer viewer) {
        return this.textureIds.get(viewer);
    }

    public void setTextureId(ContextViewer viewer, @Nullable UUID uniqueId) {
        this.setTextureIdInternal(viewer, uniqueId);
    }

    public void setTexturePropertyAndId(ContextViewer viewer, @Nullable ProfileProperty property, @Nullable UUID uniqueId) {
        this.setTextureProperty(viewer, property);
        this.setTextureId(viewer, uniqueId);
    }

    @Nullable
    public ProfileProperty getTextureProperty() {
        return this.fakeTexture;
    }

    public void setTextureProperty(@Nullable ProfileProperty property) {
        this.fakeTexture = property;
    }

    @Nullable
    public UUID getTextureId() {
        return this.fakeTextureId;
    }

    public void setTextureId(@Nullable UUID uniqueId) {
        this.setTextureIdInternal(null, uniqueId);
    }

    private void setTextureIdInternal(@Nullable ContextViewer viewer, @Nullable UUID uniqueId) {
        if (uniqueId == null) {
            return;
        }

        TextureUtil.uniqueIdToProfileProperty(uniqueId, profile -> {
            if (profile != null) {
                Collection<ProfileProperty> properties = profile.getPropertyMap().get(ProfileProperty.TEXTURES);
                if (!properties.isEmpty()) {
                    if (viewer != null) {
                        HumanoidContextStore.this.fakeSkins.put(viewer, properties.iterator().next());
                    } else {
                        HumanoidContextStore.this.setTextureProperty(properties.iterator().next());
                    }
                }
            }
        });
    }

    public void setTexturePropertyAndId(@Nullable ProfileProperty property, @Nullable UUID uniqueId) {
        if (property != null) {
            this.setTextureProperty(property);
        } else if (uniqueId != null) {
            this.setTextureId(uniqueId);
        }

        if (this.entity instanceof EntityHuman) {
            ((EntityHuman) this.entity).respawnOnClient();
        }
    }

    // ---------------
    // ---- Multi ----
    // ---------------

    public DataTransactionResult setFakeNameAndTextures(ContextViewer viewer, @Nullable String name, @Nullable ProfileProperty property) {
        boolean nameChanged = !Objects.equal(this.fakeSkins.get(viewer), property);
        boolean texturesChanged = !Objects.equal(this.fakeSkins.get(viewer), property);

        this.setFakeNameAndTextures(viewer, name, property, nameChanged, texturesChanged);

        final DataTransactionResult.Builder builder = DataTransactionResult.builder();

        if (nameChanged) {
            builder.replace(new ImmutableSpongeOptionalValue<>(Keys.FAKE_NAME, Optional.ofNullable(name)));
        }

        if (texturesChanged) {
            builder.replace(new ImmutableSpongeOptionalValue<>(Keys.HUMANOID_TEXTURES_PROPERTY, Optional.ofNullable(property)));
        }

        if (nameChanged || texturesChanged) {
            builder.result(DataTransactionResult.Type.SUCCESS);
        } else {
            builder.result(DataTransactionResult.Type.FAILURE);
        }

        return builder.build();
    }

    public void clearFakeNamesAndTextures() {
        Set<ContextViewer> viewers = Sets.newHashSet(this.fakeSkins.keySet());
        viewers.addAll(this.fakeNames.keySet());

        viewers.forEach(this::removeFromClient);
        this.fakeSkins.clear();
        this.fakeNames.clear();
        viewers.forEach(this::addToClient);
    }

    // ----

    private void setFakeNameAndTextures(@Nullable ContextViewer viewer, @Nullable String name, @Nullable ProfileProperty property, boolean nameChanged, boolean texturesChanged) {
        nameChanged = nameChanged && !Objects.equal(this.fakeNames.get(viewer), name);
        texturesChanged = texturesChanged && !Objects.equal(this.fakeSkins.get(viewer), property);

        if (nameChanged || texturesChanged) {
            if (name != null) {
                checkState(name.length() < 16, "fake player names are limited to 16 characters in length");
            }

            final Set<ContextViewer> viewers;
            if (viewer == null) {
                viewers = Sets.newHashSet(this.fakeSkins.keySet());
                viewers.addAll(this.fakeNames.keySet());
            } else {
                viewers = Collections.singleton(viewer);
            }

            viewers.forEach(this::removeFromClient);

            if (name == null) {
                this.fakeNames.remove(viewer);
            } else {
                this.fakeNames.put(viewer, name);
            }

            if (property == null) {
                this.fakeSkins.remove(viewer);
            } else {
                this.fakeSkins.put(viewer, property);
            }

            viewers.forEach(this::addToClient);
        }
    }

    private void removeFromClient(ContextViewer viewer) {
        if (!(viewer instanceof Player)) {
            return;
        }

        final EntityPlayerMP player = (EntityPlayerMP) viewer;
        if (player.connection == null) {
            return;
        }

        if (((Player) player).canSee((org.spongepowered.api.entity.Entity) this.entity)) {
            EntityTrackerEntry entry = ((WorldServer) this.entity.world).getEntityTracker().trackedEntityHashTable.lookup(this.entity.getEntityId());
            if (entry != null) {
                entry.removeTrackedPlayerSymmetric(player);
            }

            if (this.entity instanceof EntityPlayerMP) {
                player.connection.sendPacket(new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, (EntityPlayerMP) this.entity));
            } else {
                player.connection.sendPacket(TabListEntryAdapter.human((EntityHuman) this.entity, player, SPacketPlayerListItem.Action.REMOVE_PLAYER));
            }
        }

        @Nullable ScorePlayerTeam team = ((Scoreboard) ((Player) player).getScoreboard()).getPlayersTeam(this.entity.getName());
        if (team != null) {
            player.connection.sendPacket(new SPacketTeams(team, Collections.singletonList(this.entity.getName()), 4));
        }
    }

    private void addToClient(ContextViewer viewer) {
        if (!(viewer instanceof Player)) {
            return;
        }

        final EntityPlayerMP player = (EntityPlayerMP) viewer;
        if (player.connection == null) {
            return;
        }

        @Nullable ScorePlayerTeam team = ((Scoreboard) ((Player) player).getScoreboard()).getPlayersTeam(this.entity.getName());
        if (team != null) {
            player.connection.sendPacket(new SPacketTeams(team, Collections.singletonList(this.entity.getName()), 3));
        }

        if (((Player) player).canSee((org.spongepowered.api.entity.Entity) this.entity)) {
            player.connection.sendPacket(TabListEntryAdapter.addPacket((EntityPlayerMP) this.entity, player));

            EntityTrackerEntry entry = ((WorldServer) this.entity.world).getEntityTracker().trackedEntityHashTable.lookup(this.entity.getEntityId());
            if (entry != null) {
                entry.updatePlayerEntity(player);
            }
        }
    }

}
