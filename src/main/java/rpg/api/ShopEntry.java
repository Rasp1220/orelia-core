package rpg.api;

/**
 * One row of an NPC shop's stock, shared across plugins: orelia-world's NPC module builds
 * these from npc.yml and passes them to {@link GuiApi#openShop} rather than depending on
 * orelia-core's internal GUI classes. {@code kind} is {@code "WEAPON"}, {@code "ACCESSORY"},
 * or {@code "VANILLA"} (a plain vanilla Material).
 */
public record ShopEntry(String kind, String id, double price) {
}
