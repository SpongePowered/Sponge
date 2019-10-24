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
package org.spongepowered.common.mixin.api.mcp.item;

import net.minecraft.item.ItemFishFood;
import org.spongepowered.api.data.type.CookedFish;
import org.spongepowered.api.data.type.Fish;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.text.translation.SpongeTranslation;

import java.util.Optional;

import javax.annotation.Nullable;

@Mixin(ItemFishFood.FishType.class)
public abstract class ItemFishFood_FishTypeMixin_API implements Fish {

    @Shadow @Final private String translationKey;
    @Shadow @Final private boolean cookable;

    @Nullable private Translation api$translation;

    @Override
    public String getId() {
        return "minecraft:raw." + this.translationKey;
    }

    @Override
    public String getName() {
        return this.translationKey;
    }

    @Override
    public Optional<CookedFish> getCookedFish() {
        if (this.cookable) {
            final Optional<CookedFish> optional = SpongeImpl.getRegistry().getType(CookedFish.class, "cooked." + this.translationKey);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    @Override
    public Translation getTranslation() {
        // Maybe move this to a @Inject at the end of the constructor
        if (this.api$translation == null) {
            this.api$translation = new SpongeTranslation("item.fish." + this.translationKey + ".raw.name");
        }
        return this.api$translation;
    }

}
