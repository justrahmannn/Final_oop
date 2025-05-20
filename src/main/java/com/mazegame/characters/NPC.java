package com.mazegame.characters;

import com.mazegame.core.Room;
import com.mazegame.core.World;
import com.mazegame.items.Item;
import com.mazegame.items.Key;
import com.mazegame.items.Ammo;
import com.mazegame.items.AidKit;
import com.mazegame.utils.Position;
import com.mazegame.ui.SpriteManager;

import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.util.Random;

public class NPC extends LivingBeing {
    private Player targetPlayer;
    private Random randomGenerator; // For drop chances

    @Override
    public void heal(int amount) {
        // Example implementation: increase health but not above maxHealth
        this.health = Math.min(this.health + amount, this.maxHealth);
        System.out.println(this.name + " healed for " + amount + " points. Current health: " + this.health);
    }

    // Loot drop configuration
    public boolean dropsSpecialKey = false;
    private String specialKeyId = null;
    private String specialKeyName;

    public NPC(String name, World world, Position initialPosition, Room startRoom,
               int maxHealth, int strength, Player targetPlayer) {
        super(name, world, initialPosition, startRoom, maxHealth, strength);
        this.targetPlayer = targetPlayer;
        this.randomGenerator = new Random();
        this.sprite = SpriteManager.getSprite("goblin.png");

        if (this.sprite == null) {
            System.out.println("Warning: Could not load sprite for " + name);
        }

        System.out.println(name + " (NPC) created in " + startRoom.getName());
    }

    // Method to configure a special key drop for this NPC instance
    public void setSpecialKeyDrop(String keyName, String keyId) {
        this.dropsSpecialKey = true;
        this.specialKeyName = keyName;
        this.specialKeyId = keyId;
    }

    public String getSpecialKeyName() {
        return specialKeyName;
    }

    public String getSpecialKeyDrop() {
        return specialKeyName;
    }

    public String getSpecialKeyId() {
        return specialKeyId;
    }

    @Override
    public void execute() {
        if (!canAct()) return;

        if (targetPlayer.getCurrentRoom() == this.currentRoom) {
            handleCombatOrMovement();
        }
    }

    private boolean canAct() {
        return targetPlayer != null && health > 0 && currentRoom != null;
    }

    private void handleCombatOrMovement() {
        int dxPlayer = targetPlayer.getPosition().getX() - this.position.getX();
        int dyPlayer = targetPlayer.getPosition().getY() - this.position.getY();

        if (isInAttackRange(dxPlayer, dyPlayer)) {
            attack(targetPlayer);
        } else {
            moveTowardsTarget(dxPlayer, dyPlayer);
        }
    }

    private boolean isInAttackRange(int dx, int dy) {
        return Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && (dx != 0 || dy != 0);
    }

    private void moveTowardsTarget(int dxPlayer, int dyPlayer) {
        int moveX = Integer.compare(dxPlayer, 0);
        int moveY = Integer.compare(dyPlayer, 0);

        // Try horizontal movement first
        if (moveX != 0 && tryMove(moveX, 0)) {
            return;
        }

        // Try vertical movement if horizontal failed or wasn't needed
        if (moveY != 0) {
            tryMove(0, moveY);
        }
    }

    private boolean tryMove(int dx, int dy) {
        Position nextPos = new Position(position.getX() + dx, position.getY() + dy);
        if (isValidMove(nextPos)) {
            move(dx, dy);
            return true;
        }
        return false;
    }

    private boolean isValidMove(Position pos) {
        return currentRoom.getTile(pos.getX(), pos.getY()) != null &&
               currentRoom.getTile(pos.getX(), pos.getY()).isWalkable();
    }

    @Override
    protected void die() {
        System.out.println(this.name + " (" + System.identityHashCode(this) + ") has been defeated!");

        // --- LOOT DROP LOGIC ---
        if (currentRoom != null && world != null && this.position != null) {
            Position dropPosition = new Position(this.position.getX(), this.position.getY()); // Copy position BEFORE super.die()

            // Example: Drop a specific key if configured
            System.out.println("NPC.die (" + this.name + "): Checking for special key drop. dropsSpecialKey=" + dropsSpecialKey + ", specialKeyId=" + specialKeyId);
            if (dropsSpecialKey && specialKeyId != null && !specialKeyId.isEmpty()) {
                Key droppedKey = new Key(this.specialKeyName, world, dropPosition, this.specialKeyId);
                currentRoom.addItem(droppedKey);
                world.addEntity(droppedKey);
                System.out.println(this.name + " dropped the " + droppedKey.getName() + " with ID: " + droppedKey.getKeyId());
            } else {
                if (!dropsSpecialKey) System.out.println("NPC.die (" + this.name + "): Not configured to drop a special key (dropsSpecialKey is false).");
                if (specialKeyId == null || specialKeyId.isEmpty()) System.out.println("NPC.die (" + this.name + "): Special key ID is null or empty.");
            }

            // 50% chance to drop some ammo
            if (randomGenerator.nextFloat() < 0.5f) {
                Ammo droppedAmmo = new Ammo("Dropped 9mm", world, dropPosition, "9mm", randomGenerator.nextInt(3) + 1); // 1-3 bullets
                currentRoom.addItem(droppedAmmo);
                world.addEntity(droppedAmmo);
                System.out.println(this.name + " dropped " + droppedAmmo.getName());
            }

            // 25% chance to drop a small aidkit
            if (randomGenerator.nextFloat() < 0.25f) {
                AidKit droppedAidKit = new AidKit("Crude Bandage", world, dropPosition, 10); // Heals for 10
                currentRoom.addItem(droppedAidKit);
                world.addEntity(droppedAidKit);
                System.out.println(this.name + " dropped a " + droppedAidKit.getName());
            }
        } else {
            if (currentRoom == null) System.out.println("NPC.die (" + this.name + "): currentRoom is null, cannot drop loot.");
            if (world == null) System.out.println("NPC.die (" + this.name + "): world is null, cannot drop loot.");
            if (this.position == null) System.out.println("NPC.die (" + this.name + "): this.position is null, cannot determine drop location.");
        }

        //super.die(); // Handles removal from room, world, and dropping its OWN inventory (if any)
    }

    @Override
    public void draw(Graphics g, int screenX, int screenY, int tilePixelWidth, int tilePixelHeight) {
        if (health <= 0) return;

        if (this.sprite != null) { // 'sprite' is inherited from LivingBeing
            g.drawImage(this.sprite, screenX, screenY, tilePixelWidth, tilePixelHeight, null);
        } else {
            // NPC placeholder drawing AT screenX, screenY
            g.setColor(Color.RED);
            g.fillOval(screenX + tilePixelWidth / 4,
                       screenY + tilePixelHeight / 4,
                       tilePixelWidth / 2, tilePixelHeight / 2);
            g.setColor(Color.BLACK);
            g.drawOval(screenX + tilePixelWidth / 4,
                       screenY + tilePixelHeight / 4,
                       tilePixelWidth / 2, tilePixelHeight / 2);
        }
    }
}
