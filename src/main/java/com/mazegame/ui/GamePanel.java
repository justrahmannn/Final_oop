package com.mazegame.ui;

import com.mazegame.core.Room;
import com.mazegame.core.World;
import com.mazegame.characters.Player;
import com.mazegame.items.Item;
import com.mazegame.items.Gun;

import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.Font;
import java.util.List;

public class GamePanel extends JPanel {
    private World world;
    public static final int TILE_PIXEL_WIDTH = 32;
    public static final int TILE_PIXEL_HEIGHT = 32;

    public GamePanel(World world) {
        this.world = world;
        setPreferredSize(new Dimension(
                Room.ROOM_WIDTH_TILES * TILE_PIXEL_WIDTH,
                Room.ROOM_HEIGHT_TILES * TILE_PIXEL_HEIGHT + 120 // Increased space for UI
        ));
        setBackground(Color.DARK_GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // --- DEBUG LOGS ---
        System.out.println("GamePanel.paintComponent: Start. GameOver=" + world.isGameOver() + ", PlayerWon=" + world.didPlayerWin());
        Player p = world.getPlayer();
        System.out.println("GamePanel.paintComponent: Player is " + (p == null ? "null" : "NOT null (" + System.identityHashCode(p) + ", health=" + p.getHealth() + ")"));

        // 1. Check for Game Over or Win state FIRST
        if (world.isGameOver()) {
            drawEndGameMessage(g, "GAME OVER!", Color.RED);
            return; // Stop further drawing if game is over
        }
        if (world.didPlayerWin()) {
            drawEndGameMessage(g, "YOU ESCAPED!", Color.GREEN); // Or "YOU WIN!"
            return; // Stop further drawing if player won
        }
        // ------------------------------------

        // If game is not over/won, then we expect a valid player
        Player player = world.getPlayer(); // Fetch player AFTER checking game over states
        if (player == null) {
            // This case should ideally only happen if the game starts without a player
            // or some other very unexpected error where player is null but game isn't over.
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("CRITICAL ERROR: Player is null but game not over!", 50, 50);
            System.err.println("GamePanel.paintComponent: Player is null, but world.isGameOver() and world.didPlayerWin() are both false. This is an unexpected state.");
            return;
        }

        // If game is ongoing and player is valid, draw the room and UI
        Room currentRoom = player.getCurrentRoom();
        if (currentRoom == null) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("Player is not in a room!", 50, 50);
            return;
        }

        currentRoom.drawContents(g, TILE_PIXEL_WIDTH, TILE_PIXEL_HEIGHT);
        drawUI(g, player);
    }

    private void drawEndGameMessage(Graphics g, String message, Color color) {
        g.setColor(color);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        int stringWidth = g.getFontMetrics().stringWidth(message);
        // For better vertical centering:
        int stringHeight = g.getFontMetrics().getHeight();
        int ascent = g.getFontMetrics().getAscent();
        g.drawString(message, (getWidth() - stringWidth) / 2, (getHeight() - stringHeight) / 2 + ascent);
    }

    private void drawUI(Graphics g, Player player) {
        int uiYStart = Room.ROOM_HEIGHT_TILES * TILE_PIXEL_HEIGHT + 20;
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 14));

        // Debug log for UI health
        // System.out.println("drawUI: player.getHealth(): " + player.getHealth() +
        //                    " for " + player.getName() +
        //                    " (object: " + System.identityHashCode(player) + ")");

        g.drawString("Health: " + player.getHealth() + "/" + player.getMaxHealth(), 10, uiYStart);

        // Inventory
        int inventoryYOffset = uiYStart + 20;
        g.drawString("Inventory (Q/R cycle, F use):", 10, inventoryYOffset);
        inventoryYOffset += 15;

        List<Item> inventory = player.getInventory();
        Item activeItem = player.getActiveItem();
        int maxItemsToShow = 6;

        if (inventory.isEmpty()) {
            g.drawString("  (empty)", 10, inventoryYOffset);
        } else {
            for (int i = 0; i < Math.min(inventory.size(), maxItemsToShow); i++) {
                String itemText = "  " + (i + 1) + ". " + inventory.get(i).getName();
                if (inventory.get(i) == activeItem) {
                    g.setColor(Color.YELLOW);
                    itemText = "> " + itemText;
                }
                g.drawString(itemText, 10, inventoryYOffset + (i * 15));
                g.setColor(Color.WHITE);
            }
            if (inventory.size() > maxItemsToShow) {
                g.drawString("  ... (more)", 10, inventoryYOffset + (maxItemsToShow * 15));
            }
        }

        // Display active item info
        int activeItemInfoX = getWidth() / 2 - 50;
        if (activeItem instanceof Gun) {
            Gun heldGun = (Gun) activeItem;
            g.drawString("Active: " + heldGun.getName() + " | Ammo: " +
                         heldGun.getCurrentAmmo() + "/" + heldGun.getMaxAmmoCapacity(),
                         activeItemInfoX, uiYStart);
        } else if (activeItem != null) {
            g.drawString("Active: " + activeItem.getName(), activeItemInfoX, uiYStart);
        } else if (!inventory.isEmpty()){
             g.drawString("Active: (Select with Q/R)", activeItemInfoX, uiYStart);
        } else {
            g.drawString("Active: Nothing", activeItemInfoX, uiYStart);
        }
    }
}