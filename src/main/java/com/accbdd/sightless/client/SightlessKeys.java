package com.accbdd.sightless.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class SightlessKeys {
    public static final String KEY_CATEGORY_SIGHTLESS = "key.categories.sightless";
    public static final String KEY_TOGGLE_SHADER = "key.sightless.toggle_shader";

    // Defaults to the 'G' key
    public static final KeyMapping TOGGLE_SHADER_KEY = new KeyMapping(
            KEY_TOGGLE_SHADER,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_GRAVE_ACCENT,
            KEY_CATEGORY_SIGHTLESS
    );
}
