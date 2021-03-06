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
package me.simplex.buildr.runnable;

import java.util.Map;

import me.simplex.buildr.Buildr;
import me.simplex.buildr.util.Buildr_Container_UndoBlock;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.Attachable;

public class Buildr_Runnable_Undo implements Runnable {
	private final Player player;
	private final Map<Block, Buildr_Container_UndoBlock> undos;
	private final Buildr plugin;
	
public Buildr_Runnable_Undo(Player player, Buildr plugin) {
	this.player = player;
	this.plugin = plugin;
	this.undos = plugin.getUndoList().getAndDeleteFromStack(player);
}
	@Override
	public void run() {
		if (undos != null) {
            // first undo attachables, then non-attachables.
            // FIXME this doesn't work to prevent drops from items being removed.
			for (Block block : undos.keySet()) {
                boolean isAttachable = Attachable.class.isAssignableFrom(block.getType().getData());
                if (isAttachable) {
                    block.setType(undos.get(block).getMaterial());
                    block.setData(undos.get(block).getMaterialData());
                }
			}
			for (Block block : undos.keySet()) {
                boolean isAttachable = Attachable.class.isAssignableFrom(block.getType().getData());
                if (!isAttachable) {
                    block.setType(undos.get(block).getMaterial());
                    block.setData(undos.get(block).getMaterialData());
                }
			}
			plugin.log(player.getName()+" used /undo: "+undos.size()+" blocks affected");
			player.sendMessage(undos.size()+" blocks restored");
		}
		else {
			player.sendMessage("Nothing to undo");
		}
	}
}
