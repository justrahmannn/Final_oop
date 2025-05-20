package com.mazegame.items; // Assuming Chest is an Item, which means it extends Entity

import com.mazegame.core.Entity;
import com.mazegame.core.Room;
import com.mazegame.core.Tile;
import com.mazegame.core.World;
import com.mazegame.characters.LivingBeing;
import com.mazegame.interfaces.Activatable;
import com.mazegame.utils.Position;
import com.mazegame.ui.SpriteManager; // Make sure this is imported

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics;
import java.awt.Color; // For placeholder

public class Chest extends Item implements Activatable { // Item extends Entity
    private List<Item> objectsInside;
    private boolean locked;
    private String keyId;
    private Room room; // The room this chest is in, might be useful for some logic
    private boolean isOpen;
    private boolean forceable;

    @Override
    public void use(LivingBeing user) {
        open(user);
    }

    // Sprites for the chest states
    private Image spriteChestOpen;
    private Image spriteChestClosed;

    // Constructor for a chest that can be forceable
    public Chest(String name, World world, Position position, Room room,
                 boolean locked, String keyId, boolean isForceable) {
        super(name, world, position); // Calls Item's constructor, which calls Entity's
        this.objectsInside = new ArrayList<>();
        this.locked = locked;
        this.keyId = keyId;
        this.room = room; // Store the room reference
        this.isOpen = false;
        this.forceable = isForceable;

        // Load sprites
        this.spriteChestOpen = SpriteManager.getSprite("chest_open.png");
        this.spriteChestClosed = SpriteManager.getSprite("chest_closed.png");
        if (this.spriteChestOpen == null || this.spriteChestClosed == null) {
            System.err.println("Warning: Chest sprites not loaded for " + getName());
        }
        this.sprite = this.spriteChestClosed; // Set initial default sprite for Entity superclass if used

        // Mark the tile in the room as containing this chest
        if (this.room != null && this.position != null) {
            Tile chestTile = this.room.getTile(this.position.getX(), this.position.getY());
            if (chestTile != null) {
                chestTile.setEntityOnTile(this);
                chestTile.setType(Tile.TileType.CHEST);
                chestTile.setWalkable(false); // Can't walk on a chest
            } else {
                 System.err.println("Chest Constructor: Tile at " + this.position + " in room " + this.room.getName() + " is null.");
            }
        } else {
            if(this.room == null) System.err.println("Chest Constructor: Room is null for " + getName());
            if(this.position == null) System.err.println("Chest Constructor: Position is null for " + getName());
        }
    }

    // Simpler constructor if forceable is always false by default or set later
    public Chest(String name, World world, Position position, Room room, boolean locked, String keyId) {
        this(name, world, position, room, locked, keyId, false); // Default forceable to false
    }

    public String getKeyId() {
        return this.keyId;
    }

    public void unlock() {
        if (this.locked) {
            this.locked = false;
            System.out.println(getName() + " has been unlocked.");
            // No direct sprite change here; draw method handles visual based on isOpen and isLocked
        }
    }

    @Override
    public void open() { // Generic Activatable open (e.g., triggered by puzzle)
        if (isLocked()) {
            System.out.println(getName() + " is locked. Cannot open generic.");
            return;
        }
        if (!isOpen) {
            isOpen = true;
            System.out.println(getName() + " opened (generic).");
            // If opened generically, items might just spill or need a default interaction
            // For now, this just marks it open. The player interaction one is more detailed.
        }
    }

    public void open(LivingBeing user) { // Player interacts to open
        if (user == null) { // e.g. if forced open by crowbar where user might be null
            open(); // Call the generic open
            if (isOpen && !objectsInside.isEmpty()) { // If successfully opened and has items
                System.out.println(getName() + " forced open, items spill out (conceptually):");
                // Logic to spill items into the room
                for (Item item : new ArrayList<>(objectsInside)) {
                    System.out.println("- " + item.getName());
                    // For simplicity, just log. To actually spill:
                    // item.setOwner(null);
                    // item.setPosition(new Position(this.position.getX(), this.position.getY() + 1)); // Below chest
                    // this.room.addItem(item); // Add to room's floor items
                }
                // objectsInside.clear(); // Clear after spilling, or not if they remain "in" the broken chest
                System.out.println("Items from forced " + getName() + " would need to be picked up from the room.");
            } else if (isOpen && objectsInside.isEmpty()){
                 System.out.println(getName() + " forced open and is empty.");
            }
            return;
        }

        if (isLocked()) {
            System.out.println(getName() + " is locked. " + user.getName() + " cannot open it.");
            return;
        }
        if (!isOpen) {
            isOpen = true;
            System.out.println(user.getName() + " opens " + getName() + ".");
            // updateSpriteState(); // Not needed if draw() handles it

            if (objectsInside.isEmpty()) {
                System.out.println(getName() + " is empty.");
            } else {
                System.out.println(getName() + " contains:");
                for (Item item : new ArrayList<>(objectsInside)) {
                    System.out.println("- " + item.getName());
                    if (user.getInventory().add(item)) { // Add to player's inventory
                        item.setOwner(user);
                        item.setPosition(null);
                        System.out.println(item.getName() + " added to " + user.getName() + "'s inventory.");
                    } else {
                        System.out.println("Could not add " + item.getName() + " to " + user.getName() + "'s inventory (maybe full?).");
                    }
                }
                objectsInside.clear(); // Empty the chest after player takes items
            }
        } else {
             System.out.println(getName() + " is already open.");
        }
    }

    @Override
    public void close() {
        if (isOpen) {
            isOpen = false;
            System.out.println(getName() + " closed.");
            // this.locked = true; // Option: Re-lock if it had a keyId
            // updateSpriteState(); // Not needed
        }
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public void addItemInside(Item item) {
        if (item != null) {
            objectsInside.add(item);
            item.setOwner(null);
            item.setPosition(null);
        }
    }

    public void removeItemInside(Item item) { // Renamed for clarity from diagram's removeObject
        objectsInside.remove(item);
    }

    public boolean isTreasureChest() { // Renamed for clarity from diagram's isTreasure
        for (Item item : objectsInside) {
            if (item instanceof Treasure) {
                return true;
            }
        }
        return false;
    }

    public void setForceable(boolean forceable) {
        this.forceable = forceable;
    }

    public boolean canBeForcedOpen() {
        return forceable;
    }

    // From Activatable (ensure Activatable interface declares this)
    public void forceOpen() {
        if (canBeForcedOpen() && isLocked()) {
            System.out.println(getName() + " is being forced open!");
            this.locked = false; // Unlock it
            this.isOpen = true;  // Mark as open
            System.out.println(getName() + " has been forced open.");
            // updateSpriteState(); // Not needed
            // The items are not automatically given to player here. Player must interact again with 'E'.
            // Or, if forcing *also* spills items, call open(null) like before,
            // but open(LivingBeing user) needs robust handling for user == null.
            // For now, forcing just unlocks and opens it. Player E to loot.
        } else if (!isLocked()) {
            System.out.println(getName() + " is already unlocked/open, no need to force.");
        } else {
            System.out.println(getName() + " cannot be forced.");
        }
    }

    public List<Item> getItemsInside() {
        return new ArrayList<>(objectsInside); // Return a copy
    }

    private void updateSpriteState() {
        // This method is no longer strictly necessary if Tile.draw or Chest.draw
        // dynamically chooses the sprite based on isOpen.
        // If you were setting the Tile's base sprite, it would go here.
        // For now, leave empty or remove.
    }

    /**
     * Draws the chest at the given screen coordinates, using its current state.
     * This method is called by Tile.draw() if this Chest instance is the entityOnTile.
     */
    @Override
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        Image spriteToDraw = this.isOpen ? this.spriteChestOpen : this.spriteChestClosed;

        if (spriteToDraw != null) {
            g.drawImage(spriteToDraw,
                    screenX, // Use the screenX provided by the Tile
                    screenY, // Use the screenY provided by the Tile
                    tilePixelWidth, tilePixelHeight, null);
        } else {
            // Fallback placeholder drawing AT screenX, screenY
            Color chestColor = isOpen ? Color.CYAN : (isLocked() ? Color.MAGENTA.darker() : Color.ORANGE.darker());
            g.setColor(chestColor);
            g.fillRect(screenX + tilePixelWidth / 4,
                       screenY + tilePixelHeight / 4,
                       tilePixelWidth / 2, tilePixelHeight / 2);
             g.setColor(Color.BLACK);
             g.drawRect(screenX + tilePixelWidth / 4, screenY + tilePixelHeight / 4, tilePixelWidth / 2 -1 , tilePixelHeight / 2-1);
        }
    }
}