package mekfarm.containers;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.IItemHandler;

/**
 * Created by CF on 2016-11-11.
 */
public class ElectricButcherContainer extends FarmContainer {
    public ElectricButcherContainer(IInventory playerInventory, TileEntity tileEntity) {
        super(playerInventory, tileEntity);
    }

    @Override
    protected void addInputSlots(IItemHandler itemHandler, int inputs) {
        if (inputs != 1) {
            super.addInputSlots(itemHandler, inputs);
        }
        else {
            addSlotToContainer(this.createInputSlot(itemHandler, 0, 118 + 18, 6));
        }
    }

    @Override
    protected Slot createInputSlot(IItemHandler itemHandler, int slotIndex, int xPosition, int yPosition) {
        return new TexturedSlot(itemHandler, slotIndex, xPosition, yPosition, 135, 208, 1);
    }
}
