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

import com.google.common.collect.ImmutableList;
import org.spongepowered.api.text.TextBuilder;
import org.spongepowered.api.text.Texts;
import org.spongepowered.common.text.translation.SpongeTranslation;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Tr extends Element {

    @XmlAttribute(required = true)
    private String key;

    public Tr() {}

    public Tr(String key) {
        this.key = key;
    }

    @Override
    protected void modifyBuilder(TextBuilder builder) {
        // TODO: get rid of this
    }

    @Override
    public TextBuilder toText() throws Exception {
        ImmutableList.Builder<Object> build = ImmutableList.builder();
        for (Object child : this.mixedContent) {
            if (child instanceof String) {
                build.add(((String) child).replace('\u000B', ' '));
            } else if (child instanceof Element) {
                build.add(((Element) child).toText().build());
            } else {
                throw new IllegalArgumentException("What is this evenn? " + child);
            }
        }
        TextBuilder builder = Texts.builder(new SpongeTranslation(this.key), build.build());
        applyTextActions(builder);
        return builder;
    }
}
