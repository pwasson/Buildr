/*
 * Copyright 2016-2017 pwasson
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import me.simplex.buildr.Buildr;
import me.simplex.buildr.manager.builder.TileBuilderManager;
import me.simplex.buildr.manager.builder.TileBuilderManager.TileMode;
import static me.simplex.buildr.manager.commands.Buildr_Manager_Command_Super.sendTo;
import me.simplex.buildr.util.CloneOptions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TileCommand extends AbstractBuilderCommand {
    // options should be all lower-case since we compare with the lower-cased arg.
    public static final String ROUND_UP_OPTION = "roundup";
    public static final String ROUND_DOWN_OPTION = "rounddown";
    public static final String TRIM_TO_FIT_OPTION = "fit";
    public static final String LOCK_X_OPTION = "lockx";
    public static final String LOCK_X_OPTION_ALIAS1 = "lockeastwest";
    public static final String LOCK_X_OPTION_ALIAS2 = "lockew";
    public static final String LOCK_Y_OPTION = "locky";
    public static final String LOCK_Z_OPTION = "lockz";
    public static final String LOCK_Z_OPTION_ALIAS1 = "locknorthsouth";
    public static final String LOCK_Z_OPTION_ALIAS2 = "lockns";
    public static final String MIRROR_X_OPTION = "flipx";
    public static final String MIRROR_Z_OPTION = "flipz";

    public TileCommand(Buildr plugin) {
        super(plugin, "btile", "buildr.cmd.tile", 10, 0);
    }


    @Override
    public boolean onCommandSelf(CommandSender sender,
            Command command,
            String label,
            String[] args) {
        try {
            // what to do when the destination block is not a full multiple of the tile width.
            TileMode theTileMode = TileMode.DOWN;// default is to "trim to fit"
            boolean lockX = false;// lock grid width to 1 along X axis
            boolean lockY = false;// lock grid width to 1 along Y axis
            boolean lockZ = false;// lock grid width to 1 along Z axis
            boolean mirrorX = false;// mirror every other tile along X axis
            boolean mirrorZ = false;// mirror every other tile along Z asix
            CloneOptions cloneOpts = null;

            if (null != args && args.length > 0) {
                // the List from Arrays.asList doesn't support Iterator.remove, so copy it.
                List<String> argsList = new ArrayList<String>(Arrays.asList(args));
                cloneOpts = parseCloneOptions(argsList);// removes args it recognizes

                Iterator<String> argIt = argsList.iterator();
                while (argIt.hasNext()) {
                    String arg = argIt.next();
                    String argLS = arg.toLowerCase();
                    if (ROUND_UP_OPTION.equals(argLS)) {
                        theTileMode = TileMode.UP;
                    } else if (ROUND_DOWN_OPTION.equals(argLS)) {
                        theTileMode = TileMode.DOWN;
                    } else if (TRIM_TO_FIT_OPTION.equals(argLS)) {
                        theTileMode = TileMode.TRIM;
                    } else if (LOCK_X_OPTION.equals(argLS) || LOCK_X_OPTION_ALIAS1.equals(argLS)
                            || LOCK_X_OPTION_ALIAS2.equals(argLS)) {
                        lockX = true;
                    } else if (LOCK_Y_OPTION.equals(argLS)) {
                        lockY = true;
                    } else if (LOCK_Z_OPTION.equals(argLS) || LOCK_Z_OPTION_ALIAS1.equals(argLS)
                            || LOCK_Z_OPTION_ALIAS2.equals(argLS)) {
                        lockZ = true;
                    } else if (MIRROR_X_OPTION.equals(argLS)) {
                        mirrorX = true;
                    } else if (MIRROR_Z_OPTION.equals(argLS)) {
                        mirrorZ = true;
                    } else {
                        throw new BadFormatException("unrecognized argument: " + arg);
                    }
                }
            }

            cmd_tile(sender, cloneOpts, theTileMode, lockX, lockY, lockZ, mirrorX, mirrorZ);
            return true;
        } catch (BadFormatException formatX) {
            sendTo(sender, Buildr_Manager_Command_Super.MsgType.ERROR,
                    formatX.getMessage());
            return true;
        }
    }


    public void cmd_tile(CommandSender sender,
            CloneOptions cloneOpts,
            TileMode inTileMode,
            boolean inLockX,
            boolean inLockY,
            boolean inLockZ,
            boolean mirrorX,
            boolean mirrorZ) {
        if (!plugin.getConfigValue("FEATURE_BUILDER_TILE")) {
            sendTo(sender, Buildr_Manager_Command_Super.MsgType.INFO, "feature not enabled");
            return;
        }
        if (plugin.checkPlayerHasStartedBuilding((Player) sender)) {
            plugin.removeStartedBuilding((Player) sender);
            sendTo(sender, Buildr_Manager_Command_Super.MsgType.WARNING, "previous started building aborted");
        }

        plugin.addStartedBuilding(
                new TileBuilderManager((Player) sender, plugin, cloneOpts, inTileMode, inLockX, inLockY, 
                        inLockZ, mirrorX, mirrorZ));

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
