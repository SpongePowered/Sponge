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
package org.spongepowered.common.mixin.api.mcp.entity.monster;

import net.minecraft.entity.monster.SpellcastingIllagerEntity;
import org.spongepowered.api.entity.living.monster.raider.illager.spellcaster.Spellcaster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpellcastingIllagerEntity.class)
public abstract class SpellcastingIllagerEntityMixin_API extends AbstractIllagerEntityMixin_API implements Spellcaster {

    @Shadow protected int spellTicks;
    @Shadow private SpellcastingIllagerEntity.SpellType activeSpell;
    @Shadow public abstract boolean shadow$isSpellcasting();
    @Shadow public abstract void shadow$setSpellType(SpellcastingIllagerEntity.SpellType p_193081_1_);
    @Shadow protected abstract SpellcastingIllagerEntity.SpellType shadow$getSpellType();

    @Override
    public boolean isCastingSpell() {
        return this.shadow$isSpellcasting();
    }

    public void setCastingSpell(boolean castSpell) {
        if (!castSpell) {
            this.spellTicks = 0;
            return;
        }

        // i509 -> TODO: Figure out how to get the casting time from UseSpellGoal. Yes Mojang made spells a goal and their oh so exposed enum SpellType does not refer to the goals.
    }
}
