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
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.applaunch.config.common.CommonConfig;
import org.spongepowered.common.applaunch.config.core.ConfigHandle;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.common.bridge.entitycollision.CollisionCapabilityBridge;
import org.spongepowered.common.bridge.world.WorldBridge;
import org.spongepowered.common.bridge.world.storage.ServerWorldInfoBridge;
import org.spongepowered.common.config.inheritable.EntityCollisionCategory;
import org.spongepowered.common.config.inheritable.InheritableConfigHandle;
import org.spongepowered.common.config.inheritable.WorldConfig;

@Mixin(value = net.minecraft.entity.Entity.class, priority = 1002)
public abstract class EntityMixin_EntityCollision implements CollisionCapabilityBridge {

    @Shadow public abstract net.minecraft.world.World shadow$getCommandSenderWorld();

    private ResourceKey entityCollision$key;
    private int entityCollision$maxCollisions = 8;
    private boolean entityCollision$refreshCache = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void collisions$InjectActivationInformation(net.minecraft.entity.EntityType<?> type, net.minecraft.world.World world, CallbackInfo ci) {
        if (world != null && !((WorldBridge) world).bridge$isFake() && ((ServerWorldInfoBridge) world.getLevelData()).bridge$valid()) {
            if ((net.minecraft.entity.Entity) (Object) this instanceof ItemEntity) {
                final ItemEntity item = (ItemEntity) (Object) this;
                final ItemStack itemstack = item.getItem();
                if (!itemstack.isEmpty()) {
                    this.entityCollision$key =
                            Sponge.getGame().registries().registry(RegistryTypes.ITEM_TYPE).valueKey(((org.spongepowered.api.item.inventory.ItemStack) (Object) itemstack).getType());
                }
            } else {
                this.entityCollision$key = Sponge.getGame().registries().registry(RegistryTypes.ENTITY_TYPE).valueKey(((Entity) this).getType());
            }
            if (!this.shadow$getCommandSenderWorld().isClientSide()) {
                this.collision$initializeCollisionState(this.shadow$getCommandSenderWorld());
            }
        }
    }

    @Override
    public ResourceKey collision$getKey() {
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
    public void collision$initializeCollisionState(final net.minecraft.world.World world) {
        final InheritableConfigHandle<WorldConfig> worldConfigAdapter = ((ServerWorldInfoBridge) world.getLevelData()).bridge$configAdapter();
        final ConfigHandle<CommonConfig> globalConfigAdapter = SpongeConfigs.getCommon();
        final EntityCollisionCategory.ModSubCategory worldCollMod =
                worldConfigAdapter.getOrCreateValue(s -> s.entityCollision.mods.get(this.entityCollision$key.getNamespace()),
                c -> {
                    // TODO: save after populating?
                    final EntityCollisionCategory.ModSubCategory globalCollision = new EntityCollisionCategory.ModSubCategory(this.entityCollision$key.getNamespace());
                    c.entityCollision.mods.put(this.entityCollision$key.getNamespace(), globalCollision);
                    globalCollision.entities.put(this.entityCollision$key.getNamespace(), this.collision$getMaxCollisions());
                }, worldConfigAdapter.get().entityCollision.autoPopulate);
        final EntityCollisionCategory worldCollCat = worldConfigAdapter.get().entityCollision;

        this.collision$setMaxCollisions(worldCollCat.maxEntitiesWithinAABB);

        boolean requiresSave = false;
        if (worldCollMod != null) {
            if (!worldCollMod.enabled) {
                this.collision$setMaxCollisions(-1);
                return;
            }
            // check mod overrides
            final Integer modCollisionMax = worldCollMod.entityDefault;
            if (modCollisionMax != null) {
                this.collision$setMaxCollisions(modCollisionMax);
            }

            Integer entityMaxCollision = null;
            if ((net.minecraft.entity.Entity) (Object) this instanceof ItemEntity) {
                // check if all items are overridden
                entityMaxCollision = worldCollMod.entities.get(this.entityCollision$key.getValue());
            }

            if (entityMaxCollision == null) {
                entityMaxCollision = worldCollMod.entities.get(this.entityCollision$key.getValue());
            }

            // entity overrides
            if (entityMaxCollision == null && worldCollCat.autoPopulate) {
                // TODO(zml): Populate better
                //globalCollMod.getEntityList().put(this.entityCollision$key.getValue(), this.collision$getMaxCollisions());
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
