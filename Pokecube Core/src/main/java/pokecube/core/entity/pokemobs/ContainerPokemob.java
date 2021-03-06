package pokecube.core.entity.pokemobs;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import pokecube.core.PokecubeItems;
import pokecube.core.interfaces.IPokemob;

public class ContainerPokemob extends Container
{
	private IInventory	pokemobInv;
	private IPokemob	pokemob;

	public ContainerPokemob(IInventory playerInv, final IInventory pokeInv, final IPokemob e)
	{
		this.pokemobInv = pokeInv;
		this.pokemob = e;
		byte b0 = 3;
		pokeInv.openInventory(null);
		int i = (b0 - 4) * 18;
		this.addSlotToContainer(new Slot(pokeInv, 0, 8, 18)
		{
			/** Check if the stack is a valid item for this slot. Always true
			 * beside for the armor slots. */
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return super.isItemValid(stack) && stack.getItem() == Items.saddle && !this.getHasStack();
			}
		});
		this.addSlotToContainer(new Slot(pokeInv, 1, 8, 36)
		{
			/** Check if the stack is a valid item for this slot. Always true
			 * beside for the armor slots. */
			@Override
			public boolean isItemValid(ItemStack stack)
			{
				return PokecubeItems.isValidHeldItem(stack);
			}

			/** Returns the maximum stack size for a given slot (usually the
			 * same as getInventoryStackLimit(), but 1 in the case of armor
			 * slots) */
			@Override
			public int getSlotStackLimit()
			{
				return 1;
			}
		});
		int j;
		int k;

		for (j = 0; j < 1; ++j)
		{
			for (k = 0; k < 5; ++k)
			{
				this.addSlotToContainer(new Slot(pokeInv, 2 + k + j * 5, 80 + k * 18, 18 + j * 18)
				{
					/** Check if the stack is a valid item for this slot. Always
					 * true beside for the armor slots. */
					@Override
					public boolean isItemValid(ItemStack stack)
					{
						return true;//PokecubeItems.isValidHeldItem(stack);
					}
				});
			}
		}

		for (j = 0; j < 3; ++j)
		{
			for (k = 0; k < 9; ++k)
			{
				this.addSlotToContainer(new Slot(playerInv, k + j * 9 + 9, 8 + k * 18, 102 + j * 18 + i));
			}
		}

		for (j = 0; j < 9; ++j)
		{
			this.addSlotToContainer(new Slot(playerInv, j, 8 + j * 18, 160 + i));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
	{
		return this.pokemobInv.isUseableByPlayer(p_75145_1_) && ((EntityLiving)this.pokemob).isEntityAlive()
				&& ((EntityLiving)this.pokemob).getDistanceToEntity(p_75145_1_) < 8.0F;
	}

	/** Called when a player shift-clicks on a slot. You must override this or
	 * you will crash when someone does that. */
	@Override
	public ItemStack transferStackInSlot(EntityPlayer p_82846_1_, int p_82846_2_)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(p_82846_2_);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (p_82846_2_ < this.pokemobInv.getSizeInventory())
			{
				if (!this.mergeItemStack(itemstack1, this.pokemobInv.getSizeInventory(), this.inventorySlots.size(),
						true)) { return null; }
			}
			else if (this.getSlot(1).isItemValid(itemstack1) && !this.getSlot(1).getHasStack())
			{
			    this.getSlot(1).putStack(slot.getStack().splitStack(1));
			}
			else if (this.getSlot(0).isItemValid(itemstack1))
			{
				if (!this.mergeItemStack(itemstack1, 0, 1, false)) { return null; }
			}
			else if (this.pokemobInv.getSizeInventory() <= 2
					|| !this.mergeItemStack(itemstack1, 2, this.pokemobInv.getSizeInventory(), false)) { return null; }

			if (itemstack1.stackSize == 0)
			{
				slot.putStack((ItemStack) null);
			}
			else
			{
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}

	/** Called when the container is closed. */
	@Override
	public void onContainerClosed(EntityPlayer p_75134_1_)
	{
		super.onContainerClosed(p_75134_1_);
		this.pokemobInv.closeInventory(p_75134_1_);
	}
}
