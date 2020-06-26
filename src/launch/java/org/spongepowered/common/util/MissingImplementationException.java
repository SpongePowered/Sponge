package org.spongepowered.common.util;

public class MissingImplementationException extends UnsupportedOperationException {

    public MissingImplementationException(String targetClass, String methodDesc) {
        super("An implementation of " + targetClass + " with method " + methodDesc + " has not been implemented");
    }
}
