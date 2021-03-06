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
package me.simplex.buildr.util;

import org.bukkit.Material;

public class Buildr_Container_UndoBlock {
	private Material material;
	private byte materialData;
	
	public Buildr_Container_UndoBlock(Material material, byte materialData) {
		this.material = material;
		this.materialData = materialData;
	}

	public Material getMaterial() {
		return material;
	}

	public byte getMaterialData() {
		return materialData;
	}
	
	
}
