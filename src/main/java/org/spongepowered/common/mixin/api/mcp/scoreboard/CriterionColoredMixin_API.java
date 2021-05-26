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
package org.spongepowered.common.mixin.api.mcp.scoreboard;

import com.google.common.base.CaseFormat;
import net.minecraft.scoreboard.ScoreCriteriaColored;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.scoreboard.critieria.Criterion;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.registry.type.text.TextColorRegistryModule;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(ScoreCriteriaColored.class)
@Implements(@Interface(iface = Criterion.class, prefix = "criterion$"))
public abstract class CriterionColoredMixin_API implements Criterion {

    @Nullable
    private String api$spongeId;
    @Nullable
    private TextFormatting api$format;

    @Shadow public abstract String shadow$getName();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void api$onConstructed(final String name, final TextFormatting format, final CallbackInfo ci) {
        this.api$format = format;
    }

    @Intrinsic
    public String criterion$getName() {
        return this.shadow$getName();
    }

    public String criterion$getId() {
        if (this.api$spongeId == null) {
            this.api$spongeId = "minecraft:" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, this.getName().replace("count", "s"));
        }
        return this.api$spongeId;
    }

    public Optional<TextColor> criterion$getTeamColor() {
        return Optional.ofNullable(TextColorRegistryModule.enumChatColor.get(this.api$format));
    }
}
