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
package org.spongepowered.common.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.spongepowered.asm.lib.ClassReader;
import org.spongepowered.asm.lib.ClassWriter;
import org.spongepowered.asm.lib.Opcodes;
import org.spongepowered.asm.lib.tree.AbstractInsnNode;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.lib.tree.MethodInsnNode;
import org.spongepowered.asm.lib.tree.MethodNode;
import org.spongepowered.asm.lib.util.CheckClassAdapter;

public class SpongeSuperclassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        String superclass = SpongeSuperclassRegistry.getSuperclass(name);
        if (superclass != null) {
            ClassNode node = this.readClass(basicClass);

            node.superName = superclass;
            node.methods.stream().filter(m -> m.name.equals("<init>")).forEach(m -> this.transformMethod(m, name, superclass));

            node.accept(new CheckClassAdapter(new ClassWriter(0)));

            ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            node.accept(writer);
            return writer.toByteArray();

        }
        return basicClass;
    }

    private void transformMethod(MethodNode node, String name, String superClass) {
        MethodInsnNode methodInsnNode = this.findSuper(node, name);
        methodInsnNode.owner = superClass;
    }

    private MethodInsnNode findSuper(MethodNode method, String name) {
        for (AbstractInsnNode node: method.instructions.toArray()) {
            if (node.getOpcode() == Opcodes.INVOKESPECIAL && ((MethodInsnNode) node).name.equals("<init>")) {
                return (MethodInsnNode) node;
            }
        }
        throw new IllegalStateException(String.format("Unable to find super() call in class '%s'", method, name));
    }

    private ClassNode readClass(byte[] basicClass) {
        ClassReader classReader = new ClassReader(basicClass);

        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }
}
