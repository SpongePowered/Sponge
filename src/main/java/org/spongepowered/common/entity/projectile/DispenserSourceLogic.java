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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.block.entity.carrier.Dispenser;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.common.accessor.world.level.block.DispenserBlockAccessor;

import java.util.List;
import java.util.Optional;

public final class DispenserSourceLogic implements ProjectileSourceLogic<Dispenser> {

    DispenserSourceLogic() {
    }

    @Override
    public <P extends Projectile> Optional<P> launch(final ProjectileLogic<P> logic, final Dispenser source,
            final EntityType<P> projectileType, final Object... args) {
        if (args.length == 1 && args[0] instanceof Item) {
            return this.launch((DispenserBlockEntity) source, projectileType, (Item) args[0]);
        }
        final Optional<P> projectile = logic.createProjectile(source, projectileType, source.location());
        if (projectile.isPresent()) {
            final Direction enumfacing = DispenserSourceLogic.getFacing((DispenserBlockEntity) source);
            final net.minecraft.world.entity.Entity projectileEntity = (net.minecraft.world.entity.Entity) projectile.get();
            final BlockPos adjustedPosition = projectileEntity.blockPosition().offset(enumfacing.getUnitVec3i());
            projectileEntity.setPos(adjustedPosition.getX(), adjustedPosition.getY(), adjustedPosition.getZ());
        }
        return projectile;
    }

    public static Direction getFacing(final DispenserBlockEntity dispenser) {
        final BlockState state = dispenser.getLevel().getBlockState(dispenser.getBlockPos());
        return state.getValue(DispenserBlock.FACING);
    }

    @SuppressWarnings("unchecked")
    private <P extends Projectile> Optional<P> launch(final DispenserBlockEntity dispenser, final EntityType<P> projectileType, final Item item) {
        final DispenseItemBehavior behavior = DispenserBlockAccessor.accessor$DISPENSER_REGISTRY().get(item);
        final ServerLevel world = (ServerLevel) dispenser.getLevel();
        behavior.dispense(new BlockSource(world, dispenser.getBlockPos(), dispenser.getLevel().getBlockState(dispenser.getBlockPos()), dispenser), new ItemStack(item));
        final List<P> entities = (List<P>) world.getEntities((net.minecraft.world.entity.EntityType<?>) projectileType, entity -> true);
        if (entities.isEmpty()) {
            return Optional.empty();
        }
        // Hack - get the projectile that was spawned from dispense()
        return Optional.of(entities.get(entities.size() - 1));
    }
}
