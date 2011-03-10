package com.afforess.minecartmaniasigncommands;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;

import com.afforess.minecartmaniacore.Item;
import com.afforess.minecartmaniacore.MinecartManiaMinecart;
import com.afforess.minecartmaniacore.MinecartManiaPlayer;
import com.afforess.minecartmaniacore.MinecartManiaStorageCart;
import com.afforess.minecartmaniacore.MinecartManiaWorld;
import com.afforess.minecartmaniacore.utils.ChatUtils;
import com.afforess.minecartmaniacore.utils.MathUtils;
import com.afforess.minecartmaniacore.utils.MinecartUtils;
import com.afforess.minecartmaniacore.utils.EntityUtils;
import com.afforess.minecartmaniacore.utils.SignUtils;
import com.afforess.minecartmaniacore.utils.StringUtils;
import com.afforess.minecartmaniasigncommands.sensor.Sensor;
import com.afforess.minecartmaniasigncommands.sensor.SensorManager;

public class SignCommands {
	
	public static boolean doStationSign(MinecartManiaMinecart minecart) {
		if (!minecart.hasPlayerPassenger()) {
			return false;
		}
		ArrayList<Sign> signList = SignUtils.getParallelSignList(minecart);
		signList.addAll(SignUtils.getSignBeneathList(minecart, 2));
		for (Sign sign : signList) {
			for (int i = 0; i < 4; i++) {
				
				if (sign.getLine(i).toLowerCase().contains("[station")) {
					String val[] = sign.getLine(i).toLowerCase().split(":");
					if (val.length != 2) {
						continue;
					}
					val[1] = StringUtils.removeBrackets(val[1].trim());
					MinecartManiaPlayer player = MinecartManiaWorld.getMinecartManiaPlayer(minecart.getPlayerPassenger());
					player.setLastStation(val[1]);
					sign.setLine(i, "[Station:" + val[1]+"]");
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static boolean doStopAtDestination(MinecartManiaMinecart minecart) {
		if (!minecart.hasPlayerPassenger()) {
			return false;
		}
		MinecartManiaPlayer player = MinecartManiaWorld.getMinecartManiaPlayer(minecart.getPlayerPassenger());
		ArrayList<Sign> signList = SignUtils.getParallelSignList(minecart);
		signList.addAll(SignUtils.getSignBeneathList(minecart, 2));
		for (Sign sign : signList) {
			if (sign.getLine(0).toLowerCase().contains("station stop")) {
				sign.setLine(0, "[Station Stop]");
				sign.setLine(1, StringUtils.addBrackets(sign.getLine(1)));
				sign.update();
				if (StringUtils.removeBrackets(sign.getLine(1)).equals(player.getLastStation())) {
					minecart.stopCart();
					ChatUtils.sendMultilineMessage(minecart.getPlayerPassenger(), "You've arrived at your destination", ChatColor.GREEN.toString());
					return true;
				}
			}
		}

		return false;
	}
	
	public static boolean doAutoSetting(MinecartManiaStorageCart minecart, String s) {
		return doAutoSetting(minecart, s, s, Boolean.TRUE);
	}
	
	public static boolean doAutoSetting(MinecartManiaStorageCart minecart, String s, String key, Object value) {
		ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 2);
		for (Sign sign : signList) {
			for (int i = 0; i < 4; i++) {
				if (sign.getLine(i).toLowerCase().contains(s.toLowerCase())) {
					minecart.setDataValue(key, value);
					sign.setLine(i, StringUtils.addBrackets(sign.getLine(i)));
					sign.update();
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean doAlterCollectRange(MinecartManiaStorageCart minecart) {
		ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 2);
		for (Sign sign : signList) {
			for (int i = 0; i < 4; i++) {
				if (sign.getLine(i).toLowerCase().contains("range")) {
					String[] split = sign.getLine(i).split(":");
					if (split.length != 2) continue;
					int range = MathUtils.range(Integer.parseInt(StringUtils.getNumber(split[1])), MinecartManiaWorld.getIntValue(MinecartManiaWorld.getConfigurationValue("Maximum Collection Range")), 0);
					minecart.setEntityDetectionRange(range);
					sign.setLine(i, StringUtils.addBrackets(sign.getLine(i)));
					sign.update();
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean doMaxSpeedSign(MinecartManiaMinecart minecart) {
		ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 2);
		for (Sign sign : signList) {
			for (int i = 0; i < 4; i++) {
				if (sign.getLine(i).toLowerCase().contains("max speed")) {
					String[] split = sign.getLine(i).split(":");
					if (split.length != 2) continue;
					Double percent = Double.parseDouble(StringUtils.getNumber(split[1]));
					percent = Math.min(percent, MinecartManiaWorld.getMaximumMinecartSpeedPercent());
					minecart.minecart.setMaxSpeed(0.4D * percent / 100);
					sign.setLine(i, StringUtils.addBrackets(sign.getLine(i)));
					sign.update();
					return true;
				}
			}
		}
		return false;
	}
	
	
	public static boolean doEjectionSign(MinecartManiaMinecart minecart) {
		if (minecart.minecart.getPassenger() == null) {
			return false;
		}
		if (minecart.getBlockIdBeneath() != MinecartManiaWorld.getEjectorBlockId()) {
			return false;
		}
		if (minecart.isPoweredBeneath()) {
			return false;
		}
		ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 8);
		for (Sign sign : signList) {
			for (int i = 0; i < 4; i++) {
				String line = sign.getLine(i).toLowerCase();
				if (line.contains("eject here")) {
					sign.setLine(i, "[Eject Here]");
					sign.update();
					Location loc = EntityUtils.getValidLocation(sign.getBlock());
					if (loc != null) {
						Entity passenger = minecart.minecart.getPassenger();
						minecart.minecart.eject();
						passenger.teleportTo(loc);
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public static boolean doAnnouncementSign(MinecartManiaMinecart minecart) {
		ArrayList<Sign> signList = SignUtils.getSignBeneathList(minecart, 2);
		//Possibility of overlapping with sloped track, so disregard parallel signs
		if (!MinecartUtils.isSlopedTrack(minecart.minecart.getWorld(), minecart.getX(), minecart.getY(), minecart.getZ())) {
			signList.addAll(SignUtils.getParallelSignList(minecart));
		}
		for (Sign sign : signList) {
			
			//Temporary CraftBook conversion start - will be removed before MMC 1.0
			if (sign.getLine(0).equals("[Print]")) {
				sign.setLine(0, "[Announce]");
				sign.update();
			}
			//Temporary CraftBook conversion end
			
			if (sign.getLine(0).toLowerCase().contains("announce")) {
				sign.setLine(0, "[Announce]");
				if (minecart.hasPlayerPassenger()){
					
					final String title = MinecartManiaWorld.getConfigurationValue("Announcement Sign Prefix").toString() + " " + ChatColor.WHITE;
					String annoucement = title + sign.getLine(1);
					//! signifies a new line, otherwise continue message on same line
					if (sign.getLine(2).startsWith("!")) {
						annoucement += " [NEWLINE] " + title + sign.getLine(2).substring(1);
					}
					else {
						annoucement += sign.getLine(2);
					}
					
					if (sign.getLine(3).startsWith("!")) {
						annoucement += " [NEWLINE] " + title + sign.getLine(3).substring(1);
					}
					else {
						annoucement += sign.getLine(3);
					}
					ChatUtils.sendMultilineMessage(minecart.getPlayerPassenger(), annoucement);
				}
			}
		}
		return false;
	}

	public static boolean doHoldSign(MinecartManiaMinecart minecart) {
		ArrayList<Sign> signList = SignUtils.getAdjacentSignList(minecart, 2);
		for (Sign sign : signList) {
			for (int i = 0; i < 4; i++) {
				String line = sign.getLine(i).toLowerCase();
				if (line.indexOf("hold for") > -1) {
					double d;
					try {
						d = Double.valueOf(StringUtils.getNumber(line));
					}
					catch (NumberFormatException e) {
						d = 0;
					}
					if (d > 0) {
						int time = (int)d;
						ArrayList<Object> holdSignData = new ArrayList<Object>();
						holdSignData.add(new Integer(time));
						holdSignData.add(sign);
						holdSignData.add(new Integer(i));
						holdSignData.add(minecart.minecart.getVelocity().clone());
						minecart.setDataValue("hold sign data", holdSignData);
						minecart.stopCart();
						sign.setLine(i, "[Hold For " + time + "]");
						//Create sign countdown
						if (i != 4) {
							if (sign.getLine(i+1).trim().isEmpty() || sign.getLine(i+1).indexOf("[Holding For") > -1) {
								sign.setLine(i+1, "[Holding For " + time + "]");
							}
						}
						sign.update();
					}
				}
			}
		}
		return false;
	}
	
	public static boolean isElevatorSign(Sign sign) {
		for (int i = 0; i < 4; i++) {
			if (sign.getLine(i).toLowerCase().indexOf("elevator") > -1) {
				sign.setLine(i, "[Elevator]");
				sign.update();
				return true;
			}
			if (sign.getLine(i).toLowerCase().indexOf("lift up") > -1) {
				sign.setLine(i, "[Lift Up]");
				sign.update();
				return true;
			}
			if (sign.getLine(i).toLowerCase().indexOf("lift down") > -1) {
				sign.setLine(i, "[Lift Down]");
				sign.update();
				return true;
			}
		}
		return false;
	}

	public static boolean doElevatorSign(MinecartManiaMinecart minecart, Sign sign) {
		if (isElevatorSign(sign)) {
			for (int i = 0; i < 128; i++) {
				if (i != sign.getY()) {
					ArrayList<Sign> signList = SignUtils.getParallelSignList(sign.getWorld(), sign.getX(), i, sign.getZ());
					for (Sign elevator : signList) {
						if (isElevatorSign(elevator)) {
							
							Location nextFloor = null;
							if (MinecartManiaWorld.getBlockIdAt(elevator.getWorld(), elevator.getX() - 1, i, elevator.getZ()) == Item.RAILS.getId()) {
								nextFloor = new Location(elevator.getWorld(), elevator.getX() - 1, i, elevator.getZ());
							}
							else if (MinecartManiaWorld.getBlockIdAt(elevator.getWorld(), elevator.getX() + 1, i, elevator.getZ()) == Item.RAILS.getId()) {
								nextFloor = new Location(elevator.getWorld(), elevator.getX() + 1, i, elevator.getZ());
							}
							else if (MinecartManiaWorld.getBlockIdAt(elevator.getWorld(), elevator.getX(), i, elevator.getZ() - 1) == Item.RAILS.getId()) {
								nextFloor = new Location(elevator.getWorld(), elevator.getX(), i, elevator.getZ() - 1);
							}
							else if (MinecartManiaWorld.getBlockIdAt(elevator.getWorld(), elevator.getX(), i, elevator.getZ() + 1) == Item.RAILS.getId()) {
								nextFloor = new Location(elevator.getWorld(), elevator.getX(), i, elevator.getZ() + 1);
							}
							if (nextFloor != null) {
								
								minecart.minecart.teleportTo(nextFloor);
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}
	
	public static void updateSensors(MinecartManiaMinecart minecart, MinecartManiaMinecart input) {
		//Activate new sensors
		for (Block block : minecart.getParallelBlocks()) {
			Sensor s = SensorManager.getSensor(block.getLocation());
			if (s != null) {
				s.input(input);
			}
			
		}
		
		//deactivate old sensors
		if (!minecart.getPreviousLocation().equals(minecart.minecart.getLocation().toVector())) {
			for (Block block : minecart.getPreviousLocationParallelBlocks()) {
				Sensor s = SensorManager.getSensor(block.getLocation());
				if (s != null){
					s.input(null);
				}
			}
		}
	}
	
	public static void updateSensors(MinecartManiaMinecart minecart) {
		updateSensors(minecart, minecart);
	}
		
}
