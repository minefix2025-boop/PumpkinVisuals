package ru.pumpcraft.pumpkinvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.ParticlesMode;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
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
    public static boolean xrayWarningAccepted = false;
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
            while (openMenuKey.wasPressed()) {
                client.setScreen(new PumpkinVisualsScreen());
            }
            while (autoEatKey.wasPressed()) {
                useSelectedFood(client);
            }
            if (autoEatEnabled && client.player != null && client.player.getHungerManager().getFoodLevel() <= 14) {
                tryAutoEat(client);
            }
        });
    }

    private static void useSelectedFood(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        if (client.player.getMainHandStack().getFoodComponent() != null) {
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
        } else {
            client.player.sendMessage(Text.literal("§dPumpkinVisuals §7» Возьми еду в руку, чтобы использовать её."), true);
        }
    }

    private static void tryAutoEat(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - lastEatTime < 1200) return;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = client.player.getInventory().getStack(slot);
            if (stack.getFoodComponent() != null) {
                client.player.getInventory().setSelectedSlot(slot);
                client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                lastEatTime = now;
                return;
            }
        }
    }

    public static String armorSummary(MinecraftClient client) {
        if (client.player == null) return "Броня: —";
        int count = 0;
        for (ItemStack stack : client.player.getArmorItems()) {
            if (!stack.isEmpty()) count++;
        }
        return "Броня: " + count + "/4";
    }

    public static String potionSummary(MinecraftClient client) {
        if (client.player == null) return "Эффекты: —";
        int count = 0;
        for (StatusEffectInstance ignored : client.player.getStatusEffects()) count++;
        return "Эффекты зелий: " + count;
    }
}
