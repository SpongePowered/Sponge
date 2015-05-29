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

import static com.google.common.base.Preconditions.checkArgument;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.component.entity.HealthComponent;
import org.spongepowered.api.data.token.Tokens;
import org.spongepowered.common.data.component.SpongeAbstractComponent;

public class SpongeHealthComponent extends SpongeAbstractComponent<HealthComponent> implements HealthComponent {

    private double health = 20.0D;
    private double maxHealth = 20.0D;


    public SpongeHealthComponent() {
        super(HealthComponent.class);
    }

    @Override
    public HealthComponent damage(double amount) {
        final double newHealth = this.health - amount;
        if (newHealth < 0) { // we have to validate that we can't have negative health
            this.health = 0;
        } else {
            this.health = newHealth;
        }
        return this;
    }

    @Override
    public double getHealth() {
        return this.health;
    }

    @Override
    public HealthComponent setHealth(double health) {
        checkArgument(health <= this.maxHealth, "Cannot set health greater than the max health!");
        checkArgument(health >= 0, "Health must be greater than or equal to zero!");
        this.health = health;
        return this;
    }

    @Override
    public double getMaxHealth() {
        return this.maxHealth;
    }

    @Override
    public HealthComponent setMaxHealth(double maxHealth) {
        checkArgument(maxHealth > 0);
        this.maxHealth = maxHealth;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
        return this;
    }

    @Override
    public HealthComponent copy() {
        return new SpongeHealthComponent().setMaxHealth(this.maxHealth).setHealth(this.health);
    }

    @Override
    public HealthComponent reset() {
        return setHealth(getMaxHealth());
    }

    @Override
    public int compareTo(HealthComponent o) {
        return (int) Math.floor((o.getHealth() - this.health) - (o.getMaxHealth() - this.maxHealth));
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(Tokens.HEALTH.getQuery(), this.health)
                .set(Tokens.MAX_HEALTH.getQuery(), this.maxHealth);
    }
}
