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
package org.spongepowered.common.event.cause.entity.damage;

import net.minecraft.world.entity.Entity;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSource;
import org.spongepowered.common.accessor.world.damagesource.DamageSourceAccessor;
import org.spongepowered.common.accessor.world.damagesource.EntityDamageSourceAccessor;
import org.spongepowered.common.accessor.world.damagesource.IndirectEntityDamageSourceAccessor;

/*
To summarize, the way this works is that DamageSource isn't directly created, but
rather that any API abstract classes are automatically set up to extend
this class at runtime. All method calls are forwarded to the proper API calls
by definition. Granted, this cannot actively implement the interface,
but it can certainly declare the methods as abstract.

More notes are geared for abstraction of generating the builders, since those
will require sending the builders into the ctors.
 */
public abstract class SpongeCommonIndirectEntityDamageSource extends net.minecraft.world.damagesource.IndirectEntityDamageSource implements IndirectEntityDamageSource {

    public SpongeCommonIndirectEntityDamageSource() {
        super("SpongeEntityDamageSource", null, null);
    }

    /**
     * Purely for use with {@link AbstractDamageSource} such that
     * the damage type is set after the super constructor is called.
     *
     * @param type The damage type id
     */
    public void setDamageType(final String type) {
        ((DamageSourceAccessor) this).accessor$msgId(type);
    }

    public void setEntitySource(final Entity entitySource) {
        ((EntityDamageSourceAccessor) this).accessor$entity(entitySource);
    }

    public void setIndirectSource(final Entity entity) {
        ((IndirectEntityDamageSourceAccessor) this).accessor$owner(entity);
    }

    @Override
    public Entity getEntity() {
        return (Entity) this.source();
    }

    @Override
    public boolean isExplosion() {
        return this.isExplosive();
    }

    @Override
    public boolean isBypassArmor() {
        return this.isBypassingArmor();
    }

    @Override
    public boolean isBypassInvul() {
        return this.doesAffectCreative();
    }

    @Override
    public boolean isBypassMagic() {
        return this.isAbsolute();
    }

    @Override
    public boolean scalesWithDifficulty() {
        return this.isScaledByDifficulty();
    }

    @Override
    public float getFoodExhaustion() {
        return (float) this.exhaustion();
    }

}
