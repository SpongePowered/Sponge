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
package org.spongepowered.common.launch.transformer;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.util.ArrayList;
import java.util.List;

public class SpongeSuperclassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        String superclass = SpongeSuperclassRegistry.getSuperclass(name);
        if (superclass != null) {
            ClassNode node = this.readClass(basicClass);

            node.methods.stream().forEach(m -> this.transformMethod(m, name, node.superName, superclass));
            node.superName = superclass;

            node.accept(new CheckClassAdapter(new ClassWriter(0)));

            ClassWriter writer = new ClassWriter(0);
            node.accept(writer);
            return writer.toByteArray();

        }
        return basicClass;
    }

    private void transformMethod(MethodNode node, String name, String originalSuperclass, String superClass) {
        for (MethodInsnNode insn: this.findSuper(node, originalSuperclass, name)) {
            insn.owner = superClass;
        }
    }

    private List<MethodInsnNode> findSuper(MethodNode method, String originalSuperClass, String name) {
        List<MethodInsnNode> nodes = new ArrayList<>();
        for (AbstractInsnNode node: method.instructions.toArray()) {
            if (node.getOpcode() == Opcodes.INVOKESPECIAL && originalSuperClass.equals(((MethodInsnNode) node).owner)) {
                nodes.add((MethodInsnNode) node);
            }
        }
        return nodes;
    }

    private ClassNode readClass(byte[] basicClass) {
        ClassReader classReader = new ClassReader(basicClass);

        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);
        return classNode;
    }
}
