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

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.ScoreTextComponent;
import net.minecraft.util.text.TextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scoreboard.Score;
import org.spongepowered.api.text.ScoreText;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(value = ScoreText.class, remap = false)
public abstract class ScoreTextMixin extends TextMixin {

    @Shadow @Final Score score;
    @Shadow @Final Optional<String> override;

    @Shadow public abstract Score getScore();

    @SuppressWarnings("deprecation")
    @Override
    protected TextComponent createComponent() {
        ScoreTextComponent textComponentScore;
        String name = TextSerializers.LEGACY_FORMATTING_CODE.serialize(this.score.getName());
        if (this.score.getObjectives().isEmpty()) {
            textComponentScore = new ScoreTextComponent(name, "");
        } else {
            textComponentScore = new ScoreTextComponent(name, this.score.getObjectives().iterator().next().getName());
            if (Sponge.isServerAvailable()) {
                textComponentScore.resolve((ICommandSender) Sponge.getServer());
            }
        }
        this.override.ifPresent(textComponentScore::setValue);
        return textComponentScore;
    }
}
