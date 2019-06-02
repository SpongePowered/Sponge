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
package org.spongepowered.common.regression.registry;

import static org.junit.Assert.assertEquals;

import com.flowpowered.math.vector.Vector3i;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.storage.AnvilSaveHandler;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.spongepowered.api.block.tileentity.TileEntityType;
import org.spongepowered.common.registry.type.block.TileEntityTypeRegistryModule;
import org.spongepowered.common.test.RegressionTest;
import org.spongepowered.lwts.runner.LaunchWrapperTestRunner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RunWith(LaunchWrapperTestRunner.class)
public class TileEntityRegistrationTest {


    @RegressionTest(ghIssue = "https://github.com/SpongePowered/SpongeForge/issues/2761",
        relatedCommits = "https://github.com/SpongePowered/SpongeCommon/commit/2e394dc71f03026f937ad332eab57020eb55e536"
    )
    public static final String UNQUALIFIED_TILE_ID = "mod.you.are";
    public static final String CORRECTLY_QUALIFIED_ID = "sponge:myfodo";
    public static final String MINECRAFT_PREFIXED_ID = "minecraft:mod.and.youAreMySamwise";

    @BeforeClass
    public static void setupSchematic() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Idea: register a few dummy tile entities with the following:
        // 'mod.sponge.herpaderpa'
        // 'sponge:herpaderpderp'
        // 'minecraft:mod.sponge.herpadpera'
        final Method tileRegisterMethod = TileEntity.class.getDeclaredMethod("register", String.class, Class.class);
        tileRegisterMethod.setAccessible(true);
        tileRegisterMethod.invoke(null, UNQUALIFIED_TILE_ID, Test1Tile.class);
        tileRegisterMethod.invoke(null, CORRECTLY_QUALIFIED_ID, Test2Tile.class);
        tileRegisterMethod.invoke(null, MINECRAFT_PREFIXED_ID, Test3Tile.class);
    }

    /**
     * This verifies that our custom tile entity types are being registered, both as sponge mod prefixed, and
     * the rare case where a forge mod is being registered as "minecraft:" prefixed. These come into play for the
     * required mods section of schematics.
     */
    @RegressionTest(ghIssue = "https://github.com/SpongePowered/SpongeForge/issues/2785")
    @SuppressWarnings("ConstantConditions")
    @Test
    public void testGettingTileEntityTypes() {
        final TileEntityType test1type = TileEntityTypeRegistryModule.getInstance().getForClass(Test1Tile.class);
        final TileEntityType test2type = TileEntityTypeRegistryModule.getInstance().getForClass(Test2Tile.class);
        final TileEntityType test3type = TileEntityTypeRegistryModule.getInstance().getForClass(Test3Tile.class);

        @RegressionTest(ghIssue = "https://github.com/SpongePowered/SpongeForge/issues/2785",
            comment = "Specifically put, Forge has a weird case where they auto-prefix mod provided"
                      + "id's "
        )
        final String autoPrefixed = "sponge:" + UNQUALIFIED_TILE_ID;
        assertEquals(autoPrefixed, test1type.getId()); // Note that SpongeImplHooks by default will prefix based on package, so we're good.
        assertEquals(CORRECTLY_QUALIFIED_ID, test2type.getId());
        assertEquals(MINECRAFT_PREFIXED_ID, test3type.getId());

        assertEquals(((org.spongepowered.api.block.tileentity.TileEntity) (Object) new Test1Tile()).getType(), test1type);
        assertEquals(((org.spongepowered.api.block.tileentity.TileEntity) (Object) new Test2Tile()).getType(), test2type);
        assertEquals(((org.spongepowered.api.block.tileentity.TileEntity) (Object) new Test3Tile()).getType(), test3type);
    }
}
