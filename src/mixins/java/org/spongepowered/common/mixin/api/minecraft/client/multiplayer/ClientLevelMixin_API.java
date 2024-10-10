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
package org.spongepowered.common.mixin.api.minecraft.client.multiplayer;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.api.Client;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.client.ClientPlayer;
import org.spongepowered.api.world.client.ClientLocation;
import org.spongepowered.api.world.client.ClientWorld;
import org.spongepowered.api.world.storage.ChunkLayout;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.api.minecraft.world.level.LevelMixin_API;
import org.spongepowered.common.world.storage.SpongeChunkLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin_API extends LevelMixin_API<ClientWorld, ClientLocation> implements org.spongepowered.api.world.client.ClientWorld {

    // @formatter:off
    @Shadow @Final List<AbstractClientPlayer> players;

    @Shadow protected abstract LevelEntityGetter<net.minecraft.world.entity.Entity> shadow$getEntities();
    // @formatter:on

    @Override
    public Client engine() {
        return (Client) Minecraft.getInstance();
    }

    @Override
    public boolean isLoaded() {
        return Minecraft.getInstance().level == (Object) this;
    }

    @Override
    public ChunkLayout chunkLayout() {
        if (this.api$chunkLayout == null) {
            final var height = ((ClientLevel) (Object) this).getHeight();
            final var min = ((ClientLevel) (Object) this).getMinBuildHeight();
            this.api$chunkLayout = new SpongeChunkLayout(min, height);
        }
        return this.api$chunkLayout;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Optional<Entity> entity(final UUID uuid) {
        return (Optional) Optional.ofNullable(this.shadow$getEntities().get(uuid));
    }

    @Override
    public Collection<Entity> entities() {
        return (Collection<org.spongepowered.api.entity.Entity>) (Object) ImmutableList.copyOf(this.shadow$getEntities().getAll());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<? extends ClientPlayer> players() {
        return (Collection) new ArrayList<>(this.players);
    }
}
