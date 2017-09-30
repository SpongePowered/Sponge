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
package org.spongepowered.common.mixin.core.data.types;

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

@Mixin(ItemFishFood.FishType.class)
public abstract class MixinFishType implements Fish {

    @Shadow @Final private String unlocalizedName;
    @Final @Shadow private boolean cookable;

    private Translation translation;

    @Override
    public String getId() {
        return "minecraft:raw." + this.unlocalizedName;
    }

    @Override
    public String getName() {
        return this.unlocalizedName;
    }

    @Override
    public Optional<CookedFish> getCookedFish() {
        if (this.cookable) {
            final Optional<CookedFish> optional = SpongeImpl.getRegistry().getType(CookedFish.class, "cooked." + this.unlocalizedName);
            if (optional.isPresent()) {
                return optional;
            }
        }
        return Optional.empty();
    }

    @Override
    public Translation getTranslation() {
        // Maybe move this to a @Inject at the end of the constructor
        if (this.translation == null) {
            this.translation = new SpongeTranslation("item.fish." + this.unlocalizedName + ".raw.name");
        }
        return this.translation;
    }

}
