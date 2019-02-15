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
package org.spongepowered.common.world.gen;

import org.spongepowered.api.world.gen.TerrainGenerator;
import org.spongepowered.api.world.gen.TerrainGeneratorConfig;

/*
    A note about this class and it's 100% error ridden sections:
    This is an implementation intended for 1.13 and 1.13's new world generation
    pipeline. It is not at all compatible with 1.12's world generation base.

    There are some concepts that are being exposed potentially to the API,
    whether it's through the FeatureConfig, or SurfaceBuilder etc.

    1.13's world generation pipeline is as follows:
    1) Create ChunkPrimer (IChunk || ProtoChunk, haven't figured out a name for it in API)
    2) call IChunkGenerator#makeBase
      - Makes biome array/ biome buffer
      - Creates ChunkPrimerBuffer for api structures
      - Fills biomes onto target chunk
      - Base GenerationPopulator makes base terrain
      - chunk heightmap re-calculated
      - biomes build surface
      - make bedrock
      - re-calculate heightmap
      - set BASE status
    3) Call IChunkGenerator#carve
      - Create a WorldGenRegion so leaking out of the chunk is possible if and only if the task
      allows it
      - Gets ISurfaceCarver from biome at the position
      - Sponge will use
      - Return the "middle" chunk region, which would be the middle of the generation range.
      If the range is for example, 1, there would be 9 chunks available in the primer array:
      x x x
      x c x
      x x x
      And the middle "c" would be the returned chunk, while the other chunks may be already either
      in the process of generation, or are simply being allowed to bleed into them due to
      carving processes. I don't believe the outer chunks have  been fully processed in that
      kind, but I can't be too sure.
    4) Call IChunkGenerator#decorate
      - Pretty easy, just same stuff as previously, except it uses GenerationStages to determine
      what stage to process
      - Iterates over biome's populators, we can expose this as either currently generating
      populators for biomes, or expose them as keyed to a DecorationStage
      - Structures are part of this as a decoration stage.
    5) Call light engines to re-light chunk
      - Pretty basic, we don't need to really expose this if at all.
    6) Call IChunkGenerator#spawnMobs
      - Creates again, another world gen region for mob spawning
      - controls leaking entities outside of the region.
      - Again, uses biomes to figure out what mobs to spawn, can re-use animal populator
    7) Finalized Chunk
      - re-creates height map specifically for all other heightmap types
    8) Chunk is ready to be converted from a ChunkPrimer to a Chunk.

    Create
     */
public final class SpongeTerrainGenerator implements TerrainGenerator {

    @Override
    public TerrainGeneratorConfig getGenerationSettings() {
        return null;
    }
}