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
package org.spongepowered.forge.applaunch.transformation;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerActivity;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.transformers.modlauncher.ListenerTransformerHelper;

import java.util.HashSet;
import java.util.Set;

public class ListenerTransformer implements ITransformer<ClassNode> {

    @NonNull
    @Override
    public ClassNode transform(final ClassNode input, final ITransformerVotingContext context) {
        ListenerTransformerHelper.transform(input);
        return input;
    }

    @NonNull
    @Override
    public TransformerVoteResult castVote(final ITransformerVotingContext context) {
        return context.getReason().equals(ITransformerActivity.CLASSLOADING_REASON) ? TransformerVoteResult.YES : TransformerVoteResult.NO;
    }

    @NonNull
    @Override
    public Set<Target> targets() {
        final Type listenerType = Type.getType(ListenerTransformerHelper.LISTENER_DESC);

        final Set<Type> listenerClasses = new HashSet<>();
        for (ModFileInfo fileInfo : LoadingModList.get().getModFiles()) {
            for (ModFileScanData.AnnotationData annotation : fileInfo.getFile().getScanResult().getAnnotations()) {
                if (listenerType.equals(annotation.annotationType())) {
                    listenerClasses.add(annotation.clazz());
                }
            }
        }

        final Set<Target> targets = new HashSet<>();
        for (Type listener : listenerClasses) {
            targets.add(Target.targetClass(listener.getInternalName()));
        }
        return targets;
    }
}
