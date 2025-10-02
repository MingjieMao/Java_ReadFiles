import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class PartyTest {
    // return tests/items.ini
    private File itemsFile() {
        return Paths.get("tests", "items.ini").toFile();
    }

    // return tests/characters.ini
    private File charactersFile() {
        return Paths.get("tests", "characters.ini").toFile();
    }

    // return tests/fellowship.ini
    private File partiesDir() {
        return Paths.get("tests","fellowship.ini").toFile();
    }

    private GameItem[] loadAllItems() {
        GameItem[] items = GameItem.readItems(itemsFile());
        assertNotNull(items, "should not be null");
        assertTrue(items.length > 0, "items.ini should not be empty");
        return items;
    }

    private PlayerCharacter[] loadAllCharacters() {
        GameItem[] allItems = loadAllItems();
        PlayerCharacter[] pcs = PlayerCharacter.readCharacters(charactersFile(), allItems);
        assertNotNull(pcs, "characters.ini should not be null");
        assertTrue(pcs.length >= 0, "characters should not be null");
        return pcs;
    }

    private static PlayerCharacter findChar(PlayerCharacter[] pcs, String name) {
        for (PlayerCharacter pc : pcs) {
            if (pc != null && name.equals(pc.getName())) {
                return pc;
            }
        }
        return null;
    }

    private static Party findParty(Party[] parties, String name) {
        for (Party p : parties) {
            if (p != null && name.equals(p.getName())) {
                return p;
            }
        }
        return null;
    }

    private static Set<String> namesOfMembers(Party p) {
        Set<String> set = new HashSet<>();
        for (PlayerCharacter pc : p.getMembers()) {
            if (pc != null) {
                set.add(pc.getName());
            }
        }
        return set;
    }

    // Load fellowship from directory & verify members and totals
    @Test
    void testLoadParties() {
        PlayerCharacter[] allChars = loadAllCharacters();
        Party[] parties = Party.loadParties(allChars, partiesDir());
        assertNotNull(parties, "loadParties should not be null");

        assertTrue(parties.length >= 1, "No teams have been loaded.");

        Party fellowship = findParty(parties, "fellowship");
        assertNotNull(fellowship, "Can't find party [fellowship]");

        Set<String> expected = new HashSet<>(Arrays.asList("Gimli", "Legolas"));
        assertEquals(expected, namesOfMembers(fellowship), "Members mismatch");
        // Gimli 24 + Legolas 19 = 43
        assertEquals(43, fellowship.computeCombinedAttackRating(), "Combined attack mismatch");
    }




}
