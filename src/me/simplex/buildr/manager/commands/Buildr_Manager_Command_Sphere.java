/*
 * Copyright 2012 s1mpl3x
 * 
 * This file is part of Buildr.
 * 
 * Buildr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Buildr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Buildr  If not, see <http://www.gnu.org/licenses/>.
 */
package me.simplex.buildr.manager.commands;

import me.simplex.buildr.Buildr;
import me.simplex.buildr.manager.builder.Buildr_Manager_Builder_Sphere;
import me.simplex.buildr.util.Buildr_Manager_Command_Super;
import me.simplex.buildr.util.Buildr_Type_Wool;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Buildr_Manager_Command_Sphere extends Buildr_Manager_Command_Super {

	public Buildr_Manager_Command_Sphere(Buildr plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("sphere")) {
			if (args.length < 1 || args.length > 3) {
				return false;
			}
			if (plugin.checkPermission((Player)sender, "buildr.cmd.sphere")) {
				if (args.length >=1 && args.length <=3) {
					Material material;
					int id;
					byte mat_data = (byte)0;
					if (args[0].toUpperCase().startsWith("WOOL:")) {
						id = 35;
						Buildr_Type_Wool woolcolor;
						try {
							woolcolor = Enum.valueOf(Buildr_Type_Wool.class, args[0].toUpperCase().substring(5));
							mat_data = woolcolor.getBlockDataValue();
						} catch (IllegalArgumentException e) {
							sendTo(sender, MsgType.ERROR, "No such wool");
							return true;
						}
					}
					else {
						try {
							id = Integer.parseInt(args[0]);
						} catch (NumberFormatException e) {
							try {
								id = Material.matchMaterial(args[0]).getId();
							} catch (NullPointerException e2) {
								sendTo(sender, MsgType.ERROR, "Wrong format");
								return true;
							}

						}
					}
					if (Material.getMaterial(id).isBlock()) {
						material = Material.getMaterial(id);
					}
					else {
						sendTo(sender, MsgType.ERROR, "invalid blocktype");
						return true;
					}
					if (args.length==1) {
							this.cmd_sphere(sender, material, mat_data, false, false, null);
							return true;
					}
					else if (args.length == 2) {
						if (args[1].startsWith("r")) {
							Material rep = parseMaterial(args[1].substring(1));
							this.cmd_sphere(sender, material, mat_data, true,false, rep);
							return true;
						}
						else if(args[1].equalsIgnoreCase("h") || args[1].equalsIgnoreCase("hollow")) {
							this.cmd_sphere(sender, material, mat_data, false, true, null);
							return true;
						}
						else {
							return false;
						}
					}
					else if (args.length == 3) {
						Material rep = parseMaterial(args[1].substring(1));
						if (!args[1].startsWith("r")) {
							return false;
						}
						if (!args[2].equalsIgnoreCase("h") || !args[2].equalsIgnoreCase("hollow")) {
							return false;
						}
						this.cmd_sphere(sender, material, mat_data, true, true, rep);
						return true;
					}
					else {
						return false;
					}
				}
				else {
					return false;
				}

			}
			else {
				sendTo(sender, MsgType.ERROR, "You dont have the permission to perform this action");
			}
			return true;
		}
		return false;
	}
	
	public void cmd_sphere(CommandSender sender, Material material, byte material_data, boolean replace,boolean hollow, Material replace_mat) {
		if (!plugin.getConfigValue("FEATURE_BUILDER_SPHERE")) {
			return;
		}
		if (plugin.checkPlayerHasStartedBuilding((Player)sender)) {
			plugin.removeStartedBuilding((Player)sender);
			sendTo(sender, MsgType.WARNING, "previous started building aborted");
		}
		String replace_info ="";
		if (replace) {
			replace_info = "Replace: "+ChatColor.BLUE+replace_mat;
		}
		plugin.getStartedBuildings().add(new Buildr_Manager_Builder_Sphere((Player)sender, material, replace,replace_mat, hollow, false, plugin,material_data));
		String buildinfo ="Started new Sphere. Info: Blocktype: "+ChatColor.BLUE+material.toString()+ChatColor.WHITE+" (ID:"+ChatColor.BLUE+material.getId()+ChatColor.WHITE+") "+replace_info+ChatColor.WHITE+" Hollow: "+ChatColor.BLUE+hollow;

		sendTo(sender, MsgType.INFO, buildinfo);
		sendTo(sender, MsgType.INFO, "Rightclick on the center of your sphere while holding a stick to continue");
	}
}
