package ru.pumpcraft.pumpkinvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.text.Text;

public final class PumpkinVisualsScreen extends Screen {
    private static final int BG = 0xE90C0B12;
    private static final int PANEL = 0xFF17151F;
    private static final int PANEL2 = 0xFF201C2A;
    private static final int LINE = 0xFF393041;
    private static final int ORANGE = 0xFFFF8A32;
    private static final int ORANGE_DARK = 0xFFB94F22;
    private static final int WHITE = 0xFFF5F0F7;
    private static final int MUTED = 0xFFAAA1B5;
    private static final int GREEN = 0xFF64E89A;
    private static final int RED = 0xFFFF667D;

    public PumpkinVisualsScreen() { super(Text.literal("PumpkinVisuals")); }

    @Override
    protected void init() {
        clearChildren();
        int pw = Math.min(760, width - 24);
        int ph = Math.min(470, height - 24);
        int left = (width - pw) / 2;
        int top = (height - ph) / 2;
        int sidebar = Math.max(130, Math.min(165, pw / 4));
        int contentX = left + sidebar + 22;
        int contentW = pw - sidebar - 42;
        int buttonW = Math.min(150, contentW - 20);
        int rowH = 62;
        int startY = top + 106;

        addDrawableChild(ButtonWidget.builder(toggleText("Частицы", PumpkinVisualsClient.reducedParticles), b -> {
            PumpkinVisualsClient.reducedParticles = !PumpkinVisualsClient.reducedParticles;
            MinecraftClient.getInstance().options.getParticles().setValue(
                    PumpkinVisualsClient.reducedParticles ? ParticlesMode.MINIMAL : ParticlesMode.ALL);
            b.setMessage(toggleText("Частицы", PumpkinVisualsClient.reducedParticles));
        }).dimensions(contentX + contentW - buttonW - 8, startY + 14, buttonW, 28).build());

        addDrawableChild(ButtonWidget.builder(toggleText("Счётчик игроков", PumpkinVisualsClient.nearbyHud), b -> {
            PumpkinVisualsClient.nearbyHud = !PumpkinVisualsClient.nearbyHud;
            b.setMessage(toggleText("Счётчик игроков", PumpkinVisualsClient.nearbyHud));
        }).dimensions(contentX + contentW - buttonW - 8, startY + rowH + 14, buttonW, 28).build());

        addDrawableChild(ButtonWidget.builder(toggleText("Автоеда", PumpkinVisualsClient.autoEatEnabled), b -> {
            PumpkinVisualsClient.autoEatEnabled = !PumpkinVisualsClient.autoEatEnabled;
            b.setMessage(toggleText("Автоеда", PumpkinVisualsClient.autoEatEnabled));
        }).dimensions(contentX + contentW - buttonW - 8, startY + rowH * 2 + 14, buttonW, 28).build());

        addDrawableChild(ButtonWidget.builder(toggleText("Ore X-Ray", PumpkinVisualsClient.xrayEnabled), b -> {
            PumpkinVisualsClient.xrayEnabled = !PumpkinVisualsClient.xrayEnabled;
            b.setMessage(toggleText("Ore X-Ray", PumpkinVisualsClient.xrayEnabled));
        }).dimensions(contentX + contentW - buttonW - 8, startY + rowH * 3 + 14, buttonW, 28).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("ЗАКРЫТЬ"), b -> close())
                .dimensions(left + pw - 118, top + ph - 39, 100, 25).build());
    }

    private static Text toggleText(String name, boolean enabled) {
        return Text.literal(name + "  »  " + (enabled ? "§aON" : "§cOFF"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xB9000000);
        int pw = Math.min(760, width - 24);
        int ph = Math.min(470, height - 24);
        int left = (width - pw) / 2;
        int top = (height - ph) / 2;
        int sidebar = Math.max(130, Math.min(165, pw / 4));
        int contentX = left + sidebar + 22;
        int contentW = pw - sidebar - 42;
        int startY = top + 106;
        int rowH = 62;

        // Entirely custom Halloween client shell; no vanilla blur pipeline.
        ctx.fill(left - 1, top - 1, left + pw + 1, top + ph + 1, ORANGE_DARK);
        ctx.fill(left, top, left + pw, top + ph, BG);
        ctx.fill(left, top, left + pw, top + 3, ORANGE);
        ctx.fill(left, top + 3, left + sidebar, top + ph, PANEL);
        ctx.fill(left + sidebar - 1, top + 3, left + sidebar, top + ph, LINE);

        ctx.drawTextWithShadow(textRenderer, "☾  PUMPKIN", left + 18, top + 17, ORANGE);
        ctx.drawTextWithShadow(textRenderer, "VISUALS", left + 18, top + 31, WHITE);
        ctx.drawTextWithShadow(textRenderer, "CLIENT  /  1.21.6", left + 18, top + 51, MUTED);
        ctx.fill(left + 14, top + 70, left + sidebar - 14, top + 71, LINE);

        drawNav(ctx, left + 17, top + 88, "OVERVIEW", true);
        drawNav(ctx, left + 17, top + 119, "VISUALS", false);
        drawNav(ctx, left + 17, top + 150, "PLAYER", false);
        drawNav(ctx, left + 17, top + 181, "WORLD", false);
        drawNav(ctx, left + 17, top + 212, "SETTINGS", false);

        ctx.fill(left + 14, top + ph - 62, left + sidebar - 14, top + ph - 61, LINE);
        ctx.drawTextWithShadow(textRenderer, "HALLOWEEN BUILD", left + 17, top + ph - 48, ORANGE);
        ctx.drawTextWithShadow(textRenderer, "RIGHT SHIFT  •  MENU", left + 17, top + ph - 32, MUTED);

        ctx.drawTextWithShadow(textRenderer, "Utilities", contentX, top + 18, WHITE);
        ctx.drawTextWithShadow(textRenderer, "Твои функции и быстрые настройки", contentX, top + 35, MUTED);
        ctx.fill(contentX, top + 57, contentX + contentW, top + 58, LINE);

        ctx.fill(contentX, top + 68, contentX + contentW, top + 95, PANEL2);
        ctx.fill(contentX, top + 68, contentX + 3, top + 95, ORANGE);
        ctx.drawTextWithShadow(textRenderer, "●  СТАТУС СИСТЕМЫ", contentX + 12, top + 77, ORANGE);
        String status = MinecraftClient.getInstance().player == null ? "Не в мире" : "Клиент активен";
        ctx.drawTextWithShadow(textRenderer, status, contentX + contentW - textRenderer.getWidth(status) - 12, top + 77, GREEN);

        card(ctx, contentX, startY, contentW, rowH, "PARTICLE CONTROL", "Уменьшает визуальный шум и эффекты.");
        card(ctx, contentX, startY + rowH, contentW, rowH, "PLAYER COUNTER", "Показывает число игроков в текущем мире.");
        card(ctx, contentX, startY + rowH * 2, contentW, rowH, "AUTO EAT", "Автоматически использует еду при голоде. Клавиша Z — вкл/выкл.");
        card(ctx, contentX, startY + rowH * 3, contentW, rowH, "ORE X-RAY", "Подсветка найденных руд в загруженной области.");

        MinecraftClient client = MinecraftClient.getInstance();
        int players = client.world == null ? 0 : Math.max(0, client.world.getPlayers().size() - 1);
        ctx.drawTextWithShadow(textRenderer, "Игроков в мире: " + players, contentX, top + ph - 35, MUTED);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void card(DrawContext ctx, int x, int y, int w, int h, String title, String desc) {
        ctx.fill(x, y, x + w, y + h - 5, PANEL);
        ctx.fill(x, y, x + 2, y + h - 5, LINE);
        ctx.drawTextWithShadow(textRenderer, title, x + 12, y + 11, WHITE);
        ctx.drawTextWithShadow(textRenderer, desc, x + 12, y + 29, MUTED);
    }

    private void drawNav(DrawContext ctx, int x, int y, String label, boolean active) {
        if (active) {
            ctx.fill(x - 7, y - 5, x + 112, y + 17, 0xFF35241E);
            ctx.fill(x - 7, y - 5, x - 4, y + 17, ORANGE);
        }
        ctx.drawTextWithShadow(textRenderer, label, x, y, active ? ORANGE : MUTED);
    }

    @Override public boolean shouldPause() { return false; }
}