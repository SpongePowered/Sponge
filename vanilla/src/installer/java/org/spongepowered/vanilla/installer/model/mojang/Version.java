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
package org.spongepowered.vanilla.installer.model.mojang;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Version {
    public AssetIndex assetIndex;
    public String assets;
    public Downloads downloads;
    public String id;
    public List<Library> libraries;
    public String mainClass;
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    public Date releaseTime;
    @SuppressWarnings("UseOfObsoleteDateTimeApi")
    public Date time;
    public VersionManifest.Version.Type type;

    public static class AssetIndex {
        public String id;
        public String sha1;
        public int size;
        public int totalSize;
        public URL url;
    }

    public static class Downloads {
        public Download client;
        public Download client_mappings;
        public Download server;
        public Download server_mappings;

        public static class Download {
            public String sha1;
            public int size;
            public URL url;
        }
    }

    public static class Library {
        public Downloads downloads;
        public String name;

        public static class Downloads {
            public Artifact artifact;
            public Map<String, Artifact> classifiers;

            public static class Artifact {
                public String path;
                public String sha1;
                public int size;
                public URL url;
            }
        }
    }
}
