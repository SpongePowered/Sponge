package org.spongepowered.common.launch;

import net.minecraft.launchwrapper.Launch;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.launch.transformer.SpongeSuperclassRegistry;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Supplier;

public class SpongeLaunchWrapperLaunchService implements InternalLaunchService {

    private final Logger logger;

    public SpongeLaunchWrapperLaunchService(final Logger logger) {
        this.logger = logger;
    }


    @Override
    public void addJreExtensionsToClassPath() {
        // Make sure JRE extensions are loaded using the system class loader
        Launch.classLoader.addClassLoaderExclusion("jdk.");

        /*
         * By default Launchwrapper inherits the class path from the system class loader.
         * However, JRE extensions (e.g. Nashorn in the jre/lib/ext directory) are not part
         * of the class path of the system class loader.
         * Instead, they're loaded using a parent class loader (Launcher.ExtClassLoader).
         * Currently, Launchwrapper does not fall back to the parent class loader if it's
         * unable to find a class on its class path. To make the JRE extensions usable for
         * plugins we manually add the URLs from the ExtClassLoader to Launchwrapper's
         * class path.
         */
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        if (classLoader == null) {
            return;
        }

        classLoader = classLoader.getParent(); // Launcher.ExtClassLoader
        if (classLoader instanceof URLClassLoader) {
            for (final URL url : ((URLClassLoader) classLoader).getURLs()) {
                Launch.classLoader.addURL(url);
            }
        }
    }

    @Override
    public Supplier<? extends IExitHandler> getExitHandler() {
        return TerminateVM::new;
    }

    @Override
    public Supplier<? extends Logger> getLaunchLogger() {
        return () -> this.logger;
    }

    @Override
    public void registerSuperclassModification(final String targetClass, final String newSuperClass) {
        SpongeSuperclassRegistry.registerSuperclassModification(targetClass, newSuperClass);
    }


}
