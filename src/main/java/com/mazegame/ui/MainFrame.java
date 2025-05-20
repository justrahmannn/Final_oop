package com.mazegame.ui;

import com.mazegame.core.World;
import com.mazegame.items.Item;
import com.mazegame.characters.Player; // Assuming Player.health is now inherited and public for debug
// If Player.health is protected, you can't access it directly here, only via getHealth()

import javax.swing.JFrame;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class MainFrame extends JFrame {
    private GamePanel gamePanel;
    private World world;

    // public World getWorld() { // Not strictly needed by other classes if world is passed around
    //     return world;
    // }

    public MainFrame(World world) {
        this.world = world;
        this.gamePanel = new GamePanel(world);

        setTitle("Maze Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                System.out.println("\n--- KEY PRESSED EVENT START ---");

                // Check game state at the very beginning of the event
                if (world.isGameOver() || world.didPlayerWin()) {
                    System.out.println("MainFrame: Game already over or player won, repainting and exiting keyPressed.");
                    if (gamePanel != null) gamePanel.repaint(); // Ensure final screen is shown
                    System.out.println("--- KEY PRESSED EVENT END (Game Already Over) ---");
                    return;
                }

                Player playerAtStartOfEvent = world.getPlayer();
                // If player is null here, it means something went wrong before this event or game ended without flags set
                if (playerAtStartOfEvent == null) {
                    System.err.println("MainFrame CRITICAL: Player is null at start of event, but game not flagged as over/won.");
                    if (gamePanel != null) gamePanel.repaint();
                    System.out.println("--- KEY PRESSED EVENT END (Unexpected Null Player) ---");
                    return;
                }

                System.out.println("MainFrame PRE-ACTION: playerAtStartOfEvent health=" + playerAtStartOfEvent.getHealth() +
                                   ", hash=" + System.identityHashCode(playerAtStartOfEvent));

                int keyCode = e.getKeyCode();
                boolean playerTookPrimaryAction = false;
                Item itemSelectedForUseByKeybind = null;
                String primaryActionType = "NONE";

                // --- Player Action Switch Statement ---
                switch (keyCode) {
                    case KeyEvent.VK_W: case KeyEvent.VK_UP:
                        playerAtStartOfEvent.move(0, -1);
                        playerTookPrimaryAction = true; primaryActionType = "MOVE_UP";
                        break;
                    case KeyEvent.VK_S: case KeyEvent.VK_DOWN:
                        playerAtStartOfEvent.move(0, 1);
                        playerTookPrimaryAction = true; primaryActionType = "MOVE_DOWN";
                        break;
                    case KeyEvent.VK_A: case KeyEvent.VK_LEFT:
                        playerAtStartOfEvent.move(-1, 0);
                        playerTookPrimaryAction = true; primaryActionType = "MOVE_LEFT";
                        break;
                    case KeyEvent.VK_D: case KeyEvent.VK_RIGHT:
                        playerAtStartOfEvent.move(1, 0);
                        playerTookPrimaryAction = true; primaryActionType = "MOVE_RIGHT";
                        break;
                    case KeyEvent.VK_E:
                        playerAtStartOfEvent.interact();
                        playerTookPrimaryAction = true; primaryActionType = "INTERACT";
                        break;
                    case KeyEvent.VK_1:
                        if (playerAtStartOfEvent.getInventory().size() > 0) {
                            itemSelectedForUseByKeybind = playerAtStartOfEvent.getInventory().get(0);
                            playerTookPrimaryAction = true; primaryActionType = "USE_ITEM_0";
                        } else { System.out.println("No item in slot 0"); }
                        break;
                    case KeyEvent.VK_2:
                        if (playerAtStartOfEvent.getInventory().size() > 1) {
                            itemSelectedForUseByKeybind = playerAtStartOfEvent.getInventory().get(1);
                            playerTookPrimaryAction = true; primaryActionType = "USE_ITEM_1";
                        } else { System.out.println("No item in slot 1"); }
                        break;
                    case KeyEvent.VK_3:
                        if (playerAtStartOfEvent.getInventory().size() > 2) {
                            itemSelectedForUseByKeybind = playerAtStartOfEvent.getInventory().get(2);
                            playerTookPrimaryAction = true; primaryActionType = "USE_ITEM_2";
                        } else { System.out.println("No item in slot 2"); }
                        break;
                    case KeyEvent.VK_4:
                        if (playerAtStartOfEvent.getInventory().size() > 3) {
                            itemSelectedForUseByKeybind = playerAtStartOfEvent.getInventory().get(3);
                            playerTookPrimaryAction = true; primaryActionType = "USE_ITEM_3";
                        } else { System.out.println("No item in slot 3"); }
                        break;
                    case KeyEvent.VK_Q:
                        if (!playerAtStartOfEvent.getInventory().isEmpty()) {
                            int currentSlot = playerAtStartOfEvent.getActiveItemSlot();
                            currentSlot = (currentSlot - 1 + playerAtStartOfEvent.getInventory().size()) % playerAtStartOfEvent.getInventory().size();
                            playerAtStartOfEvent.setActiveItemSlot(currentSlot);
                        }
                        gamePanel.repaint(); // Update UI immediately
                        System.out.println("--- KEY PRESSED EVENT END (Cycle Item Q) ---");
                        return;
                    case KeyEvent.VK_R:
                        if (!playerAtStartOfEvent.getInventory().isEmpty()) {
                            int currentSlot = playerAtStartOfEvent.getActiveItemSlot();
                            currentSlot = (currentSlot + 1) % playerAtStartOfEvent.getInventory().size();
                            playerAtStartOfEvent.setActiveItemSlot(currentSlot);
                        }
                        gamePanel.repaint();
                        System.out.println("--- KEY PRESSED EVENT END (Cycle Item R) ---");
                        return;
                    case KeyEvent.VK_F:
                        Item activeItem = playerAtStartOfEvent.getActiveItem();
                        if (activeItem != null) {
                            itemSelectedForUseByKeybind = activeItem;
                            playerTookPrimaryAction = true;
                            primaryActionType = "USE_ACTIVE_ITEM (" + activeItem.getName() + ")";
                        } else { System.out.println("No active item to use with F key."); }
                        break;
                    default:
                        System.out.println("MainFrame: Unhandled key press '" + KeyEvent.getKeyText(keyCode) + "' or no action taken.");
                        break;
                }
                // --- End of Player Action Switch ---

                if (playerTookPrimaryAction) {
                    System.out.println("MainFrame: Player action was: " + primaryActionType + ". Updating world.");
                    world.update(); // NPCs act. Player's health field can be updated here. Player might die.
                    System.out.println("MainFrame: world.update() COMPLETED.");

                    // Check game state AGAIN after world.update() because player might have died or won
                    if (world.isGameOver() || world.didPlayerWin()) {
                        System.out.println("MainFrame: Game ended during world.update(). Repainting for final screen.");
                        gamePanel.repaint();
                        System.out.println("--- KEY PRESSED EVENT END (Game Ended in Update) ---");
                        return;
                    }

                    Player playerRefAfterUpdate = world.getPlayer();
                    if (playerRefAfterUpdate == null) {
                        if (world.isGameOver() || world.didPlayerWin()) {
                            System.out.println("MainFrame: Player is null as expected (game is over or player won).");
                        } else {
                            System.err.println("CRITICAL ERROR: world.getPlayer() returned null UNEXPECTEDLY after world.update() but game not flagged as over/won.");
                        }
                        itemSelectedForUseByKeybind = null;
                        // No further processing; repaint already handled above if needed
                        return;
                    }

                    System.out.println("MainFrame POST-WORLD.UPDATE STATE CHECK:");
                    System.out.println("  playerRefAfterUpdate (re-fetched from world): health=" + playerRefAfterUpdate.getHealth() +
                                       ", hash=" + System.identityHashCode(playerRefAfterUpdate));

                    if (itemSelectedForUseByKeybind != null) {
                        System.out.println("MainFrame: Executing use for item: " + itemSelectedForUseByKeybind.getName() +
                                           " on Player (Health: " + playerRefAfterUpdate.getHealth() +
                                           ", Obj: " + System.identityHashCode(playerRefAfterUpdate) + ")");
                        itemSelectedForUseByKeybind.use(playerRefAfterUpdate);

                        if (world.isGameOver() || world.didPlayerWin()) {
                            System.out.println("MainFrame: Game ended during item use. Repainting for final screen.");
                            gamePanel.repaint();
                            System.out.println("--- KEY PRESSED EVENT END (Game Ended by Item Use) ---");
                            return;
                        }
                    }
                    System.out.println("MainFrame: Requesting repaint.");
                    gamePanel.repaint();
                }
                System.out.println("--- KEY PRESSED EVENT END ---");
            }
        });
        setFocusable(true);
        requestFocusInWindow();
    }
}