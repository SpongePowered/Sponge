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
package org.spongepowered.common.datapack.recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import org.spongepowered.api.item.recipe.RecipeRegistration;
import org.spongepowered.common.datapack.DataPackDecoder;
import org.spongepowered.common.datapack.DataPackEncoder;
import org.spongepowered.common.datapack.JsonDataPackSerializer;
import org.spongepowered.common.datapack.SpongeDataPack;
import org.spongepowered.common.item.recipe.SpongeRecipeRegistration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class RecipeDataPackSerializer extends JsonDataPackSerializer<RecipeRegistration> {

    public RecipeDataPackSerializer(final DataPackEncoder<JsonElement, RecipeRegistration> encoder,
            final DataPackDecoder<JsonElement, RecipeRegistration> decoder) {
        super(encoder, decoder);
    }

    @Override
    protected void serializeAdditional(
            final SpongeDataPack<JsonElement, RecipeRegistration> type, final Path packDir,
            final RecipeRegistration entry) throws IOException {
        if (entry instanceof SpongeRecipeRegistration<?> spongeReg) {
            final var serialized = Advancement.CODEC.encodeStart(JsonOps.INSTANCE, spongeReg.advancement().value());

            final Path file = packDir.resolve("data")
                    .resolve(entry.key().namespace())
                    .resolve("advancement")
                    .resolve(entry.key().value() + ".json");
            Files.createDirectories(file.getParent());

            JsonDataPackSerializer.writeFile(file, serialized.result().get());
        }
    }
}
