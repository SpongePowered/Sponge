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

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import org.spongepowered.api.block.entity.carrier.Dispenser;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.projectile.source.ProjectileSource;

import java.util.Optional;
import java.util.function.Supplier;

public class SimpleItemLaunchLogic<P extends Projectile> extends SimpleEntityLaunchLogic<P> {

    protected final Item item;

    public SimpleItemLaunchLogic(final Supplier<EntityType<P>> projectileType, final Item item) {
        super(projectileType);
        this.item = item;
    }

    @Override
    public Optional<P> launch(final ProjectileSource source) {
        if (source instanceof DispenserBlockEntity) {
            return ProjectileUtil.getSourceLogic(Dispenser.class).launch(this, (Dispenser) source, this.projectileType.get(), this.item);
        }
        return super.launch(source);
    }
}
