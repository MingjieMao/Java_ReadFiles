import java.io.*;
import java.util.*;

/**
 *
 */
public class Party {
    private final String name;
    private final List<PlayerCharacter> members;

    /**
     * Creates a new empty Party with a given name.
     * Examples：
     * - new Party("fellowship");
     * - new Party("A");
     * - new Party("B");
     */
    public Party(String name) {
        this.name = name;
        this.members = new ArrayList<>();
    }

    /**
     * Returns the name of the party (e.g., from the filename).
     * Examples：
     * - new Party("fellowship").getName() -> "fellowship"
     * - new Party("A").getName() -> "A"
     * - new Party("B").getName() -> "B"
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the characters who are members of this party.
     * Examples：
     * -
     */
    public PlayerCharacter[] getMembers() {
        return members.toArray(new PlayerCharacter[0]);
    }

    /**
     * Returns the combined attack rating of the party, calculated as the
     * sum of every member's computeTotalStrength().
     * Examples：
     * -
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
        if (!members.contains(character)) {
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
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < members.size(); i++) {
            sb.append(members.get(i).getName());
            if (i < members.size()-1) {
                sb.append(",");
            }
        }
        String memberString = sb.toString();
    }

    /**
     * Given a directory containing party files and a list of all available
     * characters in the game, loads all parties from the INI files in the
     * given directory. Assume that all INI files in the directory are
     * non-empty and valid INI Party files. Moreover, assume that all
     * characters in the Party files are contained in allCharacters.
     *
     * @param allCharacters An array of all PlayerCharacters in the game.
     * @param directory The File object representing the directory containing the party INI files.
     * @return An array of Party objects loaded from the directory.
     */
    public static Party[] loadParties(PlayerCharacter[] allCharacters, File directory){
        HashMap<String, PlayerCharacter> byName = new HashMap<>();
        for (PlayerCharacter pc : allCharacters) {
            if (pc != null && pc.getName() != null) byName.put(pc.getName(), pc);
        }

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".ini"));
        List<Party> out = new ArrayList<>();
        for (File f : files) {
            Party p = loadOnePartyFromIniFile(f, byName);
            if (p != null) out.add(p);
        }
        return out.toArray(new Party[0]);
    }

    private static Party loadOnePartyFromIniFile(File ini, HashMap<String, PlayerCharacter> byName) {
        String partyNameFromFile = null;
        List<String> memberNames = new ArrayList<>();
        String currentSection = "";

        try (BufferedReader reader = new BufferedReader(new FileReader(ini))) {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {

                if (line.startsWith("[") && (line.endsWith("]"))) {
                    currentSection = (line.substring(1, (line.length() - 1)));
                } else {
                    int eq = line.indexOf('=');
                    if (eq >= 0) {
                        String key = line.substring(0, eq);
                        String val = line.substring(eq + 1);
                        if ("Party".equalsIgnoreCase(currentSection)) {
                            if ("Name".equalsIgnoreCase(key)) partyNameFromFile = val;
                        } else if ("Members".equalsIgnoreCase(currentSection)) {
                            memberNames.add(val);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        Party p = new Party(partyNameFromFile);
        for (String nm : memberNames) {
            PlayerCharacter pc = byName.get(nm);
            if (pc != null) {
                p.members.add(pc);
            }
        }
        return p;
    }
}

