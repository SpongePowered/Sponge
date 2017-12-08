package org.spongepowered.common.advancement;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.api.advancement.Advancement;
import org.spongepowered.api.advancement.AdvancementTree;
import org.spongepowered.api.advancement.TreeLayout;
import org.spongepowered.api.advancement.TreeLayoutElement;

import java.util.Collection;
import java.util.Optional;

public final class SpongeTreeLayout implements TreeLayout {

    private final SpongeAdvancementTree tree;

    public SpongeTreeLayout(SpongeAdvancementTree tree) {
        this.tree = tree;
    }

    @Override
    public AdvancementTree getTree() {
        return this.tree;
    }

    @Override
    public Collection<TreeLayoutElement> getElements() {
        final ImmutableSet.Builder<TreeLayoutElement> elements = ImmutableSet.builder();
        collectElements(this.tree.getRootAdvancement(), elements);
        return elements.build();
    }

    private static void collectElements(Advancement advancement, ImmutableSet.Builder<TreeLayoutElement> elements) {
        advancement.getDisplayInfo().ifPresent(displayInfo -> elements.add((TreeLayoutElement) displayInfo));
        advancement.getChildren().forEach(child -> collectElements(child, elements));
    }
    @Override
    public Optional<TreeLayoutElement> getElement(Advancement advancement) {
        final Optional<AdvancementTree> tree = advancement.getTree();
        if (!tree.isPresent() || !advancement.getDisplayInfo().isPresent() || tree.get() != this.tree) {
            return Optional.empty();
        }
        return Optional.of((TreeLayoutElement) advancement.getDisplayInfo().get());
    }
}
