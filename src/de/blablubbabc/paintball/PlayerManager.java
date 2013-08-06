package de.blablubbabc.paintball;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import de.blablubbabc.paintball.statistics.player.PlayerStats;


public class PlayerManager {
	private Map<String, PlayerDataStore> playerStore;
	private Map<String, PlayerStats> playerStats;
	private Set<String> playersToAdd;

	public PlayerManager() {
		playerStore = new HashMap<String, PlayerDataStore>();
		playerStats = new HashMap<String, PlayerStats>();
		playersToAdd = new HashSet<String>();
		
		addAllOnlinePlayers();
	}

	public boolean isPlayerStillLocked(String playerName) {
		return playersToAdd.contains(playerName);
	}
	
	// PLAYERSTATS
	
	public void loadPlayerStatsAsync(final String playerName, final Runnable runAfterwards) {
		if (playerStats.get(playerName) == null) {
			Paintball.instance.getServer().getScheduler().runTaskAsynchronously(Paintball.instance, new Runnable() {

				@Override
				public void run() {
					playerStats.put(playerName, new PlayerStats(playerName));
					if (runAfterwards != null) {
						// run afterwards-task sync:
						Paintball.instance.getServer().getScheduler().runTask(Paintball.instance, runAfterwards);
					}
				}
			});
		} else {
			// run afterwards-task sync:
			runAfterwards.run();
		}
	}
	
	// is done sync if Paintball is currently disableing, else async
	public void unloadPlayerStats(String playerName) {
		PlayerStats stats = playerStats.remove(playerName);
		if (stats != null) {
			if (!Paintball.instance.currentlyDisableing) stats.saveAsync();
			else stats.save();
		}
	}
	
	public PlayerStats getPlayerStats(String playerName) {
		// if currently loading..
		if (isPlayerStillLocked(playerName)) {
			return null;
		}
		// check if in cache, if not return temporary retrieved PlayerStats or null, 
		// if no stats exist for this player:
		PlayerStats stats = playerStats.get(playerName);
		if (stats == null) {
			if (exists(playerName)) stats = new PlayerStats(playerName);
		}
		return stats;
	}
	
	
	
	// METHODS
	// SETTER
	// adding all players on plugin start is done sync, because it reduces possible problems and it's a one-time-thing on plugin start only:
	private void addAllOnlinePlayers() {
		// locking is not needed, because it's done sync:
		for (Player player : Paintball.instance.getServer().getOnlinePlayers()) {
			String playerName = player.getName();
			// add player to database
			addPlayer(playerName);
			
			// autoLobby and worldMode
			if (Paintball.instance.autoLobby || (Paintball.instance.worldMode && Paintball.instance.worldModeWorlds.contains(player.getWorld().getName()))) {
				if(Paintball.instance.autoTeam) {
					Paintball.instance.commandManager.joinTeam(player, false, Lobby.RANDOM);
				} else {
					Paintball.instance.commandManager.joinLobbyPre(player, false, null);
				}
			}
		}
	}
	
	public void addPlayerAsync(final String name) {
		// lock player
		playersToAdd.add(name);
		
		Paintball.instance.getServer().getScheduler().runTaskAsynchronously(Paintball.instance, new Runnable() {

			@Override
			public void run() {
				addPlayer(name);
				
				Paintball.instance.getServer().getScheduler().runTask(Paintball.instance, new Runnable() {
					
					@Override
					public void run() {
						// Remove player lock in sync environment:
						playersToAdd.remove(name);
						
						//autoLobby
						Paintball.instance.getServer().getScheduler().runTaskLater(Paintball.instance, new Runnable() {

							@Override
							public void run() {
								Player player = Paintball.instance.getServer().getPlayerExact(name);
								if (player != null) {
									if (Paintball.instance.autoLobby || (Paintball.instance.worldMode && Paintball.instance.worldModeWorlds.contains(player.getWorld().getName()))) {
										if (Paintball.instance.autoTeam) {
											Paintball.instance.commandManager.joinTeam(player, false, Lobby.RANDOM);
										} else {
											Paintball.instance.commandManager.joinLobbyPre(player, false, null);
										}
									}
								}
							}
						}, 1L);
					}
				});
			}
		});
	}
	
	/**
	 * This method should only be used inside this class and surrounded by locking the player by adding and removing it from the playersToAdd list.
	 * 
	 * @param name
	 */
	private void addPlayer(final String name) {
		// TODO is exits check really necessary here ?
		if (!Paintball.instance.sql.sqlPlayers.isPlayerExisting(name)) {
			Paintball.instance.sql.sqlPlayers.addNewPlayer(name);
		}
	}

	public void resetAllDataAsync() {
		Paintball.instance.getServer().getScheduler().runTaskAsynchronously(Paintball.instance, new Runnable() {

			@Override
			public void run() {
				resetAllData();
			}
		});
	}

	public void resetAllData() {
		// reset stats in cache:
		for (PlayerStats stats : playerStats.values()) {
			stats.resetStats();
		}
		// reset in databse:
		Paintball.instance.sql.sqlPlayers.resetAllPlayerStats();
	}

	/*public void resetDataOfPlayerAsync(final String player) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						Paintball.instance.sql.sqlPlayers.resetPlayerStats(player);
					}
				});
	}*/

	public boolean exists(String playerName) {
		if (isPlayerStillLocked(playerName)) return true;
		return Paintball.instance.sql.sqlPlayers.isPlayerExisting(playerName);
	}

	// STATS
	/*public void addStatsAsync(final String player, final Map<PlayerStat, Integer> stats) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						addStats(player, stats);
					}
				});
	}

	public void addStats(final String player, final Map<PlayerStat, Integer> stats) {
		Paintball.instance.sql.sqlPlayers.addPlayerStats(player, stats);
		Paintball.instance.sql.sqlPlayers.calculateStats(player);
	}

	public void setStatsAsync(final String player, final Map<PlayerStat, Integer> stats) {
		Paintball.instance.getServer().getScheduler()
				.runTaskAsynchronously(Paintball.instance, new Runnable() {

					@Override
					public void run() {
						setStats(player, stats);
					}
				});
	}
	
	public void setStats(final String player, final Map<PlayerStat, Integer> stats) {
		Paintball.instance.sql.sqlPlayers.setPlayerStats(player, stats);
		Paintball.instance.sql.sqlPlayers.calculateStats(player);
	}*/

	// GETTER
	
	public int getPlayersEverPlayedCount() {
		return Paintball.instance.sql.sqlPlayers.getPlayersEverPlayedCount();
	}

	public int getPlayerCount() {
		return Paintball.instance.sql.sqlPlayers.getPlayerCount();
	}

	/*public Map<PlayerStat, Integer> getStats(String player) {
		return Paintball.instance.sql.sqlPlayers.getPlayerStats(player);
	}*/

	public void teleportStoreClearPlayer(Player player, Location to) {
		playerStore.put(player.getName(), new PlayerDataStore(player, to));
	}
	
	public void clearRestoreTeleportPlayer(Player player) {
		PlayerDataStore playerData = playerStore.remove(player.getName());
		if (playerData != null) {
			playerData.restoreTeleportPlayer(player);
		}
	}

}
