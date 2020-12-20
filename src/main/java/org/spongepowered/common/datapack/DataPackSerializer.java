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
package org.spongepowered.common.datapack;

import com.google.gson.JsonObject;
import net.minecraft.util.SharedConstants;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DataPackSerializer<T extends DataPackSerializedObject> {

    private final String token;
    private final String typeDirectoryName;

    public DataPackSerializer(final String token, final String typeDirectoryName) {
        this.token = token;
        this.typeDirectoryName = typeDirectoryName;
    }

    protected void serialize(final Path dataPackDirectory, final List<T> objects) throws IOException {
        FileUtils.deleteDirectory(dataPackDirectory.toFile());

        // Write our objects
        for (final T object : objects) {
            final Path namespacedDataDirectory = dataPackDirectory.resolve("data").resolve(object.getKey().getNamespace());
            final Path objectFile = namespacedDataDirectory.resolve(this.typeDirectoryName).resolve(object.getKey().getValue() + ".json");
            Files.createDirectories(objectFile.getParent());

            this.writeFile(objectFile, object.getObject());

            this.serializeAdditional(namespacedDataDirectory, object);
        }

        // Write our pack metadata
        final Path packMeta = dataPackDirectory.resolve("pack.mcmeta");
        final JsonObject packDataRoot = new JsonObject();
        final JsonObject packData = new JsonObject();
        packDataRoot.add("pack", packData);
        packData.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion());
        packData.addProperty("description", "Sponge plugin provided " + this.token);

        this.writeFile(packMeta, packDataRoot);
    }

    protected void serializeAdditional(Path dataDirectory, T object) throws IOException {
    }

    protected final void writeFile(final Path file, final JsonObject object) throws IOException {
        try (BufferedWriter bufferedwriter = Files.newBufferedWriter(file)) {
            bufferedwriter.write(object.toString());
        }
    }
}
