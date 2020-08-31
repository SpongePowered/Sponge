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

import net.minecraft.entity.LivingEntity;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityType;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.projectile.source.ProjectileSource;
import org.spongepowered.api.world.ServerLocation;

import java.util.Optional;
import java.util.function.Supplier;

public class SimpleEntityLaunchLogic<P extends Projectile> implements ProjectileLogic<P> {

    protected final Supplier<EntityType<P>> projectileType;

    public SimpleEntityLaunchLogic(Supplier<EntityType<P>> projectileType) {
        this.projectileType = projectileType;
    }

    @Override
    public Optional<P> launch(ProjectileSource source) {
        if (!(source instanceof Entity)) {
            return Optional.empty();
        }
        ServerLocation loc = ((Entity) source).getServerLocation().add(0, ((net.minecraft.entity.Entity) source).getHeight() / 2, 0);
        if (source instanceof LivingEntity) {
            return this.createProjectile((LivingEntity) source, loc);
        } else {
            return this.createProjectile(source, this.projectileType.get(), loc);
        }
    }

    protected Optional<P> createProjectile(LivingEntity source, ServerLocation loc) {
        return this.createProjectile((ProjectileSource) source, this.projectileType.get(), loc);
    }
}
