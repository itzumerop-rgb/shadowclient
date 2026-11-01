package dev.shadowclient.util;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/** Sample data used by the HUD editor preview. */
public final class Samples {
    private Samples() { }
    private static ItemStack[] armor;
    private static ItemStack held, totem, shield;

    public static ItemStack[] armor() {
        if (armor == null) {
            armor = new ItemStack[]{new ItemStack(Items.NETHERITE_HELMET), new ItemStack(Items.NETHERITE_CHESTPLATE),
                    new ItemStack(Items.DIAMOND_LEGGINGS), new ItemStack(Items.DIAMOND_BOOTS)};
            armor[0].setDamage(60); armor[1].setDamage(180); armor[2].setDamage(120); armor[3].setDamage(30);
        }
        return armor;
    }
    public static ItemStack held() { if (held == null) held = new ItemStack(Items.DIAMOND_SWORD); return held; }
    public static ItemStack totem() { if (totem == null) totem = new ItemStack(Items.TOTEM_OF_UNDYING); return totem; }
    public static ItemStack shield() { if (shield == null) shield = new ItemStack(Items.SHIELD); return shield; }
}
