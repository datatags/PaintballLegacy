package me.blablubbabc.paintball;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;
import me.blablubbabc.BlaDB.BlaSQLite;
import me.blablubbabc.paintball.Metrics.Graph;
import me.blablubbabc.paintball.extras.Turret;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

//This file is part of blablubbabc's paintball-Plugin. Do not redistribute or modify. Use it as it is. Usage on own risk. No warranties. No commercial usage!

public class Paintball extends JavaPlugin{
	public PlayerManager pm;
	public CommandManager cm;
	public MatchManager mm;
	public EventListener listener;
	public Newsfeeder nf;
	public ArenaManager am;
	public Translator t;
	public Stats stats;
	public InSignsFeature isf;
	public boolean active;
	public boolean softreload;
	public boolean nometrics = false;

	//LOBBYSPAWNS
	public int lobbyspawn;
	private LinkedList<Location> lobbyspawns;

	//Public afk detection
	public HashMap <String, Integer> afkMatchCount;

	//ChatColors
	public ChatColor gray = ChatColor.GRAY;
	public ChatColor gold = ChatColor.GOLD;
	public ChatColor green = ChatColor.GREEN;
	public ChatColor aqua = ChatColor.AQUA;
	public ChatColor red = ChatColor.RED;
	public ChatColor blue = ChatColor.BLUE;
	public ChatColor yellow = ChatColor.YELLOW;
	public ChatColor light_purple = ChatColor.LIGHT_PURPLE;
	public ChatColor dark_green = ChatColor.DARK_GREEN;
	public ChatColor dark_red = ChatColor.DARK_RED;
	public ChatColor white = ChatColor.WHITE;

	public ChatColor bold = ChatColor.BOLD;
	public ChatColor italic = ChatColor.ITALIC;
	public ChatColor reset = ChatColor.RESET;
	
	//CONFIG:
	//general:
	public String serverid;
	public boolean serverlist;
	public boolean versioncheck;
	public String local;
	public int countdown;
	public int countdownInit;
	public int countdownStart;
	public int roundTimer;
	public int minPlayers;
	public int maxPlayers;
	public int lives;
	public int respawns;
	public int balls;
	public double speedmulti;
	public boolean listnames;
	public boolean chatnames;
	public boolean shop;
	public ArrayList<String> shopGoods;
	public ArrayList<String> allowedCommands;
	public boolean saveInventory;
	public boolean onlyRandom;
	public boolean autoRandom;
	public boolean noPerms;
	public boolean damage;
	public boolean allowMelee;
	public int meleeDamage;
	public boolean autoLobby;
	public boolean autoTeam;
	public boolean afkDetection;
	public int afkRadius;
	public int afkMatchAmount;
	public boolean autoSpecLobby;
	public boolean effects;
	//unused
	//public boolean autoSpecDeadPlayers;
	//public int repsawns;

	//lobby join checks
	public boolean checkInventory;
	public boolean checkGamemode;
	public boolean checkFlymode;
	public boolean checkBurning;
	public boolean checkHealth;
	public boolean checkFood;
	public boolean checkEffects;

	//points und cash
	public int pointsPerKill;
	public int pointsPerHit;
	public int pointsPerTeamattack;
	public int pointsPerWin;
	public int pointsPerRound;
	public int cashPerKill;
	public int cashPerHit;
	public int cashPerWin;
	public int cashPerRound;

	//Extras:
	public boolean grenades;
	public int grenadeTime;
	public double grenadeSpeed;
	public int grenadeAmount;

	public boolean airstrike;
	public int airstrikeRange;
	public int airstrikeBombs;
	public int airstrikeAmount;
	public int airstrikeHeight;

	public boolean turret;
	public int turretAngleMin;
	public int turretAngleMax;
	public int turretTicks;
	public int turretXSize;
	public int turretYSize;
	public int turretSalve;
	public int turretCooldown;
	public int turretLives;
	
	//TODO
	//shop-items
	//Extras: Luftschlag, verschiedene munition, airdrops, punktesystem abh�ngig von spieler im match, evt. bester spieler der runde, mehr extras :D
	//schneemann-gesch�tz
	//Lobby schilder f�r points und rank und tops
	//map rotation with size-matters
	//map vote
	//map restriction
	//config in-game �ndern
	//grenade
	//luftschlag
	//friendly-fire on/off
	//friendly-fire-detection on/off
	//mySQL support f�r online stats
	//- arena disable/enable
	//- airstrike finalMark bleibt


	//Permissions:
	//paintball.general
	//paintball.arena
	//paintball.admin
	//paintball.shop.not<id> (starting with 1)


	public BlaSQLite sql;
	//public BlaDB data;

	@SuppressWarnings("unchecked")
	public void onEnable(){	
		//CONFIG
		ArrayList<String> goodsDef = new ArrayList<String>();
		//ALT
		/*goodsDef.add("10-Balls-15");
		goodsDef.add("50-Balls-65");
		goodsDef.add("100-Balls-120");
		goodsDef.add("1-Grenade-20");
		goodsDef.add("1-Airstrike-100");*/
		//OLD 2
		/*goodsDef.add("10-Balls-332-15");
		goodsDef.add("50-Balls-332-65");
		goodsDef.add("100-Balls-332-120");
		goodsDef.add("1-Grenade-344-20");
		goodsDef.add("1-Airstrike-280-100");
		goodsDef.add("1-Turret-86-200");*/
		
		goodsDef.add("10-Balls-332-0-15");
		goodsDef.add("50-Balls-332-0-65");
		goodsDef.add("100-Balls-332-0-120");
		goodsDef.add("1-Grenade-344-0-20");
		goodsDef.add("1-Airstrike-280-0-100");
		goodsDef.add("1-Turret-86-0-200");

		allowedCommands = new ArrayList<String>();


		getConfig().options().header("Use a value of -1 to give the players infinite balls or extras. If you insert a not possible value/wrong value in a section the plugin will use the default value or the nearest possible value (Example: your value at section balls: -3 -> plugin will use -1).");
		if(getConfig().get("Server.Id") == null)getConfig().set("Server.Id", UUID.randomUUID().toString());
		if(getConfig().get("Server.Version Check") == null)getConfig().set("Server.Version Check", true);
		if(getConfig().get("Server.List") == null)getConfig().set("Server.List", true);
		if(getConfig().get("Paintball.AFK Detection.enabled") == null)getConfig().set("Paintball.AFK Detection.enabled", true);
		if(getConfig().get("Paintball.AFK Detection.Movement Radius around Spawn (keep in mind: knockbacks, pushing, waterflows, falling, etc)") == null)getConfig().set("Paintball.AFK Detection.Movement Radius around Spawn (keep in mind: knockbacks, pushing, waterflows, falling, etc)", 5);
		if(getConfig().get("Paintball.AFK Detection.Amount of Matches") == null)getConfig().set("Paintball.AFK Detection.Amount of Matches", 3);
		if(getConfig().get("Paintball.Language") == null)getConfig().set("Paintball.Language", "enUS");
		if(getConfig().get("Paintball.No Permissions") == null)getConfig().set("Paintball.No Permissions", false);
		if(getConfig().get("Paintball.Auto Lobby") == null)getConfig().set("Paintball.Auto Lobby", false);
		if(getConfig().get("Paintball.Auto Team") == null)getConfig().set("Paintball.Auto Team", false);
		if(getConfig().get("Paintball.Points per Kill") == null)getConfig().set("Paintball.Points per Kill", 2);
		if(getConfig().get("Paintball.Points per Hit") == null)getConfig().set("Paintball.Points per Hit", 1);
		if(getConfig().get("Paintball.Points per Team-Attack") == null)getConfig().set("Paintball.Points per Team-Attack", -1);
		if(getConfig().get("Paintball.Points per Win") == null)getConfig().set("Paintball.Points per Win", 5);
		if(getConfig().get("Paintball.Points per Round") == null)getConfig().set("Paintball.Points per Round", 1);
		if(getConfig().get("Paintball.Cash per Kill") == null)getConfig().set("Paintball.Cash per Kill", 10);
		if(getConfig().get("Paintball.Cash per Hit") == null)getConfig().set("Paintball.Cash per Hit", 0);
		if(getConfig().get("Paintball.Cash per Win") == null)getConfig().set("Paintball.Cash per Win", 10);
		if(getConfig().get("Paintball.Cash per Round") == null)getConfig().set("Paintball.Cash per Round", 0);
		if(getConfig().get("Paintball.Ball speed multi") == null)getConfig().set("Paintball.Ball speed multi", 1.5);
		if(getConfig().get("Paintball.Colored listnames") == null)getConfig().set("Paintball.Colored listnames", true);
		if(getConfig().get("Paintball.Colored chatnames") == null)getConfig().set("Paintball.Colored chatnames", true);
		if(getConfig().get("Paintball.Only Random") == null)getConfig().set("Paintball.Only Random", false);
		if(getConfig().get("Paintball.Auto Random") == null)getConfig().set("Paintball.Auto Random", true);
		if(getConfig().get("Paintball.Auto Spec Lobby") == null)getConfig().set("Paintball.Auto Spec Lobby", false);
		if(getConfig().get("Paintball.Effects") == null)getConfig().set("Paintball.Effects", true);
		if(getConfig().get("Paintball.Allowed Commands") == null)getConfig().set("Paintball.Allowed Commands", allowedCommands);
		//lobby join checks
		if(getConfig().get("Paintball.Lobby join.Checks.Inventory") == null)getConfig().set("Paintball.Lobby join.Checks.Inventory", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Inventory Save") == null)getConfig().set("Paintball.Lobby join.Checks.Inventory Save", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Gamemode") == null)getConfig().set("Paintball.Lobby join.Checks.Gamemode", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Creative-Fly-Mode") == null)getConfig().set("Paintball.Lobby join.Checks.Creative-Fly-Mode", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Burning, Falling, Immersion") == null)getConfig().set("Paintball.Lobby join.Checks.Burning, Falling, Immersion", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Health") == null)getConfig().set("Paintball.Lobby join.Checks.Health", true);
		if(getConfig().get("Paintball.Lobby join.Checks.FoodLevel") == null)getConfig().set("Paintball.Lobby join.Checks.FoodLevel", true);
		if(getConfig().get("Paintball.Lobby join.Checks.Effects") == null)getConfig().set("Paintball.Lobby join.Checks.Effects", true);

		if(getConfig().get("Paintball.Match.Damage") == null)getConfig().set("Paintball.Match.Damage", false);
		if(getConfig().get("Paintball.Match.Allow Melee") == null)getConfig().set("Paintball.Match.Allow Melee", true);
		if(getConfig().get("Paintball.Match.Melee Damage") == null)getConfig().set("Paintball.Match.Melee Damage", 1);
		if(getConfig().get("Paintball.Match.Lives") == null)getConfig().set("Paintball.Match.Lives", 1);
		if(getConfig().get("Paintball.Match.Respawns") == null)getConfig().set("Paintball.Match.Respawns", 0);
		if(getConfig().get("Paintball.Match.Balls") == null)getConfig().set("Paintball.Match.Balls", 50);
		if(getConfig().get("Paintball.Match.Minimum players") == null)getConfig().set("Paintball.Match.Minimum players", 2);
		if(getConfig().get("Paintball.Match.Maximum players") == null)getConfig().set("Paintball.Match.Maximum players", 1000);
		if(getConfig().get("Paintball.Match.Countdown.Time") == null)getConfig().set("Paintball.Match.Countdown.Time", 20);
		if(getConfig().get("Paintball.Match.Countdown.Delay") == null)getConfig().set("Paintball.Match.Countdown.Delay", 10);
		if(getConfig().get("Paintball.Match.Countdown Round Start.Time") == null)getConfig().set("Paintball.Match.Countdown Round Start.Time", 5);
		if(getConfig().get("Paintball.Match.Round Timer.Time (at least 30)") == null)getConfig().set("Paintball.Match.Round Timer.Time (at least 30)", 120);
		if(getConfig().get("Paintball.Extras.Grenades.enabled") == null)getConfig().set("Paintball.Extras.Grenades.enabled", true);
		if(getConfig().get("Paintball.Extras.Grenades.Time-Radius in Ticks (= 1/20 sec)") == null)getConfig().set("Paintball.Extras.Grenades.Time-Radius in Ticks (= 1/20 sec)", 60);
		if(getConfig().get("Paintball.Extras.Grenades.Speed multi") == null)getConfig().set("Paintball.Extras.Grenades.Speed multi", 1.0);
		if(getConfig().get("Paintball.Extras.Grenades.Amount") == null)getConfig().set("Paintball.Extras.Grenades.Amount", 0);
		if(getConfig().get("Paintball.Extras.Airstrike.enabled") == null)getConfig().set("Paintball.Extras.Airstrike.enabled", true);
		if(getConfig().get("Paintball.Extras.Airstrike.Height") == null)getConfig().set("Paintball.Extras.Airstrike.Height", 15);
		if(getConfig().get("Paintball.Extras.Airstrike.Range (half)") == null)getConfig().set("Paintball.Extras.Airstrike.Range (half)", 30);
		if(getConfig().get("Paintball.Extras.Airstrike.Bombs") == null)getConfig().set("Paintball.Extras.Airstrike.Bombs", 15);
		if(getConfig().get("Paintball.Extras.Airstrike.Amount") == null)getConfig().set("Paintball.Extras.Airstrike.Amount", 0);
		if(getConfig().get("Paintball.Extras.Turret.enabled") == null)getConfig().set("Paintball.Extras.Turret.enabled", true);
		if(getConfig().get("Paintball.Extras.Turret.angleMin (min -90)") == null)getConfig().set("Paintball.Extras.Turret.angleMin (min -90)", -45);
		if(getConfig().get("Paintball.Extras.Turret.angleMax (max 90)") == null)getConfig().set("Paintball.Extras.Turret.angleMax (max 90)", 45);
		if(getConfig().get("Paintball.Extras.Turret.calculated ticks") == null)getConfig().set("Paintball.Extras.Turret.calculated ticks", 100);
		if(getConfig().get("Paintball.Extras.Turret.calculated range x") == null)getConfig().set("Paintball.Extras.Turret.calculated range x", 100);
		if(getConfig().get("Paintball.Extras.Turret.calculated range y (half)") == null)getConfig().set("Paintball.Extras.Turret.calculated range y (half)", 50);
		if(getConfig().get("Paintball.Extras.Turret.shots per salve") == null)getConfig().set("Paintball.Extras.Turret.shots per salve", 15);
		if(getConfig().get("Paintball.Extras.Turret.cooldown in seconds") == null)getConfig().set("Paintball.Extras.Turret.cooldown in seconds", 3);
		if(getConfig().get("Paintball.Extras.Turret.lives") == null)getConfig().set("Paintball.Extras.Turret.lives", 10);
		if(getConfig().get("Paintball.Shop.enabled") == null)getConfig().set("Paintball.Shop.enabled", true);
		if(getConfig().get("Paintball.Shop.Goods (amount-name-id-subid-price)") == null)getConfig().set("Paintball.Shop.Goods (amount-name-id-subid-price)", goodsDef);
		saveConfig();


		//server
		serverid = getConfig().getString("Server.Id", UUID.randomUUID().toString());
		if(!isValid(serverid)) {
			serverid = UUID.randomUUID().toString();
			getConfig().set("Server.Id", serverid);
			saveConfig();
		}
		versioncheck = getConfig().getBoolean("Server.Version Check", true);
		serverlist = getConfig().getBoolean("Server.List", true);
		
		//points+cash:
		pointsPerKill = getConfig().getInt("Paintball.Points per Kill", 2);
		pointsPerHit = getConfig().getInt("Paintball.Points per Hit", 1);
		pointsPerTeamattack = getConfig().getInt("Paintball.Points per Team-Attack", -1);
		pointsPerWin = getConfig().getInt("Paintball.Points per Win", 5);
		pointsPerRound = getConfig().getInt("Paintball.Points per Round", 1);
		cashPerKill = getConfig().getInt("Paintball.Cash per Kill", 10);
		cashPerHit = getConfig().getInt("Paintball.Cash per Hit", 0);
		cashPerWin = getConfig().getInt("Paintball.Cash per Win", 10);
		cashPerRound = getConfig().getInt("Paintball.Cash per Round", 0);

		//gerneral:
		damage = getConfig().getBoolean("Paintball.Match.Damage", false);
		allowMelee = getConfig().getBoolean("Paintball.Match.Allow Melee", true);
		meleeDamage = getConfig().getInt("Paintball.Match.Melee Damage", 1);
		if(meleeDamage < 1) meleeDamage = 1;
		local = getConfig().getString("Paintball.Language", "enUS");
		noPerms = getConfig().getBoolean("Paintball.No Permissions", false);
		autoLobby = getConfig().getBoolean("Paintball.Auto Lobby", false);
		autoTeam = getConfig().getBoolean("Paintball.Auto Team", false);
		allowedCommands = (ArrayList<String>) getConfig().getList("Paintball.Allowed Commands", allowedCommands);
		afkDetection = getConfig().getBoolean("Paintball.AFK Detection.enabled", true);
		afkMatchAmount = getConfig().getInt("Paintball.AFK Detection.Amount of Matches", 3);
		if(afkMatchAmount < 1) afkMatchAmount = 1;
		afkRadius = getConfig().getInt("Paintball.AFK Detection.Movement Radius around Spawn (keep in mind: knockbacks, pushing, waterflows, falling, etc)", 5);
		if(afkRadius < 1) afkRadius = 1;

		lives = getConfig().getInt("Paintball.Match.Lives", 1);
		if(lives < 1) lives = 1;
		respawns = getConfig().getInt("Paintball.Match.Respawns", 0);
		if(respawns < -1) respawns = -1;
		balls = getConfig().getInt("Paintball.Match.Balls", 50);
		if(balls < -1) balls = -1;
		minPlayers = getConfig().getInt("Paintball.Match.Minimum players", 2);
		if(minPlayers < 2) minPlayers = 2;
		maxPlayers = getConfig().getInt("Paintball.Match.Maximum players", 1000);
		if(maxPlayers < 2) maxPlayers = 2;
		//countdown:
		countdown = getConfig().getInt("Paintball.Match.Countdown.Time", 20);
		if(countdown < 0) countdown = 0;
		countdownInit = getConfig().getInt("Paintball.Match.Countdown.Delay", 10);
		if(countdownInit < 0) countdownInit = 0;
		countdownStart = getConfig().getInt("Paintball.Match.Countdown Round Start.Time", 5);
		if(countdownStart < 0) countdownStart = 0;
		roundTimer = getConfig().getInt("Paintball.Match.Round Timer.Time (at least 30)", 120);
		if(roundTimer < 30) roundTimer = 30;

		speedmulti = getConfig().getDouble("Paintball.Ball speed multi", 1.5);
		listnames = getConfig().getBoolean("Paintball.Colored listnames", true);
		chatnames = getConfig().getBoolean("Paintball.Colored chatnames", true);
		onlyRandom = getConfig().getBoolean("Paintball.Only Random", false);
		autoRandom = getConfig().getBoolean("Paintball.Auto Random", true);
		autoSpecLobby = getConfig().getBoolean("Paintball.Auto Spec Lobby", false);
		effects = getConfig().getBoolean("Paintball.Effects", true);

		//shop:
		shop = getConfig().getBoolean("Paintball.Shop.enabled", true);
		shopGoods = (ArrayList<String>) getConfig().getList("Paintball.Shop.Goods (amount-name-id-subid-price)", goodsDef);

		//lobby join checks
		checkInventory = getConfig().getBoolean("Paintball.Lobby join.Checks.Inventory", true);
		saveInventory = getConfig().getBoolean("Paintball.Lobby join.Checks.Inventory Save", true);
		checkGamemode = getConfig().getBoolean("Paintball.Lobby join.Checks.Gamemode", true);
		checkFlymode = getConfig().getBoolean("Paintball.Lobby join.Checks.Creative-Fly-Mode", true);
		checkBurning = getConfig().getBoolean("Paintball.Lobby join.Checks.Burning, Falling, Immersion", true);
		checkHealth = getConfig().getBoolean("Paintball.Lobby join.Checks.Health", true);
		checkFood = getConfig().getBoolean("Paintball.Lobby join.Checks.FoodLevel", true);
		checkEffects = getConfig().getBoolean("Paintball.Lobby join.Checks.Effects", true);

		//Extras
		grenades = getConfig().getBoolean("Paintball.Extras.Grenades.enabled", true);
		grenadeTime = getConfig().getInt("Paintball.Extras.Grenades.Time-Radius in Ticks (= 1/20 sec)", 60);
		if(grenadeTime < 0) grenadeTime = 0;
		grenadeSpeed = getConfig().getDouble("Paintball.Extras.Grenades.Speed multi", 1.0);
		grenadeAmount = getConfig().getInt("Paintball.Extras.Grenades.Amount", 0);
		if(grenadeAmount < -1) grenadeAmount = -1;

		airstrike = getConfig().getBoolean("Paintball.Extras.Airstrike.enabled", true);
		airstrikeHeight = getConfig().getInt("Paintball.Extras.Airstrike.Height", 15);
		if(airstrikeHeight < 2) airstrikeHeight = 2;
		airstrikeRange = getConfig().getInt("Paintball.Extras.Airstrike.Range (half)", 30);
		airstrikeBombs = getConfig().getInt("Paintball.Extras.Airstrike.Bombs", 15);
		if(airstrikeBombs < 0) airstrikeBombs = 0;
		airstrikeAmount = getConfig().getInt("Paintball.Extras.Airstrike.Amount", 0);
		if(airstrikeAmount < -1) airstrikeAmount = -1;
		
		turret = getConfig().getBoolean("Paintball.Extras.Turret.enabled", true);
		turretAngleMin = getConfig().getInt("Paintball.Extras.Turret.angleMin (min -90)", -45);
		if(turretAngleMin < -90) turretAngleMin = -90;
		if(turretAngleMin > 90) turretAngleMin = 90;
		turretAngleMax = getConfig().getInt("Paintball.Extras.Turret.angleMax (max 90)", 45);
		if(turretAngleMax < -90) turretAngleMax = -90;
		if(turretAngleMax > 90) turretAngleMax = 90;
		turretTicks = getConfig().getInt("Paintball.Extras.Turret.calculated ticks", 100);
		if(turretTicks < 0) turretTicks = 0;
		turretXSize = getConfig().getInt("Paintball.Extras.Turret.calculated range x", 100);
		if(turretXSize < 0) turretXSize = 0;
		turretYSize = getConfig().getInt("Paintball.Extras.Turret.calculated range y (half)", 50);
		if(turretYSize < 0) turretYSize = 0;
		turretSalve = getConfig().getInt("Paintball.Extras.Turret.shots per salve", 15);
		if(turretSalve < 0) turretSalve = 0;
		turretCooldown = getConfig().getInt("Paintball.Extras.Turret.cooldown in seconds", 3);
		if(turretCooldown < 0) turretCooldown = 0;
		turretLives = getConfig().getInt("Paintball.Extras.Turret.lives", 10);
		if(turretLives < 0) turretLives = 0;

		//SQLite with version: 110
		sql = new BlaSQLite(new File(this.getDataFolder().toString()+"/"+"pbdata_110"+".db"), this);
		//DB
		loadDB();
		//TRANSLATOR
		t = new Translator(this, local);
		if(!t.success) {
			log("ERROR: Couldn't find/load the default language file. Disables now..");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		//WAKE TEAM-ENUMS
		Lobby.values();
		//PLAYERMANAGER
		pm = new PlayerManager(this);
		//Newsfeeder
		nf = new Newsfeeder(this);
		//MATCHMANAGER|LISTENER
		mm = new MatchManager(this);
		listener = new EventListener(this);
		//ARENAMANAGER
		am = new ArenaManager(this);
		//STATS
		stats = new Stats(this);
		getServer().getPluginManager().registerEvents(listener, this);
		//getServer().getPluginManager().registerEvents(new InvisibleFix(this), this);
		//COMMANDS
		cm = new CommandManager(this);
		CommandExecutor ce = cm;
		getCommand("pb").setExecutor(ce);

		active = true;
		softreload = false;
		lobbyspawn = 0;
		afkMatchCount = new HashMap<String, Integer>();

		//autoLobby
		if(autoLobby) {
			for(Player player : getServer().getOnlinePlayers()) {
				if(autoTeam) cm.joinTeam(player, Lobby.RANDOM);
				else cm.joinLobbyPre(player);
			}
		}

		//METRICS
		try {
			Metrics metrics = new Metrics(this);

			nometrics = metrics.isOptOut();
			//Custom Data:

			//Default graph:
			//Actual playing players (Lobby)
			metrics.addCustomData(new Metrics.Plotter("Actual playing (lobby)") {

				@Override
				public int getValue() {
					try {
						return Lobby.LOBBY.number();
					} catch (Exception e) {
						// Failed to get the value :(
						return 0;
					}
				}
			});

			//Maximum playing (lobby) since last update
			metrics.addCustomData(new Metrics.Plotter("Maximum playing (lobby) since last update") {

				@Override
				public int getValue() {
					try {
						//get max:
						int max = Lobby.LOBBY.maxNumber();
						//reset max:
						Lobby.resetMaxPlayers();
						return max;
					} catch (Exception e) {
						// Failed to get the value :(
						//reset max:
						Lobby.resetMaxPlayers();
						return 0;
					}
				}
			});

			//Graph 2
			Graph graph = metrics.createGraph("Players ever played Paintball");

			//Players ever played Paintball Plotter
			graph.addPlotter(new Metrics.Plotter("Ever played Paintball") {

				@Override
				public int getValue() {
					try {
						int number = 0;
						for(String name : pm.getAllPlayerNames()) {
							if(pm.getStats(name).get("rounds") > 0) number++;
						}
						return number;
					} catch (Exception e) {
						// Failed to get the value :(
						return 0;
					}
				}
			});

			metrics.start();
		} catch (IOException e) {
			Lobby.resetMaxPlayers();
			// Failed to submit the stats :-(
		}

		//InSigns sign changer:
		Plugin insignsPlugin = getServer().getPluginManager().getPlugin("InSigns");
		if((insignsPlugin != null) && insignsPlugin.isEnabled()) {
			isf = new InSignsFeature(insignsPlugin, this);
			log("Plugin 'InSigns' found. Using it now.");
		} else {
			log("Plugin 'InSigns' not found. Additional sign features disabled.");
		}
		
		//calculating turret angles:
		log("Calculating turret angles...");
		Turret.calculateTable(turretAngleMin, turretAngleMax, turretTicks, turretXSize, turretYSize, this);
		log("Calculating done.");

		//Some license stuff: Usage on own risk, no warranties, do not modify the code, do not redistribute, do not copy, and do not use for commercial purposes! Neither direct nor indirect. So this also applies to add-ons made for this plugin! 
		log("By blablubbabc enabled.");
		
		final Paintball plugin = this;
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {
				delayedInfo();
				new Poster(plugin);
			}
		}, 1L);

	}
	
	private static boolean isValid(String uuid){
	    if( uuid == null) return false;
	    try {
	        // we have to convert to object and back to string because the built in fromString does not have 
	        // good validation logic.
	        UUID fromStringUUID = UUID.fromString(uuid);
	        String toStringUUID = fromStringUUID.toString();
	        return toStringUUID.equals(uuid);
	    } catch(IllegalArgumentException e) {
	        return false;
	    }
	}

	public void delayedInfo() {
		getServer().getScheduler().scheduleAsyncDelayedTask(this, new Runnable() {

			@Override
			public void run() {

				logBlank(" ");
				logBlank(ChatColor.YELLOW+" **************************************************");
				logBlank(ChatColor.YELLOW+" ----------------- PAINTBALL INFO -----------------");
				logBlank(" ");
				logBlank(ChatColor.RED+" License stuff:");
				logBlank(ChatColor.GOLD+"   - Usage on own risk.");
				logBlank(ChatColor.GOLD+"   - I give no warranties for anything.");
				logBlank(ChatColor.GOLD+"   - Do not modify. Use it as it is!");
				logBlank(ChatColor.GOLD+"   - Do not redistribute/upload/copy/give away.");
				logBlank(ChatColor.GOLD+"   - Do not copy or use parts of it.");
				logBlank(ChatColor.GOLD+"   - Do not use for commercial purposes!");
				logBlank(ChatColor.GOLD+"     ->This also applies to any kind of add-on you are using");
				logBlank(ChatColor.GOLD+"       related to this plugin!");
				logBlank(" ");
				logBlank(ChatColor.DARK_GREEN+" If you like this plugin: Give feedback and donate at");
				logBlank(ChatColor.DARK_GREEN+" ->http://dev.bukkit.org/server-mods/paintball_pure_war/ ");
				logBlank(" ");
				logBlank(ChatColor.GREEN+" Thank you and good shooting!");
				logBlank(ChatColor.GREEN+"   - blablubbabc");
				logBlank(" ");
				logBlank(ChatColor.YELLOW+" **************************************************");
				logBlank(" ");
			}
		}, 20L);
	}
	
	public void onDisable(){
		if(mm != null) mm.forceReload();
		sql.closeConnection();
		log("Disabled!");
	}

	public void log(String message) {
		System.out.println("["+this.getName()+"] "+message);
	}

	public void logBlank(String message) {
		getServer().getConsoleSender().sendMessage(message);
		//System.out.println(message);
	}

	public void reload() {
		reloadConfig();
		getServer().getPluginManager().disablePlugin(this);
		getServer().getPluginManager().enablePlugin(this);
	}

	
	//METHODS LOBBYSPAWNS
	private synchronized void loadDB() {
		lobbyspawns = new LinkedList<Location>();
		for(Location loc : sql.sqlArenaLobby.getLobbyspawns()) {
			lobbyspawns.add(loc);
		}
	}

	public synchronized void addLobbySpawn(Location loc) {
		lobbyspawns.add(loc);
		sql.sqlArenaLobby.addLobbyspawn(loc);
	}
	public synchronized void deleteLobbySpawns() {
		sql.sqlArenaLobby.removeLobbyspawns();
		lobbyspawns = new LinkedList<Location>();
	}

	public synchronized int getLobbyspawnsCount() {
		return lobbyspawns.size();
	}

	public synchronized Location getNextLobbySpawn() {
		lobbyspawn++;
		if(lobbyspawn > (lobbyspawns.size()-1)) lobbyspawn = 0;
		return (lobbyspawns.size() > 0 ? lobbyspawns.get(lobbyspawn) : null);
	}

	////////////////////////////////////
	//UTILS
	////////////////////////////////////
	
	public synchronized void afkRemove(String player) {
		afkMatchCount.remove(player);
	}
	
	public synchronized int afkGet(String player) {
		int amount = 0;
		if(afkMatchCount.get(player) != null) amount = afkMatchCount.get(player);
		return amount;
	}
	
	public synchronized void afkSet(String player, int amount) {
		afkMatchCount.put(player, amount);
	}
	
	public synchronized ArrayList<String> afkGetEntries() {
		ArrayList<String> entries = new ArrayList<String>();
		
		for(String s : afkMatchCount.keySet()) {
			entries.add(s);
		}
		
		return entries;
	}

	public void checks(Player player, boolean checkListname) {
		if(!isEmpty(player)) clearInv(player);
		//gamemode
		if(!player.getGameMode().equals(GameMode.SURVIVAL)) player.setGameMode(GameMode.SURVIVAL);
		//flymode (built-in)
		if(player.getAllowFlight()) player.setAllowFlight(false);
		if(player.isFlying()) player.setFlying(false);
		//feuer
		if(player.getFireTicks() > 0) player.setFireTicks(0);
		//Health + Food
		if(player.getHealth() < 20) player.setHealth(20);
		if(player.getFoodLevel() < 20) player.setFoodLevel(20);
		//effekte entfernen
		if(player.getActivePotionEffects().size() > 0) {
			ArrayList<PotionEffect> effects = new ArrayList<PotionEffect>();
			for(PotionEffect eff : player.getActivePotionEffects()) {
				effects.add(eff);
			}
			for(PotionEffect eff :effects) {
				player.removePotionEffect(eff.getType());
			}	
		}
		//Vehicle
		if(player.isInsideVehicle()) player.leaveVehicle();
		//listname
		if(checkListname && listnames) player.setPlayerListName(null);
	}

	public synchronized void joinLobby(Player player) {
		checks(player, true);
		//set waiting
		if(Lobby.isPlaying(player) || Lobby.isSpectating(player)) Lobby.getTeam(player).setWaiting(player);
		//Lobbyteleport
		player.teleport(getNextLobbySpawn());
	}

	public synchronized void leaveLobby(Player player, boolean messages, boolean teleport, boolean restoreInventory) {
		//lobby remove:
		Lobby.remove(player);
		checks(player, true);
		//restore saved inventory
		if(restoreInventory && saveInventory) {
			//PlayerInventory
			//null check added:
			ItemStack[] isc = pm.getInvContent(player);
			if(isc != null) {
				player.getInventory().setContents(isc);
			}
			ItemStack[] isa = pm.getInvArmor(player);
			if(isa != null) {
				player.getInventory().setArmorContents(isa);
			}

			player.sendMessage(t.getString("INVENTORY_RESTORED"));
		}
		//teleport:
		if(teleport) player.teleport(pm.getLoc(player));
		if(messages) {
			//messages:
			player.sendMessage(t.getString("YOU_LEFT_LOBBY"));
			nf.leave(player.getName());
		}
	}

	public boolean isEmpty(Player p) {
		for(ItemStack i : p.getInventory()) {
			if(i == null) continue;
			if(i.getTypeId() != 0) return false;
		}
		for(ItemStack i : p.getInventory().getArmorContents()) {
			if(i == null) continue;
			if(i.getTypeId() != 0) return false;
		}
		return true;
	}

	public void clearInv(Player p) {
		p.getInventory().clear();
		p.getInventory().setArmorContents(null);
	}


}
