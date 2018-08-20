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
package org.spongepowered.common.registry.type.entity;

import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.data.type.SkinPart;
import org.spongepowered.api.data.type.SkinParts;
import org.spongepowered.api.registry.AlternateCatalogRegistryModule;
import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.common.data.type.SpongeSkinPart;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.text.translation.SpongeTranslation;

@RegisterCatalog(SkinParts.class)
public final class SkinPartRegistryModule extends AbstractCatalogRegistryModule<SkinPart> implements AlternateCatalogRegistryModule<SkinPart> {

    @Override
    public void registerDefaults() {
        register(CatalogKey.minecraft("hat"), this.createSkinPart(6, "hat"));
        register(CatalogKey.minecraft("cape"), this.createSkinPart(0, "cape"));
        register(CatalogKey.minecraft("jacket"), this.createSkinPart(1, "jacket"));
        register(CatalogKey.minecraft("left_sleeve"), this.createSkinPart(2, "left_sleeve"));
        register(CatalogKey.minecraft("right_sleeve"), this.createSkinPart(3, "right_sleeve"));
        register(CatalogKey.minecraft("left_pants_leg"), this.createSkinPart(4, "left_pants_leg"));
        register(CatalogKey.minecraft("right_pants_leg"), this.createSkinPart(5, "right_pants_leg"));
    }

    private SkinPart createSkinPart(int ordinal, String id) {
        return new SpongeSkinPart(ordinal, id, new SpongeTranslation("options.modelPart." + id));
    }

}
