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
package org.spongepowered.common.mixin.optimization.entity;

import com.google.common.base.Optional;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.network.datasync.DataParameter;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.mixin.core.entity.EntityAgeableMixin;

import java.util.UUID;

import javax.annotation.Nullable;

@Mixin(TameableEntity.class)
public abstract class EntityMixinTameable_Cached_Owner extends EntityAgeableMixin {

    @Shadow @Final protected static DataParameter<Optional<UUID>> OWNER_UNIQUE_ID;
    @Nullable private UUID cachedOwner$OwnerId;

    /**
     * @author gabizou - July 26th, 2016
     * @reason Uses the cached owner id to save constant lookups from the data watcher
     *
     * @return The owner id string
     */
    @Nullable
    @Overwrite
    public UUID getOwnerId() {
        if (this.cachedOwner$OwnerId == null) {
            this.cachedOwner$OwnerId = this.dataManager.get(OWNER_UNIQUE_ID).orNull();
        }
        return this.cachedOwner$OwnerId;
    }

    /**
     * @author gabizou - July 26th, 2016
     * @reason stores the cached owner id
     *
     * @param ownerUuid The owner id to set
     */
    @Overwrite
    public void setOwnerId(@Nullable final UUID ownerUuid) {
        this.cachedOwner$OwnerId = ownerUuid;
        this.dataManager.set(OWNER_UNIQUE_ID, Optional.fromNullable(ownerUuid));
    }

}
