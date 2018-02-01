package org.spongepowered.common.data.generator.method;

import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.spongepowered.common.data.generator.KeyEntry;

import java.lang.reflect.Method;

public class UnboxedOptionalGetterMethodEntry extends MethodEntry {

    public UnboxedOptionalGetterMethodEntry(Method method, KeyEntry keyEntry) {
        super(method, keyEntry);
    }

    @Override
    void preVisit(MethodVisitor mv, String implClassDescriptor, String mutableImplClassName) {
        // Add the nullable annotation, is forced to be present in the interfaces
        mv.visitAnnotation("Ljavax/annotation/Nullable;", true).visitEnd();
    }

    @Override
    void visit(MethodVisitor mv, String implClassDescriptor, String mutableImplClassName) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, implClassDescriptor, this.keyEntry.valueFieldName, "Ljava/util/Optional;");
        mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Optional", "orElse", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, Type.getInternalName(this.method.getReturnType()));
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 1);
    }
}
