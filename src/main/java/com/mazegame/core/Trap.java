package com.mazegame.core; // Or a new package like com.mazegame.hazards

import com.mazegame.characters.Player;
import com.mazegame.interfaces.Executable; // Traps will execute each game tick to update state
import com.mazegame.ui.SpriteManager;
import com.mazegame.utils.Position;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color; // For placeholder drawing

public class Trap extends Entity implements Executable {

    public enum TrapState {
        IDLE,       // Safe, spikes retracted or hidden
        WARNING,    // Visual cue that spikes are about to activate (optional)
        ACTIVE      // Dangerous, spikes out, deals damage
    }

    private TrapState currentState;
    private long lastStateChangeTime;
    private long idleDuration;      // How long it stays safe
    private long warningDuration;   // How long the warning shows (if used)
    private long activeDuration;    // How long spikes are out
    private int damage;

    private Image spriteIdle;
    private Image spriteWarning; // Optional
    private Image spriteActive;

    public Trap(String name, World world, Position position,
                long idleTimeMs, long warningTimeMs, long activeTimeMs, int damageAmount) {
        super(name, world, position); // Trap is an entity at a specific position
        this.idleDuration = idleTimeMs;
        this.warningDuration = warningTimeMs;
        this.activeDuration = activeTimeMs;
        this.damage = damageAmount;

        this.currentState = TrapState.IDLE;
        this.lastStateChangeTime = System.currentTimeMillis();

        // Load sprites (ensure these exist in resources/sprites and SpriteManager)
        this.spriteIdle = SpriteManager.getSprite("trap_idle.png");
        this.spriteWarning = SpriteManager.getSprite("trap_warning.png");
        this.spriteActive = SpriteManager.getSprite("trap_active.png");

        if (this.spriteIdle == null)
            System.err.println("TRAP_SPRITE_ERROR: trap_idle.png for " + getName() + " is null!");
        if (this.warningDuration > 0 && this.spriteWarning == null)
            System.err.println("TRAP_SPRITE_ERROR: trap_warning.png for " + getName() + " is null (and warning is used)!");
        if (this.spriteActive == null)
            System.err.println("TRAP_SPRITE_ERROR: trap_active.png for " + getName() + " is null!");
    }

    @Override
    public void execute() { // Called by World.update() for each trap
        long currentTime = System.currentTimeMillis();
        long timeInCurrentState = currentTime - lastStateChangeTime;

        TrapState nextState = currentState;

        switch (currentState) {
            case IDLE:
                if (timeInCurrentState >= idleDuration) {
                    nextState = (warningDuration > 0) ? TrapState.WARNING : TrapState.ACTIVE;
                }
                break;
            case WARNING:
                if (timeInCurrentState >= warningDuration) {
                    nextState = TrapState.ACTIVE;
                }
                break;
            case ACTIVE:
                if (timeInCurrentState >= activeDuration) {
                    nextState = TrapState.IDLE;
                }
                // Deal damage if player is on this tile during ACTIVE state
                // This check should happen frequently, so we do it every tick while active.
                Player player = world.getPlayer();
                if (player != null && player.getPosition().equals(this.position) && player.getHealth() > 0) {
                    System.out.println(player.getName() + " stepped on active trap " + getName() + "!");
                    player.takeDamage(this.damage);
                    // Optional: Play a sound
                }
                break;
        }

        if (nextState != currentState) {
            currentState = nextState;
            lastStateChangeTime = currentTime;
            System.out.println("Trap " + getName() + " changed to state: " + currentState + " at " + this.position);
        }
    }

    @Override
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        if (this.position == null) { // Should have a position if it's an entity on a tile
            System.err.println("Trap " + getName() + " has null position, cannot determine draw coordinates correctly.");
            return;
        }
        // This method is called by Tile.draw(), so screenX, screenY are the tile's top-left.

        Image spriteToDraw = null;
        switch (currentState) {
            case IDLE:
                spriteToDraw = spriteIdle;
                System.out.println("Trap " + getName() + " drawing IDLE sprite."); // DEBUG
                break;
            case WARNING:
                spriteToDraw = spriteWarning != null ? spriteWarning : spriteIdle; // Fallback to idle
                System.out.println("Trap " + getName() + " drawing WARNING sprite."); // DEBUG
                break;
            case ACTIVE:
                spriteToDraw = spriteActive;
                System.out.println("Trap " + getName() + " drawing ACTIVE sprite."); // DEBUG
                break;
            default:
                System.err.println("Trap " + getName() + " in unknown draw state: " + currentState);
                spriteToDraw = spriteIdle; // Default to idle sprite
                break;
        }

        // --- DEBUG LOG ---
        System.out.println("Trap.draw() called for " + getName() + " in state " + currentState +
                           ". Sprite to draw: " + (spriteToDraw != null ? "VALID_IMAGE" : "NULL") +
                           " at screenX=" + screenX + ", screenY=" + screenY);

        if (spriteToDraw != null) {
            g.drawImage(spriteToDraw,
                    screenX, // Draw at the screen coordinates provided by the Tile
                    screenY,
                    tilePixelWidth, tilePixelHeight, null);
        } else {
            // Fallback placeholder drawing for the trap itself AT screenX, screenY
            Color c;
            switch (currentState) {
                case IDLE:
                    c = Color.GREEN.darker().darker();
                    System.out.println("Trap " + getName() + " drawing IDLE placeholder."); // DEBUG
                    break;
                case WARNING:
                    c = Color.ORANGE;
                    System.out.println("Trap " + getName() + " drawing WARNING placeholder."); // DEBUG
                    break;
                case ACTIVE:
                    c = Color.RED;
                    System.out.println("Trap " + getName() + " drawing ACTIVE placeholder."); // DEBUG
                    break;
                default:
                    c = Color.BLACK;
            }
            g.setColor(c);
            g.fillRect(screenX + tilePixelWidth / 4,
                       screenY + tilePixelHeight / 4,
                       tilePixelWidth / 2, tilePixelHeight / 2);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(screenX + tilePixelWidth / 4, screenY + tilePixelHeight / 4, tilePixelWidth / 2 - 1, tilePixelHeight / 2 - 1);
            if (currentState != null)
                System.err.println("Trap " + getName() + ": spriteToDraw was NULL for state " + currentState); // DEBUG
            else
                System.err.println("Trap " + getName() + ": spriteToDraw was NULL and currentState was also NULL"); // DEBUG
        }
    }
}