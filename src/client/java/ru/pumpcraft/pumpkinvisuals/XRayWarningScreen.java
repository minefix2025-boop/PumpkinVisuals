package ru.pumpcraft.pumpkinvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public final class XRayWarningScreen extends Screen {
    private final Screen parent;

    public XRayWarningScreen(Screen parent) {
        super(Text.literal("X-Ray warning"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int left = width / 2 - 105;
        addDrawableChild(ButtonWidget.builder(Text.literal("Я понял — назад"), b -> MinecraftClient.getInstance().setScreen(parent))
                .dimensions(left, height / 2 + 30, 210, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, "§cX-RAY: ВАЖНОЕ ПРЕДУПРЕЖДЕНИЕ", width / 2, height / 2 - 55, 0xFFFF5555);
        context.drawCenteredTextWithShadow(textRenderer, "X-Ray может нарушать правила серверов.", width / 2, height / 2 - 30, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Возможны бан и потеря доступа к серверу.", width / 2, height / 2 - 15, 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, "Используй только там, где это разрешено.", width / 2, height / 2, 0xFFFFCC66);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
