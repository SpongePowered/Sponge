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
package org.spongepowered.common.mixin.entitycollision.entity;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.storage.WorldInfoBridge;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.category.CollisionModCategory;
import org.spongepowered.common.config.category.EntityCollisionCategory;
import org.spongepowered.common.config.type.GlobalConfig;
import org.spongepowered.common.config.type.WorldConfig;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.bridge.entitycollision.CollisionCapabilityBridge;

@Mixin(value = net.minecraft.entity.Entity.class, priority = 1002)
public abstract class EntityMixin_EntityCollision implements CollisionCapabilityBridge {

    @Shadow public abstract World shadow$getEntityWorld();

    private CatalogKey entityCollision$key;
    private int entityCollision$maxCollisions = 8;
    private boolean entityCollision$refreshCache = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void collisions$InjectActivationInformation(net.minecraft.entity.EntityType<?> type, World world, CallbackInfo ci) {
        if (world != null && !((WorldBridge) world).bridge$isFake() && ((WorldInfoBridge) world.getWorldInfo()).bridge$isValid()) {
            final EntityType entityType = ((Entity) this).getType();
            if (entityType == EntityTypes.UNKNOWN || !(entityType instanceof SpongeEntityType)) {
                return;
            }

            if ((net.minecraft.entity.Entity) (Object) this instanceof ItemEntity) {
                final ItemEntity item = (ItemEntity) (Object) this;
                final ItemStack itemstack = item.getItem();
                if (!itemstack.isEmpty()) {
                    this.entityCollision$key = ((org.spongepowered.api.item.inventory.ItemStack) (Object) itemstack).getType().getKey();
                }
            } else {
                this.entityCollision$key = ((Entity) this).getType().getKey();
            }
            if (!this.shadow$getEntityWorld().isRemote()) {
                this.collision$initializeCollisionState(this.shadow$getEntityWorld());
            }
        }
    }

    @Override
    public CatalogKey collision$getKey() {
        return this.entityCollision$key;
    }

    @Override
    public int collision$getMaxCollisions() {
        return this.entityCollision$maxCollisions;
    }

    @Override
    public void collision$setMaxCollisions(final int max) {
        this.entityCollision$maxCollisions = max;
    }

    @Override
    public void collision$initializeCollisionState(final World world) {
        final SpongeConfig<WorldConfig> worldConfigAdapter = ((WorldInfoBridge) world.getWorldInfo()).bridge$getConfigAdapter();
        final SpongeConfig<GlobalConfig> globalConfigAdapter = SpongeImpl.getGlobalConfigAdapter();
        final EntityCollisionCategory worldCollCat = worldConfigAdapter.getConfig().getEntityCollisionCategory();
        final EntityCollisionCategory globalCollCat = globalConfigAdapter.getConfig().getEntityCollisionCategory();

        this.collision$setMaxCollisions(worldCollCat.getMaxEntitiesWithinAABB());

        boolean requiresSave = false;
        final CollisionModCategory worldCollMod = worldCollCat.getModList().get(this.entityCollision$key.getNamespace());
        CollisionModCategory globalCollMod = globalCollCat.getModList().get(this.entityCollision$key.getNamespace());
        if (worldCollMod == null && worldCollCat.autoPopulateData()) {
            globalCollMod = new CollisionModCategory(this.entityCollision$key.getNamespace());
            globalCollCat.getModList().put(this.entityCollision$key.getNamespace(), globalCollMod);
            globalCollMod.getEntityList().put(this.entityCollision$key.getValue(), this.collision$getMaxCollisions());
            globalConfigAdapter.save();
            return;
        } else if (worldCollMod != null) {
            if (!worldCollMod.isEnabled()) {
                this.collision$setMaxCollisions(-1);
                return;
            }
            // check mod overrides
            final Integer modCollisionMax = worldCollMod.getDefaultMaxCollisions().get("entities");
            if (modCollisionMax != null) {
                this.collision$setMaxCollisions(modCollisionMax);
            }

            Integer entityMaxCollision = null;
            if ((net.minecraft.entity.Entity) (Object) this instanceof ItemEntity) {
                // check if all items are overridden
                entityMaxCollision = worldCollMod.getEntityList().get(this.entityCollision$key.getValue());
            }

            if (entityMaxCollision == null) {
                entityMaxCollision = worldCollMod.getEntityList().get(this.entityCollision$key.getValue());
            }

            // entity overrides
            if (entityMaxCollision == null && worldCollCat.autoPopulateData()) {
                globalCollMod.getEntityList().put(this.entityCollision$key.getValue(), this.collision$getMaxCollisions());
                requiresSave = true;
            } else if (entityMaxCollision != null) {
                this.collision$setMaxCollisions(entityMaxCollision);
            }
        }

        // don't bother saving for negative values
        if (this.collision$getMaxCollisions() <= 0) {
            return;
        }

        if (requiresSave) {
            globalConfigAdapter.save();
        }
    }

    @Override
    public void collision$requiresCollisionsCacheRefresh(final boolean flag) {
        this.entityCollision$refreshCache = flag;
    }

    @Override
    public boolean collision$requiresCollisionsCacheRefresh() {
        return this.entityCollision$refreshCache;
    }

    @Override
    public boolean collision$isRunningCollideWithNearby() {
        return false;
    }
}
