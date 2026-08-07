package com.accbdd.sightless.block;

import com.accbdd.sightless.register.SightlessBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SightedBlockEntity extends BlockEntity {
    public SightedBlockEntity(BlockPos pos, BlockState blockState) {
        super(SightlessBlockEntities.SIGHTED_BLOCK_ENTITY.get(), pos, blockState);
    }
}
