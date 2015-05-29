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
package org.spongepowered.common.data.component.entity;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.spongepowered.api.data.DataQuery.of;

import com.google.common.collect.Sets;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.InvisibilityComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.common.data.component.AbstractBooleanComponent;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class SpongeInvisibilityComponent extends AbstractBooleanComponent<InvisibilityComponent> implements InvisibilityComponent {

    public static final DataQuery INVISIBLE_PLAYERS = of("InvisiblePlayers");
    private final Set<UUID> invisibleTo = Sets.newHashSet();

    public SpongeInvisibilityComponent() {
        super(InvisibilityComponent.class, false);
    }

    public SpongeInvisibilityComponent(Collection<UUID> uuids) {
        super(InvisibilityComponent.class, false);
        this.invisibleTo.addAll(uuids);
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        return this.invisibleTo.contains(player.getUniqueId());
    }

    @Override
    public InvisibilityComponent setInvisibleTo(Player player, boolean invisible) {
        if (invisible) {
            this.invisibleTo.add(checkNotNull(player).getUniqueId());
        } else {
            this.invisibleTo.remove(checkNotNull(player).getUniqueId());
        }
        return this;
    }

    @Override
    public InvisibilityComponent copy() {
        return new SpongeInvisibilityComponent(this.invisibleTo);
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer().set(Tokens.IS_INVISIBLE.getQuery(), this.getValue());
    }
}
