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
package org.spongepowered.common.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.PaintingType;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.projectile.EnderPearl;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;

// TODO - archetypes should solve the problem of calling the correct constructor
@SuppressWarnings("ConstantConditions")
public final class EntityFactory {
    private EntityFactory() {
    }

    // This does not add the entity to the world.
    public static <I extends Entity> I create(final SpongeEntityType<I, ?> type, final World world, final double x, final double y, final double z, final boolean naturalize) throws IllegalStateException {
        final Entity entity;
        // We have some special cases:
        // - not all entities have a (World) constructor
        // - some entities need to have non-null fields (and the easiest way to set them is to use the more specialised constructor)
        if (type == EntityTypes.ENDER_PEARL) {
            final EntityArmorStand owner = new EntityArmorStand(world, x, y, z);
            owner.posY -= owner.getEyeHeight();
            entity = new EntityEnderPearl(world, owner);
            ((EnderPearl) entity).setShooter(ProjectileSource.UNKNOWN);
        } else if (type == EntityTypes.FALLING_BLOCK) {
            entity = new EntityFallingBlock(world, x, y, z, Blocks.SAND.getDefaultState());
        } else if (type == EntityTypes.ITEM) {
            entity = new EntityItem(world, x, y, z, new ItemStack(Blocks.STONE));
        } else if (type == EntityTypes.LIGHTNING_BOLT) {
            entity = new EntityLightningBolt(world, x, y, z, false);
        } else {
            entity = type.type.create(world);
            if (entity == null) {
                throw new IllegalStateException("Could not create an entity for " + type.getKey());
            }
            entity.setPosition(x, y, z);
        }
        // TODO - replace this with an actual check
//        if (entity instanceof EntityHanging) {
//            if (((EntityHanging) entity).facingDirection == null) {
//                // TODO Some sort of detection of a valid direction?
//                // i.e scan immediate blocks for something to attach onto.
//                ((EntityHanging) entity).facingDirection = EnumFacing.NORTH;
//            }
//            if (!((EntityHanging) entity).onValidSurface()) {
//                return Optional.empty();
//            }
//        }
        if (naturalize  && entity instanceof EntityLiving) {
            ((EntityLiving) entity).onInitialSpawn(world.getDifficultyForLocation(new BlockPos(x, y, z)), null, null);
        }
        if (entity instanceof EntityPainting) {
            // TODO: This is default when art is null when reading from NBT, could choose a random art instead?
            ((EntityPainting) entity).art = PaintingType.KEBAB;
        }
        return (I) entity;
    }
}
