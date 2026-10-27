package ru.pumpcraft.pumpkinvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/** PumpkinVisuals full client menu. All controls are regular Minecraft widgets. */
public final class PumpkinVisualsScreen extends Screen {
    private static final int BG = 0xF00B0806;
    private static final int SIDEBAR = 0xFF17100B;
    private static final int CARD = 0xE91D150E;
    private static final int LINE = 0xFF553018;
    private static final int ORANGE = 0xFFFF8500;
    private static final int WHITE = 0xFFF8F0E8;
    private static final int MUTED = 0xFFB8A79A;
    private static final int GREEN = 0xFF71E09A;

    private enum Tab { HOME, COMBAT, VISUALS, WORLD, PLAYER, SETTINGS }
    private Tab tab = Tab.HOME;
    private int scroll;
    private int left, top, panelW, panelH, sideW, contentX, contentW;
    private final List<Row> rows = new ArrayList<>();
    private record Row(String title, String description, String status, Runnable action) {}

    public PumpkinVisualsScreen() { super(Text.literal("PumpkinVisuals")); }

    @Override
    protected void init() {
        panelW = Math.min(820, Math.max(300, width - 20));
        panelH = Math.min(500, Math.max(230, height - 20));
        left = (width - panelW) / 2;
        top = (height - panelH) / 2;
        sideW = Math.min(158, Math.max(105, panelW / 4));
        contentX = left + sideW + 16;
        contentW = panelW - sideW - 28;
        rebuildRows();
        rebuildWidgets();
    }

    private static String state(boolean value) { return value ? "ON" : "OFF"; }
    private void toggleParticles() {
        PumpkinVisualsClient.reducedParticles = !PumpkinVisualsClient.reducedParticles;
        MinecraftClient.getInstance().options.getParticles().setValue(
                PumpkinVisualsClient.reducedParticles ? ParticlesMode.MINIMAL : ParticlesMode.ALL);
    }

    private void rebuildRows() {
        rows.clear();
        switch (tab) {
            case HOME -> {
                rows.add(new Row("TriggerBot", "Attack when a target is under the crosshair", state(PumpkinVisualsClient.triggerBotEnabled), () -> PumpkinVisualsClient.triggerBotEnabled = !PumpkinVisualsClient.triggerBotEnabled));
                rows.add(new Row("KillAura", "Automatically attacks nearby targets", state(PumpkinVisualsClient.killAuraEnabled), () -> PumpkinVisualsClient.killAuraEnabled = !PumpkinVisualsClient.killAuraEnabled));
                rows.add(new Row("Ore X-Ray", "Draw outlines around nearby loaded ores", state(PumpkinVisualsClient.xrayEnabled), () -> PumpkinVisualsClient.xrayEnabled = !PumpkinVisualsClient.xrayEnabled));
                rows.add(new Row("Player ESP", "Highlight nearby players", state(PumpkinVisualsClient.playerEspEnabled), () -> PumpkinVisualsClient.playerEspEnabled = !PumpkinVisualsClient.playerEspEnabled));
            }
            case COMBAT -> {
                rows.add(new Row("TriggerBot", "Attacks the entity under your crosshair", state(PumpkinVisualsClient.triggerBotEnabled), () -> PumpkinVisualsClient.triggerBotEnabled = !PumpkinVisualsClient.triggerBotEnabled));
                rows.add(new Row("KillAura", "Attacks the nearest living target in range", state(PumpkinVisualsClient.killAuraEnabled), () -> PumpkinVisualsClient.killAuraEnabled = !PumpkinVisualsClient.killAuraEnabled));
            }
            case VISUALS -> {
                rows.add(new Row("Ore X-Ray", "Outline ores in the currently loaded area", state(PumpkinVisualsClient.xrayEnabled), () -> PumpkinVisualsClient.xrayEnabled = !PumpkinVisualsClient.xrayEnabled));
                rows.add(new Row("Player ESP", "Draw boxes around nearby players", state(PumpkinVisualsClient.playerEspEnabled), () -> PumpkinVisualsClient.playerEspEnabled = !PumpkinVisualsClient.playerEspEnabled));
                rows.add(new Row("Reduced Particles", "Uses Minecraft's minimal particle setting", state(PumpkinVisualsClient.reducedParticles), this::toggleParticles));
            }
            case WORLD -> {
                rows.add(new Row("Ore X-Ray", "Scan radius: 16 blocks around the player", state(PumpkinVisualsClient.xrayEnabled), () -> PumpkinVisualsClient.xrayEnabled = !PumpkinVisualsClient.xrayEnabled));
                rows.add(new Row("World Player ESP", "Highlight players in the loaded world", state(PumpkinVisualsClient.playerEspEnabled), () -> PumpkinVisualsClient.playerEspEnabled = !PumpkinVisualsClient.playerEspEnabled));
                rows.add(new Row("Particles", "Reduce visual clutter", state(PumpkinVisualsClient.reducedParticles), this::toggleParticles));
            }
            case PLAYER -> {
                rows.add(new Row("Auto Eat", "Uses food when hunger is 14 or lower", state(PumpkinVisualsClient.autoEatEnabled), () -> PumpkinVisualsClient.autoEatEnabled = !PumpkinVisualsClient.autoEatEnabled));
                rows.add(new Row("Player Counter", "Show player count in the current world", state(PumpkinVisualsClient.nearbyHud), () -> PumpkinVisualsClient.nearbyHud = !PumpkinVisualsClient.nearbyHud));
                rows.add(new Row("Auto Eat Key", "Press Z to toggle Auto Eat", "Z", () -> {}));
            }
            case SETTINGS -> {
                rows.add(new Row("Open Menu", "Right Shift opens this screen", "RSHIFT", () -> {}));
                rows.add(new Row("Minecraft", "Fabric client", "1.21.6", () -> {}));
                rows.add(new Row("Build", "PumpkinVisuals client", "BETA", () -> {}));
            }
        }
    }

    private void rebuildWidgets() {
        clearChildren();
        int navX = left + 8, navW = sideW - 16, navY = top + 78;
        addDrawableChild(navButton("Главная", Tab.HOME, navX, navY, navW));
        addDrawableChild(navButton("Combat", Tab.COMBAT, navX, navY + 27, navW));
        addDrawableChild(navButton("Visuals", Tab.VISUALS, navX, navY + 54, navW));
        addDrawableChild(navButton("World", Tab.WORLD, navX, navY + 81, navW));
        addDrawableChild(navButton("Player", Tab.PLAYER, navX, navY + 108, navW));
        addDrawableChild(navButton("Settings", Tab.SETTINGS, navX, navY + 135, navW));

        int rowY = top + 94, rowH = 48, bottom = top + panelH - 48;
        for (int i = 0; i < rows.size(); i++) {
            int y = rowY + i * (rowH + 7) - scroll;
            if (y < top + 86 || y + rowH > bottom) continue;
            Row row = rows.get(i);
            int btnW = Math.min(82, Math.max(64, contentW / 4));
            int btnX = left + panelW - btnW - 12;
            addDrawableChild(ButtonWidget.builder(buttonLabel(row), b -> {
                row.action().run();
                rebuildRows();
                rebuildWidgets();
            }).dimensions(btnX, y + 12, btnW, 22).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close())
                .dimensions(left + panelW - 91, top + panelH - 29, 79, 20).build());
    }

    private ButtonWidget navButton(String label, Tab target, int x, int y, int w) {
        String prefix = tab == target ? "§6» §f" : "§7";
        return ButtonWidget.builder(Text.literal(prefix + label), b -> {
            tab = target;
            scroll = 0;
            rebuildRows();
            rebuildWidgets();
        }).dimensions(x, y, w, 20).build();
    }

    private Text buttonLabel(Row row) {
        if (row.status().equals("ON")) return Text.literal("§aON");
        if (row.status().equals("OFF")) return Text.literal("§cOFF");
        return Text.literal("§6" + row.status());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xB8000000);
        ctx.fill(left - 1, top - 1, left + panelW + 1, top + panelH + 1, ORANGE);
        ctx.fill(left, top, left + panelW, top + panelH, BG);
        ctx.fill(left, top, left + panelW, top + 3, ORANGE);
        ctx.fill(left + 1, top + 3, left + sideW, top + panelH - 1, SIDEBAR);
        ctx.fill(left + sideW, top + 3, left + sideW + 1, top + panelH - 1, LINE);

        ctx.drawTextWithShadow(textRenderer, "PUMPKIN", left + 12, top + 15, ORANGE);
        ctx.drawTextWithShadow(textRenderer, "VISUALS", left + 12, top + 29, WHITE);
        ctx.drawTextWithShadow(textRenderer, "CLIENT • 1.21.6", left + 12, top + 47, MUTED);
        ctx.fill(left + 9, top + 64, left + sideW - 9, top + 65, LINE);
        ctx.drawTextWithShadow(textRenderer, "RIGHT SHIFT", left + 10, top + panelH - 42, MUTED);

        ctx.drawTextWithShadow(textRenderer, title(), contentX, top + 15, WHITE);
        ctx.drawTextWithShadow(textRenderer, "PumpkinVisuals  /  Halloween client", contentX, top + 32, MUTED);
        ctx.fill(contentX, top + 51, left + panelW - 11, top + 52, LINE);

        String status = MinecraftClient.getInstance().player == null ? "Not in world" : "Client active";
        ctx.fill(contentX, top + 59, left + panelW - 11, top + 80, CARD);
        ctx.fill(contentX, top + 59, contentX + 3, top + 80, ORANGE);
        ctx.drawTextWithShadow(textRenderer, "● SYSTEM STATUS", contentX + 9, top + 65, ORANGE);
        ctx.drawTextWithShadow(textRenderer, status, left + panelW - textRenderer.getWidth(status) - 19, top + 65, GREEN);

        int rowY = top + 94, rowH = 48, visibleTop = top + 87, visibleBottom = top + panelH - 48;
        ctx.enableScissor(contentX, visibleTop, left + panelW - 9, visibleBottom);
        for (int i = 0; i < rows.size(); i++) {
            int y = rowY + i * (rowH + 7) - scroll;
            if (y + rowH < visibleTop || y > visibleBottom) continue;
            Row row = rows.get(i);
            ctx.fill(contentX, y, left + panelW - 11, y + rowH, CARD);
            ctx.fill(contentX, y, contentX + 2, y + rowH, ORANGE);
            ctx.drawTextWithShadow(textRenderer, row.title(), contentX + 9, y + 7, WHITE);
            int descW = Math.max(20, contentW - 112);
            ctx.drawTextWithShadow(textRenderer, trim(row.description(), descW), contentX + 9, y + 25, MUTED);
        }
        ctx.disableScissor();
        ctx.fill(contentX, top + panelH - 39, left + panelW - 11, top + panelH - 38, LINE);
        ctx.drawTextWithShadow(textRenderer, "PUMPKINVISUALS • BETA", contentX, top + panelH - 27, ORANGE);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private String trim(String value, int maxWidth) {
        if (textRenderer.getWidth(value) <= maxWidth) return value;
        String result = value;
        while (!result.isEmpty() && textRenderer.getWidth(result + "...") > maxWidth)
            result = result.substring(0, result.length() - 1);
        return result.isEmpty() ? "" : result + "...";
    }

    private String title() {
        return switch (tab) {
            case HOME -> "Dashboard";
            case COMBAT -> "Combat";
            case VISUALS -> "Visuals";
            case WORLD -> "World";
            case PLAYER -> "Player";
            case SETTINGS -> "Settings";
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, rows.size() * 55 - (panelH - 150));
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (verticalAmount * 24)));
        rebuildWidgets();
        return true;
    }

    @Override public boolean shouldPause() { return false; }
}
