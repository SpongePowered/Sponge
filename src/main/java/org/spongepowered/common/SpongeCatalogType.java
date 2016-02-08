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

package org.spongepowered.common;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import org.spongepowered.api.CatalogType;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.Text.Builder;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.text.translation.Translation;

public abstract class SpongeCatalogType implements CatalogType, TextRepresentable {

    private final String id;

    public SpongeCatalogType(String id) {
        this.id = checkNotNull(id, "id");
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public final int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CatalogType other = (CatalogType) obj;
        return getId().equals(other.getId());
    }

    @Override
    public final String toString() {
        return toStringHelper().toString();
    }

    protected ToStringHelper toStringHelper() {
        return Objects.toStringHelper(this)
                .add("id", getId())
                .add("name", getName());
    }

    protected Text.Builder toTextBuilder() {
        return Text.builder(getName());
    }

    protected Text.Builder toTextHoverBuilder() {
        return Text.builder()
                .append(Text.of("Id: " + getId()), Text.NEW_LINE)
                .append(Text.of("Name: " + getName()));
    }

    @Override
    public Text toText() {
        final Text.Builder builder = toTextBuilder();
        builder.color(TextColors.AQUA);
        builder.style(TextStyles.BOLD);
        builder.onHover(TextActions.showText(toTextHoverBuilder().build()));
        builder.onShiftClick(TextActions.insertText(getId()));
        return builder.build();
    }

    public static abstract class Translatable extends SpongeCatalogType implements org.spongepowered.api.text.translation.Translatable {

        private final Translation translation;

        public Translatable(String id, Translation translation) {
            super(id);
            this.translation = checkNotNull(translation, "translation");
        }

        @Override
        public String getName() {
            return getTranslation().get();
        }

        @Override
        public final Translation getTranslation() {
            return this.translation;
        }

        @Override
        protected ToStringHelper toStringHelper() {
            return super.toStringHelper()
                    .add("translation", getTranslation());
        }

        @Override
        protected Builder toTextBuilder() {
            return Text.builder(this);
        }

        @Override
        protected Builder toTextHoverBuilder() {
            return super.toTextHoverBuilder().append(Text.NEW_LINE)
                    .append(Text.of("TranslationId: " + getTranslation().getId()));
        }

    }

}
