package com.accbdd.sightless.block;

import com.accbdd.sightless.register.SightlessBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

public class SightedBlockEntity extends BlockEntity {
    public static final Set<SightedBlockEntity> ACTIVE = new HashSet<>();

    public SightedBlockEntity(BlockPos pos, BlockState blockState) {
        super(SightlessBlockEntities.SIGHTED_BLOCK_ENTITY.get(), pos, blockState);
    }

    public int getRevealRadius() {
        return 4;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && level.isClientSide()) {
            ACTIVE.add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && level.isClientSide()) {
            ACTIVE.remove(this);
        }
    }
}
