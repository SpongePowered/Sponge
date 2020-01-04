package org.spongepowered.common.registry.builtin;

import net.minecraft.util.text.TextFormatting;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.text.format.TextStyle;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.common.registry.SpongeCatalogRegistry;
import org.spongepowered.common.text.format.SpongeTextStyleType;

import java.util.Arrays;
import java.util.HashSet;

public final class TextStyleRegistry {

    private TextStyleRegistry() {
    }

    public static void generateRegistry(SpongeCatalogRegistry registry) {
        registry
            .registerMappedRegistry(TextStyle.Type.class, CatalogKey.minecraft("text_style"), () -> new HashSet<>(Arrays.asList(
                Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("bold"), TextFormatting.BOLD), TextFormatting.BOLD),
                Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("italic"), TextFormatting.ITALIC), TextFormatting.ITALIC),
                Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("obfuscated"), TextFormatting.OBFUSCATED), TextFormatting.OBFUSCATED),
                Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("reset"), TextFormatting.RESET), TextFormatting.RESET),
                Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("strikethrough"), TextFormatting.STRIKETHROUGH), TextFormatting.STRIKETHROUGH),
                Tuple.of(new SpongeTextStyleType(CatalogKey.minecraft("underline"), TextFormatting.UNDERLINE), TextFormatting.UNDERLINE)))
            , true)
        ;
    }
}
