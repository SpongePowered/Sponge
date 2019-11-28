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
package org.spongepowered.common.event.damage;

import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSource;

/*
To summarize, the way this works is that DamageSource isn't directly created, but
rather that any API abstract classes are automatically set up to extend
this class at runtime. All method calls are forwarded to the proper API calls
by definition. Granted, this cannot actively implement the interface,
but it can certainly declare the methods as abstract.

More notes are geared for abstraction of generating the builders, since those
will require sending the builders into the ctors.
 */
public abstract class SpongeCommonEntityDamageSource extends EntityDamageSource implements org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource {

    public SpongeCommonEntityDamageSource() {
        super("SpongeEntityDamageSource", null);
    }

    /**
     * Purely for use with {@link AbstractDamageSource} such that
     * the damage type is set after the super constructor is called.
     *
     * @param type The damage type id
     */
    public void setDamageType(final String type) {
        this.damageType = type;
    }

    public void setEntitySource(final Entity entitySource) {
        this.damageSourceEntity = entitySource;
    }

    public void bridge$setDamageIsAbsolute() {
        this.setDamageIsAbsolute();
    }
    public void bridge$setDamageBypassesArmor() {
        this.setDamageBypassesArmor();
    }

    @Override
    public Entity getTrueSource() {
        return (Entity) getSource();
    }

    @Override
    public boolean isExplosion() {
        return this.isExplosive();
    }

    @Override
    public boolean isUnblockable() {
        return this.isBypassingArmor();
    }

    @Override
    public boolean canHarmInCreative() {
        return this.doesAffectCreative();
    }

    @Override
    public boolean isDamageAbsolute() {
        return this.isAbsolute();
    }

    @Override
    public boolean isDifficultyScaled() {
        return this.isScaledByDifficulty();
    }

    @Override
    public boolean isMagicDamage() {
        return this.isMagic();
    }

    @Override
    public float getHungerDamage() {
        return (float) this.getExhaustion();
    }

}
