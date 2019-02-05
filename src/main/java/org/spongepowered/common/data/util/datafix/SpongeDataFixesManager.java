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
package org.spongepowered.common.data.util.datafix;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import org.spongepowered.common.SpongeImpl;
import org.spongepowered.common.data.util.datafix.fixes.SpongeEntityCreatorFix;
import org.spongepowered.common.data.util.datafix.fixes.SpongeEntityNotifierFix;
import org.spongepowered.common.data.util.datafix.fixes.SpongePlayerIdTableFix;
import org.spongepowered.common.data.util.datafix.fixes.SpongeWorldUUIDFix;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SpongeDataFixesManager {

    private static final DataFixer DATA_FIXER = createFixer();

    private static DataFixer createFixer() {
        ExecutorService service =
            Executors.newCachedThreadPool((new ThreadFactoryBuilder()).setUncaughtExceptionHandler((thread, throwable) -> {
            SpongeImpl.getLogger().error("Unable to build datafixers", throwable);
            Runtime.getRuntime().exit(1);
        }).setDaemon(true).setNameFormat("Sponge Bootstrap %d").build());

        final DataFixerBuilder builder = new DataFixerBuilder(1519);
        addFixers(builder);

        return builder.build(service);
    }

    public static DataFixer getDataFixer() {
        return DATA_FIXER;
    }

    private static void addFixers(DataFixerBuilder builder) {
        // Each fix needs to be a new schema and version!
        final Schema v0001 = builder.addSchema(1, Schema::new);
        builder.addFixer(new SpongeWorldUUIDFix(v0001, true));
        builder.addFixer(new SpongePlayerIdTableFix(v0001, true));
        builder.addFixer(new SpongeEntityCreatorFix(v0001, true));
        builder.addFixer(new SpongeEntityNotifierFix(v0001, true));
    }
}
