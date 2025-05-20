package com.mazegame.core;

import com.mazegame.characters.LivingBeing; // For cleanupDeadEntities
import com.mazegame.characters.Player;
import com.mazegame.characters.NPC;
import com.mazegame.interfaces.Executable;
import com.mazegame.items.Chest;
import com.mazegame.items.Key;
import com.mazegame.items.Lever;
import com.mazegame.puzzles.PuzzleController;
import com.mazegame.items.AidKit;
import com.mazegame.items.Gun;
import com.mazegame.items.Ammo;
// import com.mazegame.items.Treasure; // Not used if Exit Door is the win condition
import com.mazegame.items.Crowbar;
import com.mazegame.utils.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator; // For safe removal during iteration

public class World {
    private String name;
    private List<Entity> entities;
    private List<Executable> executables; // Entities that have an execute() method (Player, NPCs, active traps)
    private List<Room> rooms;
    private Player player; // The single player instance
    private boolean gameOver = false;
    private boolean playerWon = false;
    // private Room startRoom; // Not strictly needed as a field if rooms.get(0) is always the start

    public static final int NUM_ROOMS_X = 3; // e.g., 3x3 grid of rooms
    public static final int NUM_ROOMS_Y = 3;

    public World(String name) {
        this.name = name;
        this.entities = new ArrayList<>();
        this.executables = new ArrayList<>();
        this.rooms = new ArrayList<>();
        // Player is created and set during initializeWorld()
    }

    public void initializeWorld() {
        System.out.println("Initializing world...");

        createRooms();
        createPlayer();
        createDoors();
        populateItemsAndChests();
        populateNPCs();
        setupSpecialGameElements();
        setupLeverPuzzle();
        createTraps(); // <<< CALL NEW METHOD

        System.out.println("World initialization complete.");
    }

    private void createRooms() {
        for (int y = 0; y < NUM_ROOMS_Y; y++) {
            for (int x = 0; x < NUM_ROOMS_X; x++) {
                int roomId = y * NUM_ROOMS_X + x;
                // Room constructor needs: int id, String name, World world, Position worldGridPosition
                Room room = new Room(roomId, "Room " + roomId, this, new Position(x, y));
                addEntity(room); // addEntity also adds to this.rooms list
            }
        }
        if (!rooms.isEmpty()) {
            System.out.println("Created " + rooms.size() + " rooms. Start room will be: " + rooms.get(0).getName());
        } else {
            System.err.println("CRITICAL: No rooms were created!");
        }
    }

    private void createPlayer() {
        if (rooms.isEmpty()) {
            System.err.println("CRITICAL: Cannot create player, no rooms exist. Creating a fallback room.");
            Room fallbackRoom = new Room(0, "Fallback Start Room", this, new Position(0,0));
            addEntity(fallbackRoom);
        }
        Room playerStartRoom = rooms.get(0); // Player starts in the first created room
        Position playerStartPos = new Position(Room.ROOM_WIDTH_TILES / 2, Room.ROOM_HEIGHT_TILES / 2);

        this.player = new Player("Hero", this, playerStartPos, playerStartRoom, 100, 10);
        if (playerStartRoom != null) {
            playerStartRoom.addLivingBeing(this.player);
        }
        addEntity(this.player);
        System.out.println("Player created in " + (playerStartRoom != null ? playerStartRoom.getName() : "NO START ROOM"));

        // Give player starting Pistol using Option A (direct inventory reference)
        Gun pistol = new Gun("Pistol", this, null, 15, 6, "9mm", 5);
        if (player != null) {
            player.getInventory().add(pistol); // Directly add to the list
            pistol.setOwner(player);
            pistol.addAmmo(3);
            System.out.println("Gave Pistol (3/6 ammo) to Player.");
        }
    }

    private void createDoors() {
        // Example: Connect all adjacent rooms in a grid
        for (int y = 0; y < NUM_ROOMS_Y; y++) {
            for (int x = 0; x < NUM_ROOMS_X; x++) {
                Room current = getRoomById(y * NUM_ROOMS_X + x);
                if (current == null) continue;

                // Connect to room on the right (East)
                if (x < NUM_ROOMS_X - 1) {
                    Room eastNeighbor = getRoomById(y * NUM_ROOMS_X + (x + 1));
                    if (eastNeighbor != null) {
                        // Door from East of current to West of eastNeighbor
                        // Parameters: name, world, room1, posInR1, room2, posInR2, locked, keyId, forceable
                        Door doorE = new Door("Door " + current.getRoomID() + "-" + eastNeighbor.getRoomID(), this,
                                current, new Position(Room.ROOM_WIDTH_TILES - 1, Room.ROOM_HEIGHT_TILES / 2),
                                eastNeighbor, new Position(0, Room.ROOM_HEIGHT_TILES / 2),
                                false, null, false); // Unlocked, no key, not forceable by default
                        addEntity(doorE);
                    }
                }
                // Connect to room below (South)
                if (y < NUM_ROOMS_Y - 1) {
                    Room southNeighbor = getRoomById((y + 1) * NUM_ROOMS_X + x);
                    if (southNeighbor != null) {
                        // Door from South of current to North of southNeighbor
                        Door doorS = new Door("Door " + current.getRoomID() + "-" + southNeighbor.getRoomID(), this,
                                current, new Position(Room.ROOM_WIDTH_TILES / 2, Room.ROOM_HEIGHT_TILES - 1),
                                southNeighbor, new Position(Room.ROOM_WIDTH_TILES / 2, 0),
                                false, null, false);
                        addEntity(doorS);
                    }
                }
            }
        }
        System.out.println("Created basic grid doors.");
    }


    private void populateItemsAndChests() {
        if (rooms.isEmpty()) return;
        Room room0 = rooms.get(0); // Start room

        // Items in Start Room (Room 0)
        room0.addItem(new Key("Generic Key", this, new Position(3,3), "door_key_generic"));
        room0.addItem(new Key("Chest Key", this, new Position(2,2), "chest_key_1"));
        room0.addItem(new AidKit("Floor Medkit", this, new Position(4,4), 30));
        room0.addItem(new Crowbar("Sturdy Crowbar", this, new Position(1,5)));
        System.out.println("Populated Room 0 with starting items.");

        // Chest in Room 1
        Room room1 = getRoomById(1);
        if (room1 != null) {
            Chest oldChest = new Chest("Old Chest", this, new Position(5,5), room1,
                                     true, "chest_key_1", true); // Locked, needs key, forceable
            oldChest.addItemInside(new AidKit("Small Medkit", this, null, 25));
            oldChest.addItemInside(new Ammo("9mm Rounds", this, null, "9mm", 12));
            addEntity(oldChest); // The chest itself is an entity
            System.out.println("Added Old Chest (forceable, w/ Medkit, Ammo) to Room 1.");
        }

        // Another chest in Room 3
        Room room3 = getRoomById(3);
        if (room3 != null) {
            Chest dustyCrate = new Chest("Dusty Crate", this, new Position(4,8), room3,
                                       false, null, true); // Unlocked, no key, forceable
            dustyCrate.addItemInside(new Ammo("Shotgun Shells", this, null, "shotgun", 5)); // If you add a shotgun
            addEntity(dustyCrate);
            System.out.println("Added Dusty Crate (forceable, w/ Shells) to Room 3.");
        }
    }

    private void populateNPCs() {
        if (player == null || rooms.size() < 2) {
            System.err.println("Cannot populate NPCs: Player or sufficient rooms not available.");
            return;
        }
        Room room1 = getRoomById(1); // NPCs in Room 1

        if (room1 != null) {
            NPC goblin = new NPC("Goblin", this, new Position(2, 8), room1, 30, 5, player);
            room1.addLivingBeing(goblin); // Explicitly add NPC to room's list
            addEntity(goblin);
            System.out.println("Added Goblin to " + room1.getName());

            NPC goblinGuard = new NPC("Goblin Guard", this, new Position(7, 8), room1, 50, 8, player);
            goblinGuard.setSpecialKeyDrop("Guard's Key", "door_room2_exit_key");
            room1.addLivingBeing(goblinGuard); // Explicitly add NPC to room's list
            addEntity(goblinGuard);
            System.out.println("Added Goblin Guard (will drop: " + goblinGuard.dropsSpecialKey +
                               ", keyName: " + goblinGuard.getSpecialKeyName() +
                               ", keyId: " + goblinGuard.getSpecialKeyId() +
                               ") to " + room1.getName());
        }
    }

    private void setupSpecialGameElements() {
        // Guarded Door requiring key from Goblin Guard
        Room room2 = getRoomById(2);
        Room secretRoom = getRoomById(6); // e.g., Room 6 is the secret/next area
        if (room2 != null && secretRoom != null) {
            Door guardedDoor = new Door("Heavy Vault Door", this,
                    room2, new Position(Room.ROOM_WIDTH_TILES - 1, 5), // East wall, mid-height
                    secretRoom, new Position(0, 5),                  // West wall, mid-height
                    true, "door_room2_exit_key", false); // Locked, needs key, NOT forceable
            addEntity(guardedDoor);
            System.out.println("Placed Heavy Vault Door in Room 2 (to Room 6), requires 'door_room2_exit_key'.");
        }

        // Rickety Door (forceable)
        Room room3 = getRoomById(3);
        Room room4 = getRoomById(4);
        if (room3 != null && room4 != null) {
            Door ricketyDoor = new Door("Rickety Door", this,
                    room3, new Position(5, Room.ROOM_HEIGHT_TILES - 1), // South wall of Room 3
                    room4, new Position(5, 0),                          // North wall of Room 4
                    true, "rickety_door_key", true); // Locked, could have a key, IS forceable
            addEntity(ricketyDoor);
            // Optionally add "rickety_door_key" somewhere if you want it to be openable by key too
            // rooms.get(0).addItem(new Key("Bent Key", this, new Position(1,1), "rickety_door_key"));
            System.out.println("Placed Rickety Door (forceable) between Room 3 and 4.");
        }

        // Exit Door
        Room finalRoom = getRoomById(8); // The last room in a 3x3 grid
        if (finalRoom != null) {
            Door exitDoor = new Door("Dimensional Exit", this,
                    finalRoom, new Position(Room.ROOM_WIDTH_TILES / 2, 0), // North wall of final room
                    null, null, // No "other" room, this is the exit
                    false, null, false); // Unlocked, no key, not forceable. Passing through wins.
            // Note: For this to work, Door.handleDoorMovement needs to check for otherRoom == null
            // OR player.interact checks for this door name.
            // The LivingBeing.handleDoorMovement was updated to check for specific name.
            addEntity(exitDoor);
            System.out.println("Placed Dimensional Exit in " + finalRoom.getName());
        }
    }

    private void setupLeverPuzzle() {
        Room puzzleRoom = getRoomById(4); // Example: Puzzle in Room 4
        Room rewardRoom = getRoomById(5); // Door leads from Room 4 to Room 5

        if (puzzleRoom == null || rewardRoom == null) {
            System.err.println("Cannot setup lever puzzle: Puzzle room or reward room not found.");
            return;
        }

        // 1. Create the door that will be controlled by the puzzle (initially locked)
        Door puzzleDoor = new Door("Sealed Passage", this,
                puzzleRoom, new Position(Room.ROOM_WIDTH_TILES - 1, 3), // East wall of PuzzleRoom
                rewardRoom, new Position(0, 3),                         // West wall of RewardRoom
                true, "puzzle_door_key_id_not_used", false); // Locked, key ID not relevant if puzzle controls, not forceable
        addEntity(puzzleDoor);
        System.out.println("Placed Sealed Passage (puzzle door) between " + puzzleRoom.getName() + " and " + rewardRoom.getName());

        // 2. Create the PuzzleController for this door
        PuzzleController leverPuzzleController = new PuzzleController(puzzleDoor);

        // 3. Create and place Levers in the puzzleRoom, linking them to the controller
        Lever lever1 = new Lever("Lever Alpha", this, new Position(3, 3), leverPuzzleController, false); // Initially OFF
        puzzleRoom.getTile(3,3).setEntityOnTile(lever1);
        addEntity(lever1);

        Lever lever2 = new Lever("Lever Beta", this, new Position(3, 7), leverPuzzleController, false);  // Initially OFF
        puzzleRoom.getTile(3,7).setEntityOnTile(lever2);
        addEntity(lever2);

        Lever lever3 = new Lever("Lever Gamma", this, new Position(8, 5), leverPuzzleController, true); // Initially ON
        puzzleRoom.getTile(8,5).setEntityOnTile(lever3);
        addEntity(lever3);

        System.out.println("Setup Lever Puzzle in " + puzzleRoom.getName() + " controlling " + puzzleDoor.getName());
    }

    private void createTraps() {
        Room trapRoom = getRoomById(3); // Example: Place traps in Room 3
        if (trapRoom != null) {
            // A row of traps
            for (int x = 3; x <= 7; x++) {
                Trap spikeTrap = new Trap("Spike Trap " + x, this, new Position(x, 5),
                                          3000, 1000, 1500, 10);
                addEntity(spikeTrap);
                // Place the trap on the tile so interaction/drawing is consistent
                trapRoom.getTile(x, 5).setEntityOnTile(spikeTrap);
                // Optional: If you have a TRAP_FLOOR type for special drawing
                // trapRoom.getTile(x, 5).setType(Tile.TileType.TRAP_FLOOR);
                System.out.println("Placed " + spikeTrap.getName() + " in " + trapRoom.getName());
            }

            // A single, faster trap
            Trap fastTrap = new Trap("Quick Spikes", this, new Position(5, 8),
                                     1500, 500, 1000, 15);
            addEntity(fastTrap);
            trapRoom.getTile(5, 8).setEntityOnTile(fastTrap);
            // trapRoom.getTile(5, 8).setType(Tile.TileType.TRAP_FLOOR);
            System.out.println("Placed " + fastTrap.getName() + " in " + trapRoom.getName());
        }
    }

    public Room getRoomById(int id) {
        for (Room room : rooms) {
            if (room.getRoomID() == id) {
                return room;
            }
        }
        System.err.println("Warning: Room with ID " + id + " not found.");
        return null;
    }

    public void addEntity(Entity entity) {
        if (entity == null) {
            System.err.println("Warning: Attempted to add a null entity to the world.");
            return;
        }
        if (!entities.contains(entity)) {
            entities.add(entity);
            if (entity instanceof Executable && !executables.contains(entity)) {
                executables.add((Executable) entity);
            }
            if (entity instanceof Room && !rooms.contains(entity)) {
                rooms.add((Room) entity);
            }
            // If an Item is added directly to the world (not in a room/chest/inventory yet),
            // it might need separate handling or this method assumes it's already placed in a room.
            // My Room.addItem calls world.addEntity implicitly if you structure it that way, or call it explicitly.
            // For items added via room.addItem(), they are in room's list.
            // This addEntity is more for global entities like Player, NPCs, Doors, Rooms themselves.
        }
    }

    public void removeEntity(Entity entity) {
        if (entity == null) return;
        entities.remove(entity);
        if (entity instanceof Executable) {
            executables.remove((Executable) entity);
        }
        if (entity instanceof Room) {
            rooms.remove((Room) entity);
        }
        // If it's the player being removed (e.g. game over sequence elsewhere)
        if (entity == this.player) {
            this.player = null; // Nullify the world's player reference
        }
    }

    public void update() {
        if (gameOver || playerWon) return;

        if (this.player != null) {
            // The player's direct actions (move, interact, use item via F key) are handled by MainFrame's KeyListener.
            // Player.execute() might be for passive effects or queued actions if you develop that.
            // For now, it might be empty or not strictly needed if MainFrame handles all player-initiated state changes.
            // If Player implements Executable and has something to do every "tick" passively:
            // player.execute();
        } else {
            // No player, maybe game should end or be in a different state.
            // For now, if no player, likely means game over was processed.
            return;
        }

        // Check game state after any player passive execution
        if (gameOver || playerWon) return;

        // Process other executables (NPCs)
        // Use an iterator for safe removal if NPCs can die and remove themselves from executables list
        Iterator<Executable> iter = executables.iterator();
        while (iter.hasNext()) {
            Executable ex = iter.next();
            if (ex == player) continue; // Player actions are input-driven

            boolean shouldExecute = true;
            if (ex instanceof LivingBeing) {
                if (((LivingBeing) ex).getHealth() <= 0) {
                    shouldExecute = false;
                }
            }

            if (shouldExecute) {
                // Only update NPCs in the same room as the player for simplicity/performance
                if (ex instanceof NPC) {
                    NPC npc = (NPC) ex;
                    if (player != null && npc.getCurrentRoom() == player.getCurrentRoom()) {
                        npc.execute(); // NPC AI and actions
                    }
                } else {
                    ex.execute(); // For Traps and any other non-NPC executables
                }
            }
            // Check game state changes after each executable's action
            if (gameOver || playerWon) return;
        }

        // Player health already logged by MainFrame after world.update() completes.
    }


    public Player getPlayer() {
        return player;
    }

    // setPlayer is usually only called during initialization
    public void setPlayer(Player player) {
        this.player = player;
        if (player != null && !this.entities.contains(player)) {
            this.addEntity(player);
        }
    }

    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (gameOver) System.out.println("World: Game Over flag set.");
    }

    public boolean didPlayerWin() { return playerWon; }
    public void setPlayerWon(boolean playerWon) {
        this.playerWon = playerWon;
        if (playerWon) System.out.println("World: Player Won flag set.");
    }

    // getStartRoom() might not be needed if you always use rooms.get(0)
    // public Room getStartRoom() {
    //     return rooms.isEmpty() ? null : rooms.get(0);
    // }

    // getEntitiesInRoom - can be useful for AI or area effects
    public List<Entity> getEntitiesInRoom(Room room) {
        if (room == null) return new ArrayList<>();
        List<Entity> roomEntities = new ArrayList<>();
        // Items on the floor
        roomEntities.addAll(room.getItemsInRoom());
        // Living beings
        roomEntities.addAll(room.getLivingBeingsInRoom());
        // Static entities on tiles like Chests (Doors are part of room connections)
        for (int y = 0; y < Room.ROOM_HEIGHT_TILES; y++) {
            for (int x = 0; x < Room.ROOM_WIDTH_TILES; x++) {
                Tile tile = room.getTile(x,y);
                if(tile != null && tile.getEntityOnTile() != null &&
                   tile.getEntityOnTile() instanceof Chest && // Only add chests this way
                   !roomEntities.contains(tile.getEntityOnTile())) {
                     roomEntities.add(tile.getEntityOnTile());
                }
            }
        }
        return roomEntities;
    }

    // Add this method to provide access to the world's entities
    public List<Entity> getEntities() {
        return new ArrayList<>(entities);
    }
}