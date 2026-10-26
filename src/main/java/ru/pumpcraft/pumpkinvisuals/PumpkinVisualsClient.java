package ru.pumpcraft.pumpkinvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public final class PumpkinVisualsClient implements ClientModInitializer {
    public static KeyBinding openMenuKey;
    public static KeyBinding autoEatKey;
    public static boolean reducedParticles = true;
    public static boolean nearbyHud = true;
    public static boolean autoEatEnabled = false;
    public static boolean xrayEnabled = false;

    private static long lastEatTime;
    private static int ticksSinceScan;
    private static List<BlockPos> cachedOres = List.of();

    @Override
    public void onInitializeClient() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pumpkinvisuals.open_menu", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.misc"));
        autoEatKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.pumpkinvisuals.auto_eat", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z, "key.categories.misc"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) client.setScreen(new PumpkinVisualsScreen());
            while (autoEatKey.wasPressed()) {
                autoEatEnabled = !autoEatEnabled;
                if (client.player != null) {
                    client.player.sendMessage(Text.literal("§6PumpkinVisuals §8» §fАвтоеда: "
                            + (autoEatEnabled ? "§aВКЛ" : "§cВЫКЛ")), true);
                }
            }

            if (autoEatEnabled && client.player != null && client.interactionManager != null
                    && client.player.getHungerManager().getFoodLevel() <= 14
                    && !client.player.isUsingItem()) {
                tryAutoEat(client);
            }

            if (client.world == null || client.player == null || !xrayEnabled) {
                cachedOres = List.of();
                ticksSinceScan = 0;
            } else if (++ticksSinceScan >= 10) {
                ticksSinceScan = 0;
                cachedOres = scanNearbyOres(client);
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!xrayEnabled || client.world == null || client.player == null || cachedOres.isEmpty()) return;
            MatrixStack matrices = context.matrixStack();
            if (matrices == null) return;
            Vec3d camera = context.camera().getPos();
            VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
            VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
            matrices.push();
            matrices.translate(-camera.x, -camera.y, -camera.z);
            try {
                for (BlockPos pos : cachedOres) {
                    Box box = new Box(pos).expand(0.002);
                    float[] color = oreColor(client.world.getBlockState(pos));
                    VertexRendering.drawBox(matrices, lines, box, color[0], color[1], color[2], 0.95f);
                }
                consumers.draw();
            } finally {
                matrices.pop();
            }
        });
    }

    private static List<BlockPos> scanNearbyOres(MinecraftClient client) {
        if (client.world == null || client.player == null) return List.of();
        BlockPos origin = client.player.getBlockPos();
        List<BlockPos> found = new ArrayList<>();
        int radius = 16;
        int minY = Math.max(client.world.getBottomY(), origin.getY() - 12);
        int maxY = Math.min(client.world.getTopYInclusive(), origin.getY() + 12);
        for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
            for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
                if ((x - origin.getX()) * (x - origin.getX())
                        + (z - origin.getZ()) * (z - origin.getZ()) > radius * radius) continue;
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = client.world.getBlockState(pos);
                    String id = state.getBlock().getTranslationKey();
                    if (!state.isAir() && (id.contains("_ore") || id.endsWith(".ancient_debris"))) {
                        found.add(pos.toImmutable());
                    }
                }
            }
        }
        return List.copyOf(found);
    }

    private static float[] oreColor(BlockState state) {
        String id = state.getBlock().getTranslationKey();
        if (id.contains("diamond")) return new float[]{0.15f, 0.95f, 1.0f};
        if (id.contains("emerald")) return new float[]{0.2f, 1.0f, 0.35f};
        if (id.contains("gold")) return new float[]{1.0f, 0.72f, 0.12f};
        if (id.contains("redstone")) return new float[]{1.0f, 0.18f, 0.18f};
        if (id.contains("lapis")) return new float[]{0.2f, 0.35f, 1.0f};
        if (id.contains("copper")) return new float[]{1.0f, 0.48f, 0.2f};
        if (id.contains("quartz") || id.endsWith(".ancient_debris")) return new float[]{1.0f, 0.35f, 0.85f};
        if (id.contains("iron")) return new float[]{0.85f, 0.88f, 0.92f};
        if (id.contains("coal")) return new float[]{0.35f, 0.35f, 0.4f};
        return new float[]{0.65f, 0.35f, 1.0f};
    }

    private static boolean isFood(ItemStack stack) {
        return !stack.isEmpty() && stack.contains(DataComponentTypes.FOOD);
    }

    private static void tryAutoEat(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - lastEatTime < 1400) return;

        // Only choose food that can restore hunger; avoid repeatedly trying an empty slot.
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = client.player.getInventory().getStack(slot);
            if (!isFood(stack)) continue;
            client.player.getInventory().setSelectedSlot(slot);
            client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
            lastEatTime = now;
            return;
        }
    }
}