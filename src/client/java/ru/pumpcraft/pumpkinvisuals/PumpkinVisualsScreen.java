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
    private static final int BG = 0xE9100D16;
    private static final int PANEL = 0xFF1A1722;
    private static final int PANEL2 = 0xFF24202D;
    private static final int LINE = 0xFF403448;
    private static final int ORANGE = 0xFFFF8A32;
    private static final int WHITE = 0xFFF4EFF8;
    private static final int MUTED = 0xFFB0A6B9;
    private static final int GREEN = 0xFF69E99B;
    private static final int RED = 0xFFFF7185;

    private enum Tab { VISUALS, PLAYER, WORLD, SETTINGS }
    private Tab tab = Tab.VISUALS;
    private int scroll;
    private final List<ButtonWidget> featureButtons = new ArrayList<>();

    public PumpkinVisualsScreen() { super(Text.literal("PumpkinVisuals")); }

    @Override
    protected void init() {
        rebuildWidgets();
    }

    private void rebuildWidgets() {
        clearChildren();
        featureButtons.clear();

        int pw = Math.min(650, width - 20);
        int ph = Math.min(390, height - 20);
        int left = (width - pw) / 2;
        int top = (height - ph) / 2;
        int side = 118;

        addDrawableChild(ButtonWidget.builder(nav("Визуал", Tab.VISUALS), b -> changeTab(Tab.VISUALS))
                .dimensions(left + 8, top + 77, side - 16, 22).build());
        addDrawableChild(ButtonWidget.builder(nav("Игрок", Tab.PLAYER), b -> changeTab(Tab.PLAYER))
                .dimensions(left + 8, top + 105, side - 16, 22).build());
        addDrawableChild(ButtonWidget.builder(nav("Мир", Tab.WORLD), b -> changeTab(Tab.WORLD))
                .dimensions(left + 8, top + 133, side - 16, 22).build());
        addDrawableChild(ButtonWidget.builder(nav("Настройки", Tab.SETTINGS), b -> changeTab(Tab.SETTINGS))
                .dimensions(left + 8, top + 161, side - 16, 22).build());

        int cx = left + side + 16;
        int cw = pw - side - 30;
        int rowW = Math.min(190, cw - 8);
        int rowH = 28;
        int y = top + 91 - scroll;

        if (tab == Tab.VISUALS) {
            addFeature(cx, y, rowW, rowH, "Частицы", "Сократить эффекты",
                    PumpkinVisualsClient.reducedParticles, () -> {
                        PumpkinVisualsClient.reducedParticles = !PumpkinVisualsClient.reducedParticles;
                        MinecraftClient.getInstance().options.getParticles().setValue(
                                PumpkinVisualsClient.reducedParticles ? ParticlesMode.MINIMAL : ParticlesMode.ALL);
                    });
            addFeature(cx, y + 42, rowW, rowH, "Ore X-Ray", "Подсветка руд",
                    PumpkinVisualsClient.xrayEnabled, () ->
                            PumpkinVisualsClient.xrayEnabled = !PumpkinVisualsClient.xrayEnabled);
            addFeature(cx + rowW + 10, y, rowW, rowH, "Игроки", "Счётчик в HUD",
                    PumpkinVisualsClient.nearbyHud, () ->
                            PumpkinVisualsClient.nearbyHud = !PumpkinVisualsClient.nearbyHud);
        } else if (tab == Tab.PLAYER) {
            addFeature(cx, y, rowW, rowH, "Автоеда", "Есть при голоде",
                    PumpkinVisualsClient.autoEatEnabled, () ->
                            PumpkinVisualsClient.autoEatEnabled = !PumpkinVisualsClient.autoEatEnabled);
            addInfo(cx, y + 44, "Клавиша Z", "Включить или выключить автоеду.");
        } else if (tab == Tab.WORLD) {
            addInfo(cx, y, "Ore X-Ray", "Показывает контуры руд в загруженной области.");
            addInfo(cx, y + 44, "Важно", "Видимость сквозь стены пока не подтверждена.");
        } else {
            addInfo(cx, y, "PumpkinVisuals", "Fabric • Minecraft 1.21.6");
            addInfo(cx, y + 44, "Меню", "Открыть: Right Shift");
            addInfo(cx, y + 88, "Прокрутка", "Колёсико мыши для списка функций.");
        }

        addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close())
                .dimensions(left + pw - 92, top + ph - 29, 82, 20).build());
    }

    private Text nav(String name, Tab target) {
        return Text.literal((tab == target ? "§6» " : "§7") + name);
    }

    private void changeTab(Tab next) {
        tab = next;
        scroll = 0;
        rebuildWidgets();
    }

    private void addFeature(int x, int y, int w, int h, String title, String desc,
                            boolean enabled, Runnable action) {
        addDrawableChild(ButtonWidget.builder(
                Text.literal(title + "  §8|  " + (enabled ? "§aВКЛ" : "§cВЫКЛ")), b -> {
                    action.run();
                    rebuildWidgets();
                }).dimensions(x, y, w, h).build());
        featureButtons.add(null);
    }

    private void addInfo(int x, int y, String title, String desc) {
        // Descriptions are drawn in the content area, never over buttons.
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xA9000000);
        int pw = Math.min(650, width - 20);
        int ph = Math.min(390, height - 20);
        int left = (width - pw) / 2;
        int top = (height - ph) / 2;
        int side = 118;
        int cx = left + side + 16;
        int cw = pw - side - 30;
        int rowW = Math.min(190, cw - 8);
        int y = top + 91 - scroll;

        ctx.fill(left - 1, top - 1, left + pw + 1, top + ph + 1, ORANGE);
        ctx.fill(left, top, left + pw, top + ph, BG);
        ctx.fill(left, top, left + pw, top + 3, ORANGE);
        ctx.fill(left + 1, top + 3, left + side, top + ph - 1, PANEL);
        ctx.fill(left + side, top + 3, left + side + 1, top + ph - 1, LINE);

        ctx.drawTextWithShadow(textRenderer, "PUMPKIN", left + 12, top + 14, ORANGE);
        ctx.drawTextWithShadow(textRenderer, "VISUALS", left + 12, top + 27, WHITE);
        ctx.drawTextWithShadow(textRenderer, "HALLOWEEN", left + 12, top + 43, MUTED);
        ctx.fill(left + 8, top + 62, left + side - 8, top + 63, LINE);

        ctx.drawTextWithShadow(textRenderer, tabTitle(), cx, top + 16, WHITE);
        ctx.drawTextWithShadow(textRenderer, "Выбери функцию", cx, top + 34, MUTED);
        ctx.fill(cx, top + 54, left + pw - 12, top + 55, LINE);

        // Clip-like content boundary: only draw row labels within the panel's content region.
        if (tab == Tab.VISUALS) {
            drawFeatureLabel(ctx, cx, y - 20, "ЭФФЕКТЫ");
            drawFeatureLabel(ctx, cx, y + 22, "МИР");
            drawFeatureLabel(ctx, cx + rowW + 10, y - 20, "ИНФОРМАЦИЯ");
        } else if (tab == Tab.PLAYER) {
            drawFeatureLabel(ctx, cx, y - 20, "ПОМОЩНИК");
            drawFeatureLabel(ctx, cx, y + 25, "УПРАВЛЕНИЕ");
        } else if (tab == Tab.WORLD) {
            drawFeatureLabel(ctx, cx, y - 20, "РАЗВЕДКА");
            drawFeatureLabel(ctx, cx, y + 25, "ПРИМЕЧАНИЕ");
        } else {
            drawFeatureLabel(ctx, cx, y - 20, "О МОДЕ");
        }

        // Compact status footer.
        ctx.fill(cx, top + ph - 50, left + pw - 12, top + ph - 49, LINE);
        ctx.drawTextWithShadow(textRenderer, "RIGHT SHIFT  •  МЕНЮ", cx, top + ph - 38, MUTED);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawFeatureLabel(DrawContext ctx, int x, int y, String text) {
        ctx.drawTextWithShadow(textRenderer, text, x, y, ORANGE);
    }

    private String tabTitle() {
        return switch (tab) {
            case VISUALS -> "Визуал";
            case PLAYER -> "Игрок";
            case WORLD -> "Мир";
            case SETTINGS -> "Настройки";
        };
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int pw = Math.min(650, width - 20);
        int ph = Math.min(390, height - 20);
        int top = (height - ph) / 2;
        if (mouseY >= top + 60 && mouseY <= top + ph - 55) {
            scroll = Math.max(0, Math.min(160, scroll - (int)(verticalAmount * 24)));
            rebuildWidgets();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override public boolean shouldPause() { return false; }
}