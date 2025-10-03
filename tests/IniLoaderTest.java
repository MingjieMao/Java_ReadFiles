import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class IniLoaderTest {
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


    // Part 1

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
        GameItem[] items = IniLoader.readItems(itemsFile());
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
        GameItem[] items = IniLoader.readItems(itemsFile());
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
        GameItem[] items = IniLoader.readItems(itemsFile());
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
        GameItem[] items = IniLoader.readItems(itemsFile());
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
        GameItem[] items = IniLoader.readItems(itemsFile());
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
        GameItem[] items = IniLoader.readItems(itemsFile());
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
        GameItem[] items = IniLoader.readItems(itemsFile());
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
        GameItem[] items = IniLoader.readItems(emptyFile);
        assertEquals(0, items.length, "Empty INI file should yield empty items array");
        emptyFile.delete();
    }

    // Edge Test: Missing required property (e.g., Weight or Value).
    @Test
    void testParse_missingValueOrWeight_fromFile() {
        GameItem[] items = IniLoader.readItems(itemsFile());
        GameItem mf = findByName(items, "MissingFields");
        assertNotNull(mf, "Can't find [MissingFields] in items.ini");
        assertEquals(0, mf.getValue(), "Missing Value should default to 0");
        assertEquals(0, mf.getWeight(), "Missing Weight should default to 0");
        assertEquals(5, mf.getAttackBonus(), "AttackBonus should be parsed");
        assertEquals(2, mf.getAgilityBonus(), "AgilityBonus should be parsed");
        assertEquals(0, mf.getDefenseBonus(), "DefenseBonus should default to 0");
    }


    // Part 2

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
        GameItem[] items = IniLoader.readItems(itemsFile());
        assertNotNull(items, "items.ini should not be null");
        assertTrue(items.length > 0, "items.ini should not be empty");
        return items;
    }

    // Load all characters
    private PlayerCharacter[] loadAllChars(GameItem[] allItems) {
        PlayerCharacter[] pcs = IniLoader.readCharacters(charactersFile(), allItems);
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
    void testReadCharacters_emptyFile() throws IOException {
        // Prepare items first (characters parsing depends on items)
        GameItem[] allItems = loadAllItems();

        // Create a temporary empty characters.ini
        File emptyChars = File.createTempFile("charactersEmpty", ".ini");
        Files.writeString(emptyChars.toPath(), "");  // empty content

        PlayerCharacter[] pcs = IniLoader.readCharacters(emptyChars, allItems);
        assertNotNull(pcs, "Should not return null for empty file");
        assertEquals(0, pcs.length, "Empty characters.ini should yield empty array");

        emptyChars.delete();
    }

    // characters.ini: section missing base stats defaults to 0
    @Test
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


    // Part 3

    public PlayerCharacter[] loadCharacters(File file) {
        GameItem[] allItems = IniLoader.readItems(Paths.get("tests", "items.ini").toFile());
        return IniLoader.readCharacters(file, allItems);
    }

    private File partiesDir() {
        // tests/Parties
        return Paths.get("tests", "Parties").toFile();
    }

    private HashMap<String, PlayerCharacter> indexByName(PlayerCharacter[] all) {
        HashMap<String, PlayerCharacter> map = new HashMap<>();
        for (PlayerCharacter pc : all) {
            if (pc != null && pc.getName() != null) map.put(pc.getName(), pc);
        }
        return map;
    }

    @Test
    public void testConstructorAndGetName() {
        Party p = new Party("fellowship");
        assertEquals("fellowship", p.getName());
    }

    @Test
    public void testGetMembersDefensiveCopy() {
        // Load all characters from characters.ini
        PlayerCharacter[] all = loadCharacters(Paths.get("tests", "characters.ini").toFile());
        HashMap<String, PlayerCharacter> byName = indexByName(all);

        // Create a new Party and add two members
        Party p = new Party("test");
        p.addMember(byName.get("Gimli"));
        p.addMember(byName.get("Legolas"));

        // First retrieval of members
        PlayerCharacter[] a1 = p.getMembers();
        assertEquals(2, a1.length, "Initial size should be 2");

        // Modify the returned array (this should NOT affect the internal party state)
        PlayerCharacter savedFirst = a1[0];
        a1[0] = a1[1];

        // Retrieve members again
        PlayerCharacter[] a2 = p.getMembers();
        assertEquals(2, a2.length, "Size should still be 2 after modification of the copy");
        assertSame(savedFirst, a2[0], "Modifying the returned array should not change internal order");
    }

    @Test
    public void testAddRemoveMember() {
        // Load all characters from characters.ini
        PlayerCharacter[] all = loadCharacters(Paths.get("tests", "characters.ini").toFile());
        HashMap<String, PlayerCharacter> byName = indexByName(all);

        // Create a new Party and prepare two members
        Party p = new Party("team");
        PlayerCharacter g = byName.get("Gimli");
        PlayerCharacter l = byName.get("Legolas");

        // Add members, duplicate add should be ignored
        p.addMember(g);
        p.addMember(g);
        p.addMember(l);

        // Check: duplicates do not increase size
        assertEquals(2, p.getMembers().length, "Adding a duplicate member should not increase the size");

        // Remove a member
        p.removeMember(g);
        assertEquals(1, p.getMembers().length, "Removing a member should decrease the size by one");
        assertEquals("Legolas", p.getMembers()[0].getName(),
                "After removing Gimli, the only remaining member should be Legolas");
    }

    @Test
    public void testComputeCombinedAttackRating() {
        // Load all characters from characters.ini
        PlayerCharacter[] all = loadCharacters(Paths.get("tests", "characters.ini").toFile());
        HashMap<String, PlayerCharacter> byName = indexByName(all);

        // Create a new Party and add two members
        Party p = new Party("rating");
        PlayerCharacter g = byName.get("Gimli");
        PlayerCharacter l = byName.get("Legolas");
        p.addMember(g);
        p.addMember(l);

        // Expected combined attack rating = sum of individual strengths
        int expected = g.computeTotalStrength() + l.computeTotalStrength();
        // Verify that the Party correctly computes the combined rating
        assertEquals(expected, p.computeCombinedAttackRating());
    }

    @Test
    public void testLoadPartiesFellowship() {
        // 1. Load all characters from characters.ini
        PlayerCharacter[] all = loadCharacters(Paths.get("tests", "characters.ini").toFile());

        // 2. Load parties from tests/Parties
        Party[] parties = Party.loadParties(all, new File("tests/Parties"));
        assertNotNull(parties, "parties array should not be null");
        assertTrue(parties.length >= 1, "should load at least one party");

        // 3. Find the party named "fellowship"
        Party fellowship = null;
        for (Party p : parties) {
            if (p != null && p.getName() != null && p.getName().equalsIgnoreCase("fellowship")) {
                fellowship = p;
                break;
            }
        }
        assertNotNull(fellowship, "party named 'fellowship' must exist");

        // 4. Check member count and order
        PlayerCharacter[] members = fellowship.getMembers();
        assertEquals(3, members.length, "member count should be 3");

        String[] expected = {"Gimli", "Legolas", "Aragorn"};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], members[i].getName(), "wrong name at index " + i);
        }
    }

    @Test
    public void testStorePartyNoThrowForNow() {
        // Load characters and build a lookup map
        PlayerCharacter[] all = loadCharacters(Paths.get("tests", "characters.ini").toFile());
        HashMap<String, PlayerCharacter> byName = indexByName(all);

        // Create a party and add two members
        Party p = new Party("saveTest");
        p.addMember(byName.get("Gimli"));
        p.addMember(byName.get("Aragorn"));

        // Current requirement: just ensure no exception is thrown when calling storeParty
        // (Enable file content assertions after you implement actual writing.)
        assertDoesNotThrow(() -> p.storeParty(new File("out")));
    }

    /**
     * Writes the given text content to the target file (overwriting if it exists).
     *
     * @param f target file (may be in a non-existent parent directory)
     * @param content text to write into the file (written as-is)
     * @throws Exception wraps/propagates any I/O error
     * @implSpec Precondition: {@code f != null}.
     *           Postcondition: {@code f} exists and its contents equal {@code content}.
     */
    private static void writeText(File f, String content) throws Exception {
        // Ensure parent directory exists
        File parent = f.getParentFile();
        if (parent != null) parent.mkdirs();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(f))) {
            w.write(content);
        }
    }

    /**
     * Creates a new temporary directory with the given prefix and marks it for deletion on JVM exit.
     *
     * Effects: creates a new directory on the filesystem; schedules deletion on JVM exit.
     *
     * @param prefix prefix string to help identify the temp directory
     * @return a {@link File} pointing to the newly created temporary directory
     * @throws Exception if the directory cannot be created
     * @implSpec Precondition: {@code prefix != null}.
     *           Postcondition: returned directory exists and is empty.
     */
    private static File newTempDir(String prefix) throws Exception {
        Path p = Files.createTempDirectory(prefix);
        File dir = p.toFile();
        dir.deleteOnExit();
        return dir;
    }

    @Test
    public void testLoadPartiesEmptyDirectory() throws Exception {
        // Arrange: create an empty temporary directory (no .ini files inside)
        File emptyDir = newTempDir("parties-empty-");

        // Load all characters from the existing test file (needed by loadParties)
        PlayerCharacter[] all = loadCharacters(Paths.get("tests", "characters.ini").toFile());

        // Act: load parties from the empty directory
        Party[] parties = Party.loadParties(all, emptyDir);

        // Assert: result is a non-null empty array
        assertNotNull(parties, "should not be null even for empty dir");
        assertEquals(0, parties.length, "no ini files -> no parties");
    }

    // loadParties: empty ini -> one Party with null name and no members
    @Test
    public void testLoadPartiesWithEmptyIni() throws Exception {
        // Arrange: temp directory + an empty .ini file
        File dir = newTempDir("parties-empty-ini-");
        File emptyIni = new File(dir, "empty.ini");
        writeText(emptyIni, ""); // truly empty file

        // Characters are required to resolve names during loading
        PlayerCharacter[] all = loadCharacters(Paths.get("tests", "characters.ini").toFile());

        // Act: load parties from the directory containing the empty ini
        Party[] parties = Party.loadParties(all, dir);

        // Assert: exactly one Party is produced, with null name and no members
        assertNotNull(parties);
        assertEquals(1, parties.length, "one empty ini -> one Party object constructed");
        Party p = parties[0];
        assertNull(p.getName(), "empty ini -> party name is null");
        assertEquals(0, p.getMembers().length, "empty ini -> no members");
    }

    // loadParties: missing member names are skipped
    @Test
    public void testLoadPartiesSkipsMissingMembers() throws Exception {
        // Arrange: temp directory; place a prepared ini (with one missing member) into the directory
        File dir = newTempDir("parties-missing-members-");
        Path src = Paths.get("tests", "Parties", "mix.ini");
        Path dst = dir.toPath().resolve("mix.ini");
        Files.copy(src, dst);

        // Characters needed for name resolution
        PlayerCharacter[] all = loadCharacters(Paths.get("tests", "characters.ini").toFile());

        // Act
        Party[] parties = Party.loadParties(all, dir);

        // Assert
        assertNotNull(parties);
        assertEquals(1, parties.length);
        Party p = parties[0];
        assertEquals("mix", p.getName());

        PlayerCharacter[] members = p.getMembers();
        assertEquals(2, members.length); // the missing member was skipped
        assertEquals("Gimli",   members[0].getName());
        assertEquals("Legolas", members[1].getName());
    }
}

