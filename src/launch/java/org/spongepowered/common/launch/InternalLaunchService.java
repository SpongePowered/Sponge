package org.spongepowered.common.launch;

import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public interface InternalLaunchService {

    void addJreExtensionsToClassPath();

    void registerSuperclassModification(final String targetClass, final String newSuperClass);

    Supplier<? extends IExitHandler> getExitHandler();

    Supplier<? extends Logger> getLaunchLogger();


}
