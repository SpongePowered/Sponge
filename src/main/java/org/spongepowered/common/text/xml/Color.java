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
package org.spongepowered.common.text.xml;

import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.common.Sponge;

import java.util.Optional;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso(Color.C.class)
@XmlRootElement
public class Color extends Element {

    @XmlAttribute
    private String name;

    @XmlAttribute
    protected String n;

    public Color() {
    }

    public Color(TextColor color) {
        this.name = color.getName();
    }


    @Override
    protected void modifyBuilder(TextBuilder builder) {
        if (this.name == null && this.n != null) {
            this.name = this.n;
        }

        if (this.name != null) {
            Optional<TextColor> color = Sponge.getGame().getRegistry().getType(TextColor.class, this.name.toUpperCase());
            if (color.isPresent()) {
                builder.color(color.get());
            }
        }
    }

    @XmlRootElement
    public static class C extends Color {
        public C() {
        }

        public C(TextColor color) {
            this.n = color.getName();
        }

    }
}
