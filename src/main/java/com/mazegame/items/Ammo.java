package com.mazegame.items;

import com.mazegame.core.World;
import com.mazegame.characters.LivingBeing;
import com.mazegame.utils.Position;

public class Ammo extends Item {
    private int quantity;
    private String ammoType; // e.g., "pistol_rounds", "shotgun_shells" - for future if multiple guns

    public Ammo(String name, World world, Position position, String ammoType, int quantity) {
        super(name, world, position); // Position can be null if starting in inventory/chest
        this.ammoType = ammoType;
        this.quantity = quantity;
        // this.sprite = SpriteManager.getSprite("ammo_" + ammoType + ".png"); // e.g., ammo_pistol_rounds.png
        // For a generic ammo sprite:
        // this.sprite = SpriteManager.getSprite("ammo.png");
    }

    public String getAmmoType() {
        return ammoType;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public void use(LivingBeing user) {
        boolean ammoUsed = false;
        for (Item itemInInventory : user.getInventory()) {
            if (itemInInventory instanceof Gun) {
                Gun gun = (Gun) itemInInventory;
                // Check if the gun uses this type of ammo
                if (gun.getRequiredAmmoType().equals(this.ammoType)) {
                    gun.addAmmo(this.quantity);
                    System.out.println(user.getName() + " reloaded " + gun.getName() + " with " + this.quantity + " " + this.name + ".");
                    ammoUsed = true;
                    break; // Stop after reloading one gun
                }
            }
        }

        if (ammoUsed) {
            user.getInventory().remove(this); // Consume the ammo pack
            if (world != null) {
                world.removeEntity(this);
            }
        } else {
            System.out.println(user.getName() + " has no compatible gun to reload with " + this.name + ".");
        }
    }
}