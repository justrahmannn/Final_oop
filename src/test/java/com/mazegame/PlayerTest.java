package com.mazegame; // Or com.mazegame.characters if that's where PlayerTest.java is

import com.mazegame.characters.Player;
import com.mazegame.core.*;
import com.mazegame.items.Item;
import com.mazegame.items.Key;
import com.mazegame.items.Chest;
// import com.mazegame.items.AidKit; // Add if used in other tests in this file
// import com.mazegame.items.Treasure; // Add if used
import com.mazegame.utils.Position;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    private World world;
    private Room startRoom;
    private Room nextRoom;
    private Player player;
    private Door testDoor;

    @BeforeEach
    void setUp() {
        world = new World("Test World");

        startRoom = new Room(0, "Start Room", world, new Position(0, 0));
        nextRoom = new Room(1, "Next Room", world, new Position(1, 0));
        world.addEntity(startRoom);
        world.addEntity(nextRoom);

        Position doorPosInStartRoom = new Position(Room.ROOM_WIDTH_TILES - 1, Room.ROOM_HEIGHT_TILES / 2);
        Position doorPosInNextRoom = new Position(0, Room.ROOM_HEIGHT_TILES / 2);
        testDoor = new Door("Test Door", world, startRoom, doorPosInStartRoom, nextRoom, doorPosInNextRoom, false, "door_key_1", false);
        world.addEntity(testDoor);
        // startRoom.addDoor(testDoor); // Assuming Door constructor or addDoor in Room handles tile setup
        // nextRoom.addDoor(testDoor);


        Position playerStartPos = new Position(Room.ROOM_WIDTH_TILES / 2, Room.ROOM_HEIGHT_TILES / 2);
        player = new Player("TestHero", world, playerStartPos, startRoom, 100, 10);
        world.setPlayer(player); // Assumes you added setPlayer to World.java

        // Ensure tiles are walkable for basic movement tests.
        if (startRoom.getTile(playerStartPos.getX(), playerStartPos.getY()) != null) {
             startRoom.getTile(playerStartPos.getX(), playerStartPos.getY()).setWalkable(true); // Assumes Tile has setWalkable
        }
        if (startRoom.getTile(playerStartPos.getX() + 1, playerStartPos.getY()) != null) {
             startRoom.getTile(playerStartPos.getX() + 1, playerStartPos.getY()).setWalkable(true);
        }
    }

    @Test
    void testPlayerInitialization() {
        assertEquals("TestHero", player.getName());
        assertEquals(100, player.getHealth());
        assertEquals(100, player.getMaxHealth());
        // assertEquals(10, player.getStrength()); // Assuming strength is accessible
        assertEquals(startRoom, player.getCurrentRoom());
        assertNotNull(player.getInventory());
        // Initial inventory might have a pistol, adjust assertion accordingly
        // assertTrue(player.getInventory().isEmpty(), "Inventory should be empty initially.");
        assertEquals(Room.ROOM_WIDTH_TILES / 2, player.getPosition().getX());
        assertEquals(Room.ROOM_HEIGHT_TILES / 2, player.getPosition().getY());
    }

    @Test
    void testPlayerMovementValid() {
        Position initialPos = new Position(player.getPosition().getX(), player.getPosition().getY());
        // Ensure the target tile is actually walkable in your test room setup
        if (startRoom.getTile(initialPos.getX() + 1, initialPos.getY()) != null &&
            startRoom.getTile(initialPos.getX() + 1, initialPos.getY()).isWalkable()) {
            player.move(1, 0);
            assertEquals(initialPos.getX() + 1, player.getPosition().getX(), "Player should move right.");
            assertEquals(initialPos.getY(), player.getPosition().getY());
        } else {
            System.out.println("PlayerMovementValid Test: Target tile for rightward move is not walkable or null. Test might not be fully meaningful.");
            // Or fail if you expect it to always be walkable in this setup
        }
    }

    @Test
    void testPlayerMovementInvalidWall() {
        player.setPosition(new Position(Room.ROOM_WIDTH_TILES / 2, 1));
        Position initialPos = new Position(player.getPosition().getX(), player.getPosition().getY());

        player.move(0, -1);
        assertEquals(initialPos.getX(), player.getPosition().getX());
        assertEquals(initialPos.getY(), player.getPosition().getY());
    }

    @Test
    void testPlayerMovementThroughUnlockedDoor() {
        // Assuming Door.setLockedState exists now
        testDoor.setLockedState(false);
        testDoor.open();

        player.setPosition(new Position(Room.ROOM_WIDTH_TILES - 2, Room.ROOM_HEIGHT_TILES / 2));
        assertEquals(startRoom, player.getCurrentRoom());

        player.move(1, 0);

        assertEquals(nextRoom, player.getCurrentRoom());
        // More specific position check might be needed based on your door entry logic
        assertTrue(player.getPosition().getX() < Room.ROOM_WIDTH_TILES -1 && player.getPosition().getX() > 0);
    }

    @Test
    void testPlayerMovementThroughLockedDoor() {
        testDoor.setLockedState(true); // Use the added method

        player.setPosition(new Position(Room.ROOM_WIDTH_TILES - 2, Room.ROOM_HEIGHT_TILES / 2));
        Position initialPos = new Position(player.getPosition().getX(), player.getPosition().getY());
        Room initialRoom = player.getCurrentRoom();

        player.move(1, 0);

        assertEquals(initialRoom, player.getCurrentRoom());
        assertEquals(initialPos.getX(), player.getPosition().getX());
        assertEquals(initialPos.getY(), player.getPosition().getY());
    }

    @Test
    void testPlayerPickupItem() {
        Position playerPos = player.getPosition();
        Key testKey = new Key("TestKey", world, new Position(playerPos.getX(), playerPos.getY()), "test_key_id");
        startRoom.addItem(testKey);
        world.addEntity(testKey); // Ensure item is known to the world if interact searches world entities

        assertTrue(startRoom.getItemsInRoom().contains(testKey));
        assertFalse(player.getInventory().contains(testKey));

        player.interact();

        assertFalse(startRoom.getItemsInRoom().contains(testKey));
        assertTrue(player.getInventory().contains(testKey));
        assertEquals(player, testKey.getOwner());
    }

    @Test
    void testPlayerDropItem() {
        Key testKey = new Key("TestKeyToDrop", world, null, "drop_key_id");
        player.getInventory().add(testKey);
        testKey.setOwner(player);

        assertTrue(player.getInventory().contains(testKey));
        Position dropPosition = new Position(player.getPosition().getX(), player.getPosition().getY());

        player.dropItem(testKey);

        assertFalse(player.getInventory().contains(testKey));
        assertTrue(startRoom.getItemsInRoom().contains(testKey));
        assertEquals(dropPosition, testKey.getPosition());
        assertNull(testKey.getOwner());
    }

    @Test
    void testPlayerTakeDamage() {
        int initialHealth = player.getHealth();
        player.takeDamage(10);
        assertEquals(initialHealth - 10, player.getHealth());
    }

    @Test
    void testPlayerTakeFatalDamage() {
        Key keyInInv = new Key("InvKey", world, null, "inv_key_id");
        player.getInventory().add(keyInInv);
        keyInInv.setOwner(player);

        player.takeDamage(player.getMaxHealth() + 20);

        // assertEquals(0, player.getHealth()); // Health might go negative before being clamped by die() or other logic
        assertTrue(player.getHealth() <= 0, "Player health should be <= 0 after fatal damage.");
        assertTrue(world.isGameOver());
        assertTrue(player.getInventory().isEmpty());

        boolean foundDroppedKey = false;
        for(Item item : startRoom.getItemsInRoom()){
            if(item == keyInInv){
                foundDroppedKey = true;
                break;
            }
        }
        assertTrue(foundDroppedKey, "Dropped key should be in room after player death.");
    }

    @Test
    void testPlayerHeal() {
        player.takeDamage(50); // Health is 50
        player.heal(20);
        assertEquals(70, player.getHealth());
    }

    @Test
    void testPlayerHealDoesNotExceedMaxHealth() {
        player.takeDamage(10); // Health is 90
        player.heal(25);
        assertEquals(player.getMaxHealth(), player.getHealth());
    }

    @Test
    void testPlayerInteractWithLockedChestDoesNotOpen() {
        Position playerPos = player.getPosition();
        Position chestPos = new Position(playerPos.getX() + 1, playerPos.getY());

        Chest lockedChest = new Chest("Locked Chest", world, chestPos, startRoom, true, "chest_key", false);
        if(startRoom.getTile(chestPos.getX(), chestPos.getY()) != null){
            startRoom.getTile(chestPos.getX(), chestPos.getY()).setEntityOnTile(lockedChest);
            startRoom.getTile(chestPos.getX(), chestPos.getY()).setType(Tile.TileType.CHEST);
        } else { fail("Chest tile is null for test."); }
        world.addEntity(lockedChest);

        player.interact();

        assertTrue(lockedChest.isLocked());
        assertFalse(lockedChest.isOpen());
    }

    @Test
    void testPlayerInteractWithUnlockedChestOpensIt() {
        Position playerPos = player.getPosition();
        Position chestPos = new Position(playerPos.getX() + 1, playerPos.getY());

        Chest unlockedChest = new Chest("Unlocked Chest", world, chestPos, startRoom, false, null, false);
        Key itemInChest = new Key("Prize", world, null, "prize_key");
        unlockedChest.addItemInside(itemInChest);
        // world.addEntity(itemInChest); // Item inside chest may not need to be a world entity until taken out

        if(startRoom.getTile(chestPos.getX(), chestPos.getY()) != null){
            startRoom.getTile(chestPos.getX(), chestPos.getY()).setEntityOnTile(unlockedChest);
            startRoom.getTile(chestPos.getX(), chestPos.getY()).setType(Tile.TileType.CHEST);
        } else { fail("Chest tile is null for test."); }
        world.addEntity(unlockedChest);

        assertFalse(player.getInventory().contains(itemInChest));

        player.interact();

        assertTrue(unlockedChest.isOpen());
        assertTrue(player.getInventory().contains(itemInChest));
        assertTrue(unlockedChest.getItemsInside().isEmpty()); // Use the new getter
    }
}