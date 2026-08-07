package com.accbdd.sightless.register;

import com.accbdd.sightless.block.SightedBlock;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.accbdd.sightless.Sightless.MODID;

public class SightlessBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    public static final DeferredBlock<Block> SIGHTED_BLOCK = BLOCKS.registerBlock("sighted_block", SightedBlock::new);
}
