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
package org.spongepowered.vanilla.applaunch.service;

import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.serviceapi.ILaunchPluginService;
import net.fabricmc.accesswidener.AccessWidener;
import net.fabricmc.accesswidener.AccessWidenerReader;
import net.fabricmc.accesswidener.AccessWidenerVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.vanilla.installer.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;

public class AccessWidenerLaunchService implements ILaunchPluginService {

    public static final String NAME = "access_widener";
    public static final String ACCESS_WIDENER_EXTENSION = "accesswidener";
    private static final Logger LOGGER = LogManager.getLogger();

    private final AccessWidener widener = new AccessWidener();
    private final AccessWidenerReader reader = new AccessWidenerReader(this.widener);

    @Override
    public String name() {
        return AccessWidenerLaunchService.NAME;
    }

    @Override
    public int processClassWithFlags(final Phase phase, final ClassNode classNode, final Type classType, final String reason) {
        if (!this.widener.getTargets().contains(classNode.name) || !reason.equals(ITransformerActivity.CLASSLOADING_REASON)) {
            return ComputeFlags.NO_REWRITE;
        }

        // TODO: This is a bit ugly, since AW only works on class visitors, rather than nodes
        final ClassNode temp = new ClassNode(Constants.ASM_VERSION);
        classNode.accept(temp);
        final ClassVisitor visitor = AccessWidenerVisitor.createClassVisitor(Constants.ASM_VERSION, classNode, this.widener);

        // clear out node
        classNode.visibleAnnotations = null;
        classNode.invisibleAnnotations = null;
        classNode.visibleTypeAnnotations = null;
        classNode.invisibleTypeAnnotations = null;
        classNode.attrs = null;
        classNode.nestMembers = null;
        classNode.permittedSubclasses = null;
        classNode.recordComponents = null;
        classNode.innerClasses.clear();
        classNode.fields.clear();
        classNode.methods.clear();
        classNode.interfaces.clear();
        temp.accept(visitor);

        return ComputeFlags.SIMPLE_REWRITE;
    }

    @Override
    public void offerResource(final Path resource, final String name) {
        if (resource.getFileName().toString().endsWith(AccessWidenerLaunchService.ACCESS_WIDENER_EXTENSION)) {
            try (final BufferedReader reader = Files.newBufferedReader(resource, StandardCharsets.UTF_8)) {
                this.reader.read(reader);
            } catch (final IOException ex) {
                LOGGER.error("Failed to load access widener {} from {}", name, resource, ex);
            }
        }
    }

    private static final EnumSet<Phase> MATCH = EnumSet.of(Phase.BEFORE);
    private static final EnumSet<Phase> FAIL = EnumSet.noneOf(Phase.class);

    @Override
    public EnumSet<Phase> handlesClass(final Type classType, final boolean isEmpty) {
        throw new UnsupportedOperationException("Outdated ModLauncher!");
    }

    @Override
    public EnumSet<Phase> handlesClass(final Type classType, final boolean isEmpty, final String reason) {
        if (reason.equals(ITransformerActivity.CLASSLOADING_REASON) && this.widener.getTargets().contains(classType.getClassName())) {
            return AccessWidenerLaunchService.MATCH;
        } else {
            return AccessWidenerLaunchService.FAIL;
        }
    }
}
