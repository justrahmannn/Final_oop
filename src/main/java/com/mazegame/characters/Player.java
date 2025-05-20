package com.mazegame.characters;

import com.mazegame.core.Room;
import com.mazegame.core.World;
import com.mazegame.core.Tile;
import com.mazegame.core.Entity;
import com.mazegame.core.Door;
import com.mazegame.items.Item;
import com.mazegame.items.Lever;
import com.mazegame.items.Chest;
import com.mazegame.items.Treasure;
import com.mazegame.interfaces.Activatable;
import com.mazegame.utils.Position;
import com.mazegame.ui.SpriteManager;

import java.awt.Graphics;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
// No Image import needed if sprite is handled by LivingBeing

public class Player extends LivingBeing {

    // Fields specific to Player
    private boolean recentlyDamaged = false;
    private long lastDamageTime = 0;
    private static final long DAMAGE_FLASH_DURATION = 200; // milliseconds
    private int activeItemSlot = 0;

    public Player(String name, World world, Position initialPosition, Room startRoom,
                 int maxHealth, int strength) {
        super(name, world, initialPosition, startRoom, maxHealth, strength);
        this.sprite = SpriteManager.getSprite("player.png"); // Assigns to LivingBeing.sprite
        if (this.sprite == null) {
            System.err.println("Warning: Player sprite ('player.png') could not be loaded for " + this.name);
        }
    }

    // getHealth() is inherited from LivingBeing
    // getMaxHealth() is inherited from LivingBeing
    // heal(int amount) is NOW FULLY INHERITED from LivingBeing

    public int getActiveItemSlot() {
        return activeItemSlot;
    }

    public void setActiveItemSlot(int slotIndex) {
        if (inventory == null) {
            System.err.println("Player.setActiveItemSlot: Inventory is null!");
            return;
        }
        if (slotIndex >= 0 && slotIndex < inventory.size()) {
            this.activeItemSlot = slotIndex;
            System.out.println("Active item set to slot: " + (slotIndex + 1) + " (" + inventory.get(slotIndex).getName() + ")");
        } else if (inventory.isEmpty() && slotIndex == 0) {
            this.activeItemSlot = 0;
        } else {
            System.out.println("Cannot set active item to invalid slot: " + (slotIndex + 1) + ". Inventory size: " + inventory.size());
        }
    }

    public Item getActiveItem() {
        if (inventory == null) {
            System.err.println("Player.getActiveItem: Inventory is null!");
            return null;
        }
        if (activeItemSlot >= 0 && activeItemSlot < inventory.size()) {
            return inventory.get(activeItemSlot);
        }
        return null;
    }

    @Override
    public void execute() {
        // Player actions are driven by GUI events
    }

    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount); // Modifies LivingBeing.health
        if (this.health > 0) {    // 'this.health' now refers to LivingBeing.health
            this.recentlyDamaged = true;
            this.lastDamageTime = System.currentTimeMillis();
        }
    }

    @Override
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        if (this.recentlyDamaged) {
            if (System.currentTimeMillis() - lastDamageTime > DAMAGE_FLASH_DURATION) {
                this.recentlyDamaged = false;
            } else {
                if ((System.currentTimeMillis() / 50) % 2 == 0) { // Flicker
                    return;
                }
            }
        }
        if (this.sprite != null) {
            g.drawImage(this.sprite, screenX, screenY, tilePixelWidth, tilePixelHeight, null);
        } else {
            g.setColor(Color.BLUE); // Player placeholder
            g.fillOval(screenX + tilePixelWidth / 4, screenY + tilePixelHeight / 4, tilePixelWidth / 2, tilePixelHeight / 2);
        }
    }

    // customPlayerDrawLogic is not needed if Player.draw() handles everything itself.
    // public void customPlayerDrawLogic(...)

    public void interact() {
        if (currentRoom == null || position == null) {
            System.err.println("Player.interact: currentRoom or position is null. Cannot interact.");
            return;
        }
        System.out.println(getName() + " attempts to interact at (" + position.getX() + "," + position.getY() +")...");

        List<Item> itemsAtFeet = new ArrayList<>();
        for (Item item : currentRoom.getItemsInRoom()) {
            if (item.getPosition() != null && item.getPosition().equals(this.position)) {
                itemsAtFeet.add(item);
            }
        }
        if (!itemsAtFeet.isEmpty()) {
            Item itemToInteractWith = itemsAtFeet.get(0);
            System.out.println("  Found item at feet: " + itemToInteractWith.getName());
            if (itemToInteractWith instanceof Treasure) {
                ((Treasure) itemToInteractWith).winGame(this);
                return;
            } else {
                pickUpItem(itemToInteractWith);
                return;
            }
        }
        if (tryInteractWithAdjacentEntities()) {
            return;
        }
        System.out.println("  Nothing specific to interact with nearby.");
    }

    private boolean tryInteractWithAdjacentEntities() {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                Tile adjacentTile = currentRoom.getTile(position.getX() + dx, position.getY() + dy);
                if (adjacentTile != null && adjacentTile.getEntityOnTile() != null) {
                    Entity entity = adjacentTile.getEntityOnTile();
                    if (entity instanceof Activatable) {
                        handleActivatableEntity((Activatable) entity, entity);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void handleActivatableEntity(Activatable activatable, Entity entityAsEntity) {
        System.out.println("  Found activatable: " + entityAsEntity.getName());
        if (activatable instanceof Chest) {
            ((Chest) activatable).open(this);
        } else if (activatable instanceof Door) {
            Door door = (Door) activatable;
            if (door.getName().equals("Dimensional Exit") || door.getName().equals("Shimmering Portal")) {
                if (!door.isLocked()) {
                    System.out.println(this.getName() + " has found the " + door.getName() + "! YOU WIN!");
                    if (world != null) world.setPlayerWon(true);
                } else {
                    System.out.println("  " + door.getName() + " is sealed. It might need a special key or condition.");
                }
            } else {
                if (door.isLocked()) {
                    System.out.println("  " + door.getName() + " is locked. Try using a key or crowbar.");
                } else {
                    System.out.println("  " + door.getName() + " is unlocked. Move into it to pass through.");
                }
            }
        } else if (activatable instanceof Lever) {
            ((Lever) activatable).pull();
        }
    }

    @Override
    protected void die() {
        System.out.println("Player " + this.name + " is dying (pre-super.die). Health: " + this.health);
        super.die(); // This calls LivingBeing.die() for item drops & removal from room/world lists
        System.out.println("GAME OVER! The player (" + this.name + ") has died (post-super.die).");
        if (world != null) {
            world.setGameOver(true);
        }
    }

    // NO heal(int amount) METHOD HERE - IT WILL BE INHERITED FROM LivingBeing
}