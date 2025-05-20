package com.mazegame;

import com.mazegame.core.World;
import com.mazegame.ui.MainFrame;
import com.mazegame.ui.SpriteManager; // Added import for sprite loading
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Maze Game...");

        SwingUtilities.invokeLater(() -> {
            try {
                // Load sprites before creating world
                SpriteManager.loadAllSprites();
                System.out.println("Sprites loaded successfully");

                // Create and initialize world
                World world = new World("Labyrinth of Xar");
                world.initializeWorld();

                // Create and show main window
                MainFrame frame = new MainFrame(world);
                frame.setLocationRelativeTo(null); // Center on screen
                frame.setVisible(true);

                System.out.println("Game started successfully!");
            } catch (Exception e) {
                System.err.println("Failed to start game: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                    "Failed to start game: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}