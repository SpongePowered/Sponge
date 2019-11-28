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
package org.spongepowered.common.mixin.core.api.text;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentBase;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.ClickAction;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.ShiftClickAction;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.bridge.util.text.ITextComponentBridge;
import org.spongepowered.common.bridge.api.text.TextBridge;
import org.spongepowered.common.text.action.SpongeClickAction;
import org.spongepowered.common.text.action.SpongeHoverAction;
import org.spongepowered.common.text.format.SpongeTextColor;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(value = Text.class, remap = false)
public abstract class TextMixin implements TextBridge {

    @Shadow @Final TextFormat format;
    @Shadow @Final ImmutableList<Text> children;
    @Shadow @Final Optional<ClickAction<?>> clickAction;
    @Shadow @Final Optional<HoverAction<?>> hoverAction;
    @Shadow @Final Optional<ShiftClickAction<?>> shiftClickAction;

    private ITextComponent component;
    private String json;

    protected TextComponentBase createComponent() {
        throw new UnsupportedOperationException();
    }

    private ITextComponent initializeComponent() {
        if (this.component == null) {
            this.component = createComponent();
            Style style = this.component.func_150256_b();

            if (this.format.getColor() != TextColors.NONE) {
                style.func_150238_a(((SpongeTextColor) this.format.getColor()).getHandle());
            }

            if (!this.format.getStyle().isEmpty()) {
                style.func_150227_a(this.format.getStyle().isBold().orElse(null));
                style.func_150217_b(this.format.getStyle().isItalic().orElse(null));
                style.func_150228_d(this.format.getStyle().hasUnderline().orElse(null));
                style.func_150225_c(this.format.getStyle().hasStrikethrough().orElse(null));
                style.func_150237_e(this.format.getStyle().isObfuscated().orElse(null));
            }

            if (this.clickAction.isPresent()) {
                style.func_150241_a(SpongeClickAction.getHandle(this.clickAction.get()));
            }

            if (this.hoverAction.isPresent()) {
                style.func_150209_a(SpongeHoverAction.getHandle(this.hoverAction.get()));
            }

            if (this.shiftClickAction.isPresent()) {
                ShiftClickAction.InsertText insertion = (ShiftClickAction.InsertText) this.shiftClickAction.get();
                style.func_179989_a(insertion.getResult());
            }

            for (Text child : this.children) {
                this.component.func_150257_a(((TextBridge) child).bridge$toComponent());
            }
        }

        return this.component;
    }

    private ITextComponent getHandle() {
        return initializeComponent();
    }

    @Override
    public ITextComponent bridge$toComponent() {
        return getHandle().func_150259_f(); // Mutable instances are not nice :(
    }

    /**
     * @author gabizou - June 30th, 2019 - 1.12.2
     * @reason After splitting mixins and providing bridges with proper prefixes,
     * we find that the previous legacy bridge was soft overwriting the API
     * method {@link Text#toPlain()}.... this now properly overwrites...
     *
     * @return The plain component text
     */
    @Overwrite
    public final String toPlain() {
        return ((ITextComponentBridge) getHandle()).bridge$toPlain();
    }

    /**
     * @author gabizou - June 30th, 2019 - 1.12.2
     * @reason After splitting mixins and providing bridges with proper prefixes,
     * we find that the previous legacy bridge was soft overwriting the API
     * method {@link Text#toPlainSingle()}.... this now properly overwrites...
     *
     * @return The plain unformatted component text
     */
    @Overwrite
    public final String toPlainSingle() {
        return getHandle().func_150261_e();
    }

    @Override
    public String bridge$toJson() {
        if (this.json == null) {
            this.json = ITextComponent.Serializer.func_150696_a(getHandle());
        }

        return this.json;
    }

    @Override
    public String bridge$toLegacy(char code) {
        return ((ITextComponentBridge) getHandle()).bridge$toLegacy(code);
    }
}
