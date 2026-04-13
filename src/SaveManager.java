import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Multi-slot save/load for the game.
 *
 * - Each game lives in its own file under {@link #SAVE_DIR}, named by game id.
 * - {@link #save(Game)} writes a Game via Java serialization (overwrites if same id).
 * - {@link #load(String)} reads a specific saved game by id.
 * - {@link #delete(String)} removes a single saved game (called when it ends).
 * - {@link #listSaves()} returns every saved game, newest first.
 */
public class SaveManager {

    // TODO: change the save directory here if you want it elsewhere.
    private static final File SAVE_DIR = new File("saves");

    private static File fileFor(String gameId) {
        return new File(SAVE_DIR, gameId + ".dat");
    }

    public static void save(Game game) throws IOException {
        if (!SAVE_DIR.exists()) {
            SAVE_DIR.mkdirs();
        }
        try (ObjectOutputStream out =
                 new ObjectOutputStream(new FileOutputStream(fileFor(game.getId())))) {
            out.writeObject(game);
        }
    }

    public static Game load(String gameId) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in =
                 new ObjectInputStream(new FileInputStream(fileFor(gameId)))) {
            return (Game) in.readObject();
        }
    }

    public static void delete(String gameId) {
        File f = fileFor(gameId);
        if (f.exists()) {
            f.delete();
        }
    }

    public static boolean hasAnySave() {
        File[] files = listSaveFiles();
        return files != null && files.length > 0;
    }

    /**
     * Returns all saved games, most recently modified first.
     * Skips files that fail to load (corrupt or version mismatch).
     */
    public static List<Game> listSaves() {
        List<Game> result = new ArrayList<>();
        File[] files = listSaveFiles();
        if (files == null) return result;

        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        for (File f : files) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(f))) {
                Object obj = in.readObject();
                if (obj instanceof Game) {
                    result.add((Game) obj);
                }
            } catch (IOException | ClassNotFoundException ex) {
                // Skip unreadable saves
            }
        }
        return result;
    }

    private static File[] listSaveFiles() {
        if (!SAVE_DIR.exists() || !SAVE_DIR.isDirectory()) return null;
        return SAVE_DIR.listFiles((dir, name) -> name.endsWith(".dat"));
    }
}
