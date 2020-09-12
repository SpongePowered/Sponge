package org.spongepowered.common.event.resource;

import com.google.common.reflect.TypeToken;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.resources.data.PackMetadataSection;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.resource.ResourceManager;
import org.spongepowered.api.resource.ResourceReloadListener;
import org.spongepowered.api.resource.pack.PackDiscoverer;
import org.spongepowered.api.resource.pack.PackInfo;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ResourceEventFactory {

    public static <E extends Engine> void registerPackDiscoverers(TypeToken<E> type, E engine, ResourcePackList<?> packList) {
        try (CauseStackManager.StackFrame frame = engine.getCauseStackManager().pushCauseFrame()) {
            RegisterPackFinderEventImpl<E> event = new RegisterPackFinderEventImpl<>(frame.getCurrentCause(), type);
            Sponge.getEventManager().post(event);

            for (PackDiscoverer discoverer : event.getInternalList()) {
                packList.addPackFinder(new DiscovererFinderBridge(discoverer));
            }
        }
    }

    private static class DiscovererFinderBridge implements IPackFinder {

        final PackDiscoverer discoverer;

        DiscovererFinderBridge(PackDiscoverer discoverer) {
            this.discoverer = discoverer;
        }

        @Override
        public <T extends ResourcePackInfo> void addPackInfosToMap(Map<String, T> nameToPackMap, ResourcePackInfo.IFactory<T> packInfoFactory) {
            discoverer.populate((Map) nameToPackMap, (name, forced, pack, priority) -> {
                IResourcePack vanillaPack = (IResourcePack) pack.get();
                PackMetadataSection packMetadata;
                try {
                    packMetadata = vanillaPack.getMetadata(PackMetadataSection.SERIALIZER);
                }catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                ResourcePackInfo.Priority vanillaPriority = toVanillaPriority(priority);
                return Optional.ofNullable((PackInfo) packInfoFactory.create(name, forced, (Supplier) pack, vanillaPack, packMetadata, vanillaPriority));
            });
        }

        private static ResourcePackInfo.Priority toVanillaPriority(PackInfo.Priority priority) {
            switch (priority) {
                case FIRST:
                    return ResourcePackInfo.Priority.TOP;
                case LAST:
                    return ResourcePackInfo.Priority.BOTTOM;
            }
            throw new NullPointerException();
        }
    }

    public static <E extends Engine> void registerResourceReloadListeners(TypeToken<E> type, E engine, IReloadableResourceManager manager) {
        try (CauseStackManager.StackFrame frame = engine.getCauseStackManager().pushCauseFrame()) {
            RegisterResourcePackReloadListenerImpl<E> event = new RegisterResourcePackReloadListenerImpl<>(frame.getCurrentCause(), type);
            Sponge.getEventManager().post(event);

            for (ResourceReloadListener listener : event.getInternalList()) {
                manager.addReloadListener(new SpongeResourceListenerToVanilla(listener));
            }
        }
    }

    private static class SpongeResourceListenerToVanilla implements IFutureReloadListener {

        ResourceReloadListener listener;

        SpongeResourceListenerToVanilla(ResourceReloadListener listener) {
            this.listener = listener;
        }

        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
            return listener.onReload((ResourceReloadListener.AsyncStage) stage,
                    (ResourceManager) resourceManager,
                    backgroundExecutor, gameExecutor);
        }

        @Override
        public String func_225594_i_() {
            return listener.getClass().getSimpleName();
        }
    }
}
