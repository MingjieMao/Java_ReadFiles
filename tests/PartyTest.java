import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class PartyTest {
    public PlayerCharacter[] loadCharacters(File file) {
        GameItem[] allItems = GameItem.readItems(Paths.get("tests", "items.ini").toFile());
        return PlayerCharacter.readCharacters(file, allItems);
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
     * Creates a new temporary directory with the given prefix and marks it for deletion on JVM exit.
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

    // directory contains one empty .ini file -> expect one Party with null name and no members
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

        Party[] parties = Party.loadParties(all, dir);
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
