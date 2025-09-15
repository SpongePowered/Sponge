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
package org.spongepowered.transformers.modlauncher.jacoco;

import org.jacoco.core.internal.instr.CondyProbeArrayStrategy;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public record PrefixedCondyProbeArrayStrategy(
    String className, boolean isInterface, long classId, String prefix,
    IExecutionDataAccessorGenerator accessorGenerator
) implements IProbeArrayStrategy {

    @Override
    public int storeInstance(final MethodVisitor mv, final boolean clinit, final int variable) {
        final Handle bootstrapMethod = new Handle(Opcodes.H_INVOKESTATIC, this.className, this.prefix + InstrSupport.INITMETHOD_NAME, CondyProbeArrayStrategy.B_DESC, this.isInterface);
        mv.visitLdcInsn(new ConstantDynamic(this.prefix + InstrSupport.DATAFIELD_NAME, "Ljava/lang/Object;", bootstrapMethod));
        mv.visitTypeInsn(Opcodes.CHECKCAST, "[Z");
        mv.visitVarInsn(Opcodes.ASTORE, variable);
        return 1;
    }

    @Override
    public void addMembers(final ClassVisitor cv, final int probeCount) {
        if (probeCount == 0) {
            return;
        }

        final MethodVisitor mv = cv.visitMethod(InstrSupport.INITMETHOD_ACC, this.prefix + InstrSupport.INITMETHOD_NAME, CondyProbeArrayStrategy.B_DESC, null, null);
        final int maxStack = this.accessorGenerator.generateDataAccessor(this.classId, this.className, probeCount, mv);
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(maxStack, 3);
        mv.visitEnd();
    }
}
