package com.mazegame.items;

import com.mazegame.core.Room;
import com.mazegame.core.World;
import com.mazegame.characters.LivingBeing;
import com.mazegame.characters.NPC;
import com.mazegame.characters.Player;
import com.mazegame.utils.Position;
// import com.mazegame.ui.SpriteManager;

public class Gun extends Item {
    private int damage;
    private int currentAmmo;
    private int maxAmmoCapacity;
    private String requiredAmmoType; // Matches Ammo.ammoType
    private int range; // Optional: for ranged attacks, in tiles

    public Gun(String name, World world, Position position, int damage, int maxAmmoCapacity, String requiredAmmoType, int range) {
        super(name, world, position);
        this.damage = damage;
        this.maxAmmoCapacity = maxAmmoCapacity;
        this.currentAmmo = 0; // Start empty or partially loaded
        this.requiredAmmoType = requiredAmmoType;
        this.range = range; // e.g., 5 tiles
        // this.sprite = SpriteManager.getSprite("gun_" + name.toLowerCase().replace(" ", "_") + ".png");
        // For a generic gun sprite:
        // this.sprite = SpriteManager.getSprite("gun.png");
    }

    public String getRequiredAmmoType() {
        return requiredAmmoType;
    }

    public void addAmmo(int amount) {
        this.currentAmmo = Math.min(this.currentAmmo + amount, this.maxAmmoCapacity);
        System.out.println(getName() + " ammo: " + currentAmmo + "/" + maxAmmoCapacity);
    }

    public int getCurrentAmmo() {
        return currentAmmo;
    }

    public int getMaxAmmoCapacity() {
        return this.maxAmmoCapacity;
    }

    // The "use" method for a gun means to "fire" it.
    // This needs a target or a direction. For simplicity, let's make it target the closest NPC in a line of sight.
    // This is complex. A simpler first step: fire at an adjacent NPC.
    @Override
    public void use(LivingBeing user) {
        if (currentAmmo <= 0) {
            System.out.println(getName() + " is out of ammo! Click-click.");
            return;
        }

        // For now, let's assume "use" means fire at an adjacent enemy if player is using.
        // More complex targeting (e.g., direction player is facing, mouse click) is for later.
        // This example targets the first adjacent enemy.
        Room currentRoom = user.getCurrentRoom();
        if (currentRoom == null) return;

        LivingBeing targetToShoot = null;

        // Simplified: Check adjacent tiles for an enemy.
        // A more robust system would involve a "facing" direction for the user or mouse aiming.
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue; // Skip self

                Position adjacentPos = new Position(user.getPosition().getX() + dx, user.getPosition().getY() + dy);
                for (LivingBeing beingInRoom : currentRoom.getLivingBeingsInRoom()) {
                    if (beingInRoom.getPosition().equals(adjacentPos) && beingInRoom != user) {
                        // Found a being on an adjacent tile that isn't the user
                        if ((user instanceof Player && beingInRoom instanceof NPC) ||
                            (user instanceof NPC && beingInRoom instanceof Player)) { // Ensure it's an enemy
                            targetToShoot = beingInRoom;
                            break;
                        }
                    }
                }
                if (targetToShoot != null) break;
            }
            if (targetToShoot != null) break;
        }


        if (targetToShoot != null) {
            System.out.println(user.getName() + " fires " + getName() + " at " + targetToShoot.getName() + "!");
            targetToShoot.takeDamage(this.damage);
            this.currentAmmo--;
            System.out.println(getName() + " ammo: " + currentAmmo + "/" + maxAmmoCapacity);

            if (targetToShoot.getHealth() <= 0 && world != null) {
                // Target might die, world.update() usually handles removal from executables.
                // If not, ensure target is properly removed or marked as dead here or in takeDamage.
                 System.out.println(targetToShoot.getName() + " was defeated by " + user.getName() + "'s " + getName() + "!");
            }

        } else {
            System.out.println(user.getName() + " fires " + getName() + " but hits nothing nearby.");
            // Could still consume ammo for a missed shot if desired
            // this.currentAmmo--;
            // System.out.println(getName() + " ammo: " + currentAmmo + "/" + maxAmmoCapacity);
        }
    }

    // You might want a separate "attack" or "fire" method that takes a target or direction
    // if "use" is only for reloading via an Ammo item.
    // For now, we'll overload "use" for firing.
}