import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PartyTest {
    public PlayerCharacter[] loadCharacters(File file) {
        GameItem[] allItems = GameItem.readItems(Paths.get("tests", "items.ini").toFile());
        return PlayerCharacter.readCharacters(file, allItems);
    }

    private File partiesDir() {
        // tests/Parties 目录（相对项目根目录）
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

}
