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
package org.spongepowered.common.data.type;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.ItemFishFood;
import org.spongepowered.api.data.type.CookedFish;
import org.spongepowered.api.data.type.Fish;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextRepresentable;
import org.spongepowered.api.text.translation.Translation;
import org.spongepowered.common.text.SpongeTexts;

public class SpongeCookedFish implements CookedFish, TextRepresentable {

    private final String id;
    private final String name;
    private final Translation translation;
    public final ItemFishFood.FishType fish;

    public SpongeCookedFish(String id, String name, Translation translation, ItemFishFood.FishType fish) {
        this.id = checkNotNull(id);
        this.name = checkNotNull(name);
        this.translation = checkNotNull(translation, "translation");
        this.fish = checkNotNull(fish);
    }

    @Override
    public String getId() {
        return "cooked." + this.id;
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
    public Fish getRawFish() {
        return (Fish) (Object) this.fish;
    }

    @Override
    public Text toText() {
        return SpongeTexts.toText(this);
    }

}
