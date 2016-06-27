/*
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
import me.simplex.buildr.manager.builder.TileBuilderManager;
import me.simplex.buildr.manager.builder.TileBuilderManager.TileMode;
import static me.simplex.buildr.manager.commands.Buildr_Manager_Command_Super.sendTo;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TileCommand extends AbstractBuilderCommand {
    public static final String ROUND_UP_OPTION = "roundup";
    public static final String ROUND_DOWN_OPTION = "rounddown";
    public static final String LOCK_X_OPTION = "lockX";
    public static final String LOCK_Y_OPTION = "lockY";
    public static final String LOCK_Z_OPTION = "lockZ";

    public TileCommand(Buildr plugin) {
        super(plugin, "btile", "buildr.cmd.tile", 1, 0);
    }
/* TODO maybe support options from built-in clone command:
    "masked" (do not copy air blocks)
    ? "filtered" (with material, only copy blocks of specified material)

    TODO add option(s) to restrict axes, regardless of where you tap. For example, "noy" (no y) would lock
        the tiling onto the same y position as the original, even if the third tap was a few blocks lower
        then the source cuboid. "nox" and "noz" might help you to line up the end of the tiling by tapping
        something you want it to extend to in one axis but not all of them.
    TODO add options to flip every other row along any combination of the x, y, and/or z axes.
*/
    @Override
    public boolean onCommandSelf(CommandSender sender,
            Command command,
            String label,
            String[] args) {
        try {
            // what to do when the destination block is not a full multiple of the tile width.
            TileMode theTileMode = TileMode.DOWN;// defualt is to "round down"
            boolean lockX = false;// lock grid width to 1 along X axis
            boolean lockY = false;// lock grid width to 1 along Y axis
            boolean lockZ = false;// lock grid width to 1 along Z axis

            if (null != args) {
                for (String arg : args) {
                    if (null != arg) {
                        if (ROUND_UP_OPTION.equalsIgnoreCase(arg)) {
                            theTileMode = TileMode.UP;
                        } else if (ROUND_DOWN_OPTION.equalsIgnoreCase(arg)) {
                            theTileMode = TileMode.DOWN;
                        } else if (LOCK_X_OPTION.equalsIgnoreCase(arg)) {
                            lockX = true;
                        } else if (LOCK_Y_OPTION.equalsIgnoreCase(arg)) {
                            lockY = true;
                        } else if (LOCK_Z_OPTION.equalsIgnoreCase(arg)) {
                            lockZ = true;
                        } else {
                            sendTo(sender, Buildr_Manager_Command_Super.MsgType.ERROR,
                                    "unrecognized argument.");
                            return true;
                        }
                    }
                }
            }

            cmd_tile(sender, theTileMode, lockX, lockY, lockZ);
            return true;
        } catch (BadFormatException formatX) {
            sendTo(sender, Buildr_Manager_Command_Super.MsgType.ERROR,
                    formatX.getMessage());
            return true;
        }
    }


    public void cmd_tile(CommandSender sender,
            TileMode inTileMode,
            boolean inLockX,
            boolean inLockY,
            boolean inLockZ) {
        if (!plugin.getConfigValue("FEATURE_BUILDER_TILE")) {
            sendTo(sender, Buildr_Manager_Command_Super.MsgType.INFO, "feature not enabled");
            return;
        }
        if (plugin.checkPlayerHasStartedBuilding((Player) sender)) {
            plugin.removeStartedBuilding((Player) sender);
            sendTo(sender, Buildr_Manager_Command_Super.MsgType.WARNING, "previous started building aborted");
        }

        plugin.addStartedBuilding(
                new TileBuilderManager((Player) sender, plugin, inTileMode, inLockX, inLockY, inLockZ));

        StringBuilder sb = new StringBuilder("Started new Tile");
        switch (inTileMode) {
            case UP:
                sb.append(", rounding to next full tile");
                break;
            case DOWN:
                sb.append(", rounding down to last full tile");
                break;
            case TRIM:
                sb.append(", trimming tiles to exact position");
                break;
        }
        sb.append(".");
        String buildinfo = sb.toString();

        sendTo(sender, Buildr_Manager_Command_Super.MsgType.INFO, buildinfo);
        sendTo(sender, Buildr_Manager_Command_Super.MsgType.INFO,
                "Right-click on one corner of the cuboid to tile while holding a stick to begin");
    }
}
