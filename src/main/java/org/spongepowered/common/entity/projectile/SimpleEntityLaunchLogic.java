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

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.projectile.source.ProjectileSource;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;
import net.minecraft.entity.LivingEntity;

public class SimpleEntityLaunchLogic<P extends Projectile> implements ProjectileLogic<P> {

    protected final Class<P> projectileClass;

    public SimpleEntityLaunchLogic(Class<P> projectileClass) {
        this.projectileClass = projectileClass;
    }

    @Override
    public Optional<P> launch(ProjectileSource source) {
        if (!(source instanceof Entity)) {
            return Optional.empty();
        }
        Location<World> loc = ((Entity) source).getLocation().add(0, ((net.minecraft.entity.Entity) source).height / 2, 0);
        Optional<P> projectile;
        if (source instanceof LivingEntity) {
            projectile = createProjectile((LivingEntity) source, loc);
        } else {
            projectile = createProjectile(source, this.projectileClass, loc);
        }
        return projectile;
    }

    protected Optional<P> createProjectile(LivingEntity source, Location<?> loc) {
        return createProjectile((ProjectileSource) source, this.projectileClass, loc);
    }
}
