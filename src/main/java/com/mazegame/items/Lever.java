package com.mazegame.items; // Or com.mazegame.items if you prefer

import com.mazegame.core.Entity;
import com.mazegame.core.World;
import com.mazegame.interfaces.Activatable;
import com.mazegame.puzzles.PuzzleController;
import com.mazegame.ui.SpriteManager;
import com.mazegame.utils.Position;

import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

public class Lever extends Entity implements Activatable {
    private boolean isActive;
    private Image spriteOn;
    private Image spriteOff;
    private PuzzleController controller; // Reference to an object that checks puzzle state

    public Lever(String name, World world, Position position, PuzzleController controller, boolean initiallyActive) {
        super(name, world, position);
        this.isActive = initiallyActive;
        this.controller = controller;
        // Assuming you have sprites: "lever_on.png" and "lever_off.png"
        this.spriteOn = SpriteManager.getSprite("lever_on.png");
        this.spriteOff = SpriteManager.getSprite("lever_off.png");

        if (this.spriteOn == null || this.spriteOff == null) {
            System.err.println("Warning: Lever sprites not loaded for " + name);
        }
        // Register with the controller if it exists
        if (this.controller != null) {
            this.controller.registerLever(this);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    // When player interacts with the lever
    public void pull() {
        isActive = !isActive; // Toggle state
        System.out.println(getName() + " is now " + (isActive ? "ON" : "OFF"));
        if (controller != null) {
            controller.checkPuzzleState(); // Notify controller to check if puzzle is solved
        }
    }

    // Implementing Activatable methods (though 'pull' is more specific)
    @Override
    public void open() { // 'open' for a lever means turn it ON
        if (!isActive) {
            pull();
        }
    }

    @Override
    public void close() { // 'close' for a lever means turn it OFF
        if (isActive) {
            pull();
        }
    }

    @Override
    public boolean isLocked() {
        return false; // Levers are not typically "locked" themselves
    }

    public void forceOpen() {
        // Forcing a lever doesn't make sense in this context
        System.out.println(getName() + " cannot be forced.");
    }

    @Override
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        Image currentSprite = isActive ? spriteOn : spriteOff;

        if (currentSprite != null) {
            // Draw the lever AT the screenX, screenY provided by the Tile
            g.drawImage(currentSprite,
                    screenX,
                    screenY,
                    tilePixelWidth, tilePixelHeight, null);
        } else if (this.position != null) { // Fallback placeholder if sprites are null
            // Draw placeholder AT screenX, screenY
            g.setColor(isActive ? java.awt.Color.GREEN : java.awt.Color.RED);
            g.fillRect(
                    screenX + tilePixelWidth / 4, // Offset within the tile for placeholder appearance
                    screenY,
                    tilePixelWidth / 2, tilePixelHeight);
        } else {
            // This case should ideally not happen if a lever always has a position
            System.err.println("Lever " + getName() + " has no position to draw at.");
        }
    }
}