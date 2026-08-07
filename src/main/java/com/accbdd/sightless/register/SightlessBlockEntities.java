package com.accbdd.sightless.register;

import com.accbdd.sightless.block.SightedBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.accbdd.sightless.Sightless.MODID;

public class SightlessBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Supplier<BlockEntityType<SightedBlockEntity>> SIGHTED_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "sighted_block",
            () -> BlockEntityType.Builder.of(
                    SightedBlockEntity::new,
                    SightlessBlocks.SIGHTED_BLOCK.get()
            ).build(null)
    );
}
