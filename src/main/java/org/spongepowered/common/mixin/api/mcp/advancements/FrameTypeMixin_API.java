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
package org.spongepowered.common.mixin.api.mcp.advancements;

import net.minecraft.advancements.FrameType;
import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.advancement.AdvancementType;
import org.spongepowered.api.text.format.TextFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.text.format.SpongeTextColor;
import org.spongepowered.common.text.format.SpongeTextStyle;

import javax.annotation.Nullable;

@Implements(@Interface(iface = AdvancementType.class, prefix = "type$"))
@Mixin(FrameType.class)
public class FrameTypeMixin_API {

    @Shadow @Final private String name;
    @Shadow @Final private TextFormatting format;

    @Nullable private String api$id;
    @Nullable private String api$spongeName;
    @Nullable private TextFormat api$textFormat;

    public String type$getId() {
        if (this.api$id == null) {
            this.api$id = "minecraft:" + this.name;
        }
        return this.api$id;
    }

    @Intrinsic
    public String type$getName() {
        if (this.api$spongeName == null) {
            this.api$spongeName = Character.toUpperCase(this.name.charAt(0)) + this.name.substring(1);
        }
        return this.api$spongeName;
    }

    public TextFormat type$getTextFormat() {
        if (this.api$textFormat == null) {
            this.api$textFormat = TextFormat.of(
                    SpongeTextColor.of(this.format),
                    SpongeTextStyle.of(this.format));
        }
        return this.api$textFormat;
    }
}
