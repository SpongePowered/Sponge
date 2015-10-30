package org.spongepowered.common.registry.factory;

import org.spongepowered.api.text.sink.MessageSinkFactory;
import org.spongepowered.api.text.sink.MessageSinks;
import org.spongepowered.common.registry.FactoryRegistry;
import org.spongepowered.common.text.sink.SpongeMessageSinkFactory;

public class MessageSinkFactoryModule implements FactoryRegistry<MessageSinkFactory, MessageSinks> {

    @Override
    public Class<MessageSinks> getFactoryOwner() {
        return MessageSinks.class;
    }

    @Override
    public MessageSinkFactory initialize() {
        return SpongeMessageSinkFactory.INSTANCE;
    }
}
