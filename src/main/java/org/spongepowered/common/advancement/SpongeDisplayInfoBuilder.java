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
package org.spongepowered.common.advancement;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import net.minecraft.advancements.FrameType;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.advancement.AdvancementTypes;
import org.spongepowered.api.advancement.DisplayInfo;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.common.text.SpongeTexts;

public class SpongeDisplayInfoBuilder implements DisplayInfo.Builder {

    private AdvancementType advancementType;
    private Text description;
    private Text title;
    private ItemStackSnapshot icon;
    private boolean showToast;
    private boolean announceToChat;
    private boolean hidden;

    public SpongeDisplayInfoBuilder() {
        reset();
    }

    @Override
    public DisplayInfo.Builder type(final AdvancementType advancementType) {
        checkNotNull(advancementType, "advancementType");
        this.advancementType = advancementType;
        return this;
    }

    @Override
    public DisplayInfo.Builder description(final Text description) {
        checkNotNull(description, "description");
        this.description = description;
        return this;
    }

    @Override
    public DisplayInfo.Builder title(final Text title) {
        checkNotNull(title, "title");
        this.title = title;
        return this;
    }

    @Override
    public DisplayInfo.Builder icon(final ItemStackSnapshot itemStackSnapshot) {
        checkNotNull(itemStackSnapshot, "itemStackSnapshot");
        this.icon = itemStackSnapshot;
        return this;
    }

    @Override
    public DisplayInfo.Builder showToast(final boolean showToast) {
        this.showToast = showToast;
        return this;
    }

    @Override
    public DisplayInfo.Builder announceToChat(final boolean announceToChat) {
        this.announceToChat = announceToChat;
        return this;
    }

    @Override
    public DisplayInfo.Builder hidden(final boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Override
    public DisplayInfo build() {
        checkState(this.title != null, "Title has not been set");
        checkState(this.icon != null, "Icon has not been set");
        final ITextComponent title = SpongeTexts.toComponent(this.title);
        final ITextComponent description = SpongeTexts.toComponent(this.description);
        final FrameType frameType = (FrameType) (Object) this.advancementType;
        final net.minecraft.item.ItemStack icon = (net.minecraft.item.ItemStack) this.icon.createStack();
        return (DisplayInfo) new net.minecraft.advancements.DisplayInfo(icon, title, description, null,
                frameType, this.showToast, this.announceToChat, this.hidden);
    }

    @Override
    public DisplayInfo.Builder from(final DisplayInfo value) {
        this.icon = value.getIcon();
        this.description = value.getDescription();
        this.advancementType = value.getType();
        this.announceToChat = value.doesAnnounceToChat();
        this.hidden = value.isHidden();
        this.showToast = value.doesShowToast();
        this.title = value.getTitle();
        return this;
    }

    @Override
    public DisplayInfo.Builder reset() {
        this.icon = null;
        this.description = Text.EMPTY;
        this.advancementType = AdvancementTypes.TASK;
        this.announceToChat = true;
        this.hidden = false;
        this.showToast = true;
        this.title = null;
        return this;
    }
}
