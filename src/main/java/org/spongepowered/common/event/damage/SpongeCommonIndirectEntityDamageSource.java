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
import net.minecraft.util.EntityDamageSourceIndirect;
import org.spongepowered.api.event.cause.entity.damage.source.IndirectEntityDamageSource;
import org.spongepowered.api.event.cause.entity.damage.source.common.AbstractDamageSource;
import org.spongepowered.common.mixin.core.util.EntityDamageSourceIndirectAccessor;

/*
To summarize, the way this works is that DamageSource isn't directly created, but
rather that any API abstract classes are automatically set up to extend
this class at runtime. All method calls are forwarded to the proper API calls
by definition. Granted, this cannot actively implement the interface,
but it can certainly declare the methods as abstract.

More notes are geared for abstraction of generating the builders, since those
will require sending the builders into the ctors.
 */
public abstract class SpongeCommonIndirectEntityDamageSource extends EntityDamageSourceIndirect implements IndirectEntityDamageSource {

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
        this.field_76373_n = type;
    }

    public void setEntitySource(final Entity entitySource) {
        this.field_76386_o = entitySource;
    }

    public void setIndirectSource(final Entity entity) {
        ((EntityDamageSourceIndirectAccessor) this).accessor$setIndirectEntity(entity);
    }


    public void bridge$setDamageIsAbsolute() {
        this.func_151518_m();
    }
    public void bridge$setDamageBypassesArmor() {
        this.func_76348_h();
    }


    @Override
    public Entity func_76346_g() {
        return (Entity) getSource();
    }

    @Override
    public boolean func_94541_c() {
        return this.isExplosive();
    }

    @Override
    public boolean func_76363_c() {
        return this.isBypassingArmor();
    }

    @Override
    public boolean func_76357_e() {
        return this.doesAffectCreative();
    }

    @Override
    public boolean func_151517_h() {
        return this.isAbsolute();
    }

    @Override
    public boolean func_76350_n() {
        return this.isScaledByDifficulty();
    }

    @Override
    public boolean func_82725_o() {
        return this.isMagic();
    }

    @Override
    public float func_76345_d() {
        return (float) this.getExhaustion();
    }

}
