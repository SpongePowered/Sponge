package org.spongepowered.common.mixin.core.profiler;

import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Collections;
import java.util.List;

@Mixin(Profiler.class)
public class MixinProfiler {

    /**
     * @author gabizou - January 22nd, 2017
     * @reason Clears out the profiler entirely. Since it's used as a debugging tool
     * for Mojang, it serves no purpose for production servers and clients.
     */
    @Overwrite
    public void clearProfiling() {
    }

    /**
     * @author gabizou - January 22nd, 2017
     * @reason Clears out the profiler entirely. Since it's used as a debugging tool
     * for Mojang, it serves no purpose for production servers and clients.
     */
    @Overwrite
    public void startSection(String name) {
    }

    /**
     * @author gabizou - January 22nd, 2017
     * @reason Clears out the profiler entirely. Since it's used as a debugging tool
     * for Mojang, it serves no purpose for production servers and clients.
     */
    @Overwrite
    public void endSection() {
    }

    /**
     * @author gabizou - January 22nd, 2017
     * @reason Clears out the profiler entirely. Since it's used as a debugging tool
     * for Mojang, it serves no purpose for production servers and clients.
     */
    @Overwrite
    public List<Profiler.Result> getProfilingData(String profilerName) {
        return Collections.emptyList();
    }

    /**
     * @author gabizou - January 22nd, 2017
     * @reason Clears out the profiler entirely. Since it's used as a debugging tool
     * for Mojang, it serves no purpose for production servers and clients.
     */
    @Overwrite
    public void endStartSection(String name) {
    }

    /**
     * @author gabizou - January 22nd, 2017
     * @reason Clears out the profiler entirely. Since it's used as a debugging tool
     * for Mojang, it serves no purpose for production servers and clients.
     */
    @Overwrite
    public String getNameOfLastSection() {
        return "[UNKNOWN]";
    }

}
