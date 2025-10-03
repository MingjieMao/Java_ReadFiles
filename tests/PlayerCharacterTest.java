import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerCharacterTest {
    // return tests/items.ini
    private File itemsFile() {
        return Paths.get("tests", "items.ini").toFile();
    }

    // return tests/characters.ini
    private File charactersFile() {
        return Paths.get("tests", "characters.ini").toFile();
    }

    // Convert GameItem[] to Set<String> of names
    private static Set<String> toNameSet(GameItem[] inv) {
        Set<String> s = new HashSet<>();
        if (inv != null) {
            for (GameItem it : inv) {
                if (it != null) s.add(it.getName());
            }
        }
        return s;
    }

    // Load all items from items.ini
    private GameItem[] loadAllItems() {
        GameItem[] items = GameItem.readItems(itemsFile());
        assertNotNull(items, "items.ini should not be null");
        assertTrue(items.length > 0, "items.ini should not be empty");
        return items;
    }

    // Load all characters
    private PlayerCharacter[] loadAllChars(GameItem[] allItems) {
        PlayerCharacter[] pcs = PlayerCharacter.readCharacters(charactersFile(), allItems);
        assertNotNull(pcs, "characters.ini returned null");
        assertTrue(pcs.length >= 0, "Characters should not be empty");
        return pcs;
    }

    // Find a character by name
    private static PlayerCharacter findByName(PlayerCharacter[] pcs, String name) {
        for (PlayerCharacter pc : pcs) {
            if (pc != null && name.equals(pc.getName())) return pc;
        }
        return null;
    }

    // Convert inventory to set of item names (ignoring nulls)
    private static Set<String> namesOf(GameItem[] inv) {
        Set<String> set = new HashSet<>();
        if (inv != null) {
            for (GameItem gi : inv) if (gi != null) set.add(gi.getName());
        }
        return set;
    }

    @Test
    void testGimli() {
        // [Gimli]: Strength=18, Dexterity=12, Fortitude=30, Inventory=Battle Axe,Chainmail,Health Potion
        // [Battle Axe]: AttackBonus=6, AgilityBonus=-2
        // [Chainmail]: AgilityBonus=-3, DefenseBonus=4
        // [Health Potion]: 0
        GameItem[] all = loadAllItems();
        PlayerCharacter[] pcs = loadAllChars(all);
        PlayerCharacter g = findByName(pcs, "Gimli");
        assertNotNull(g, "Can't find [Gimli]");
        Set<String> expected = new HashSet<>(Arrays.asList("Battle Axe", "Chainmail", "Health Potion"));
        assertEquals(expected, namesOf(g.getInventory()), "Gimli inventory mismatch");
        // Strength = 18 + 6 = 24
        // Dexterity = 12 - 2 - 3 = 7
        // Fortitude = 30 + 4 = 34
        assertEquals(24, g.computeTotalStrength(), "Total Strength mismatch");
        assertEquals(7, g.computeTotalDexterity(), "Total Dexterity mismatch ");
        assertEquals(34, g.computeTotalFortitude(), "Total Fortitude mismatch ");
    }

    @Test
    void testLegolas() {
        // [Legolas]: Strength=14, Dexterity=20, Fortitude=25, Inventory=Longbow,Leather Armor,Arrows
        // [Longbow]: AttackBonus=5
        // [Leather Armor]: AgilityBonus=-1, DefenseBonus=2
        // [Arrows]: 0
        GameItem[] all = loadAllItems();
        PlayerCharacter[] pcs = loadAllChars(all);
        PlayerCharacter l = findByName(pcs, "Legolas");
        assertNotNull(l, "Can't find [Legolas]");
        Set<String> expected = new HashSet<>(Arrays.asList("Longbow", "Leather Armor", "Arrows"));
        assertEquals(expected, namesOf(l.getInventory()), "Legolas inventory mismatch");
        // Totals: Str=14+5=19, Dex=20-1=19, Fort=25+2=27
        assertEquals(19, l.computeTotalStrength(), "Total Strength mismatch");
        assertEquals(19, l.computeTotalDexterity(), "Total Dexterity mismatch");
        assertEquals(27, l.computeTotalFortitude(), "Total Fortitude mismatch");
    }

    @Test
    void testClara_emptyInventory() {
        // Inventory=
        GameItem[] all = loadAllItems();
        PlayerCharacter[] pcs = loadAllChars(all);
        PlayerCharacter c = findByName(pcs, "Clara");
        assertNotNull(c, "Can't find [Clara]");
        assertEquals(0, c.getInventory().length, "Empty Inventory should yield empty array");
        // Base only: 10, 11, 12
        assertEquals(10, c.computeTotalStrength(), "Total Strength should equal base");
        assertEquals(11, c.computeTotalDexterity(), "Total Dexterity should equal base");
        assertEquals(12, c.computeTotalFortitude(), "Total Fortitude should equal base");
    }

    @Test
    void testDave_missingInventory() {
        GameItem[] all = loadAllItems();
        PlayerCharacter[] pcs = loadAllChars(all);
        PlayerCharacter d = findByName(pcs, "Dave");
        assertNotNull(d, "Can't find [Dave]");
        assertEquals(0, d.getInventory().length, "Missing Inventory should yield empty array");
        // Base only: 7, 8, 9
        assertEquals(7, d.computeTotalStrength(), "Total Strength should equal base");
        assertEquals(8, d.computeTotalDexterity(), "Total Dexterity should equal base");
        assertEquals(9, d.computeTotalFortitude(), "Total Fortitude should equal base");
    }

    @Test
    void testPotionTester() {
        // [PotionTester]: Strength=5, Dexterity=5, Fortitude=5, Inventory=Health Potion,Health Potion,Wooden Shield
        // [Health Potion]: 0
        // [Wooden Shield]: AttackBonus=-7, AgilityBonus=-1, DefenseBonus=5
        GameItem[] all = loadAllItems();
        PlayerCharacter[] pcs = loadAllChars(all);
        PlayerCharacter p = findByName(pcs, "PotionTester");
        assertNotNull(p, "Can't find [PotionTester]");
        assertEquals(3, p.getInventory().length, "Should have 3 entries");
        // Base(5,5,5) + Shield(-7,-1,+5)
        assertEquals(-2, p.computeTotalStrength(), "Total Strength mismatch");
        assertEquals(4,  p.computeTotalDexterity(), "Total Dexterity mismatch");
        assertEquals(10, p.computeTotalFortitude(), "Total Fortitude mismatch");
    }

    @Test
    void testOrderFree() {
        // [OrderFree]: Dexterity=3, Inventory=Elven Cloak, Strength=2, Fortitude=4
        // [Elven Cloak]: AgilityBonus=3, DefenseBonus=1
        GameItem[] all = loadAllItems();
        PlayerCharacter[] pcs = loadAllChars(all);
        PlayerCharacter o = findByName(pcs, "OrderFree");
        assertNotNull(o, "Can't find [OrderFree]");

        Set<String> expected = new HashSet<>(Arrays.asList("Elven Cloak"));
        assertEquals(expected, namesOf(o.getInventory()), "Inventory mismatch");

        assertEquals(2, o.getStrength(),  "Base Strength mismatch");
        assertEquals(3, o.getDexterity(), "Base Dexterity mismatch");
        assertEquals(4, o.getFortitude(), "Base Fortitude mismatch");
        // Totals: Str=2+0=2, Dex=3+3=6, Fort=4+1=5
        assertEquals(2, o.computeTotalStrength(),  "Total Strength mismatch");
        assertEquals(6, o.computeTotalDexterity(), "Total Dexterity mismatch");
        assertEquals(5, o.computeTotalFortitude(), "Total Fortitude mismatch");
    }

    // Edge: empty characters file -> empty array
    @Test
    @DisplayName("readCharacters: empty file yields empty array")
    void testReadCharacters_emptyFile() throws IOException {
        // Prepare items first (characters parsing depends on items)
        GameItem[] allItems = loadAllItems();

        // Create a temporary empty characters.ini
        File emptyChars = File.createTempFile("charactersEmpty", ".ini");
        Files.writeString(emptyChars.toPath(), "");  // empty content

        PlayerCharacter[] pcs = PlayerCharacter.readCharacters(emptyChars, allItems);
        assertNotNull(pcs, "Should not return null for empty file");
        assertEquals(0, pcs.length, "Empty characters.ini should yield empty array");

        emptyChars.delete();
    }

    @Test
    @DisplayName("characters.ini: section missing base stats defaults to 0")
    void testCharacters_missingBaseStats_fromFile() {
        GameItem[] allItems = loadAllItems();
        PlayerCharacter[] pcs = loadAllChars(allItems);
        PlayerCharacter nb = findByName(pcs, "NoBase");
        assertNotNull(nb, "Can't find [NoBase] in characters.ini");

        // Missing base stats → default to 0
        assertEquals(0, nb.getStrength(),  "Missing Strength should default to 0");
        assertEquals(0, nb.getDexterity(), "Missing Dexterity should default to 0");
        assertEquals(0, nb.getFortitude(), "Missing Fortitude should default to 0");

        // Inventory should still be parsed correctly
        var expectedInv = new java.util.HashSet<>(java.util.Arrays.asList("Health Potion", "Wooden Shield"));
        assertEquals(expectedInv, namesOf(nb.getInventory()), "Inventory should be parsed even when base stats are missing");
    }

}
