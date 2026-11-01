package dev.shadowclient.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

/**
 * Funnels the version-specific input signatures (1.21.9+ Click/KeyInput/CharInput) into simple
 * primitive callbacks, so all Shadow screens are insulated from input API drift.
 */
public abstract class ShadowScreen extends Screen {
    protected ShadowScreen(String title) { super(Text.literal(title)); }

    protected boolean onClick(double mx, double my, int button) { return false; }
    protected boolean onRelease(double mx, double my, int button) { return false; }
    protected boolean onDrag(double mx, double my, int button) { return false; }
    protected boolean onKey(int key, int mods) { return false; }
    protected boolean onChar(int codepoint) { return false; }
    protected boolean onScroll(double mx, double my, double amount) { return false; }

    @Override public boolean mouseClicked(Click click, boolean doubled) {
        return onClick(click.x(), click.y(), click.button()) || super.mouseClicked(click, doubled);
    }
    @Override public boolean mouseReleased(Click click) {
        boolean r = onRelease(click.x(), click.y(), click.button());
        return r || super.mouseReleased(click);
    }
    @Override public boolean mouseDragged(Click click, double dx, double dy) {
        return onDrag(click.x(), click.y(), click.button()) || super.mouseDragged(click, dx, dy);
    }
    @Override public boolean keyPressed(KeyInput input) {
        return onKey(input.key(), input.modifiers()) || super.keyPressed(input);
    }
    @Override public boolean charTyped(CharInput input) {
        return onChar(input.codepoint()) || super.charTyped(input);
    }
    @Override public boolean mouseScrolled(double mx, double my, double h, double v) {
        return onScroll(mx, my, v) || super.mouseScrolled(mx, my, h, v);
    }
    @Override public boolean shouldPause() { return false; }

    protected static boolean in(double mx, double my, int x, int y, int w, int h) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    protected static boolean in(double mx, double my, int[] r) { return in(mx, my, r[0], r[1], r[2], r[3]); }
    protected static void set(int[] r, int x, int y, int w, int h) { r[0] = x; r[1] = y; r[2] = w; r[3] = h; }
}
