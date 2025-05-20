package com.mazegame.characters;

import com.mazegame.core.Entity;
import com.mazegame.core.Room;
import com.mazegame.core.World;
import com.mazegame.core.Tile;
import com.mazegame.core.Door;
import com.mazegame.interfaces.Executable;
import com.mazegame.items.Item;
import com.mazegame.utils.Position;

import java.awt.Image;
import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public abstract class LivingBeing extends Entity implements Executable {
    protected int health;
    protected int maxHealth;
    protected int strength;
    protected Room currentRoom;
    protected List<Item> inventory;
    protected Image sprite; // Sprite specific to this LivingBeing (set by Player/NPC constructor)

    public LivingBeing(String name, World world, Position initialPosition, Room startRoom,
                      int maxHealth, int strength) {
        super(name, world, initialPosition); // Entity handles name, world, position
        this.maxHealth = maxHealth;
        this.health = this.maxHealth;      // Initialize current health to maxHealth
        this.strength = strength;
        this.inventory = new ArrayList<>();
        this.currentRoom = startRoom;
        // The World's createPlayer/populateNPCs methods will call room.addLivingBeing(this)
    }

    // --- Core Attributes ---
    public int getHealth() {
        return this.health;
    }

    public int getMaxHealth() {
        return this.maxHealth;
    }

    public int getStrength() {
        return this.strength;
    }

    public List<Item> getInventory() {
        // Return the actual internal list to allow direct modification
        return this.inventory;
    }

    public Room getCurrentRoom() {
        return this.currentRoom;
    }

    /**
     * Moves the LivingBeing to a new room.
     * Handles removing from the old room and adding to the new room.
     */
    public void setCurrentRoom(Room newRoom) {
        if (this.currentRoom == newRoom) {
            return; // No change needed
        }
        String oldRoomName = (this.currentRoom != null) ? this.currentRoom.getName() : "null";
        String newRoomName = (newRoom != null) ? newRoom.getName() : "null";
        System.out.println(getName() + " - setCurrentRoom: Transitioning from " + oldRoomName + " to " + newRoomName);

        if (this.currentRoom != null) {
            this.currentRoom.removeLivingBeing(this);
        }
        this.currentRoom = newRoom; // Update own reference
        if (this.currentRoom != null) {
            this.currentRoom.addLivingBeing(this);
        }
    }

    // --- Actions & Mechanics ---
    public void takeDamage(int amount) {
        if (amount <= 0) return;

        int healthBeforeDamage = this.health;
        this.health -= amount;

        System.out.println(getName() + " takes " + amount + " damage. Health: " + this.health + "/" + this.maxHealth +
                           " (Was: " + healthBeforeDamage + ")");
        // System.out.println("takeDamage: this.health is now " + this.health + " for " + this.name +
        //                    " (object: " + System.identityHashCode(this) + ")"); // Debugging

        if (this.health <= 0 && healthBeforeDamage > 0) { // Check healthBeforeDamage to ensure die() is called only once
            this.health = 0; // Clamp health at 0
            die();
        }
    }

    /**
     * Heals the LivingBeing by the specified amount, up to maxHealth.
     * This method is now concrete in LivingBeing.
     */
    public void heal(int amount) {
        if (amount <= 0 || this.health >= this.maxHealth) {
            if (this.health >= this.maxHealth) {
                 System.out.println(getName() + " is already at full health. Heal attempt with " + amount + " ignored.");
            }
            return;
        }
        int healthBeforeHeal = this.health;
        this.health = Math.min(this.health + amount, this.maxHealth);
        if (this.health > healthBeforeHeal) {
            System.out.println(getName() + " heals for " + (this.health - healthBeforeHeal) + " HP. Health: " + this.health + "/" + this.maxHealth);
        } else {
            System.out.println(getName() + " heal attempt had no effect (already max or zero amount). Health: " + this.health + "/" + this.maxHealth);
        }
    }

    /**
     * Handles the death of this LivingBeing.
     * Drops inventory, removes from current room and world.
     * Subclasses (like Player) will override this to add specific game over logic
     * and then call super.die().
     */
    protected void die() {
        System.out.println(this.name + " (" + System.identityHashCode(this) + ") [LivingBeing.die()] processing death.");

        if (currentRoom != null && position != null) {
            for (Item item : new ArrayList<>(this.inventory)) { // Iterate copy
                dropItem(item);
            }
        }
        this.inventory.clear();

        if (currentRoom != null) {
            currentRoom.removeLivingBeing(this);
        }
        if (world != null) {
            world.removeEntity(this); // This will also set world.player = null if this is the player
        }
    }

    public void move(int dx, int dy) {
        if (currentRoom == null || position == null) {
            System.err.println(getName() + " cannot move: not in a room or no position.");
            return;
        }

        int currentX = position.getX();
        int currentY = position.getY();
        int newX = currentX + dx;
        int newY = currentY + dy;

        // 1. Boundary Check for the current room
        if (newX < 0 || newX >= Room.ROOM_WIDTH_TILES ||
            newY < 0 || newY >= Room.ROOM_HEIGHT_TILES) {
            // Check if currently on a door tile and this move is "through" it
            Tile tilePlayerIsOn = currentRoom.getTile(currentX, currentY);
            if (tilePlayerIsOn != null && tilePlayerIsOn.getEntityOnTile() instanceof Door) {
                Door doorPlayerIsOn = (Door) tilePlayerIsOn.getEntityOnTile();
                // Check if the out-of-bounds move aligns with an exit for this door
                Room otherRoom = doorPlayerIsOn.getOtherRoom(currentRoom);
                if (otherRoom != null) { // If it leads somewhere
                    Position doorPosInOtherRoom = doorPlayerIsOn.getPositionInRoom(otherRoom);
                    // A simple check: if new target is out of *this* room's bounds, but aligns with door exit logic
                    // This can be complex. For now, handleDoorMovement will be called if player is ON a door tile.
                    // The better way is if moving onto a door tile is the trigger.
                    System.out.println(getName() + " is on door tile " + doorPlayerIsOn.getName() + ". Attempting to move (" + dx + "," + dy + ") (potentially out of bounds).");
                    handleDoorMovement(doorPlayerIsOn, dx, dy);
                    return;
                }
            }
            System.out.println(getName() + " cannot move there (out of bounds of current room). Target: (" + newX + "," + newY + ")");
            return;
        }

        // 2. Get the target tile (now guaranteed to be within bounds)
        Tile targetTile = currentRoom.getTile(newX, newY);
        if (targetTile == null) {
            System.err.println(getName() + " - move: Target tile at (" + newX + "," + newY + ") is null unexpectedly (after bounds check).");
            return;
        }

        // 3. Check if the target tile contains a Door entity
        if (targetTile.getEntityOnTile() instanceof Door) {
            System.out.println(getName() + " is moving onto/through door tile: " + ((Door)targetTile.getEntityOnTile()).getName());
            handleDoorMovement((Door) targetTile.getEntityOnTile(), dx, dy);
        }
        // 4. Else, check if the target tile is walkable (and not a door)
        else if (targetTile.isWalkable()) {
            setPosition(new Position(newX, newY));
            System.out.println(getName() + " moved to (" + newX + ", " + newY + ")");
        }
        // 5. Else, it's blocked
        else {
            System.out.println(getName() + " cannot move there. Target: (" + newX + "," + newY + ") is blocked.");
        }
    }

    private void handleDoorMovement(Door door, int intendedDx, int intendedDy) {
        if (door == null) { /* ... error ... */ return; }
        System.out.println(getName() + " - handleDoorMovement: Interacting with Door '" + door.getName() + "'. Locked: " + door.isLocked() + ", Open: " + door.isCurrentlyOpen());

        if (this instanceof Player && (door.getName().equals("Dimensional Exit") || door.getName().equals("Shimmering Portal"))) {
            if (!door.isLocked()) {
                System.out.println(name + " steps through the " + door.getName() + "! YOU WIN!");
                if (world != null) world.setPlayerWon(true);
                return;
            } else { /* ... sealed message ... */ return; }
        }

        if (!door.isLocked()) {
            door.passThrough(); // Sets isCurrentlyOpen to true
            Room nextRoom = door.getOtherRoom(this.currentRoom);
            if (nextRoom != null) {
                Position doorPosInNextRoom = door.getPositionInRoom(nextRoom);
                if (doorPosInNextRoom == null) { /* ... error ... */ return; }
                Position newPositionInNextRoom = findEntrySpot(nextRoom, doorPosInNextRoom, intendedDx, intendedDy);
                setCurrentRoom(nextRoom);
                setPosition(newPositionInNextRoom);
                System.out.println(name + " successfully moved to room: " + nextRoom.getName() + " at " + newPositionInNextRoom);
            } else { /* ... door leads nowhere ... */ }
        } else { /* ... door locked ... */ }
    }

    private Position findEntrySpot(Room newRoom, Position doorPosInNewRoom, int entryDx, int entryDy) {
        // ... (Keep your existing findEntrySpot logic, it seemed reasonable)
        // For robustness, ensure it always returns a valid position within newRoom bounds.
        int targetX = doorPosInNewRoom.getX() + entryDx;
        int targetY = doorPosInNewRoom.getY() + entryDy;

        // Clamp to new room boundaries first
        targetX = Math.max(0, Math.min(targetX, Room.ROOM_WIDTH_TILES - 1));
        targetY = Math.max(0, Math.min(targetY, Room.ROOM_HEIGHT_TILES - 1));

        Tile preferredEntryTile = newRoom.getTile(targetX, targetY);
        if (preferredEntryTile != null && preferredEntryTile.isWalkable() && (preferredEntryTile.getEntityOnTile() == null || preferredEntryTile.getEntityOnTile().isPassable())) {
            return new Position(targetX, targetY);
        }

        // Fallback: try adjacent to door in new room
        int[] dxOffsets = {0, 0, 1, -1, 1, -1, 1, -1}; // N, S, E, W, NE, NW, SE, SW
        int[] dyOffsets = {-1, 1, 0, 0, -1, -1, 1, 1};

        for (int i = 0; i < dxOffsets.length; i++) {
            int checkX = doorPosInNewRoom.getX() + dxOffsets[i];
            int checkY = doorPosInNewRoom.getY() + dyOffsets[i];
            if (checkX >= 0 && checkX < Room.ROOM_WIDTH_TILES && checkY >= 0 && checkY < Room.ROOM_HEIGHT_TILES) {
                Tile t = newRoom.getTile(checkX, checkY);
                if (t != null && t.isWalkable() && (t.getEntityOnTile() == null || t.getEntityOnTile().isPassable())) {
                    return new Position(checkX, checkY);
                }
            }
        }
        System.err.println("findEntrySpot: Critically failed to find any walkable entry spot for door " + doorPosInNewRoom + " in " + newRoom.getName() + ". Placing on door tile.");
        return doorPosInNewRoom; // Last resort
    }

    public boolean pickUpItem(Item item) {
        if (item == null) {
            System.out.println(name + " tried to pick up a null item.");
            return false;
        }
        if (currentRoom == null) {
            System.out.println(name + " is not in a room, cannot pick up items.");
            return false;
        }
        if (position == null) {
            System.out.println(name + " has no position, cannot pick up items.");
            return false;
        }
        if (item.getPosition() == null) {
            System.out.println("Item " + item.getName() + " has no position, cannot be picked up from floor.");
            return false;
        }

        // Check if the item is in the current room's list of items on the floor
        // AND if the item is at the player's current position.
        if (currentRoom.getItemsInRoom().contains(item) && item.getPosition().equals(this.position)) {
            List<Item> currentInventory = this.getInventory(); // This gets the direct reference to this.inventory
            if (currentInventory.add(item)) { // Add to the player's actual inventory
                currentRoom.removeItem(item);   // Remove from room's floor
                item.setOwner(this);            // Set owner
                item.setPosition(null);         // Item in inventory has no world position
                System.out.println(name + " picked up " + item.getName());
                return true;
            } else {
                System.out.println(name + " failed to add " + item.getName() + " to inventory (list add failed).");
                return false;
            }
        }
        System.out.println(name + " could not pick up " + item.getName() + ". It's not at " + this.position +
                           " in " + currentRoom.getName() + " or not in room's item list.");
        return false;
    }

    public void dropItem(Item item) {
        // ... (your existing dropItem logic, seems fine) ...
        if (item == null) return;
        if (inventory.remove(item)) {
            if (currentRoom != null && position != null) {
                item.setOwner(null);
                // Drop at current player's position
                item.setPosition(new Position(this.position.getX(), this.position.getY()));
                currentRoom.addItem(item); // This adds to room's list. Room.drawContents() will draw it.
                System.out.println(name + " dropped " + item.getName());
            } else {
                inventory.add(item); // Add it back if drop failed
                System.err.println(name + " failed to drop " + item.getName() + ": not in a room or no valid position.");
            }
        }
    }

    public void attack(LivingBeing target) {
        // ... (your existing attack logic, seems fine) ...
        if (target == null || target == this || target.getHealth() <= 0) {
            System.out.println(name + " cannot attack invalid target.");
            return;
        }
        if (target.getCurrentRoom() != this.currentRoom) {
            System.out.println(target.getName() + " is not in the same room.");
            return;
        }
        if (this.position == null || target.getPosition() == null) return;

        int dx = Math.abs(this.position.getX() - target.getPosition().getX());
        int dy = Math.abs(this.position.getY() - target.getPosition().getY());

        if (dx <= 1 && dy <= 1 && (dx + dy > 0)) { // Adjacent, not self
            System.out.println(name + " attacks " + target.getName() + " for " + strength + " damage.");
            target.takeDamage(strength);
        } else {
            System.out.println(name + " is too far to melee attack " + target.getName());
        }
    }

    // Abstract execute() method - to be implemented by Player and NPC
    @Override
    public abstract void execute();

    /**
     * Draw method for LivingBeings.
     * Assumes it's called by Room.drawContents(), which calculates the base screenX/Y for the being's tile.
     * OR this method calculates its own screenX/Y if Entity.draw() is different.
     * For consistency with Tile.draw() calling entityOnTile.draw(screenX, screenY, w, h),
     * this LivingBeing.draw() should ideally also take screenX, screenY if it could BE an entityOnTile.
     *
     * However, typically LivingBeings are mobile and drawn in a separate loop in Room.
     * So, this signature (taking tilePixelWidth/Height and calculating screenX/Y internally) is common.
     */
    // This is the version called by Room.drawContents for mobile beings
    public void draw(Graphics g, int tilePixelWidth, int tilePixelHeight) {
        if (health <= 0 && !(this instanceof Player && world != null && world.isGameOver())) {
            return;
        }
        if (this.position == null) {
            System.err.println("LivingBeing " + getName() + " has null position. Cannot draw.");
            return;
        }

        int screenX = this.position.getX() * tilePixelWidth;
        int screenY = this.position.getY() * tilePixelHeight;

        // Player will override this for its flashing logic
        if (this.sprite != null) {
            g.drawImage(this.sprite, screenX, screenY, tilePixelWidth, tilePixelHeight, null);
        } else {
            Color beingColor = (this instanceof Player) ? Color.BLUE : Color.RED;
            g.setColor(beingColor);
            g.fillOval(screenX + tilePixelWidth / 4, screenY + tilePixelHeight / 4, tilePixelWidth / 2, tilePixelHeight / 2);
        }
    }

    /**
     * This method satisfies the Entity abstract contract if Entity.draw requires screenX, screenY.
     * If LivingBeings are never set as a Tile's primary entityOnTile (which is typical),
     * this can be a simple delegation or a no-op.
     */
    @Override
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        // This method is called if a LivingBeing IS the entityOnTile (less common).
        // It's given the screen coordinates of the tile.
        if (health <= 0 && !(this instanceof Player && world != null && world.isGameOver())) {
            return;
        }

        // Player class should override this to include its flashing logic if it can be an entityOnTile.
        if (this.sprite != null) {
            g.drawImage(this.sprite, screenX, screenY, tilePixelWidth, tilePixelHeight, null);
        } else {
            Color beingColor = (this instanceof Player) ? Color.BLUE : Color.RED;
            g.setColor(beingColor);
            g.fillOval(screenX + tilePixelWidth / 4, screenY + tilePixelHeight / 4, tilePixelWidth / 2, tilePixelHeight / 2);
        }
    }

    public boolean isPassable() {
        return false; // Living beings generally block the tile they are on.
    }

    public void addItemToInventory(Item item) {
        if (this.inventory != null && item != null) {
            this.inventory.add(item);
            item.setOwner(this); // Set owner when adding
            item.setPosition(null); // Item in inventory has no world position
            System.out.println(item.getName() + " added to " + this.getName() + "'s inventory.");
        }
    }

    public void removeItemFromInventory(Item item) {
        if (this.inventory != null && item != null) {
            if (this.inventory.remove(item)) {
                item.setOwner(null); // Clear owner
                System.out.println(item.getName() + " removed from " + this.getName() + "'s inventory.");
            }
        }
    }
}