package server;

import java.io.*;
import java.util.*;

public class WordManager {
    private static List<String> words = new ArrayList<>();
    
    static {
        // Load words from the "resources/words.txt" file.
        try (BufferedReader br = new BufferedReader(new FileReader("resources/words.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line.trim());
            }
        } catch (IOException e) {
            System.err.println("Error loading words: " + e.getMessage());
        }
    }
    
    public static String getRandomWord() {
        if (words.isEmpty()) return "default";
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }
}