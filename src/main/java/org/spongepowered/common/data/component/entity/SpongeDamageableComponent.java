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
package org.spongepowered.common.data.component.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.DamageableComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

import java.lang.ref.WeakReference;

public class SpongeDamageableComponent extends SpongeAbstractComponent<DamageableComponent> implements DamageableComponent {

    private WeakReference<Living> lastAttacker;
    private double lastDamage;

    public SpongeDamageableComponent() {
        super(DamageableComponent.class);
    }

    @Override
    public Optional<Living> getLastAttacker() {
        return Optional.fromNullable(this.lastAttacker.get());
    }

    @Override
    public DamageableComponent setLastAttacker(Living lastAttacker) {
        this.lastAttacker = new WeakReference<Living>(checkNotNull(lastAttacker));
        return this;
    }

    @Override
    public Optional<Double> getLastDamage() {
        return this.lastAttacker.get() == null ? Optional.<Double>absent() : Optional.of(this.lastDamage);
    }

    @Override
    public DamageableComponent setLastDamage(double damage) {
        this.lastDamage = damage;
        return this;
    }


    @Override
    public DamageableComponent copy() {
        final DamageableComponent copied = new SpongeDamageableComponent();
        if (this.lastAttacker.get() == null) {
            return copied;
        } else {
            return copied.setLastAttacker(this.lastAttacker.get()).setLastDamage(this.lastDamage);
        }
    }

    @Override
    public DamageableComponent reset() {
        this.lastAttacker = new WeakReference<Living>(null);
        this.lastDamage = 0;
        return this;
    }

    @Override
    public int compareTo(DamageableComponent o) {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Tokens.LAST_ATTACKER.getQuery(), this.lastAttacker.get() == null ? "none" : this.lastAttacker.get().getUniqueId())
                .set(Tokens.LAST_DAMAGE.getQuery(), this.lastDamage);
    }
}
