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
package org.spongepowered.common.world.server;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.world.server.WorldArchetypeType;
import org.spongepowered.api.world.server.storage.ServerWorldProperties;

import java.util.Optional;
import java.util.function.Consumer;

public record SpongeServerWorldPropertiesLoadOptions(
    @Nullable GetOperation getOperationValue,
    @Nullable LoadOperation loadOperationValue,
    @Nullable CreateOperation createOperationValue) implements ServerWorldProperties.LoadOptions {

    @Override
    public Optional<GetOperation> getOperation() {
        return Optional.ofNullable(this.getOperationValue);
    }

    @Override
    public Optional<LoadOperation> loadOperation() {
        return Optional.ofNullable(this.loadOperationValue);
    }

    @Override
    public Optional<CreateOperation> createOperation() {
        return Optional.ofNullable(this.createOperationValue);
    }

    public record GetOperationImpl(@Nullable Consumer<ServerWorldProperties> callback) implements GetOperation {

        @Override
        public Optional<Consumer<ServerWorldProperties>> getCallback() {
            return Optional.ofNullable(this.callback);
        }
    }

    public record LoadOperationImpl(@Nullable Consumer<ServerWorldProperties> callback) implements LoadOperation {

        @Override
        public Optional<Consumer<ServerWorldProperties>> loadCallback() {
            return Optional.ofNullable(this.callback);
        }
    }

    public record CreateOperationImpl(WorldArchetypeType worldArchetype, @Nullable Consumer<ServerWorldProperties> callback) implements CreateOperation {

        @Override
        public WorldArchetypeType worldArchetype() {
            return this.worldArchetype;
        }

        @Override
        public Optional<Consumer<ServerWorldProperties>> createCallback() {
            return Optional.ofNullable(this.callback);
        }
    }

    public static final class BuilderImpl implements ServerWorldProperties.LoadOptions.Builder.GetStep,
        ServerWorldProperties.LoadOptions.Builder.LoadStep, ServerWorldProperties.LoadOptions.Builder.CreateStep {

        private @Nullable GetOperation getOperation;
        private @Nullable LoadOperation loadOperation;
        private @Nullable CreateOperation createOperation;

        @Override
        public GetStep get() {
            this.getOperation = new GetOperationImpl(null);
            return this;
        }

        @Override
        public LoadStep load() {
            this.loadOperation = new LoadOperationImpl(null);
            return this;
        }

        @Override
        public CreateStep create(final WorldArchetypeType worldArchetype) {
            this.createOperation = new CreateOperationImpl(worldArchetype, null);
            return this;
        }

        @Override
        public GetStep getCallback(final Consumer<ServerWorldProperties> getCallback) {
            this.getOperation = new GetOperationImpl(getCallback);
            return this;
        }

        @Override
        public LoadStep loadCallback(final Consumer<ServerWorldProperties> loadCallback) {
            this.loadOperation = new LoadOperationImpl(loadCallback);
            return this;
        }

        @Override
        public CreateStep createCallback(final Consumer<ServerWorldProperties> initializeCallback) {
            this.createOperation = new CreateOperationImpl(this.createOperation.worldArchetype(), initializeCallback);
            return this;
        }

        @Override
        public Builder reset() {
            this.getOperation = null;
            this.loadOperation = null;
            this.createOperation = null;
            return this;
        }

        @Override
        public ServerWorldProperties.LoadOptions build() {
            return new SpongeServerWorldPropertiesLoadOptions(this.getOperation, this.loadOperation, this.createOperation);
        }
    }
}
