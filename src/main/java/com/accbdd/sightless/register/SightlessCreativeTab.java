package com.accbdd.sightless.register;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.accbdd.sightless.Sightless.MODID;

public class SightlessCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("sightless", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sightless"))
                    .icon(() -> SightlessItems.LIDAR.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(SightlessItems.LIDAR);
                        output.accept(SightlessItems.SIGHTED_BLOCK);
                    }).build()
    );
}
