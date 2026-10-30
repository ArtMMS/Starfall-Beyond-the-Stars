package com.artmms.starfall.client;

import java.util.List;
import java.util.Locale;

import com.artmms.starfall.entity.rocket.RocketEntity;
import com.artmms.starfall.network.RocketActionPayload;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Rocket control panel: fuel, capacity, fill percentage, efficiency,
 * launch cost, crew and the launch button.
 * The tank and engine buttons are temporary (until real upgrades exist).
 */
public class RocketScreen extends Screen {
    private static final int WIDTH = 240;
    private static final int HEIGHT = 206;
    private static final int PADDING = 10;
    private static final int LINE = 12;

    private static final int PANEL = 0xEE1B1F2A;
    private static final int BORDER = 0xFF6C7A99;
    private static final int TEXT = 0xFFE6ECFF;
    private static final int MUTED = 0xFF9AA6C2;
    private static final int GOOD = 0xFF6EE7A0;
    private static final int BAD = 0xFFFF7A7A;
    private static final int BAR_BACK = 0xFF2C3242;
    private static final int BAR_FUEL = 0xFFE8A33D;

    private final RocketEntity rocket;
    private Button launchButton;
    private Button tankButton;
    private Button enginesButton;

    public RocketScreen(RocketEntity rocket) {
        super(Component.translatable("gui.starfall.rocket.title"));
        this.rocket = rocket;
    }

    private int left() {
        return (width - WIDTH) / 2;
    }

    private int top() {
        return (height - HEIGHT) / 2;
    }

    @Override
    protected void init() {
        int buttonWidth = WIDTH - PADDING * 2;
        int x = left() + PADDING;
        int y = top() + HEIGHT - PADDING - 20 * 3 - 4 * 2;

        tankButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.starfall.rocket.button.tank"),
                        button -> send(RocketActionPayload.CYCLE_TANK))
                .bounds(x, y, (buttonWidth - 4) / 2, 20).build());
        enginesButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.starfall.rocket.button.engines"),
                        button -> send(RocketActionPayload.TOGGLE_ENGINES))
                .bounds(x + (buttonWidth + 4) / 2, y, (buttonWidth - 4) / 2, 20).build());
        launchButton = addRenderableWidget(Button.builder(
                        Component.translatable("gui.starfall.rocket.button.launch"),
                        button -> send(RocketActionPayload.LAUNCH))
                .bounds(x, y + 24, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(
                        Component.translatable("gui.starfall.rocket.button.close"),
                        button -> onClose())
                .bounds(x, y + 48, buttonWidth, 20).build());
    }

    private void send(int action) {
        PacketDistributor.sendToServer(new RocketActionPayload(rocket.getId(), action));
    }

    @Override
    public void tick() {
        super.tick();
        if (rocket.isRemoved()) {
            onClose();
            return;
        }
        boolean idle = !rocket.isFlying();
        tankButton.active = idle;
        enginesButton.active = idle;
        launchButton.active = idle && rocket.getFuel() >= rocket.getLaunchCost()
                && rocket.getFirstPassenger() != null;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        int x = left();
        int y = top();
        graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, BORDER);
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, PANEL);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);

        int x = left() + PADDING;
        int y = top() + PADDING;

        graphics.drawString(font, title, x, y, TEXT);
        y += LINE + 4;

        // Fuel bar
        int fuel = rocket.getFuel();
        int capacity = Math.max(1, rocket.getCapacity());
        int percent = Math.round(fuel * 100.0F / capacity);
        int barWidth = WIDTH - PADDING * 2;
        graphics.fill(x, y, x + barWidth, y + 8, BAR_BACK);
        graphics.fill(x, y, x + Math.round(barWidth * (fuel / (float) capacity)), y + 8, BAR_FUEL);
        y += 12;

        graphics.drawString(font, Component.translatable("gui.starfall.rocket.fuel", fuel, capacity, percent),
                x, y, TEXT);
        y += LINE;
        graphics.drawString(font, Component.translatable("gui.starfall.rocket.tank",
                Component.translatable(rocket.getTankTier().translationKey())), x, y, MUTED);
        y += LINE;

        double speed = rocket.getSpeedMultiplier();
        graphics.drawString(font, Component.translatable("gui.starfall.rocket.engines",
                rocket.getEngines(), format(speed)), x, y, MUTED);
        y += LINE;
        graphics.drawString(font, Component.translatable("gui.starfall.rocket.efficiency",
                Math.round(speed * 100)), x, y, MUTED);
        y += LINE;

        int cost = rocket.getLaunchCost();
        graphics.drawString(font, Component.translatable("gui.starfall.rocket.launch_cost", cost),
                x, y, fuel >= cost ? MUTED : BAD);
        y += LINE;

        graphics.drawString(font, crewLine(), x, y, MUTED);
        y += LINE;

        Component status = statusLine(fuel, cost);
        boolean ready = !rocket.isFlying() && fuel >= cost && rocket.getFirstPassenger() != null;
        graphics.drawString(font, status, x, y, ready ? GOOD : BAD);
    }

    private Component crewLine() {
        List<Entity> crew = rocket.getPassengers();
        Component empty = Component.translatable("gui.starfall.rocket.empty_seat");
        Component pilot = crew.size() > 0 ? crew.get(0).getName() : empty;
        Component copilot = crew.size() > 1 ? crew.get(1).getName() : empty;
        return Component.translatable("gui.starfall.rocket.crew", pilot, copilot);
    }

    private Component statusLine(int fuel, int cost) {
        if (rocket.isFlying()) {
            return Component.translatable("gui.starfall.rocket.status.flying");
        }
        if (rocket.getFirstPassenger() == null) {
            return Component.translatable("gui.starfall.rocket.status.no_pilot");
        }
        if (fuel < cost) {
            return Component.translatable("gui.starfall.rocket.status.no_fuel");
        }
        return Component.translatable("gui.starfall.rocket.status.ready");
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
