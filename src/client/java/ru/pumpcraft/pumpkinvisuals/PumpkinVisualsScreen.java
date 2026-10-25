package ru.pumpcraft.pumpkinvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class PumpkinVisualsScreen extends Screen {
    private static final int PURPLE = 0xFFB76CFF;
    private static final int PANEL = 0xE91B1428;
    private static final int MUTED = 0xFFB7AAC9;

    public PumpkinVisualsScreen() {
        super(Text.literal("PumpkinVisuals"));
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 105;
        int top = this.height / 2 - 70;
        int w = 210;
        int h = 20;
        int gap = 25;

        addDrawableChild(ButtonWidget.builder(toggleText("Частицы", PumpkinVisualsClient.reducedParticles), b -> {
            PumpkinVisualsClient.reducedParticles = !PumpkinVisualsClient.reducedParticles;
            MinecraftClient.getInstance().options.getParticles().setValue(
                    PumpkinVisualsClient.reducedParticles
                            ? net.minecraft.client.option.ParticlesMode.MINIMAL
                            : net.minecraft.client.option.ParticlesMode.ALL);
            b.setMessage(toggleText("Частицы", PumpkinVisualsClient.reducedParticles));
        }).dimensions(left, top, w, h).build());

        addDrawableChild(ButtonWidget.builder(toggleText("Nearby HUD", PumpkinVisualsClient.nearbyHud), b -> {
            PumpkinVisualsClient.nearbyHud = !PumpkinVisualsClient.nearbyHud;
            b.setMessage(toggleText("Nearby HUD", PumpkinVisualsClient.nearbyHud));
        }).dimensions(left, top + gap, w, h).build());

        addDrawableChild(ButtonWidget.builder(toggleText("Auto Eat", PumpkinVisualsClient.autoEatEnabled), b -> {
            PumpkinVisualsClient.autoEatEnabled = !PumpkinVisualsClient.autoEatEnabled;
            b.setMessage(toggleText("Auto Eat", PumpkinVisualsClient.autoEatEnabled));
        }).dimensions(left, top + gap * 2, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("X-Ray — предупреждение"), b -> {
            MinecraftClient.getInstance().setScreen(new XRayWarningScreen(this));
        }).dimensions(left, top + gap * 3, w, h).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close())
                .dimensions(left, top + gap * 4, w, h).build());
    }

    private static Text toggleText(String name, boolean enabled) {
        return Text.literal(name + ": " + (enabled ? "§aВКЛ" : "§cВЫКЛ"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int left = this.width / 2 - 125;
        int top = this.height / 2 - 104;
        context.fill(left, top, left + 250, top + 235, PANEL);
        context.fill(left, top, left + 250, top + 3, PURPLE);
        context.drawCenteredTextWithShadow(textRenderer, "PUMPKINVISUALS", this.width / 2, top + 12, PURPLE);
        context.drawCenteredTextWithShadow(textRenderer, "Minecraft 1.21.6 • Client", this.width / 2, top + 28, MUTED);

        MinecraftClient client = MinecraftClient.getInstance();
        String players = client.world == null ? "Игроки рядом: —" :
                "Игроки рядом: " + Math.max(0, client.world.getPlayers().size() - 1);
        context.drawCenteredTextWithShadow(textRenderer, players, this.width / 2, top + 204, 0xFFFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
