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

import org.jacoco.core.internal.data.CRC64;
import org.jacoco.core.internal.flow.ClassProbesAdapter;
import org.jacoco.core.internal.instr.ClassInstrumenter;
import org.jacoco.core.internal.instr.IProbeArrayStrategy;
import org.jacoco.core.internal.instr.InstrSupport;
import org.jacoco.core.runtime.IExecutionDataAccessorGenerator;
import org.jacoco.core.runtime.OfflineInstrumentationAccessGenerator;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class Instrumenter {
    // Incompatible with Mixin (ClassMetadataNotFoundException: java.lang.$JaCoCo)
    private static final IExecutionDataAccessorGenerator agentAccessGenerator = new AgentAccessGenerator();

    // Module must read org.jacoco.agent.rt
    private static final IExecutionDataAccessorGenerator offlineAccessGenerator = new OfflineInstrumentationAccessGenerator();

    public static byte[] instrument(final byte[] originalBytes, final boolean mixin) {
        final ClassReader reader = InstrSupport.classReaderFor(originalBytes);
        final int version = InstrSupport.getMajorVersion(reader);
        if (version < Opcodes.V11) {
            // not supported
            return originalBytes;
        }

        final long classId = CRC64.classId(originalBytes);
        final String prefix = mixin ? Long.toHexString(classId) : "";
        final String className = reader.getClassName();
        final boolean isInterfaceOrModule = (reader.getAccess() & (Opcodes.ACC_INTERFACE | Opcodes.ACC_MODULE)) != 0;

        final IProbeArrayStrategy strategy = new PrefixedCondyProbeArrayStrategy(className, isInterfaceOrModule, classId, prefix,
            mixin ? Instrumenter.offlineAccessGenerator : Instrumenter.agentAccessGenerator);

        final ClassWriter writer = new ClassWriter(reader, 0) {
            @Override
            protected String getCommonSuperClass(final String type1, final String type2) {
                throw new IllegalStateException();
            }
        };
        final ClassVisitor visitor = new ClassProbesAdapter(new ClassInstrumenter(strategy, writer), InstrSupport.needsFrames(version));
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }
}
