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
package org.spongepowered.common.mixin.core.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.api.context.ContextViewer;
import org.spongepowered.api.context.NamedContextual;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.entity.living.Humanoid;
import org.spongepowered.api.profile.property.ProfileProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.entity.context.store.EntityContextStore;
import org.spongepowered.common.entity.context.store.HumanoidContextStore;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.interfaces.entity.IMixinEntityContext;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin({EntityHuman.class, EntityPlayerMP.class})
public abstract class MixinHumanoid implements Humanoid, NamedContextual, IMixinEntityContext {

    @Override
    public String getName(@Nullable ContextViewer viewer) {
        return ((HumanoidContextStore) this.getContextStore()).getName(viewer);
    }

    @Override
    public EntityContextStore createContextStore() {
        return new HumanoidContextStore((Entity) (Object) this);
    }

    @Override
    public Optional<ProfileProperty> getTextures() {
        return ((HumanoidContextStore) this.getContextStore()).getTextures();
    }

    @Override
    public Optional<ProfileProperty> getTextures(@Nullable ContextViewer viewer) {
        return ((HumanoidContextStore) this.getContextStore()).getTextures(viewer);
    }

    @Override
    public DataTransactionResult setFakeNameAndTextures(ContextViewer viewer, String name, ProfileProperty property) {
        return ((HumanoidContextStore) this.getContextStore()).setFakeNameAndTextures(viewer, name, property);
    }

    @Override
    public void clearFakeNames() {
        ((HumanoidContextStore) this.getContextStore()).clearFakeNames();
    }

    @Override
    public void clearTextures() {
        ((HumanoidContextStore) this.getContextStore()).clearTextures();
    }

    @Override
    public void clearFakeNamesAndTextures() {
        ((HumanoidContextStore) this.getContextStore()).clearFakeNamesAndTextures();
    }

}
