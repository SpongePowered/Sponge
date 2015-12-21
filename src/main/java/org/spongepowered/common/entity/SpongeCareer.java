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
package org.spongepowered.common.entity;

import com.google.common.base.Objects;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Profession;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.api.util.annotation.NonnullByDefault;
import org.spongepowered.common.text.SpongeTexts;

@NonnullByDefault
public class SpongeCareer extends SpongeEntityMeta implements Career, TextRepresentable {

    private final Profession profession;
    private final Translation translation;

    public SpongeCareer(int id, String name, Profession profession, Translation translation) {
        super(id, name);
        this.profession = profession;
        this.translation = translation;
    }

    @Override
    public Profession getProfession() {
        return this.profession;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Translation getTranslation() {
        return this.translation;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("profession", this.profession)
                .add("type", this.type)
                .add("name", this.name)
                .toString();
    }

    @Override
    public Text toText() {
        return SpongeTexts.toText(this);
    }

}
