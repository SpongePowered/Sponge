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

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockSourceImpl;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
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
            return launch((TileEntityDispenser) source, projectileClass, (Item) args[0]);
        }
        Optional<P> projectile = logic.createProjectile(source, projectileClass, source.getLocation());
        if (projectile.isPresent()) {
            EnumFacing enumfacing = getFacing((TileEntityDispenser) source);
            net.minecraft.entity.Entity projectileEntity = (net.minecraft.entity.Entity) projectile.get();
            projectileEntity.field_70159_w = enumfacing.func_82601_c();
            projectileEntity.field_70181_x = enumfacing.func_96559_d() + 0.1F;
            projectileEntity.field_70179_y = enumfacing.func_82599_e();
        }
        return projectile;
    }

    public static EnumFacing getFacing(TileEntityDispenser dispenser) {
        IBlockState state = dispenser.func_145831_w().func_180495_p(dispenser.func_174877_v());
        return state.func_177229_b(BlockDispenser.field_176441_a);
    }

    @SuppressWarnings("unchecked")
    private <P extends Projectile> Optional<P> launch(TileEntityDispenser dispenser, Class<P> projectileClass, Item item) {
        BehaviorDefaultDispenseItem behavior = (BehaviorDefaultDispenseItem) BlockDispenser.field_149943_a.func_82594_a(item);
        List<Entity> entityList = dispenser.func_145831_w().field_72996_f;
        int numEntities = entityList.size();
        behavior.func_82482_a(new BlockSourceImpl(dispenser.func_145831_w(), dispenser.func_174877_v()), new ItemStack(item));
        // Hack - get the projectile that was spawned from dispense()
        for (int i = entityList.size() - 1; i >= numEntities; i--) {
            if (projectileClass.isInstance(entityList.get(i))) {
                return Optional.of((P) entityList.get(i));
            }
        }
        return Optional.empty();
    }
}
