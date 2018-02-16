// Josh King - 16200099
// George Ridgway - 16200132
// Kelsey Osos - 16201972

package cluedo_game;

import java.util.ArrayList;

public class Room {
	private String name;
	private Weapon weaponInRoom = null;
	private Room secretPassage;
	private final ArrayList<FloorSquare> exits;
	private final ArrayList<EntrySquare> entrances;

	ArrayList<Token> playersInRoom = new ArrayList<>();

	/**
	 * Other than the cellar, rooms with no secretPassage all have multiple entries.
	 * As a result, this constructor handles those rooms by merely taking an entire ArrayList
	 * as an argument for its doorway variable. The exception is cellar, and we will just send
	 * 'null' for the doorway argument in that case.
	 * @param name String representation of the name of the room
	 * @param entrances The EntrySquares that lead to this room
	 * @param exits The FloorSquares to which this room exits
	 */
	public Room(String name, ArrayList<EntrySquare> entrances, ArrayList<FloorSquare> exits) {
		this.name = name;
		this.entrances = entrances;
		this.exits = exits;
		this.secretPassage = null;
	}

	/**
	 * Since this method handles rooms with only one entry (besides the secret passage), we
	 * just construct an ArrayList for doorway within the constructor then add the single
	 * EntrySquare argument for the doorway.
	 * @param name String representation of the name of the room
	 * @param secretPassage A pointer to the room to which this room's passage leads
	 * @param entrance The EntrySquare that leads to this room
	 * @param exit The FloorSquare to which this room exits
	 */
	public Room(String name, Room secretPassage, EntrySquare entrance, FloorSquare exit) {
		this.name = name;
		this.entrances = new ArrayList<>();
		this.entrances.add(entrance);
		this.exits = new ArrayList<>();
		this.exits.add(exit);
		this.secretPassage = secretPassage;
	}

	//
	//Accessors
	//
	public String getName() {return name;}
	public Weapon getWeaponInRoom() {return weaponInRoom;}
	public Room getSecretPassage() {return secretPassage;}
	public ArrayList<Token> getPlayersInRoom() {return playersInRoom;}
	public ArrayList<EntrySquare> getEntrances() {return entrances;}
	public ArrayList<FloorSquare> getExits() {
		return exits;
	}

	//
	//Mutators
	//
	public void setName(String name) {this.name = name;}
	public void setWeaponInRoom(Weapon weaponInRoom) {this.weaponInRoom = weaponInRoom;}
	public void setsecretPassage(Room secretPassage) {this.secretPassage = secretPassage;}
	public void setPlayersInRoom(ArrayList<Token> playersInRoom) {this.playersInRoom = playersInRoom;}
}
