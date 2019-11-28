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

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.dispenser.ProxyBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.Direction;
import org.spongepowered.api.block.tileentity.carrier.Dispenser;
import org.spongepowered.api.entity.projectile.Projectile;

import java.util.List;
import java.util.Optional;

public class DispenserSourceLogic implements ProjectileSourceLogic<Dispenser> {

    DispenserSourceLogic() {
    }

    @Override
    public <P extends Projectile> Optional<P> launch(ProjectileLogic<P> logic, Dispenser source, Class<P> projectileClass, Object... args) {
        if (args.length == 1 && args[0] instanceof Item) {
            return launch((DispenserTileEntity) source, projectileClass, (Item) args[0]);
        }
        Optional<P> projectile = logic.createProjectile(source, projectileClass, source.getLocation());
        if (projectile.isPresent()) {
            Direction enumfacing = getFacing((DispenserTileEntity) source);
            net.minecraft.entity.Entity projectileEntity = (net.minecraft.entity.Entity) projectile.get();
            projectileEntity.motionX = enumfacing.getXOffset();
            projectileEntity.motionY = enumfacing.getYOffset() + 0.1F;
            projectileEntity.motionZ = enumfacing.getZOffset();
        }
        return projectile;
    }

    public static Direction getFacing(DispenserTileEntity dispenser) {
        BlockState state = dispenser.getWorld().getBlockState(dispenser.getPos());
        return state.get(DispenserBlock.FACING);
    }

    @SuppressWarnings("unchecked")
    private <P extends Projectile> Optional<P> launch(DispenserTileEntity dispenser, Class<P> projectileClass, Item item) {
        DefaultDispenseItemBehavior behavior = (DefaultDispenseItemBehavior) DispenserBlock.DISPENSE_BEHAVIOR_REGISTRY.getOrDefault(item);
        List<Entity> entityList = dispenser.getWorld().loadedEntityList;
        int numEntities = entityList.size();
        behavior.dispense(new ProxyBlockSource(dispenser.getWorld(), dispenser.getPos()), new ItemStack(item));
        // Hack - get the projectile that was spawned from dispense()
        for (int i = entityList.size() - 1; i >= numEntities; i--) {
            if (projectileClass.isInstance(entityList.get(i))) {
                return Optional.of((P) entityList.get(i));
            }
        }
        return Optional.empty();
    }
}
