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
package org.spongepowered.common.mixin.entitycollisions;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.config.SpongeConfig;
import org.spongepowered.common.config.SpongeConfig.CollisionModNode;
import org.spongepowered.common.config.SpongeConfig.EntityCollisionCategory;
import org.spongepowered.common.entity.SpongeEntityType;
import org.spongepowered.common.interfaces.world.IMixinWorld;
import org.spongepowered.common.interfaces.world.IMixinWorldInfo;
import org.spongepowered.common.mixin.plugin.entitycollisions.interfaces.IModData_Collisions;

@Mixin(value = net.minecraft.entity.Entity.class, priority = 1002)
public class MixinEntity_Collisions implements IModData_Collisions {

    private net.minecraft.entity.Entity mcEntity = (net.minecraft.entity.Entity) (Object) this;
    private int maxCollisions = 8;
    private boolean refreshCache = false;
    private SpongeEntityType spongeEntityType;
    private String entityName;
    private String entityModId;
    @Shadow public World worldObj;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void onEntityConstruction(World world, CallbackInfo ci) {
        if (world != null && ((IMixinWorldInfo) world.getWorldInfo()).isValid()) {
            this.spongeEntityType = (SpongeEntityType) ((Entity) this.mcEntity).getType();
            if (mcEntity instanceof EntityItem) {
                EntityItem item = (EntityItem) mcEntity;
                ItemStack itemstack = item.getEntityItem();
                if (itemstack != null) {
                    this.entityName = itemstack.getUnlocalizedName().replace("item.", "");
                }
            } else {
                this.entityName = this.spongeEntityType.getName();
            }
            this.entityModId = this.spongeEntityType.getModId();
            initializeCollisionState();
        }
    }

    @Override
    public int getMaxCollisions() {
        return this.maxCollisions;
    }

    @Override
    public void setMaxCollisions(int max) {
        this.maxCollisions = max;
    }

    @Override
    public void setModDataName(String name) {
        this.entityName = name;
    }

    @Override
    public String getModDataName() {
        return this.entityName;
    }

    @Override
    public String getModDataId() {
        return this.entityModId;
    }

    @Override
    public void setModDataId(String id) {
        this.entityModId = id;
    }

    private boolean initializeCollisionState() {
        SpongeConfig<?> activeConfig = ((IMixinWorld) this.worldObj).getActiveConfig();
        EntityCollisionCategory collisionCat = activeConfig.getConfig().getEntityCollisionCategory();
        this.maxCollisions = collisionCat.getMaxEntitiesWithinAABB();
        boolean requiresSave = false;
        CollisionModNode collisionMod = collisionCat.getModList().get(this.entityModId);
        if (collisionMod == null && activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
            collisionMod = new CollisionModNode(this.entityModId);
            collisionCat.getModList().put(this.entityModId, collisionMod);
            collisionMod.getEntityList().put(this.entityName, maxCollisions);
            activeConfig.save();
            return true;
        } else if (collisionMod != null) {
            // check mod overrides
            Integer modCollisionMax = collisionMod.getDefaultMaxCollisions().get("entities");
            if (modCollisionMax != null) {
                this.maxCollisions = modCollisionMax;
            }

            Integer entityMaxCollision = null;
            if (this.mcEntity instanceof EntityItem) {
                // check if all items are overidden
                entityMaxCollision = collisionMod.getEntityList().get(this.spongeEntityType.getName());
            }

            if (entityMaxCollision == null) {
                entityMaxCollision = collisionMod.getEntityList().get(this.entityName);
            }
            // entity overrides
            if (entityMaxCollision == null && activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
                collisionMod.getEntityList().put(this.entityName, maxCollisions);
                requiresSave = true;
            } else if (entityMaxCollision != null) {
                this.maxCollisions = entityMaxCollision;
            }
        }

        if (this.maxCollisions <= 0) {
            return false;
        }

        if (requiresSave && activeConfig.getConfig().getEntityCollisionCategory().autoPopulateData()) {
            activeConfig.save();
        }
        return true;
    }

    @Override
    public void requiresCacheRefresh(boolean flag) {
        this.refreshCache = flag;
    }

    @Override
    public boolean requiresCacheRefresh() {
        return this.refreshCache;
    }
}
