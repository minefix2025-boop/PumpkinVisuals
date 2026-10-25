package ru.pumpcraft.pumpkinvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public final class PumpkinVisualsClient implements ClientModInitializer {
    public static KeyBinding openMenuKey;
    public static KeyBinding autoEatKey;
    public static boolean reducedParticles = false;
    public static boolean nearbyHud = true;
    public static boolean autoEatEnabled = true;
    private static long lastEatTime = 0L;

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pumpkinvisuals.open_menu", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, KeyBinding.Category.MISC));
        autoEatKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pumpkinvisuals.auto_eat", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z, KeyBinding.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) client.setScreen(new PumpkinVisualsScreen());
            while (autoEatKey.wasPressed()) useFoodInHand(client);
            if (autoEatEnabled && client.player != null
                    && client.player.getHungerManager().getFoodLevel() <= 14) {
                tryAutoEat(client);
            }
        });
    }

    private static boolean isFood(ItemStack stack) {
        return stack.contains(DataComponentTypes.FOOD);
    }

    private static void useFoodInHand(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        if (isFood(client.player.getMainHandStack())) {
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
        } else {
            client.player.sendMessage(Text.literal("§dPumpkinVisuals §7» Возьми еду в руку."), true);
        }
    }

    private static void tryAutoEat(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - lastEatTime < 1200) return;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = client.player.getInventory().getStack(slot);
            if (isFood(stack)) {
                client.player.getInventory().setSelectedSlot(slot);
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                lastEatTime = now;
                return;
            }
        }
    }
}
