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
package org.spongepowered.common.mixin.core.stats;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.statistic.achievement.Achievement;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.interfaces.statistic.IMixinAchievement;
import org.spongepowered.common.item.inventory.util.ItemStackUtil;
import org.spongepowered.common.registry.type.statistic.AchievementRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Mixin(net.minecraft.stats.Achievement.class)
public abstract class MixinAchievement extends MixinStatBase implements Achievement, IMixinAchievement {

    @Shadow @Final public net.minecraft.stats.Achievement parentAchievement;
    @Shadow @Final public net.minecraft.item.ItemStack icon;
    @Shadow @Final private String achievementDescription;

    @Shadow private boolean isSpecial;

    private final Set<Achievement> children = Sets.newHashSet();

    @Inject(method = "registerStat()Lnet/minecraft/stats/Achievement;", at = @At("RETURN"))
    public void registerAchievement(CallbackInfoReturnable<net.minecraft.stats.Achievement> ci) {
        AchievementRegistryModule.getInstance().registerAdditionalCatalog(this);
    }

    @Override
    public Translation getDescription() {
        return new SpongeTranslation(this.achievementDescription);
    }

    @Override
    public Optional<Achievement> getParent() {
        return Optional.ofNullable((Achievement) this.parentAchievement);
    }

    @Override
    public Collection<Achievement> getChildren() {
        return ImmutableSet.copyOf(this.children);
    }

    @Override
    public Optional<ItemStackSnapshot> getItemStackSnapshot() {
        return Optional.ofNullable(ItemStackUtil.fromNative(this.icon).createSnapshot());
    }

    @Override
    public boolean isSpecial() {
        return this.isSpecial;
    }

    @Override
    public void addChild(net.minecraft.stats.Achievement child) {
        this.children.add((Achievement) child);
    }

}
