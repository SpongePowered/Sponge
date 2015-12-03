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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.world.WorldServer;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.entity.player.tab.TabListEntryAdapter;
import org.spongepowered.common.interfaces.entity.IMixinEntity;
import org.spongepowered.common.interfaces.entity.IMixinEntityContext;
import org.spongepowered.common.text.SpongeTexts;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

/**
 * A context store for an {@link Entity}.
 */
public class EntityContextStore {

    protected net.minecraft.entity.Entity entity;
    @Nullable protected Text displayName;
    protected final Map<ContextViewer, Text> displayNames = new WeakHashMap<>();
    protected final Set<UUID> hiddenEntities = Sets.newHashSet();

    public EntityContextStore(net.minecraft.entity.Entity entity) {
        this.entity = checkNotNull(entity, "entity");
    }

    public Text getDisplayName() {
        return Objects.firstNonNull(this.displayName, SpongeTexts.toText(this.entity.getDisplayName()));
    }

    public void setDisplayName(@Nullable Text displayName) {
        this.displayName = displayName;

        if (this.entity instanceof EntityPlayer) {
            return;
        }

        ((IMixinEntityContext) this.entity).pushSkipCustomNameSet(true);

        if (this.displayName == null) {
            this.entity.setCustomNameTag("");
        } else {
            this.entity.setCustomNameTag(SpongeTexts.toLegacy(this.displayName));
        }

        ((IMixinEntityContext) this.entity).pushSkipCustomNameSet(false);
    }

    @SuppressWarnings("ConstantConditions")
    public Text getDisplayName(@Nullable ContextViewer viewer) {
        return this.getDisplayName(viewer, true);
    }

    @Nullable
    public Text getDisplayName(@Nullable ContextViewer viewer, boolean defaulted) {
        if (viewer == null) {
            return this.getDisplayName();
        } else {
            @Nullable Text displayName = this.displayNames.get(viewer);
            if (displayName != null) {
                return displayName;
            } else {
                return defaulted ? this.getDisplayName() : null;
            }
        }
    }

    public void setDisplayName(@Nullable ContextViewer viewer, @Nullable Text displayName) {
        if (viewer == null) {
            this.displayName = displayName;
        } else {
            this.displayNames.put(viewer, displayName);
        }
    }

    public void clearDisplayNames() {
        this.displayNames.clear();
    }

    // TODO: check this.hiddenEntities
    public boolean canSee(Entity entity) {
        checkNotNull(entity, "entity");
        final Optional<Boolean> optional = entity.get(Keys.INVISIBLE);
        return (!optional.isPresent() || !optional.get()) && !((IMixinEntity) entity).isVanished();
    }

    public void hide(Entity entity) {
        checkNotNull(entity, "entity");
        if (this.entity.getUniqueID().equals(entity.getUniqueId()) || !this.canSee(entity)) {
            return;
        }

        this.hiddenEntities.add(entity.getUniqueId());

        if (this.entity instanceof EntityPlayerMP) {
            EntityTrackerEntry entry = ((WorldServer) this.entity.world).getEntityTracker().trackedEntityHashTable.lookup(((net.minecraft.entity.Entity) entity).getEntityId());
            if (entry != null) {
                entry.removeTrackedPlayerSymmetric((EntityPlayerMP) this.entity);
            }

            if (entity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) this.entity).connection.sendPacket(new SPacketPlayerListItem(SPacketPlayerListItem.Action.REMOVE_PLAYER, (EntityPlayerMP) entity));
            }
        }
    }

    public void show(Entity entity) {
        checkNotNull(entity, "entity");
        if (this.entity.getUniqueID().equals(entity.getUniqueId()) || this.canSee(entity)) {
            return;
        }

        this.hiddenEntities.remove(entity.getUniqueId());

        if (this.entity instanceof EntityPlayerMP) {
            if (entity instanceof EntityPlayerMP) {
                ((EntityPlayerMP) this.entity).connection.sendPacket(TabListEntryAdapter.addPacket((EntityPlayerMP) this.entity, (EntityPlayerMP) entity));
            }

            EntityTrackerEntry entry = ((WorldServer) this.entity.world).getEntityTracker().trackedEntityHashTable.lookup(((net.minecraft.entity.Entity) entity).getEntityId());
            if (entry != null) {
                entry.updatePlayerEntity((EntityPlayerMP) this.entity);
            }
        }
    }

}
