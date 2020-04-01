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
package org.spongepowered.common.launch.transformer.tracker;

import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.IRETURN;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TrackerClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || TrackerRegistry.trackerClasses.contains(name)) {
            return basicClass;
        }

        final ClassReader classReader = new ClassReader(basicClass);
        final ClassWriter classWriter = new ClassWriter(classReader, 0);

        final TrackerClassVisitor classVisitor = new TrackerClassVisitor(classWriter);
        classReader.accept(classVisitor, 0);

        return classWriter.toByteArray();
    }

    private static class TrackerClassVisitor extends ClassVisitor {

        private final Map<String, TrackerMethodEntry> addedMethods = new HashMap<>();
        private String name;

        TrackerClassVisitor(ClassVisitor cv) {
            super(ASM5, cv);
        }

        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
            this.name = name;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new TrackerMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions), this);
        }

        @Override
        public void visitEnd() {
            for (TrackerMethodEntry e : this.addedMethods.values()) {
                final MethodVisitor m = super.visitMethod(ACC_PRIVATE | ACC_STATIC, e.nName, e.nDesc, null, null);
                m.visitCode();
                final Set<Map.Entry<TrackedType, MethodEntry.TargetTracker>> set = e.entry.entries.entrySet();
                for (Map.Entry<TrackedType, MethodEntry.TargetTracker> entry1 : set) {
                    final MethodEntry.TargetTracker target = entry1.getValue();
                    final String targetName = entry1.getKey().name;
                    // Do instance check
                    m.visitVarInsn(ALOAD, 0);
                    m.visitTypeInsn(INSTANCEOF, targetName);
                    final Label ifLabel = new Label();
                    m.visitJumpInsn(IFEQ, ifLabel);
                    // Call the static tracker method
                    m.visitVarInsn(ALOAD, 0);
                    // Cast the target object
                    m.visitTypeInsn(CHECKCAST, targetName);
                    for (int i = 0; i < e.entry.paramTypes.length; i++) {
                        m.visitVarInsn(e.entry.paramTypes[i].getOpcode(ILOAD), i + 1);
                    }
                    m.visitMethodInsn(INVOKESTATIC, target.type, e.oName, target.desc, false);
                    m.visitInsn(e.entry.returnType.getOpcode(IRETURN));
                    m.visitLabel(ifLabel);
                    m.visitFrame(F_SAME, 0, null, 0, null);
                }
                // None of the instance checks succeeded, call the original method
                m.visitVarInsn(ALOAD, 0);
                m.visitTypeInsn(CHECKCAST, e.oOwner);
                for (int i = 0; i < e.entry.paramTypes.length; i++) {
                    m.visitVarInsn(e.entry.paramTypes[i].getOpcode(ILOAD), i + 1);
                }
                m.visitMethodInsn(e.oOpcode, e.oOwner, e.oName, e.oDesc, e.oItf);
                m.visitInsn(e.entry.returnType.getOpcode(IRETURN));
                final int locals = e.entry.paramTypes.length + 1;
                m.visitMaxs(locals, locals);
                m.visitEnd();
            }
            super.visitEnd();
        }
    }

    private static class TrackerMethodEntry {

        private MethodEntry entry;

        private String oOwner;
        private String oName;
        private String oDesc;
        private int oOpcode;
        private boolean oItf;

        private String nName;
        private String nDesc;
    }

    private static class TrackerMethodVisitor extends MethodVisitor {

        private final TrackerClassVisitor classVisitor;

        TrackerMethodVisitor(MethodVisitor mv, TrackerClassVisitor classVisitor) {
            super(ASM5, mv);
            this.classVisitor = classVisitor;
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            MethodEntry entry;
            if ((opcode != INVOKEVIRTUAL && opcode != INVOKEINTERFACE) ||
                    (entry = TrackerRegistry.methodLists.get(owner + ';' + name + ';' + desc)) == null) {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
                return;
            }
            // First, try to directly redirect the method
            for (Map.Entry<TrackedType, MethodEntry.TargetTracker> entry1 : entry.entries.entrySet()) {
                if (entry1.getKey().knownSubtypes.contains(owner)) {
                    final MethodEntry.TargetTracker target = entry1.getValue();
                    super.visitMethodInsn(INVOKESTATIC, target.type, name, target.desc, false);
                    return;
                }
            }
            // Now try to generate a static helper method
            String simpleOwner = owner;
            final int index = simpleOwner.lastIndexOf('/');
            if (index != -1) {
                simpleOwner = simpleOwner.substring(index + 1);
            }
            final String methodName = "redirect" + simpleOwner + '$' + name;
            final String methodId = methodName + ';' + desc;
            final String methodDesc = "(Ljava/lang/Object;" + desc.substring(1);
            if (!this.classVisitor.addedMethods.containsKey(methodId)) {
                final TrackerMethodEntry methodEntry = new TrackerMethodEntry();
                methodEntry.entry = entry;
                methodEntry.oOpcode = opcode;
                methodEntry.oName = name;
                methodEntry.oDesc = desc;
                methodEntry.oOwner = owner;
                methodEntry.oItf = itf;
                methodEntry.nName = methodName;
                methodEntry.nDesc = methodDesc;
                this.classVisitor.addedMethods.put(methodId, methodEntry);
            }
            super.visitMethodInsn(INVOKESTATIC, this.classVisitor.name, methodName, methodDesc, false);
        }
    }
}
