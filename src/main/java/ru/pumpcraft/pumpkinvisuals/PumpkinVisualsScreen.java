package ru.pumpcraft.pumpkinvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.text.Text;

public final class PumpkinVisualsScreen extends Screen {
    private static final int PURPLE = 0xFFB86BFF;
    private static final int PANEL = 0xF0111020;
    private static final int PANEL_EDGE = 0xFF39304E;
    private static final int MUTED = 0xFFB7AEC8;
    private static final int WHITE = 0xFFF6F1FF;
    private static final int GREEN = 0xFF64F0A2;
    private static final int RED = 0xFFFF718A;

    public PumpkinVisualsScreen() {
        super(Text.literal("PumpkinVisuals"));
    }

    @Override
    protected void init() {
        int left = width / 2 - 130;
        int top = height / 2 - 72;
        int buttonWidth = 260;
        int gap = 31;

        addDrawableChild(ButtonWidget.builder(toggleText("Частицы", PumpkinVisualsClient.reducedParticles), b -> {
            PumpkinVisualsClient.reducedParticles = !PumpkinVisualsClient.reducedParticles;
            MinecraftClient.getInstance().options.getParticles().setValue(
                PumpkinVisualsClient.reducedParticles ? ParticlesMode.MINIMAL : ParticlesMode.ALL);
            b.setMessage(toggleText("Частицы", PumpkinVisualsClient.reducedParticles));
        }).dimensions(left, top, buttonWidth, 24).build());

        addDrawableChild(ButtonWidget.builder(toggleText("Счётчик игроков", PumpkinVisualsClient.nearbyHud), b -> {
            PumpkinVisualsClient.nearbyHud = !PumpkinVisualsClient.nearbyHud;
            b.setMessage(toggleText("Счётчик игроков", PumpkinVisualsClient.nearbyHud));
        }).dimensions(left, top + gap, buttonWidth, 24).build());

        addDrawableChild(ButtonWidget.builder(toggleText("Автоеда (Z)", PumpkinVisualsClient.autoEatEnabled), b -> {
            PumpkinVisualsClient.autoEatEnabled = !PumpkinVisualsClient.autoEatEnabled;
            b.setMessage(toggleText("Автоеда (Z)", PumpkinVisualsClient.autoEatEnabled));
        }).dimensions(left, top + gap * 2, buttonWidth, 24).build());

        addDrawableChild(ButtonWidget.builder(toggleText("ORE X-RAY", PumpkinVisualsClient.xrayEnabled), b -> {
            PumpkinVisualsClient.xrayEnabled = !PumpkinVisualsClient.xrayEnabled;
            b.setMessage(toggleText("ORE X-RAY", PumpkinVisualsClient.xrayEnabled));
        }).dimensions(left, top + gap * 3, buttonWidth, 24).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть меню"), b -> close())
            .dimensions(left, top + gap * 4, buttonWidth, 24).build());
    }

    private static Text toggleText(String name, boolean enabled) {
        String status = enabled ? "§aВКЛ" : "§cВЫКЛ";
        return Text.literal(name + "   §8»   " + status);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Flat dim overlay deliberately avoids the vanilla blur pipeline that caused a crash.
        context.fill(0, 0, width, height, 0xA9000000);

        int panelWidth = 320;
        int panelHeight = 350;
        int left = width / 2 - panelWidth / 2;
        int top = height / 2 - panelHeight / 2;

        context.fill(left - 1, top - 1, left + panelWidth + 1, top + panelHeight + 1, PANEL_EDGE);
        context.fill(left, top, left + panelWidth, top + panelHeight, PANEL);
        context.fill(left, top, left + panelWidth, top + 3, PURPLE);
        context.fill(left + 18, top + 48, left + panelWidth - 18, top + 49, 0xFF39304E);

        context.drawCenteredTextWithShadow(textRenderer, "PUMPKINVISUALS", width / 2, top + 17, PURPLE);
        context.drawCenteredTextWithShadow(textRenderer, "CLIENT TOOLKIT  /  MC 1.21.6", width / 2, top + 34, MUTED);

        context.drawTextWithShadow(textRenderer, "СОСТОЯНИЕ", left + 20, top + 61, MUTED);
        String xrayStatus = PumpkinVisualsClient.xrayEnabled ? "ORE X-RAY АКТИВЕН" : "ORE X-RAY ОТКЛЮЧЁН";
        context.drawTextWithShadow(textRenderer, xrayStatus, left + 20, top + 77,
                PumpkinVisualsClient.xrayEnabled ? GREEN : RED);

        MinecraftClient client = MinecraftClient.getInstance();
        int players = client.world == null ? 0 : Math.max(0, client.world.getPlayers().size() - 1);
        context.drawTextWithShadow(textRenderer, "Игроков в мире: " + players, left + 20, top + 99, WHITE);

        super.render(context, mouseX, mouseY, delta);

        context.fill(left + 18, top + panelHeight - 34, left + panelWidth - 18, top + panelHeight - 33, 0xFF39304E);
        context.drawCenteredTextWithShadow(textRenderer, "RIGHT SHIFT  •  МЕНЮ       Z  •  ЕДА", width / 2,
                top + panelHeight - 23, MUTED);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
