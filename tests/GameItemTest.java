import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GameItemTest {
    // return tests/items.ini
    private File itemsFile() {
        return Paths.get("tests", "items.ini").toFile();
    }

    private static GameItem findByName(GameItem[] items, String name) {
        for (GameItem g : items) {
            if (g != null && name.equals(g.getName())) return g;
        }
        return null;
    }

    //Getters: all fields must equal the given constructor args.
    @Test
    public void testGetter_basic() {
        GameItem HeavySword = new GameItem("Heavy Sword", 250, 15,
                10, -5, 0);

        assertEquals("Heavy Sword", HeavySword.getName());
        assertEquals(250, HeavySword.getValue());
        assertEquals(15, HeavySword.getWeight());
        assertEquals(10, HeavySword.getAttackBonus());
        assertEquals(-5, HeavySword.getAgilityBonus());
        assertEquals(0, HeavySword.getDefenseBonus());
    }

    // Getters: allow zero.
    @Test
    public void testGetters_zero() {
        GameItem weird = new GameItem("Weird", 0, 0, 0, 0, 0);
        assertEquals("Weird", weird.getName());
        assertEquals(0, weird.getValue());
        assertEquals(0, weird.getWeight());
        assertEquals(0, weird.getAttackBonus());
        assertEquals(0, weird.getAgilityBonus());
        assertEquals(0, weird.getDefenseBonus());
    }

    // Getters: allow negative numbers.
    @Test
    public void testGetters_negative() {
        GameItem weird = new GameItem("Weird", -30, -1, -2, -49, -43);
        assertEquals("Weird", weird.getName());
        assertEquals(-30, weird.getValue());
        assertEquals(-1, weird.getWeight());
        assertEquals(-2, weird.getAttackBonus());
        assertEquals(-49, weird.getAgilityBonus());
        assertEquals(-43, weird.getDefenseBonus());
    }

    // Parsing: [Heavy Sword], Value=250, Weight=15, AttackBonus=10, AgilityBonus=-5
    // validate all properties, including Type being ignored.
    @Test
    void testParse_heavySword() {
        GameItem[] items = GameItem.readItems(itemsFile());
        GameItem hs = findByName(items, "Heavy Sword");
        assertNotNull(hs, "Can't find [Heavy Sword] in items.ini");
        assertEquals(250, hs.getValue());
        assertEquals(15, hs.getWeight());
        assertEquals(10, hs.getAttackBonus());
        assertEquals(-5, hs.getAgilityBonus());
        assertEquals(0, hs.getDefenseBonus()); // Default
    }

    // Parsing: [Elven Cloak], Value=120, Weight=3, AgilityBonus=3, DefenseBonus=1
    // validate that property order does not affect parsing.
    @Test
    void testParse_elvenCloak() {
        GameItem[] items = GameItem.readItems(itemsFile());
        GameItem ec = findByName(items, "Elven Cloak");
        assertNotNull(ec, "Can't find [Elven Cloak] in items.ini");
        assertEquals(120, ec.getValue());
        assertEquals(3, ec.getWeight());
        assertEquals(0, ec.getAttackBonus()); // Default
        assertEquals(3, ec.getAgilityBonus());
        assertEquals(1, ec.getDefenseBonus());
    }

    // Parsing: [Health Potion], Value=50, Weight=1
    // validate only Value and Weight are set， others return 0.
    @Test
    void testParse_healthPotion() {
        GameItem[] items = GameItem.readItems(itemsFile());
        GameItem hp = findByName(items, "Health Potion");
        assertNotNull(hp, "Can't find [Health Potion] in items.ini");
        assertEquals(50, hp.getValue());
        assertEquals(1, hp.getWeight());
        assertEquals(0, hp.getAttackBonus());   // Default
        assertEquals(0, hp.getAgilityBonus());  // Default
        assertEquals(0, hp.getDefenseBonus());  // Default
    }

    // Parsing: [Wooden Shield], Value=5, Weight=10, AttackBonus=-7, AgilityBonus=-1, DefenseBonus=5
    @Test
    void parse_woodenShield() {
        GameItem[] items = GameItem.readItems(itemsFile());
        GameItem ws = findByName(items, "Wooden Shield");
        assertNotNull(ws, "Can't find [Wooden Shield] in items.ini");
        assertEquals(5, ws.getValue());
        assertEquals(10, ws.getWeight());
        assertEquals(-7, ws.getAttackBonus());
        assertEquals(-1, ws.getAgilityBonus());
        assertEquals(5, ws.getDefenseBonus());
    }

    // Edge Test: Validate unknown properties are ignored ([UnknownPropertiesAreIgnored]).
    @Test
    void testUnknownPropertiesAreIgnored() {
        GameItem[] items = GameItem.readItems(itemsFile());
        GameItem ignored = findByName(items, "UnknownPropertiesAreIgnored");
        assertNotNull(ignored, "Can't find [UnknownPropertiesAreIgnored] in items.ini");
        assertEquals(50, ignored.getValue(), "Value should be 50");
        assertEquals(10, ignored.getWeight(), "Weight should be 10");
        assertEquals(5, ignored.getAttackBonus(), "AttackBonus should be 5");
        assertEquals(0, ignored.getAgilityBonus(), "Unknown property should not affect AgilityBonus");
        assertEquals(0, ignored.getDefenseBonus(), "Unknown property should not affect DefenseBonus");
    }

    // Order Test: Inverted property order should not affect the result.
    @Test
    void testPropertyOrder() throws IOException {
        GameItem[] items = GameItem.readItems(itemsFile());
        GameItem ooo = findByName(items, "OutOfOrder");
        assertNotNull(ooo, "Can't find [OutOfOrder] in items.ini");
        // AgilityBonus=1, DefenseBonus=2, AttackBonus=1, Weight=2, Value=30
        assertEquals(30, ooo.getValue(), "Value false");
        assertEquals(2, ooo.getWeight(), "Weight false");
        assertEquals(-1, ooo.getAttackBonus(), "AttackBonus false");
        assertEquals(1, ooo.getAgilityBonus(), "AgilityBonus false");
        assertEquals(2, ooo.getDefenseBonus(), "DefenseBonus false");
    }

    // [Repeat]: Verify attribute duplication and take the last value.
    @Test
    void testParse_repeatProperties() {
        GameItem[] items = GameItem.readItems(itemsFile());
        GameItem repeat = findByName(items, "Repeat");
        assertNotNull(repeat, "Can't find [Repeat] in items.ini");
        assertEquals(55, repeat.getValue(), "Value mismatch (should take the last one: 55)");
        assertEquals(3, repeat.getWeight(), "Weight mismatch (should take the last one: 3)");
        assertEquals(4, repeat.getAttackBonus(), "AttackBonus mismatch (should take the last one: 6)");
        assertEquals(1, repeat.getAgilityBonus(), "AgilityBonus mismatch");
        assertEquals(0, repeat.getDefenseBonus(), "DefenseBonus mismatch");
    }

    @Test
    void testParse_emptyFile() throws IOException {
        File emptyFile = File.createTempFile("empty", ".ini");
        GameItem[] items = GameItem.readItems(emptyFile);
        assertEquals(0, items.length, "Empty INI file should yield empty items array");
        emptyFile.delete();
    }

    // Edge Test: Missing required property (e.g., Weight or Value).
    @Test
    void testParse_missingValueOrWeight_fromFile() {
        GameItem[] items = GameItem.readItems(itemsFile());
        GameItem mf = findByName(items, "MissingFields");
        assertNotNull(mf, "Can't find [MissingFields] in items.ini");
        assertEquals(0, mf.getValue(), "Missing Value should default to 0");
        assertEquals(0, mf.getWeight(), "Missing Weight should default to 0");
        assertEquals(5, mf.getAttackBonus(), "AttackBonus should be parsed");
        assertEquals(2, mf.getAgilityBonus(), "AgilityBonus should be parsed");
        assertEquals(0, mf.getDefenseBonus(), "DefenseBonus should default to 0");
    }
}
