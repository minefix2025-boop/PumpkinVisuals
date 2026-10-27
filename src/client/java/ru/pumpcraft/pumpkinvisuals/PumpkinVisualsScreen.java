package ru.pumpcraft.pumpkinvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.particle.ParticlesMode;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public final class PumpkinVisualsScreen extends Screen {
    private static final int BG = 0xF00B0806;
    private static final int PANEL = 0xFF17100B;
    private static final int CARD = 0xFF21150D;
    private static final int CARD_HOVER = 0xFF302014;
    private static final int LINE = 0xFF593019;
    private static final int ORANGE = 0xFFFF8500;
    private static final int WHITE = 0xFFF5F0F8;
    private static final int MUTED = 0xFFB6AABD;
    private static final int GREEN = 0xFF62E79A;
    private static final int RED = 0xFFFF6D80;

    private enum Tab { VISUALS, PLAYER, WORLD, SETTINGS }
    private Tab tab = Tab.VISUALS;
    private int scroll;
    private int panelLeft, panelTop, panelWidth, panelHeight, sideWidth, contentLeft, contentWidth;
    private final List<Row> rows = new ArrayList<>();

    private record Row(String title, String description, String status, Runnable action) {}

    public PumpkinVisualsScreen() {
        super(Text.literal("PumpkinVisuals"));
    }

    @Override
    protected void init() {
        panelWidth = Math.min(760, width - 20);
        panelHeight = Math.min(470, height - 20);
        panelLeft = (width - panelWidth) / 2;
        panelTop = (height - panelHeight) / 2;
        sideWidth = 142;
        contentLeft = panelLeft + sideWidth + 16;
        contentWidth = panelWidth - sideWidth - 30;
        rebuildRows();
        rebuildWidgets();
    }

    private void rebuildRows() {
        rows.clear();
        switch (tab) {
            case VISUALS -> {
                rows.add(new Row("Частицы", "Снижает количество частиц Minecraft",
                        state(PumpkinVisualsClient.reducedParticles), () -> {
                    PumpkinVisualsClient.reducedParticles = !PumpkinVisualsClient.reducedParticles;
                    MinecraftClient.getInstance().options.getParticles().setValue(
                            PumpkinVisualsClient.reducedParticles ? ParticlesMode.MINIMAL : ParticlesMode.ALL);
                }));
                rows.add(new Row("Ore X-Ray", "Подсветка найденных руд в загруженной области",
                        state(PumpkinVisualsClient.xrayEnabled), () ->
                        PumpkinVisualsClient.xrayEnabled = !PumpkinVisualsClient.xrayEnabled));
            }
            case PLAYER -> {
                rows.add(new Row("Автоеда", "Пробует есть еду при голоде 7 единиц или ниже",
                        state(PumpkinVisualsClient.autoEatEnabled), () ->
                        PumpkinVisualsClient.autoEatEnabled = !PumpkinVisualsClient.autoEatEnabled));
                rows.add(new Row("Счётчик игроков", "Показывает количество игроков в мире",
                        state(PumpkinVisualsClient.nearbyHud), () ->
                        PumpkinVisualsClient.nearbyHud = !PumpkinVisualsClient.nearbyHud));
                rows.add(new Row("Клавиша автоеды", "Z — включить или выключить автоеду", "Z", () -> {}));
            }
            case WORLD -> {
                rows.add(new Row("Ore X-Ray", "Контуры руд в радиусе сканирования",
                        state(PumpkinVisualsClient.xrayEnabled), () ->
                        PumpkinVisualsClient.xrayEnabled = !PumpkinVisualsClient.xrayEnabled));
                rows.add(new Row("Примечание", "Видимость контуров сквозь стены пока не подтверждена", "INFO", () -> {}));
            }
            case SETTINGS -> {
                rows.add(new Row("Открыть меню", "Правая Shift", "RSHIFT", () -> {}));
                rows.add(new Row("Версия", "Fabric • Minecraft 1.21.6", "1.21.6", () -> {}));
                rows.add(new Row("PumpkinVisuals", "Хэллоуинская сборка клиента", "BETA", () -> {}));
            }
        }
    }

    private static String state(boolean value) { return value ? "ВКЛ" : "ВЫКЛ"; }

    private void rebuildWidgets() {
        clearChildren();
        int navX = panelLeft + 10;
        int navW = sideWidth - 20;
        int navY = panelTop + 90;
        addDrawableChild(nav("Визуал", Tab.VISUALS, navX, navY, navW));
        addDrawableChild(nav("Игрок", Tab.PLAYER, navX, navY + 30, navW));
        addDrawableChild(nav("Мир", Tab.WORLD, navX, navY + 60, navW));
        addDrawableChild(nav("Настройки", Tab.SETTINGS, navX, navY + 90, navW));

        int rowY = panelTop + 88;
        int rowH = 46;
        int visibleBottom = panelTop + panelHeight - 55;
        for (int i = 0; i < rows.size(); i++) {
            int y = rowY + i * (rowH + 8) - scroll;
            if (y < panelTop + 65 || y + 24 > visibleBottom) continue;
            Row row = rows.get(i);
            int btnW = Math.min(105, contentWidth - 12);
            int btnX = panelLeft + panelWidth - btnW - 12;
            addDrawableChild(ButtonWidget.builder(buttonLabel(row), b -> {
                row.action().run();
                rebuildRows();
                rebuildWidgets();
            }).dimensions(btnX, y + 7, btnW, 24).build());
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close())
                .dimensions(panelLeft + panelWidth - 94, panelTop + panelHeight - 31, 82, 20).build());
    }

    private ButtonWidget nav(String label, Tab target, int x, int y, int w) {
        return ButtonWidget.builder(Text.literal((tab == target ? "§6» §f" : "§7") + label), b -> {
            tab = target;
            scroll = 0;
            rebuildRows();
            rebuildWidgets();
        }).dimensions(x, y, w, 22).build();
    }

    private Text buttonLabel(Row row) {
        if (row.status().equals("ВКЛ")) return Text.literal("§aВКЛ");
        if (row.status().equals("ВЫКЛ")) return Text.literal("§cВЫКЛ");
        return Text.literal("§6" + row.status());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xB9000000);
        ctx.fill(panelLeft - 1, panelTop - 1, panelLeft + panelWidth + 1, panelTop + panelHeight + 1, ORANGE);
        ctx.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + panelHeight, BG);
        ctx.fill(panelLeft, panelTop, panelLeft + panelWidth, panelTop + 3, ORANGE);
        ctx.fill(panelLeft + 1, panelTop + 3, panelLeft + sideWidth, panelTop + panelHeight - 1, PANEL);
        ctx.fill(panelLeft + sideWidth, panelTop + 3, panelLeft + sideWidth + 1, panelTop + panelHeight - 1, LINE);

        ctx.drawTextWithShadow(textRenderer, "PUMPKIN", panelLeft + 13, panelTop + 17, ORANGE);
        ctx.drawTextWithShadow(textRenderer, "VISUALS", panelLeft + 13, panelTop + 31, WHITE);
        ctx.drawTextWithShadow(textRenderer, "HALLOWEEN CLIENT", panelLeft + 13, panelTop + 49, MUTED);
        ctx.fill(panelLeft + 10, panelTop + 69, panelLeft + sideWidth - 10, panelTop + 70, LINE);
        ctx.drawTextWithShadow(textRenderer, "1.21.6 • FABRIC", panelLeft + 12, panelTop + panelHeight - 35, MUTED);

        ctx.drawTextWithShadow(textRenderer, title(), contentLeft, panelTop + 17, WHITE);
        ctx.drawTextWithShadow(textRenderer, "Функции клиента", contentLeft, panelTop + 34, MUTED);
        ctx.fill(contentLeft, panelTop + 54, panelLeft + panelWidth - 12, panelTop + 55, LINE);

        int rowY = panelTop + 88;
        int rowH = 46;
        int visibleTop = panelTop + 65;
        int visibleBottom = panelTop + panelHeight - 55;
        ctx.enableScissor(contentLeft, visibleTop, panelLeft + panelWidth - 10, visibleBottom);
        for (int i = 0; i < rows.size(); i++) {
            int y = rowY + i * (rowH + 8) - scroll;
            if (y + rowH < visibleTop || y > visibleBottom) continue;
            Row row = rows.get(i);
            ctx.fill(contentLeft, y, panelLeft + panelWidth - 12, y + rowH, CARD);
            ctx.fill(contentLeft, y, contentLeft + 2, y + rowH, LINE);
            ctx.drawTextWithShadow(textRenderer, row.title(), contentLeft + 10, y + 7, WHITE);
            ctx.drawTextWithShadow(textRenderer, row.description(), contentLeft + 10, y + 24, MUTED);
        }
        ctx.disableScissor();

        ctx.fill(contentLeft, panelTop + panelHeight - 51, panelLeft + panelWidth - 12, panelTop + panelHeight - 50, LINE);
        ctx.drawTextWithShadow(textRenderer, "PUMPKINVISUALS  /  BETA", contentLeft, panelTop + panelHeight - 38, ORANGE);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private String title() {
        return switch (tab) {
            case VISUALS -> "Визуал";
            case PLAYER -> "Игрок";
            case WORLD -> "Мир";
            case SETTINGS -> "Настройки";
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, rows.size() * 54 - (panelHeight - 150));
        scroll = Math.max(0, Math.min(maxScroll, scroll - (int) (verticalAmount * 24)));
        rebuildWidgets();
        return true;
    }

    @Override public boolean shouldPause() { return false; }
}