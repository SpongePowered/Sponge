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
package org.spongepowered.common.adventure;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.minecraft.network.chat.Style;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class AdventureTextComponentTest {

    private static final net.minecraft.network.chat.Component GREETING =
        SpongeAdventure.asVanilla(Component.text("Hello ").append(Component.text("world", null, TextDecoration.BOLD)));

    @Test
    public void stringIsTheTextOfTheComponentAndItsChildren() {
        Assertions.assertEquals("Hello world", AdventureTextComponentTest.GREETING.getString());
    }

    @Test
    public void styledVisitSeesEachPartWithItsStyle() {
        final List<String> parts = new ArrayList<>();
        AdventureTextComponentTest.GREETING.visitWithoutResult((style, contents) -> {
            parts.add((style.isBold() ? "bold:" : "plain:") + contents);
            return Optional.empty();
        }, Style.EMPTY);
        Assertions.assertEquals(List.of("plain:Hello ", "bold:world"), parts);
    }
}
