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
package org.spongepowered.common.gui.window;

import org.spongepowered.api.gui.window.AnvilWindow;
import org.spongepowered.api.gui.window.BeaconWindow;
import org.spongepowered.api.gui.window.BookWindow;
import org.spongepowered.api.gui.window.BrewingStandWindow;
import org.spongepowered.api.gui.window.ChestWindow;
import org.spongepowered.api.gui.window.CommandBlockWindow;
import org.spongepowered.api.gui.window.CraftingTableWindow;
import org.spongepowered.api.gui.window.DemoWindow;
import org.spongepowered.api.gui.window.DispenserWindow;
import org.spongepowered.api.gui.window.DropperWindow;
import org.spongepowered.api.gui.window.EnchantmentTableWindow;
import org.spongepowered.api.gui.window.FurnaceWindow;
import org.spongepowered.api.gui.window.HopperWindow;
import org.spongepowered.api.gui.window.HorseInventoryWindow;
import org.spongepowered.api.gui.window.PlayerInventoryWindow;
import org.spongepowered.api.gui.window.SignWindow;
import org.spongepowered.api.gui.window.SleepingWindow;
import org.spongepowered.api.gui.window.VillagerTradeWindow;
import org.spongepowered.api.gui.window.WinGameWindow;
import org.spongepowered.api.gui.window.Window;
import org.spongepowered.common.registry.SpongeGameRegistry;

public abstract class SpongeWindowBuilder<W extends Window, B extends Window.Builder<W, B>> implements Window.Builder<W, B> {

    public static void registerBuilders(SpongeGameRegistry registry) {
        registry.registerBuilderSupplier(AnvilWindow.Builder.class, SpongeAnvilWindow.Builder::new);
        registry.registerBuilderSupplier(BeaconWindow.Builder.class, SpongeBeaconWindow.Builder::new);
        registry.registerBuilderSupplier(BookWindow.Builder.class, SpongeBookWindow.Builder::new);
        registry.registerBuilderSupplier(BrewingStandWindow.Builder.class, SpongeBrewingStandWindow.Builder::new);
        registry.registerBuilderSupplier(ChestWindow.Builder.class, SpongeChestWindow.Builder::new);
        registry.registerBuilderSupplier(CommandBlockWindow.Builder.class, SpongeCommandBlockWindow.Builder::new);
        registry.registerBuilderSupplier(CraftingTableWindow.Builder.class, SpongeCraftingTableWindow.Builder::new);
        registry.registerBuilderSupplier(DemoWindow.Builder.class, SpongeDemoWindow.Builder::new);
        registry.registerBuilderSupplier(DispenserWindow.Builder.class, SpongeDispenserWindow.Builder::new);
        registry.registerBuilderSupplier(DropperWindow.Builder.class, SpongeDropperWindow.Builder::new);
        registry.registerBuilderSupplier(EnchantmentTableWindow.Builder.class, SpongeEnchantmentTableWindow.Builder::new);
        registry.registerBuilderSupplier(FurnaceWindow.Builder.class, SpongeFurnaceWindow.Builder::new);
        registry.registerBuilderSupplier(HopperWindow.Builder.class, SpongeHopperWindow.Builder::new);
        registry.registerBuilderSupplier(HorseInventoryWindow.Builder.class, SpongeHorseInventoryWindow.Builder::new);
        registry.registerBuilderSupplier(PlayerInventoryWindow.Builder.class, SpongePlayerInventoryWindow.Builder::new);
        registry.registerBuilderSupplier(SignWindow.Builder.class, SpongeSignWindow.Builder::new);
        registry.registerBuilderSupplier(SleepingWindow.Builder.class, SpongeSleepingWindow.Builder::new);
        registry.registerBuilderSupplier(VillagerTradeWindow.Builder.class, SpongeVillagerTradeWindow.Builder::new);
        registry.registerBuilderSupplier(WinGameWindow.Builder.class, SpongeWinGameWindow.Builder::new);
    }

    @Override
    public B from(W value) {
        return reset();
    }

    @SuppressWarnings("unchecked")
    @Override
    public B reset() {
        return (B) this;
    }

}
