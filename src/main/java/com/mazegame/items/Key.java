package com.mazegame.items;

import com.mazegame.core.Door;
import com.mazegame.core.Entity;
import com.mazegame.core.Tile;
import com.mazegame.core.World;
import com.mazegame.characters.LivingBeing;
import com.mazegame.interfaces.Activatable;
import com.mazegame.utils.Position;
import com.mazegame.utils.SpriteManager;

public class Key extends Item {
    private final String keyId;  // Made final since it shouldn't change

    public Key(String name, World world, Position position, String keyId) {
        super(name, world, position);
        this.keyId = keyId != null ? keyId : "";
    }

    public String getKeyId() {
        return keyId;
    }

    @Override
    public void use(LivingBeing user) {
        System.out.println(user.getName() + " tries to use " + getName() + " (Key's ID: " + this.keyId + ")");
        boolean foundTarget = false;
        boolean unlockedSomething = false;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;

                Position adjacentPos = new Position(user.getPosition().getX() + dx, user.getPosition().getY() + dy);
                Tile adjacentTile = user.getCurrentRoom().getTile(adjacentPos.getX(), adjacentPos.getY());

                if (adjacentTile != null && adjacentTile.getEntityOnTile() != null) {
                    Entity entityOnTile = adjacentTile.getEntityOnTile();
                    if (entityOnTile instanceof Activatable) {
                        foundTarget = true;
                        Activatable activatable = (Activatable) entityOnTile;
                        System.out.println("  Found activatable nearby: " + entityOnTile.getName());

                        if (activatable.isLocked()) {
                            String targetKeyId = null;
                            boolean canUnlock = false;

                            if (entityOnTile instanceof Door) {
                                Door door = (Door) entityOnTile;
                                targetKeyId = door.getKeyId();
                                if (this.keyId.equals(targetKeyId) || targetKeyId == null || targetKeyId.isEmpty()) {
                                    door.unlock();
                                    canUnlock = true;
                                }
                            } else if (entityOnTile instanceof Chest) {
                                Chest chest = (Chest) entityOnTile;
                                targetKeyId = chest.getKeyId();
                                if (this.keyId.equals(targetKeyId) || targetKeyId == null || targetKeyId.isEmpty()) {
                                    chest.unlock();
                                    canUnlock = true;
                                }
                            }

                            if (canUnlock) {
                                System.out.println("  SUCCESS: " + getName() + " successfully unlocked " + entityOnTile.getName());
                                // Optional: consume key
                                // user.getInventory().remove(this);
                                // if (world != null) world.removeEntity(this);
                                unlockedSomething = true;
                                return;
                            } else {
                                System.out.println("  FAIL: Key ID '" + this.keyId + "' does not match " + entityOnTile.getName() + " (requires ID '" + targetKeyId + "')");
                            }
                        } else {
                            System.out.println("  INFO: " + entityOnTile.getName() + " is already unlocked.");
                        }
                    }
                }
            }
        }
        if (!foundTarget) {
            System.out.println("  No activatable objects found nearby to use " + getName() + " on.");
        } else if (!unlockedSomething) {
            System.out.println("  " + getName() + " could not be used on any nearby locked objects requiring its specific ID, or they were already unlocked.");
        }
    }

    @Override
    protected void loadSprite() {
        this.sprite = SpriteManager.getSprite("key.png");
        if (this.sprite == null) {
            System.out.println("Warning: Could not load sprite for key");
        }
    }

    @Override
    public String toString() {
        return String.format("Key[id=%s, name=%s]", keyId, getName());
    }
}