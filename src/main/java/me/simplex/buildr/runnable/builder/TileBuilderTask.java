/*
 * Copyright 2016 pdwasson
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
import me.simplex.buildr.manager.builder.TileBuilderManager.TileMode;
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
public class TileBuilderTask extends AbstractCloningBuilderTask {
    private final int nx, ny, nz;
    private final Block position3;


    public TileBuilderTask(Buildr plugin,
            Player player,
            Block position1,
            Block position2,
            Block inPosition3,
            int inNumXTiles,
            int inNumYTiles,
            int inNumZTiles) {
        super(plugin.checkPermission(player, "buildr.feature.break_bedrock"));
        this.plugin = plugin;
        this.player = player;
        this.position1 = position1;
        this.position2 = position2;
        this.position3 = inPosition3;
        this.nx = inNumXTiles;
        this.ny = inNumYTiles;
        this.nz = inNumZTiles;
    }


    @Override
    public void run() {
        Cuboid source = new Cuboid(position1.getLocation(), position2.getLocation());
        int tileHeight = Math.abs(position1.getY() - position2.getY()) + 1;
        int tileWidth = Math.abs(position1.getX() - position2.getX()) + 1;
        int tileDepth = Math.abs(position1.getZ() - position2.getZ()) + 1;

        int dx = (position3.getX() > source.getHighCorner().getX()) ? +1 : -1;
        int dy = (position3.getY() > source.getHighCorner().getY()) ? +1 : -1;
        int dz = (position3.getZ() > source.getHighCorner().getZ()) ? +1 : -1;
plugin.getLogger().info(String.format("tileWidth: %d, tileHeight: %d, tileDepth: %d, dx: %d, dy: %d, dz: %d",
        tileWidth, tileHeight, tileDepth, dx, dy, dz));

        Map<Block, Buildr_Container_UndoBlock> undoBlocks = new HashMap<Block, Buildr_Container_UndoBlock>();
        World theWorld = position1.getWorld();

        for (int ix = 0; ix < nx; ++ix) {
            for (int iy = 0; iy < ny; ++iy) {
                for (int iz = 0; iz < nz; ++iz) {
                    if (ix == 0 && iy == 0 && iz == 0) continue;

                    int lowX = source.getLowCorner().getX() + (ix * tileWidth * dx);
                    int lowY = source.getLowCorner().getY() + (iy * tileHeight * dy);
                    int lowZ = source.getLowCorner().getZ() + (iz * tileDepth * dz);
plugin.getLogger().info(String.format("ix: %d, iy: %d, iz: %d, lowX: %d, lowY: %d, lowZ: %d",
        ix, iy, iz, lowX, lowY, lowZ));

                    BlockLocation loc3 = new BlockLocation(lowX, lowY, lowZ);
                    BlockLocation loc4 = new BlockLocation(lowX + tileWidth - 1, lowY + tileHeight - 1,
                            lowZ + tileDepth - 1);

                    Cuboid dest = new Cuboid(loc3, loc4);

                    cloneLoop(source, dest, 0, theWorld, undoBlocks, false);
                    cloneLoop(source, dest, 0, theWorld, undoBlocks, true);
                }
            }
        }

        plugin.getUndoList().addToStack(undoBlocks, player);
        player.sendMessage(String.format("Done! Placed %d blocks", undoBlocks.size()));
        plugin.log(String.format("%s tiled a cuboid: %d blocks affected.", player.getName(),
                undoBlocks.size()));
    }
}
