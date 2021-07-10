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
package org.spongepowered.common.mixin.plugin;

import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.Level;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.util.PrettyPrinter;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.util.MissingImplementationException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CorePlugin extends AbstractMixinConfigPlugin {

    private static final Map<String, BiConsumer<ClassNode, IMixinInfo>> SUPERCLASS_TRANSFORMATIONS;
    private static final Map<String, Consumer<ClassNode>> INCOMPATIBILITY_DETECTION_ERRORS;

    static {
        final Map<String, BiConsumer<ClassNode, IMixinInfo>> transformers = new ConcurrentHashMap<>();
        SUPERCLASS_TRANSFORMATIONS = Collections.unmodifiableMap(transformers);
        INCOMPATIBILITY_DETECTION_ERRORS = new ConcurrentHashMap<>();
    }

    private static final List<String> MUTABLE_BLOCK_POS_FIELDS = ImmutableList.<String>builder()
        .add("x:field_177997_b")
        .add("y:field_177998_c")
        .add("z:field_177996_d")
        .build();

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) {
        final Consumer<ClassNode> classNodeConsumer = CorePlugin.INCOMPATIBILITY_DETECTION_ERRORS.get(mixinClassName);
        if (classNodeConsumer != null) {
            classNodeConsumer.accept(targetClass);
        }
        final BiConsumer<ClassNode, IMixinInfo> superTransformer = CorePlugin.SUPERCLASS_TRANSFORMATIONS.get(targetClassName);
        if (superTransformer != null) {
            superTransformer.accept(targetClass, mixinInfo);
        }
    }

    private static void printFoamFixAndShutDown() {
        new PrettyPrinter(60).add("!!! FoamFix Incompatibility !!!").centre().hr()
            .addWrapped("Hello! You are running SpongeForge and \"likely\" FoamFix on the same server, and we've discoverd"
                        + " a missing field that would otherwise cause some of Sponge not to work, because foamfix removes "
                        + "that field. As the issue stands, it's not possible to \"patch fix\", but we can suggest the "
                        + "configuration option change in foamfix's config to allow your game to start! Please change the "
                        + "following options in foamfix'es config.")
            .add()
            .add("In config/foamfix.cfg, change these values: ")
            .add("B:optimizedBlockPos=false")
            .add("B:patchChunkSerialization=false")
            .add()
            .addWrapped("We at Sponge appreciate your patience as this can be frustrating when the game doesn't start "
                        + "right away, or that SpongeForge isn't an easy drop-in-and-get-running sometimes. Thank you "
                        + "for your consideration, and have a nice day!")
            .add()
            .add(new IncompatibleClassChangeError("FoamFix Incompatibility Detected"))
            .log(SpongeCommon.logger(), Level.FATAL);
        throw new MissingImplementationException("SpongeCommon", "forceEarlyExit");
    }

}
