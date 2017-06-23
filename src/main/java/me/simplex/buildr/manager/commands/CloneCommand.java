/*
 * Copyright 2015 s1mpl3x
 * Copyright 2015 pwasson
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
import me.simplex.buildr.manager.builder.CloneBuilderManager;
import static me.simplex.buildr.manager.commands.Buildr_Manager_Command_Super.sendTo;
import me.simplex.buildr.util.CloneOptions;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class CloneCommand extends AbstractBuilderCommand {
    public CloneCommand(Buildr plugin) {
        super(plugin, "bclone", "buildr.cmd.clone", 6, 0);
    }
/* TODO support options from built-in command:
    "masked" (do not copy air blocks)
    ? "filtered" (with material, only copy blocks of specified material)
    ? "move" (allow overlap of source and dest; blocks in source that are not occupied by dest afterward,
        but were copied, are replaced by air)
*/
    @Override
    public boolean onCommandSelf(CommandSender sender,
            Command command,
            String label,
            String[] args) {
        try {
            // the only argument we have is an optional rotation
            // TODO call CloneOptions parseCloneOptions(List<String> ioList) throws BadFormatException
            // to get maskMode and replace material
            // CONFLICT: parseCloneOptions wants "r" to prefix the replace material, which IS consistent
            // with most of the other commands, so we'll have to change Rotate to be e.g. "rotate90"
            // or "rotate 90" (accepting "rot" as short for rotate).
            int rotationAngle = 0;
            CloneOptions cloneOpts = null;

            if (null != args && args.length > 0) {
                // the List from Arrays.asList doesn't support Iterator.remove, so copy it.
                List<String> argsList = new ArrayList<String>(Arrays.asList(args));
                cloneOpts = parseCloneOptions(argsList);// removes args it recognizes

                Iterator<String> argIt = argsList.iterator();
                while (argIt.hasNext()) {
                    String arg = argIt.next().toLowerCase();
                    if ("rotate".equals(arg) || "rot".equals("arg")) {
                        if (!argIt.hasNext())
                            throw new BadFormatException("Rotate option requires a rotation angle.");
                        arg = argIt.next().toLowerCase();
                        try {
                            rotationAngle = Integer.parseInt(arg);
                            if ((rotationAngle % 90) != 0)
                                throw new BadFormatException("Invalid rotation angle; must be a multiple of 90.");
                            // normalize the rotation angle to > 0, < 360.
                            rotationAngle = rotationAngle % 360;
                            while (rotationAngle < 0)
                                rotationAngle += 360;
                        } catch (NumberFormatException numX) {
                            throw new BadFormatException("Invalid rotation angle; must be a multiple of 90.");
                        }
                    }
                }
            }

            cmd_clone(sender, cloneOpts, rotationAngle);
            return true;
        } catch (BadFormatException formatX) {
            sendTo(sender, Buildr_Manager_Command_Super.MsgType.ERROR,
                    formatX.getMessage());
            return true;
        }
    }


    public void cmd_clone(CommandSender sender,
            CloneOptions cloneOpts,
            int rotationAngle) {
        if (!plugin.getConfigValue("FEATURE_BUILDER_CLONE")) {
            sendTo(sender, Buildr_Manager_Command_Super.MsgType.INFO, "feature not enabled");
            return;
        }
        if (plugin.checkPlayerHasStartedBuilding((Player) sender)) {
            plugin.removeStartedBuilding((Player) sender);
            sendTo(sender, Buildr_Manager_Command_Super.MsgType.WARNING, "previous started building aborted");
        }

        plugin.addStartedBuilding(
                new CloneBuilderManager((Player) sender, plugin, cloneOpts, rotationAngle));

        StringBuilder sb = new StringBuilder("Started new Clone");
        if (rotationAngle != 0) {
            sb.append(String.format(", rotating %d degrees", rotationAngle));
        }
        sb.append(".");
        String buildinfo = sb.toString();

        sendTo(sender, Buildr_Manager_Command_Super.MsgType.INFO, buildinfo);
        sendTo(sender, Buildr_Manager_Command_Super.MsgType.INFO,
                "Right-click on one corner of the cuboid to clone while holding a stick to begin");
    }
}
