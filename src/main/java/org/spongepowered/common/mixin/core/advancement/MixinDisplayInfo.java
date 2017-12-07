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
package org.spongepowered.common.mixin.core.advancement;

import static com.google.common.base.Preconditions.checkState;

import com.flowpowered.math.vector.Vector2d;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.FrameType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.advancement.TreeLayoutElement;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.interfaces.advancement.IMixinDisplayInfo;
import org.spongepowered.common.item.inventory.SpongeItemStackSnapshot;
import org.spongepowered.common.text.SpongeTexts;

import javax.annotation.Nullable;

@Implements(@Interface(iface = org.spongepowered.api.advancement.DisplayInfo.class, prefix = "info$"))
@Mixin(DisplayInfo.class)
public abstract class MixinDisplayInfo implements IMixinDisplayInfo, TreeLayoutElement {

    @Shadow @Final private FrameType frame;
    @Shadow @Final private ItemStack icon;
    @Shadow @Final private ITextComponent title;
    @Shadow @Final private ITextComponent description;
    @Shadow @Final private boolean showToast;
    @Shadow @Final private boolean announceToChat;
    @Shadow @Final private boolean hidden;
    @Shadow private float x;
    @Shadow private float y;

    @Nullable private Advancement advancement;

    @Override
    public Advancement getAdvancement() {
        checkState(this.advancement != null, "The advancement is not yet initialized");
        return this.advancement;
    }

    @Override
    public void setAdvancement(Advancement advancement) {
        this.advancement = advancement;
    }

    public ItemStackSnapshot info$getIcon() {
        return new SpongeItemStackSnapshot((org.spongepowered.api.item.inventory.ItemStack) this.icon);
    }

    public boolean info$doesShowToast() {
        return this.showToast;
    }

    public boolean info$doesAnnounceToChat() {
        return this.announceToChat;
    }

    public boolean info$isHidden() {
        return this.hidden;
    }

    public AdvancementType info$getType() {
        return (AdvancementType) (Object) this.frame;
    }

    public Text info$getDescription() {
        return SpongeTexts.toText(this.description);
    }

    public Text info$getTitle() {
        return SpongeTexts.toText(this.title);
    }

    @Override
    public Vector2d getPosition() {
        return new Vector2d(this.x, this.y);
    }

    @Override
    public void setPosition(Vector2d position) {
        setPosition(position.getX(), position.getY());
    }

    @Override
    public void setPosition(double x, double y) {
        this.x = (float) x;
        this.y = (float) y;
    }
}
