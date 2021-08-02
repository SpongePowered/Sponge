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
package org.spongepowered.common.mixin.api.minecraft.advancements;

import net.kyori.adventure.text.Component;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.advancement.TreeLayoutElement;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Interface.Remap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.adventure.SpongeAdventure;
import org.spongepowered.common.bridge.advancements.DisplayInfoBridge;
import org.spongepowered.common.item.util.ItemStackUtil;
import org.spongepowered.math.vector.Vector2d;

@Mixin(DisplayInfo.class)
@Implements(@Interface(iface = org.spongepowered.api.advancement.DisplayInfo.class, prefix = "displayInfo$", remap = Remap.NONE))
public abstract class DisplayInfoMixin_API implements TreeLayoutElement, org.spongepowered.api.advancement.DisplayInfo {

    @Shadow @Final private FrameType frame;
    @Shadow @Final private net.minecraft.network.chat.Component title;
    @Shadow @Final private net.minecraft.network.chat.Component description;
    @Shadow @Final private boolean showToast;
    @Shadow private float x;
    @Shadow private float y;
    @Shadow public abstract boolean shadow$shouldAnnounceChat();
    @Shadow public abstract boolean shadow$isHidden();

    @Shadow @Final private ItemStack icon;

    @Shadow @Final private boolean hidden;

    @Override
    public Advancement advancement() {
        return ((DisplayInfoBridge) this).bridge$getAdvancement();
    }

    @Override
    public Vector2d position() {
        return new Vector2d(this.x, this.y);
    }

    @Override
    public void setPosition(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public AdvancementType type() {
        return (AdvancementType) (Object) this.frame;
    }

    @Override
    public Component description() {
        return SpongeAdventure.asAdventure(this.description);
    }

    @Override
    public Component title() {
        return SpongeAdventure.asAdventure(this.title);
    }

    @Override
    public ItemStackSnapshot icon() {
        return ItemStackUtil.snapshotOf(this.icon);
    }

    @Override
    public boolean isHidden() {
        return this.hidden;
    }

    @Override
    public boolean doesShowToast() {
        return this.showToast;
    }

    @Override
    public boolean doesAnnounceToChat() {
        return this.shadow$shouldAnnounceChat();
    }

    @Intrinsic
    public boolean displayInfo$isHidden() {
        return this.shadow$isHidden();
    }
}
