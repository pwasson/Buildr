/*
 * Copyright 2016-2017 pdwasson
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
package me.simplex.buildr.manager.builder;

import me.simplex.buildr.Buildr;
import me.simplex.buildr.runnable.builder.TileBuilderTask;
import me.simplex.buildr.util.CloneOptions;
import me.simplex.buildr.util.Cuboid;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;


/**
 *
 * @author pwasson
 */
public class TileBuilderManager extends AbstractBuilderManager {
    public enum TileMode {
        UP,
        DOWN,
        TRIM
    }
    private static final String COORD_FAIL_MESSAGE_TOO_BIG = "The volume of the area to be cloned must be no more than 32768 blocks. Tiling stopped.";
    private static final String COORD_FAIL_MESSAGE_TOO_SMALL = "The destination is too near the source for any tiles to be cloned.";
    private static final String COORD_FAIL_MESSAGE_OVERLAP = "The destination is within the source. Tiling stopped.";

    private final CloneOptions cloneOpts;
    private final TileMode tileMode;
    private final boolean lockX;
    private final boolean lockY;
    private final boolean lockZ;
    private final boolean mirrorX;
    private final boolean mirrorZ;

    private CoordFail coordReason = null;
    private int nx = 0, ny = 0, nz = 0;

    public TileBuilderManager(
            Player inPlayer,
            Buildr inPlugin,
            CloneOptions cloneOpts,
            TileMode inTileMode,
            boolean inLockX,
            boolean inLockY,
            boolean inLockZ,
            boolean inMirrorX,
            boolean inMirrorZ) {
        super("Tile", inPlugin, inPlayer, null, (byte)0, null);
        this.cloneOpts = cloneOpts;
        this.tileMode = inTileMode;
        this.lockX = inLockX;
        this.lockY = inLockY;
        this.lockZ = inLockZ;
        this.mirrorX = inMirrorX;
        this.mirrorZ = inMirrorZ;
    }


    @Override
    public String getNextPositionMessage() {
        String s;
        switch (positions.size()) {
            case 0:
                s = "Right-click with a stick on the first corner of the cuboid to clone.";
                break;
            case 1:
                s = "Now right-click (again with a stick) on the second corner of the cuboid to clone.";
                break;
            default:
                s = "Now right-click (again with a stick) on the block that tiles should extend to.";
        }
        return s;
    }


    @Override
    public String getLastPositionMessage() {
        String s;
        switch (positions.size()) {
            case 0:
                s = "";
                break;
            case 1:
                s = String.format("Got the first corner of your clone source at %s.", 
                        getCoordForChat(positions.get(0)));
                break;
            case 2:
                s = String.format("Got the second corner of your clone source at %s.", 
                        getCoordForChat(positions.get(1)));
                break;
            default:
                s = String.format("Got the tiling extent at %s.",
                        getCoordForChat(positions.get(2)));
        }
        return s;
    }


    @Override
    public boolean gotAllCoordinates() {
		return (positions.size() >= 3);
    }


    private enum CoordFail {
        TOO_BIG,
        OVERLAP,
        TOO_SMALL
    }

    @Override
    public boolean checkCoordinates() {
        Block position1;
        Block position2;
        Block position3;

        // if we have the source coordinates but the cuboid volume is bigger than 32K blocks, that's a fail.
        if (positions.size() >= 2) {
            position1 = getPosition(1);
            position2 = getPosition(2);
            int sourceHeight = Math.abs(position1.getY() - position2.getY()) + 1;
            int sourceWidth = Math.abs(position1.getX() - position2.getX()) + 1;
            int sourceDepth = Math.abs(position1.getZ() - position2.getZ()) + 1;
            long tileVolume = (long)sourceHeight * (long)sourceWidth * (long)sourceDepth;
            if (tileVolume > 32768) {
                coordReason = CoordFail.TOO_BIG;
                return false;
            }

            /* if we have the destination and it overlaps with the source, that's a fail. (We don't support
                    the built-in command's "force" option. */
            if (positions.size() >= 3) {
                Cuboid source = new Cuboid(position1.getLocation(), position2.getLocation());
                position3 = getPosition(3);

                if (source.contains(position3.getLocation())) {
                    coordReason = CoordFail.OVERLAP;
                    return false;
                }

                // determine tile extents for both following checks.
                // d -> distance, r -> remainder, n -> num tiles (including source).
                int dx = (position3.getX() > source.getHighCorner().getX()) ?
                        position3.getX() - source.getHighCorner().getX()
                        : source.getLowCorner().getX() - position3.getX();
                nx = 1;
                int rx = 0;
                if (dx > 0 && !lockX) {
                    rx = dx % sourceWidth;
                    nx = (dx / sourceWidth) + 1;
                }

                int dy = (position3.getY() > source.getHighCorner().getY()) ?
                        position3.getY() - source.getHighCorner().getY()
                        : source.getLowCorner().getY() - position3.getY();
                ny = 1;
                int ry = 0;
                if (dy > 0 && !lockY) {
                    ry = dy % sourceHeight;
                    ny = (dy / sourceHeight) + 1;
                }

                int dz = (position3.getZ() > source.getHighCorner().getZ()) ?
                        position3.getZ() - source.getHighCorner().getZ()
                        : source.getLowCorner().getZ() - position3.getZ();
                nz = 1;
                int rz = 0;
                if (dz > 0 && !lockZ) {
                    rz = dz % sourceDepth;
                    nz = (dz / sourceDepth) + 1;
                }
//plugin.getLogger().info(String.format("[dx: %d, nx: %d, rx: %d]; [dy: %d, ny: %d, ry: %d]; [dz: %d, nz: %d, rz: %d]",
//        dx, nx, rx, dy, ny, ry, dz, nz, rz));

                if (tileMode == TileMode.UP) {
                    if (rx > 0) {
                        nx += 1;
//plugin.getLogger().info(String.format("Rounding nx up to %d", nx));
                    }
                    if (ry > 0) {
                        ny += 1;
//plugin.getLogger().info(String.format("Rounding ny up to %d", ny));
                    }
                    if (rz > 0) {
                        nz += 1;
//plugin.getLogger().info(String.format("Rounding nz up to %d", nz));
                    }
                }

                // if the tiled volume is > 32K blocks, that's a fail.
                long totalTiles = ((long)nx * ny * nz) - 1;// remove 1 for the source
                long totalVolume = totalTiles * tileVolume;
                if (totalVolume > 32768) {
                    coordReason = CoordFail.TOO_BIG;
                    return false;
                }

                /* if tile mode is "round down" and position 3 is less than a tile width/height away from
                the source cuboid in all dimensions, then no tiling would occur, so detect and report that.
                */
                if (totalTiles < 1) {
                    coordReason = CoordFail.TOO_SMALL;
                    return false;
                }

//plugin.getLogger().info(String.format("Source: [%d, %d, %d] - [%d, %d, %d]",
//        source.getLowCorner().getX(), source.getLowCorner().getY(), source.getLowCorner().getZ(),
//        source.getHighCorner().getX(), source.getHighCorner().getY(), source.getHighCorner().getZ()));

//plugin.getLogger().info(String.format("nTiles: %d, %d, %d", nx, ny, nz));
            }
        }

        return true;
    }


    @Override
    public String getCoordinateCheckFailed() {
        if (coordReason != null)
            switch (coordReason) {
                case TOO_BIG:
                    return COORD_FAIL_MESSAGE_TOO_BIG;
                case TOO_SMALL:
                    return COORD_FAIL_MESSAGE_TOO_SMALL;
                case OVERLAP:
                    return COORD_FAIL_MESSAGE_OVERLAP;
            }
        return "<internal error>";
    }


    @Override
    public void startBuild() {
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin,
                new TileBuilderTask(plugin, creator, getPosition(1), getPosition(2),
                        getPosition(3), nx, ny, nz, cloneOpts, mirrorX, mirrorZ));
    }
}
