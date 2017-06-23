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
package me.simplex.buildr.runnable.builder;

import java.util.Map;
import me.simplex.buildr.util.BlockLocation;
import me.simplex.buildr.util.Buildr_Container_UndoBlock;
import me.simplex.buildr.util.CloneOptions;
import me.simplex.buildr.util.Cuboid;
import me.simplex.buildr.util.MaterialAndData;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Sign;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.Attachable;
import org.bukkit.material.Directional;
import org.bukkit.material.Door;
import org.bukkit.material.Ladder;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Stairs;
import org.bukkit.material.Tree;


/**
 *
 * @author pwasson
 */
public abstract class AbstractCloningBuilderTask extends Buildr_Runnable_Builder_Super {
    protected boolean canBreakBedrock;

    public AbstractCloningBuilderTask(boolean inCanBreakBedrock) {
        this.canBreakBedrock = inCanBreakBedrock;
    }
/*
    TODO support maskMode as per built-in clone command:
        * filtered: only copy a specified material.
        * masked: copy only non-air blocks.
        * replace: (Default) copy and replace everything.
    TODO support cloneMode as per built-in clone command:
        * force: allow even if source and dest cuboids overlap.
        * move: Clone the source region to the destination region, then replace the source region with air.
            When used in filtered mask mode, only the cloned blocks will be replaced with air.
        * normal: (Default) Don't move or force.
*/

    protected void cloneLoop(Cuboid source,
            Cuboid dest,
            World theWorld,
            Map<Block, Buildr_Container_UndoBlock> undoBlocks,
            CloneOptions cloneOpts,
            int rotation,
            boolean mirrorX,
            boolean mirrorZ,
            boolean copyAttachables) {
        for (int srcX = source.getLowCorner().getX(); srcX <= source.getHighCorner().getX(); ++srcX) {
            for (int srcZ = source.getLowCorner().getZ(); srcZ <= source.getHighCorner().getZ(); ++srcZ) {
                BlockLocation destLoc = calcDest(source, dest, srcX, srcZ, rotation, mirrorX, mirrorZ);
                int yOffset = dest.getLowCorner().getY() - source.getLowCorner().getY();
                for (int srcY = source.getLowCorner().getY(); srcY <= source.getHighCorner().getY(); ++srcY) {
                    int destY = srcY + yOffset;
                    Block srcBlockHandle = theWorld.getBlockAt(srcX, srcY, srcZ);
                    Block destBlockHandle = theWorld.getBlockAt(destLoc.getX(), destY, destLoc.getZ());
                    
                    boolean forceToAir = false;
                    boolean isAttachable = Attachable.class.isAssignableFrom(srcBlockHandle.getType().
                            getData());

                    if (isPartOfBedOrDoor(srcBlockHandle) && !copyAttachables) {
                        forceToAir = true;
                    }

                    if (!forceToAir && (copyAttachables != isAttachable)) continue;
                    cloneBlock(srcBlockHandle, destBlockHandle, cloneOpts, rotation, undoBlocks, forceToAir);
                }
            }
        }
    }
    
    private boolean isPartOfBedOrDoor(Block srcBlockHandle) {
        Material itsType = srcBlockHandle.getType();
        return (itsType.equals(Material.WOODEN_DOOR)
                || itsType.equals(Material.WOOD_DOOR)
                || itsType.equals(Material.IRON_DOOR)
                || itsType.equals(Material.BED));
    }


    /**
     * calculates the destination X and Z coordinates based on the source and destination cuboids, the
     * source X, source Z, and rotation angle. (The destination Y is always set to 0.)
     * @param src the cuboid that is the source of the cloning operaton.
     * @param dest the cuboid that is the destination of the cloning operation, already rotated if the
     * source is not square and rotation is not 0.
     * @param srcX the X coordinate of the block in the source cuboid being cloned.
     * @param srcZ the Z coordinate of the block in the source cuboid being cloned.
     * @param rotation how many degrees to rotate the object about the Y axis, in multiples of 90.
     * @param mirrorX whether to mirror <i>along</i> the X axis, i.e. to flip the X coordinates.
     * @param mirrorZ whether to mirror <i>along</i> the Z axis, i.e. to flip the Z coordinates.
     * @return the BlockLocation in the destination cuboid that corresponds to the specified source location.
     */
    protected static BlockLocation calcDest(Cuboid src,
            Cuboid dest,
            int srcX,
            int srcZ,
            int rotation,
            boolean mirrorX,
            boolean mirrorZ) {
        int offsetX;
        if (mirrorX)
            offsetX = src.getHighCorner().getX() - srcX;
        else
            offsetX = srcX - src.getLowCorner().getX();
        int offsetZ;
        if (mirrorZ)
            offsetZ = src.getHighCorner().getZ() - srcZ;
        else
            offsetZ = srcZ - src.getLowCorner().getZ();

        int destX;
        int destZ;
        switch (rotation) {
            case 90:
            case -270:
                destX = dest.getHighCorner().getX() - offsetZ;
                destZ = dest.getLowCorner().getZ() + offsetX;
                break;
            case 180:
            case -180:
                destX = dest.getHighCorner().getX() - offsetX;
                destZ = dest.getHighCorner().getZ() - offsetZ;
                break;
            case 270:
            case -90:
                destX = dest.getLowCorner().getX() + offsetZ;
                destZ = dest.getHighCorner().getZ() - offsetX;
                break;
            default:
                // assume no rotation
                destX = dest.getLowCorner().getX() + offsetX;
                destZ = dest.getLowCorner().getZ() + offsetZ;
                break;
        }
        //plugin.getLogger().info(String.format(
        //        "X %d -> %d, Z %d - %d", srcX, destX, srcZ, destZ));
        return new BlockLocation(destX, 0, destZ);
    }


    protected void cloneBlock(Block srcBlockHandle,
            Block destBlockHandle,
            CloneOptions cloneOpts,
            int rotation,
            Map<Block, Buildr_Container_UndoBlock> undo,
            boolean forceToAir) {
        if (canBuild(player, destBlockHandle)) {
            if (canBreakBedrock || !destBlockHandle.getType().equals(Material.BEDROCK)) {
                /* if we are filtering by material or only cloning non-air blocks, 
                 * this is where we check and skip this block if it doesn't pass the filter.
                 */
                if (null != cloneOpts) {
                    if (CloneOptions.MaskMode.MASK.equals(cloneOpts.getMaskMode())) {
                        // skip out if source is AIR
                        if (Material.AIR.equals(srcBlockHandle.getType()))
                            return;
                    } else if (CloneOptions.MaskMode.FILTER.equals(cloneOpts.getMaskMode())) {
                        // skip out if source is not cloneOpts.filterMaterial
                        if (!blockMatchesMaterialAndData(srcBlockHandle, cloneOpts.getFilterMaterial()))
                            return;
                    }
                    if (null != cloneOpts.getReplaceMaterial()) {
                        // skip out if dest is not cloneOpts.replaceMaterial
                        if (!blockMatchesMaterialAndData(destBlockHandle, cloneOpts.getReplaceMaterial()))
                            return;
                    }
                }

                undo.put(destBlockHandle,
                        new Buildr_Container_UndoBlock(destBlockHandle.getType(), destBlockHandle.getData()));
                
                if (forceToAir) {
                    destBlockHandle.setType(Material.AIR);
                    return;
                }
                
                Material mat = srcBlockHandle.getType();
                byte blockData = srcBlockHandle.getData();

                if (0 != rotation && Directional.class.isAssignableFrom(mat.getData())) {
                    MaterialData newData = mat.getNewData(blockData);
                    Directional newDataAsDirectional = (Directional) newData;
                    BlockFace originalFacing = getActualFacing(newDataAsDirectional);
                    //plugin.getLogger().info(String.format("%s Before: %s", mat.name(), originalFacing));
                    BlockFace newFacing = getRotatedFacing(originalFacing, rotation);
                    //plugin.getLogger().info(String.format("\tRotated: %s", newFacing));
                    setFacingCorrectly(newData, newDataAsDirectional, newFacing);
                    //plugin.getLogger().info(String.format("\tSet Data: %s", getActualFacing(newDataAsDirectional)));
                    blockData = newData.getData();
                    //plugin.getLogger().info(String.format("\tAfter: %s", getActualFacing((Directional)mat.getNewData(destBlockHandle.getData()))));
                } else if ((90 == rotation || 270 == rotation) && Tree.class.isAssignableFrom(mat.getClass())) {
                    // bits 2 & 3 of data are log orientation: 0=up/down, 0x04=east/west, 0x08=north/south, 0x0C=bark only
                    MaterialData newData = mat.getNewData(blockData);
                    byte orientation = (byte)(newData.getData() & 0x0C);
                    if (orientation == 4)
                        orientation = 8;
                    else if (orientation == 8)
                        orientation = 4;
                    blockData = (byte)((newData.getData() & 0xF3) | orientation);
                } else if (Door.class.isAssignableFrom(mat.getClass())) {
                    Door door = (Door)mat.getNewData(blockData);

                    // TODO if other half of door is not being cloned, skip it (return).
                    if (rotation != 0) {
                        // TODO fix door rotation
                    }
                }
                destBlockHandle.setTypeIdAndData(mat.getId(), blockData, true);
                BlockState srcState = srcBlockHandle.getState();
                BlockState destState = destBlockHandle.getState();
                boolean stateChanged = false;
                /* copy inventory of items with inventories: Beacon, Brewing Stand, Chest, Dispenser, Dropper,
                Furnace, Hopper
                 */
                if (srcState instanceof InventoryHolder) {
                    ((InventoryHolder) destState).getInventory().
                            setContents(((InventoryHolder) srcState).getInventory().getContents());
                    stateChanged = true;
                }
                if (srcState instanceof CommandBlock) {
                    ((CommandBlock) destState).setName(((CommandBlock) srcState).getName());
                    ((CommandBlock) destState).setCommand(((CommandBlock) srcState).getCommand());
                    stateChanged = true;
                }
                if (srcState instanceof Sign) {
                    for (int i = 0; i <= 3; ++i) {
                        ((Sign) destState).setLine(i, ((Sign) srcState).getLine(i));
                    }
                    stateChanged = true;
                }
                // TODO lots of other specific BlockState subclasses to copy...
                if (stateChanged) destState.update();
            }
        }
    }


    /**
     * Some block types appear to lie about their facing.<table>
     * <tr><td>Stairs</td><td>getFacing() returns opposite of what it&rsquo;s set to.</td></tr>
     * <tr><td>Torch</td><td> Facing and attached face are opposite; getFacing() is accurate.</td></tr>
     * <tr><td>Ladder</td><td> Facing and AttachedFace are the same, but SimpleAttachableMaterialData will
     * flip the result when you call getFacing() so it lies.</td></tr>
     * <tr><td>Button</td><td> same as Torch</td></tr>
     * <tr><td>Lever</td><td> same as Torch?</td></tr>
     * <tr><td>Trapdoor</td><td> same as Torch</td></tr>
     * <tr><td>TripwireHook</td><td> same as Torch</td></tr>
     * </table>
     * Basically, this ask the Block for its facing, and if it is one that lies, the result is flipped.
     * @param dirData the Directional object whose facing is to be determined.
     * @return the true facing of the block.
     */
    protected static BlockFace getActualFacing(Directional dirData) {
        BlockFace facing = dirData.getFacing();
        if (Stairs.class.isAssignableFrom(dirData.getClass()) || Ladder.class.isAssignableFrom(dirData.getClass())) {
            facing = facing.getOppositeFace();
        }
        return facing;
    }


    protected static BlockFace getRotatedFacing(BlockFace inFacing,
            int rotation) {
        BlockFace facing = inFacing;
        int angle = rotation;
        while (angle > 0) {
            switch (facing) {
                case NORTH:
                    facing = BlockFace.EAST;
                    break;
                case EAST:
                    facing = BlockFace.SOUTH;
                    break;
                case SOUTH:
                    facing = BlockFace.WEST;
                    break;
                case WEST:
                    facing = BlockFace.NORTH;
                    break;
                case NORTH_EAST:
                    facing = BlockFace.SOUTH_EAST;
                    break;
                case NORTH_WEST:
                    facing = BlockFace.NORTH_EAST;
                    break;
                case SOUTH_EAST:
                    facing = BlockFace.SOUTH_WEST;
                    break;
                case SOUTH_WEST:
                    facing = BlockFace.NORTH_WEST;
                    break;
                case WEST_NORTH_WEST:
                    facing = BlockFace.NORTH_NORTH_EAST;
                    break;
                case NORTH_NORTH_WEST:
                    facing = BlockFace.EAST_NORTH_EAST;
                    break;
                case NORTH_NORTH_EAST:
                    facing = BlockFace.EAST_SOUTH_EAST;
                    break;
                case EAST_NORTH_EAST:
                    facing = BlockFace.SOUTH_SOUTH_EAST;
                    break;
                case EAST_SOUTH_EAST:
                    facing = BlockFace.SOUTH_SOUTH_WEST;
                    break;
                case SOUTH_SOUTH_EAST:
                    facing = BlockFace.WEST_SOUTH_WEST;
                    break;
                case SOUTH_SOUTH_WEST:
                    facing = BlockFace.WEST_NORTH_WEST;
                    break;
                case WEST_SOUTH_WEST:
                    facing = BlockFace.NORTH_NORTH_WEST;
                    break;
            }
            angle -= 90;
        }
        return facing;
    }


    /**
     * attempts to work around any bugs in any Directional block type&rsquo;s
     * {@link Directional#setFacingDirection(org.bukkit.block.BlockFace) setFacingDirection()}.
     * @param data
     * @param dataAsDirectional
     * @param newFacing
     */
    protected static void setFacingCorrectly(MaterialData data,
            Directional dataAsDirectional,
            BlockFace newFacing) {
        if (Door.class.isAssignableFrom(data.getClass())) {
            int faceVal = (newFacing == BlockFace.WEST) ? 0x00 : ((newFacing == BlockFace.NORTH) ? 0x01 : ((newFacing == BlockFace.EAST) ? 0x02 : 0x03));
            int cleanData = data.getData() & 0x03;
            int newData = cleanData | faceVal;
            data.setData((byte) newData);
        } else {
            dataAsDirectional.setFacingDirection(newFacing);
        }
    }


    private boolean blockMatchesMaterialAndData(Block h,
            MaterialAndData mad) {
        if (!h.getType().equals(mad.getMaterial()))
            return false;

        if (null != mad.getData()) {
            // this is probably insufficient, but I don't want to have to test each
            // MaterialData type separately in order to separate e.g. wood species from rotation.
            if (!h.getState().getData().equals(mad.getData()))
                return false;
        }

        return true;
    }
}
