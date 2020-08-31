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
package org.spongepowered.common.entity.projectile;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.dispenser.ProxyBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.api.block.entity.carrier.Dispenser;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.common.accessor.block.DispenserBlockAccessor;
import org.spongepowered.common.accessor.world.server.ServerWorldAccessor;

import java.util.List;
import java.util.Optional;
import java.util.Queue;

public final class DispenserSourceLogic implements ProjectileSourceLogic<Dispenser> {

    DispenserSourceLogic() {
    }

    @Override
    public <P extends Projectile> Optional<P> launch(final ProjectileLogic<P> logic, final Dispenser source,
            final EntityType<P> projectileType, final Object... args) {
        if (args.length == 1 && args[0] instanceof Item) {
            return this.launch((DispenserTileEntity) source, projectileType, (Item) args[0]);
        }
        final Optional<P> projectile = logic.createProjectile(source, projectileType, source.getLocation());
        if (projectile.isPresent()) {
            Direction enumfacing = DispenserSourceLogic.getFacing((DispenserTileEntity) source);
            net.minecraft.entity.Entity projectileEntity = (net.minecraft.entity.Entity) projectile.get();
            BlockPos adjustedPosition = projectileEntity.getPosition().add(enumfacing.getDirectionVec());
            projectileEntity.setPosition(adjustedPosition.getX(), adjustedPosition.getY(), adjustedPosition.getZ());
        }
        return projectile;
    }

    public static Direction getFacing(final DispenserTileEntity dispenser) {
        final BlockState state = dispenser.getWorld().getBlockState(dispenser.getPos());
        return state.get(DispenserBlock.FACING);
    }

    @SuppressWarnings("unchecked")
    private <P extends Projectile> Optional<P> launch(final DispenserTileEntity dispenser, final EntityType<P> projectileType, final Item item) {
        final IDispenseItemBehavior behavior = DispenserBlockAccessor.accessor$DISPENSE_BEHAVIOR_REGISTRY().get(item);
        final ServerWorld world = (ServerWorld) dispenser.getWorld();
        behavior.dispense(new ProxyBlockSource(world, dispenser.getPos()), new ItemStack(item));
        final List<Entity> entities = world.getEntities((net.minecraft.entity.EntityType<?>) projectileType, entity -> true);
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        // Hack - get the projectile that was spawned from dispense()
        return Optional.of((P) entities.get(entities.size() - 1));
    }
}
