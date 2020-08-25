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
package org.spongepowered.vanilla.applaunch.launcher.model;

import com.google.gson.annotations.SerializedName;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class VersionManifest {
    public Latest latest;
    public List<Version> versions;

    public static class Latest {
        public String snapshot;
        public String release;
    }

    public static class Version {
        public String id;
        public Type type;
        public URL url;
        @SuppressWarnings("UseOfObsoleteDateTimeApi")
        public Date time;
        @SuppressWarnings("UseOfObsoleteDateTimeApi")
        public Date releaseTime;

        @Override
        public boolean equals(final Object other) {
            if(this == other)  return true;
            if(other == null || this.getClass() != other.getClass()) return false;
            final Version that = (Version) other;
            return Objects.equals(this.id, that.id) && Objects.equals(this.type, that.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id, this.type);
        }

        @Override
        public String toString() {
            return this.type + ": " + this.id;
        }

        public enum Type {
            @SerializedName("old_alpha")
            OLD_ALPHA,
            @SerializedName("old_beta")
            OLD_BETA,
            @SerializedName("release")
            RELEASE,
            @SerializedName("snapshot")
            SNAPSHOT;
        }
    }
}
