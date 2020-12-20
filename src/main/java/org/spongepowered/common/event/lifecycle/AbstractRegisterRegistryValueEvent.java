package org.spongepowered.common.event.lifecycle;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.api.Engine;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.lifecycle.RegisterRegistryValueEvent;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryType;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractRegisterRegistryValueEvent extends AbstractLifecycleEvent implements RegisterRegistryValueEvent {

    public AbstractRegisterRegistryValueEvent(final Cause cause, final Game game) {
        super(cause, game);
    }

    @Override
    public <T> RegistryStep<T> registry(final RegistryType<T> registryType) {
        final Optional<Registry<T>> optRegistry = this.getHolder().findRegistry(Objects.requireNonNull(registryType, "registryType"));
        if (!optRegistry.isPresent()) {
            throw new IllegalArgumentException(String.format("RegistryType '%s' has no registered registry!", registryType));
        }
        final Registry<T> registry = optRegistry.get();
        if (!registry.isDynamic()) {
            throw new IllegalStateException(String.format("RegistrtType '%s' is read only and cannot accept new registry values!", registryType));
        }
        return new RegistryStepImpl<>(registry);
    }

    protected abstract RegistryHolder getHolder();

    public static final class RegistryStepImpl<T> implements RegistryStep<T> {

        private final Registry<T> registry;

        public RegistryStepImpl(final Registry<T> registry) {
            this.registry = registry;
        }

        @Override
        public RegistryStep<T> register(final ResourceKey key, final T value) {
            this.registry.register(key, value);
            return this;
        }
    }
    public static final class GameScopedImpl extends AbstractRegisterRegistryValueEvent implements RegisterRegistryValueEvent.GameScoped {

        public GameScopedImpl(final Cause cause, final Game game) {
            super(cause, game);
        }

        @Override
        protected RegistryHolder getHolder() {
            return this.game.registries();
        }
    }

    public static final class EngineScopedImpl<E extends Engine> extends AbstractRegisterRegistryValueEvent implements RegisterRegistryValueEvent.EngineScoped<E> {

        private final TypeToken<E> token;
        private final E engine;

        public EngineScopedImpl(final Cause cause, final Game game, final TypeToken<E> token, final E engine) {
            super(cause, game);
            this.token = token;
            this.engine = engine;
        }

        @Override
        public TypeToken<E> getParamType() {
            return this.token;
        }

        @Override
        public E getEngine() {
            return this.engine;
        }

        @Override
        protected RegistryHolder getHolder() {
            return this.engine.registries();
        }
    }

    public static final class WorldScopedImpl extends AbstractRegisterRegistryValueEvent implements RegisterRegistryValueEvent.WorldScoped {

        private final ResourceKey worldKey;

        public WorldScopedImpl(final Cause cause, final Game game, final ResourceKey worldKey) {
            super(cause, game);
            this.worldKey = worldKey;
        }

        @Override
        public ResourceKey getWorldKey() {
            return this.worldKey;
        }

        @Override
        protected RegistryHolder getHolder() {
            return Sponge.getServer().getWorldManager().getWorld(this.worldKey).orElse(null).registries();
        }
    }
}
