import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Party {
    private final String name;
    private final List<PlayerCharacter> members = new ArrayList<>();

    /**
     * Creates a new empty Party with a given name.
     */
    public Party(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the party (e.g., from the filename).
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the characters who are members of this party.
     */
    public PlayerCharacter[] getMembers() {
        return members.toArray(new PlayerCharacter[0]);
    }

    /**
     * Returns the combined attack rating of the party, calculated as the
     * sum of every member's computeTotalStrength().
     */
    public int computeCombinedAttackRating() {
        int sum = 0;
        for (PlayerCharacter player : members) {
            sum = sum + player.computeTotalStrength();
        }
        return sum;
    }

    /**
     * Adds a character to the party.
     */
    public void addMember(PlayerCharacter character) {
        if (character != null) {
            members.add(character);
        }
    }

    /**
     * Removes a character from the party.
     */
    public void removeMember(PlayerCharacter character) {
        members.remove(character);
    }

    /**
     * Saves the current state of the party to an appropriately named file
     * ("[name].ini") in the given directory.
     */
    public void storeParty(File directory) {
        File file = new File(directory, name + ".ini");
        try(var writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Members=");
            for (int i=0; i < members.size(); i++) {
                writer.write(members.get(i).getName());
                if (i < members.size()-1) {
                    writer.write(",");
                }
            }
            writer.newLine();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Given a directory containing party files and a list of all available
     * characters in the game, loads all parties from the INI files in the
     * given directory. Assume that all INI files in the directory are
     * non-empty and valid INI Party files. Moreover, assume that all
     * characters in the Party files are contained in allCharacters.
     */
    public static Party[] loadParties(PlayerCharacter[] allCharacters, File directory){
        List<Party> parties = new ArrayList<>();
        Map<String, PlayerCharacter> lookup = new HashMap<>();
        for (PlayerCharacter player : allCharacters) {
            lookup.put(player.getName(), player);
        }
    }

}

