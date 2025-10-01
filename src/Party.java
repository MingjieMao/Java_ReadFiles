import java.io.*;
import java.util.*;

/**
 *
 */
public class Party {
    private final String name;
    private final List<PlayerCharacter> members = new ArrayList<>();

    /**
     * Creates a new empty Party with a given name.
     * Examples：
     * - new Party("fellowship");
     * - new Party("A");
     * - new Party("B");
     */
    public Party(String name) {
        this.name = name;
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
        StringBuilder sb = new StringBuilder();
        for (int i=0; i < members.size(); i++) {
            sb.append(members.get(i).getName());
            if (i < members.size()-1) {
                sb.append(",");
            }
        }
        String memberString = sb.toString();

        try(var writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("[Party]");
            writer.newLine();
            writer.write("Name=" + this.name);
            writer.newLine();
            writer.write("Members=" + memberString);
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
     *
     * @param allCharacters An array of all PlayerCharacters in the game.
     * @param directory The File object representing the directory containing the party INI files.
     * @return An array of Party objects loaded from the directory.
     */
    public static Party[] loadParties(PlayerCharacter[] allCharacters, File directory){
        List<Party> parties = new ArrayList<>();

        Map<String, PlayerCharacter> lookup = new HashMap<>();
        for (PlayerCharacter player : allCharacters) {
            lookup.put(player.getName(), player);
        }

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".ini"));
        for (File file : files) {
            String partyName = null;
            String membersString = null;

            try (var reader = new BufferedReader(new FileReader(file))) {
                for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (line.contains("=")) {
                        String[] parts = line.split("=", 2);
                        String key = parts[0];
                        String value = parts[1];

                        if (key.equals("Name")) {
                            partyName = value;
                        } else if (key.equals("Members")) {
                            membersString = value;
                        }
                    }
                }

                if (partyName != null) {
                    Party newParty = new Party(partyName);
                    String[] characterNames = membersString.split(",");
                    for (String charName : characterNames) {
                        PlayerCharacter character = lookup.get(charName);
                        newParty.addMember(character);
                    }
                }
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
        return parties.toArray(new Party[0]);
    }
}

