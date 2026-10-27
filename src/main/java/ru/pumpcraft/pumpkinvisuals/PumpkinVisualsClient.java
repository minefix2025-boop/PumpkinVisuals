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
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.hit.EntityHitResult;
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
    public static boolean triggerBotEnabled = false;
    public static boolean killAuraEnabled = false;
    public static boolean playerEspEnabled = false;
    public static boolean chineseHatEnabled = true;
    public static boolean autoSprintEnabled = false;

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

            if (autoSprintEnabled && client.player != null && client.player.input != null
                    && client.player.input.movementForward > 0.0f && !client.player.isSneaking()) {
                client.player.setSprinting(true);
            }

            if (autoEatEnabled && client.player != null && client.interactionManager != null
                    && client.player.getHungerManager().getFoodLevel() <= 14
                    && !client.player.isUsingItem()) {
                tryAutoEat(client);
            }

            if (client.player != null && client.world != null && client.interactionManager != null) {
                runCombatAssist(client);
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
            if (client.world == null || client.player == null
                    || ((!xrayEnabled || cachedOres.isEmpty()) && !playerEspEnabled && !chineseHatEnabled)) return;
            MatrixStack matrices = context.matrixStack();
            if (matrices == null) return;
            Vec3d camera = context.camera().getPos();
            VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
            VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
            matrices.push();
            matrices.translate(-camera.x, -camera.y, -camera.z);
            try {
                if (xrayEnabled) {
                    for (BlockPos pos : cachedOres) {
                        Box box = new Box(pos).expand(0.002);
                        float[] color = oreColor(client.world.getBlockState(pos));
                        VertexRendering.drawBox(matrices, lines, box, color[0], color[1], color[2], 0.95f);
                    }
                }
                if (chineseHatEnabled) {
                    // A stepped conical hat made from three outlined rings/tiers, attached to the local player's head.
                    double hx = client.player.getX();
                    double hy = client.player.getY() + (client.player.getHeight() * 0.86);
                    double hz = client.player.getZ();
                    VertexRendering.drawBox(matrices, lines, new Box(hx - 0.36, hy + 0.10, hz - 0.36, hx + 0.36, hy + 0.14, hz + 0.36), 1.0f, 0.48f, 0.05f, 1.0f);
                    VertexRendering.drawBox(matrices, lines, new Box(hx - 0.24, hy + 0.14, hz - 0.24, hx + 0.24, hy + 0.24, hz + 0.24), 1.0f, 0.58f, 0.10f, 1.0f);
                    VertexRendering.drawBox(matrices, lines, new Box(hx - 0.10, hy + 0.24, hz - 0.10, hx + 0.10, hy + 0.34, hz + 0.10), 1.0f, 0.68f, 0.18f, 1.0f);
                }
                if (playerEspEnabled) {
                    for (var other : client.world.getPlayers()) {
                        if (other == client.player || other.isSpectator() || !other.isAlive()) continue;
                        Box box = other.getBoundingBox().expand(0.06);
                        VertexRendering.drawBox(matrices, lines, box, 1.0f, 0.42f, 0.05f, 0.95f);
                    }
                }
                consumers.draw();
            } finally {
                matrices.pop();
            }
        });
    }

    private static long lastCombatAttack;

    private static void runCombatAssist(MinecraftClient client) {
        long now = System.currentTimeMillis();
        if (now - lastCombatAttack < 80 || client.player.isSpectator()
                || client.player.isCreative() || client.player.isUsingItem()) return;

        LivingEntity target = null;
        if (triggerBotEnabled && client.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity living
                && living != client.player && living.isAlive() && !living.isSpectator()
                && client.player.squaredDistanceTo(living) <= 3.4 * 3.4) {
            target = living;
        } else if (killAuraEnabled && !triggerBotEnabled) {
            target = client.world.getOtherEntities(client.player,
                            client.player.getBoundingBox().expand(3.2),
                            entity -> entity instanceof LivingEntity && entity.isAlive()
                                    && !entity.isSpectator() && !(entity instanceof ArmorStandEntity))
                    .stream()
                    .filter(entity -> entity instanceof LivingEntity)
                    .map(entity -> (LivingEntity) entity)
                    .filter(entity -> client.player.squaredDistanceTo(entity) <= 3.2 * 3.2)
                    .min(java.util.Comparator.comparingDouble(client.player::squaredDistanceTo))
                    .orElse(null);
        }

        if (target != null && client.player.getAttackCooldownProgress(0.0f) >= 1.0f) {
            client.interactionManager.attackEntity(client.player, target);
            client.player.swingHand(Hand.MAIN_HAND);
            lastCombatAttack = now;
        }
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