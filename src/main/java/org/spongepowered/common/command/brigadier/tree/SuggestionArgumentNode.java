package org.spongepowered.common.command.brigadier.tree;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.command.ISuggestionProvider;

import java.util.Objects;

// Used to differentiate between custom suggestions
public final class SuggestionArgumentNode<T> extends ArgumentCommandNode<ISuggestionProvider, T> {

    public SuggestionArgumentNode(final RequiredArgumentBuilder<ISuggestionProvider, T> builder) {
        super(builder.getName(),
                builder.getType(),
                builder.getCommand(),
                builder.getRequirement(),
                builder.getRedirect(),
                builder.getRedirectModifier(),
                builder.isFork(),
                builder.getSuggestionsProvider());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getCustomSuggestions());
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof SuggestionArgumentNode && super.equals(o)) {
            return Objects.equals(this.getCustomSuggestions(), ((SuggestionArgumentNode<?>) o).getCustomSuggestions());
        }
        return false;
    }
}
