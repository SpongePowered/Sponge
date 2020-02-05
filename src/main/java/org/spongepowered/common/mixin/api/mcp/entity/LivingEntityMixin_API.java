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
package org.spongepowered.common.mixin.api.mcp.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.api.data.value.Value;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.projectile.ProjectileLauncher;
import org.spongepowered.math.vector.Vector3d;

import java.util.Optional;
import java.util.Set;

@SuppressWarnings("rawtypes")
@Mixin(value = LivingEntity.class, priority = 999)
public abstract class LivingEntityMixin_API extends EntityMixin_API implements Living {

    @Shadow public abstract ItemStack shadow$getHeldItem(Hand hand);
    @Shadow public abstract float shadow$getHealth();

    @Override
    public Text getTeamRepresentation() {
        return Text.of(this.shadow$getUniqueID().toString());
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass) {
        return ProjectileLauncher.launch(checkNotNull(projectileClass, "projectile class"), this, null);
    }

    @Override
    public <T extends Projectile> Optional<T> launchProjectile(Class<T> projectileClass, Vector3d velocity) {
        return ProjectileLauncher.launch(checkNotNull(projectileClass, "projectile class"), this, checkNotNull(velocity, "velocity"));
    }

    @Override
    protected Set<Value.Immutable<?>> api$getVanillaValues() {
        final Set<Value.Immutable<?>> values = super.api$getVanillaValues();

        values.add(this.health().asImmutable());
        values.add(this.maxHealth().asImmutable());
        values.add(this.lastAttacker().asImmutable());
        values.add(this.headRotation().asImmutable());

        this.lastDamageReceived().map(Value::asImmutable).ifPresent(values::add);

        return values;
    }

}
