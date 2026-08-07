package com.accbdd.sightless.register;

import com.accbdd.sightless.item.LidarItem;
import net.minecraft.world.item.BlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.accbdd.sightless.Sightless.MODID;

public class SightlessItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<LidarItem> LIDAR = ITEMS.registerItem("lidar", LidarItem::new);
    public static final DeferredItem<BlockItem> SIGHTED_BLOCK = ITEMS.registerSimpleBlockItem(SightlessBlocks.SIGHTED_BLOCK);
}
