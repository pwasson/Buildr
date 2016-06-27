/*
 * Copyright 2015 s1mpl3x
 * Copyright 2015-2016 pdwasson
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
package me.simplex.buildr.runnable.builder;

import java.util.HashMap;
import java.util.Map;
import me.simplex.buildr.Buildr;
import me.simplex.buildr.util.BlockLocation;
import me.simplex.buildr.util.Buildr_Container_UndoBlock;
import me.simplex.buildr.util.Cuboid;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Attachable;
import org.bukkit.material.Directional;
import org.bukkit.material.Door;
import org.bukkit.material.Ladder;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;


/**
 *
 * @author pwasson
 */
public class CloneBuilderTask extends AbstractCloningBuilderTask {
    private final Block position3;
    private final int rotation;


    public CloneBuilderTask(Buildr plugin,
            Player player,
            Block position1,
            Block position2,
            Block position3,
            int inRotation) {
        super(plugin.checkPermission(player, "buildr.feature.break_bedrock"));
        this.plugin = plugin;
        this.player = player;
        this.position1 = position1;
        this.position2 = position2;
        this.position3 = position3;
        this.rotation = inRotation;
    }


    @Override
    public void run() {
        int height = Math.abs(position1.getY() - position2.getY()) + 1;
        int width = Math.abs(position1.getX() - position2.getX()) + 1;
        int depth = Math.abs(position1.getZ() - position2.getZ()) + 1;

        Cuboid source = new Cuboid(position1.getLocation(), position2.getLocation());
        /* if we're rotating, we have to adjust the destination coordinates accordingly, i.e. swap
         width and depth if angle is 90 or 270 */
        boolean quarterRot = (rotation % 180 != 0);
        int destWidth = quarterRot ? depth : width;
        int destDepth = quarterRot ? width : depth;
        BlockLocation loc4 = new BlockLocation(position3.getLocation().getBlockX() + destWidth - 1,
                position3.getLocation().getBlockY() + height - 1,
                position3.getLocation().getBlockZ() + destDepth - 1);
        Cuboid dest = new Cuboid(new BlockLocation(position3.getLocation()), loc4);

        Map<Block, Buildr_Container_UndoBlock> undoBlocks = new HashMap<Block, Buildr_Container_UndoBlock>();
        World theWorld = position1.getWorld();
// FIXME doors still drop as items. Don't know why yet.
        /*
        We need to do this in two passes. If we clone an attachable block, such as a torch or ladder,
        before the block it's attached to, it will drop as an item as soon as it's placed. So clone the
        non-attachable blocks in the first pass (to provide surfaces), then only the attachable
        blocks in the second pass.
        */
        cloneLoop(source, dest, rotation, theWorld, undoBlocks, false);
        cloneLoop(source, dest, rotation, theWorld, undoBlocks, true);

        plugin.getUndoList().addToStack(undoBlocks, player);
        player.sendMessage(String.format("Done! Placed %d blocks", undoBlocks.size()));
        plugin.log(String.format("%s cloned a cuboid: %d blocks affected.", player.getName(),
                undoBlocks.size()));
    }
}
