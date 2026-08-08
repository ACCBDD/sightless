package com.accbdd.sightless.datagen;

import com.accbdd.sightless.register.SightlessBlocks;
import com.accbdd.sightless.register.SightlessItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import static com.accbdd.sightless.Sightless.MODID;

public class ItemModelGenerator extends ItemModelProvider {
    public ItemModelGenerator(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(SightlessItems.LIDAR.get());
        simpleBlockItem(SightlessBlocks.SIGHTED_BLOCK.get());
    }
}
