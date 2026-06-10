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
package org.spongepowered.common.launch.plugin.loader;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.VersionRange;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public class VersionChecker {

    public static boolean check(final @Nullable VersionRange requestedVersion, final ArtifactVersion dependencyVersion) {
        if (requestedVersion == null || !requestedVersion.hasRestrictions()) {
            // we don't care which version
            return true;
        }
        // Maven Artifact version resolution has a bug(?) where VersionRange#containsVersion()
        // returns false if there are no restrictions when logically it should be true because
        // theoretically all versions are included. Except in our case, the recommended version
        // might be populated, yet no restrictions are in the VersionRange object, because we
        // want a specific version, which should be a restriction.
        //
        // Further, VersionRange#hasRestrictions() returns true even if VersionRange#getRestrictions()
        // is empty as it accounts for if there is a recommended version. Thus, we have to do the check
        // on the recommended version first... which might be null, hence the Objects.equals check.
        return Objects.equals(requestedVersion.getRecommendedVersion(), dependencyVersion) || requestedVersion.containsVersion(dependencyVersion);
    }
}
