package com.accbdd.sightless.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static com.accbdd.sightless.Sightless.MODID;

@EventBusSubscriber(modid = MODID)
public class SightlessDatagen {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new BlockStateGenerator(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ItemModelGenerator(output, existingFileHelper));
    }
}
