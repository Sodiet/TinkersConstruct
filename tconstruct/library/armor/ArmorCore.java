package tconstruct.library.armor;

import ic2.api.item.IBoxable;
import ic2.api.item.ICustomElectricItem;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumArmorMaterial;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * NBTTags
 * Main tag - InfiArmor
 */
public abstract class ArmorCore extends ItemArmor implements ICustomElectricItem, IBoxable {

	public static final String SET_NAME = "InfiArmor";
	public final EnumArmorPart armorPart;
    private static final IBehaviorDispenseItem dispenserBehavior = new BehaviorDispenseArmorCopy();
    public final int baseProtection;

	public ArmorCore(int par1, int baseProtection, EnumArmorPart part) {
		super(par1, EnumArmorMaterial.CHAIN, 0, 0);
		this.maxStackSize = 1;
		this.setMaxDamage(100);
		this.setUnlocalizedName(SET_NAME);
		this.armorPart = part;
		this.baseProtection = baseProtection;
        BlockDispenser.dispenseBehaviorRegistry.putObject(this, dispenserBehavior);
	}

	public String getArmorName() {
		return this.getClass().getSimpleName();
	}

	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer) {
		int i = EntityLiving.getArmorPosition(par1ItemStack) - 1;
		ItemStack itemstack1 = par3EntityPlayer.getCurrentArmor(i);

		if (itemstack1 == null) {
			par3EntityPlayer.setCurrentItemOrArmor(i + 1, par1ItemStack.copy()); //Forge: Vanilla bug fix associated with fixed setCurrentItemOrArmor indexs for players.
			par1ItemStack.stackSize = 0;
		}

		return par1ItemStack;
	}
	
	/*
	 * IC2 API support
	 */
	
	@Override
	public boolean canBeStoredInToolbox(ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean canProvideEnergy(ItemStack stack) {
		NBTTagCompound tags = stack.getTagCompound();

		return tags.hasKey("charge");
	}

	@Override
	public int getChargedItemId(ItemStack stack) {
		return this.itemID;
	}

	@Override
	public int getEmptyItemId(ItemStack stack) {
		return this.itemID;
	}

	@Override
	public int getMaxCharge(ItemStack stack) {
		NBTTagCompound tags = stack.getTagCompound();
		return tags.hasKey("charge") ? 10000 : 0;
	}

	@Override
	public int getTier(ItemStack stack) {
		return 0;
	}

	@Override
	public int getTransferLimit(ItemStack stack) {
		NBTTagCompound tags = stack.getTagCompound();

		return tags.hasKey("charge") ? 32 : 0;
	}

	@Override
	public int charge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
		NBTTagCompound tags = stack.getTagCompound();
		if (!tags.hasKey("charge")) {
			return 0;
		}

		if (amount > 0) {
			if (amount > getTransferLimit(stack) && !ignoreTransferLimit) {
				amount = getTransferLimit(stack);
			}

			int charge = tags.getInteger("charge");

			if (amount > getMaxCharge(stack)) {
				amount = getMaxCharge(stack);
			}

			charge += amount;

			if (!simulate) {
				tags.setInteger("charge", charge);
				stack.setItemDamage(1 + (getMaxCharge(stack) - charge) * (stack.getMaxDamage() - 2) / getMaxCharge(stack));
			}
			return amount;
		}

		else
			return 0;
	}

	@Override
	public int discharge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
		NBTTagCompound tags = stack.getTagCompound();
		if (!tags.hasKey("charge")) {
			return 0;
		}

		if (amount > 0) {
			if (amount > getTransferLimit(stack)) {
				amount = getTransferLimit(stack);
			}

			int charge = tags.getInteger("charge");

			if (amount > charge) {
				amount = charge;
			}

			charge -= amount;

			if (!simulate) {
				tags.setInteger("charge", charge);
				stack.setItemDamage(1 + (getMaxCharge(stack) - charge) * (stack.getMaxDamage() - 1) / getMaxCharge(stack));
			}

			return charge;
		}

		else
			return 0;
	}

	@Override
	public boolean canUse(ItemStack stack, int amount) {
		return false;
	}

	@Override
	public boolean canShowChargeToolTip(ItemStack stack) {
		return false;
	}

	// Vanilla overrides
	public boolean isItemTool(ItemStack par1ItemStack) {
		return false;
	}

	@Override
	public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack) {
		return false;
	}

	public boolean isRepairable() {
		return false;
	}

	public int getItemEnchantability() {
		return 0;
	}

	public boolean isFull3D() {
		return true;
	}
	
	public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
		return this.armorPart.getPartId() == armorType;
	}
	
    public String getArmorTexture(ItemStack stack, Entity entity, int slot, int layer){
        return "minecraft:textures/models/armor/iron_layer_" + layer + ".png";
    }

	/* Proper stack damage */
	public int getItemMaxDamageFromStack(ItemStack stack) {
		NBTTagCompound tags = stack.getTagCompound();
		if (tags == null) {
			return 0;
		}

		if (tags.hasKey("charge")) {
			int charge = tags.getInteger("charge");
			if (charge > 0) {
				return this.getMaxCharge(stack);
			}
		}

		return tags.getCompoundTag(SET_NAME).getInteger("TotalDurability");
	}

	public int getItemMaxDamageFromStackForDisplay(ItemStack stack) {
		NBTTagCompound tags = stack.getTagCompound();
		if (tags == null) {
			return 0;
		}

		if (tags.hasKey("charge")) {
			int charge = tags.getInteger("charge");
			if (charge > 0) {
				return this.getMaxCharge(stack) - charge;
			}
		}

		return tags.getCompoundTag(SET_NAME).getInteger("Damage");
	}
	
}