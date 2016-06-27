/*
 * Copyright 2016 s1mpl3x
 * Copyright 2016 pwasson
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
import me.simplex.buildr.manager.builder.BuilderManager;
import me.simplex.buildr.runnable.Buildr_Runnable_Undo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author pwasson
 */
public class RetryCommand extends Buildr_Manager_Command_Super {

	public RetryCommand(Buildr plugin) {
		super(plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("bretry")) {
			if (args.length != 0) {
				return false;
			}

            Player player = (Player)sender;
            if (plugin.checkPermission(player, "buildr.cmd.retry")) {
				if (plugin.checkPlayerHasStartedBuilding(player)) {
                    retryLastTap(player);
                } else if (plugin.checkPlayerHasLastBuilding(player)) {
                    retryLastCommand(player);
                } else {// nothing at all to retry
                    sendTo(sender, Buildr_Manager_Command_Super.MsgType.ERROR, "You dont have anything to retry.");
                }
			}
			else {
				sendTo(sender, Buildr_Manager_Command_Super.MsgType.ERROR, "You dont have the permission to perform this action.");
			}
			return true;
		}
		return false;
	}


	public void retryLastTap(Player player) {
        BuilderManager mgr = plugin.giveBuilderManager(player);
        mgr.retry();
        player.sendMessage(mgr.getLastPositionMessage());
	}


    public void retryLastCommand(Player player) {
        BuilderManager mgr = plugin.giveLastManager(player).retry();
        // undo the last command if the user hasn't already.
        if (!mgr.wasUndone()) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Buildr_Runnable_Undo(player, plugin));
        }
        plugin.addStartedBuilding(mgr);
        player.sendMessage(mgr.getLastPositionMessage());
    }
}
