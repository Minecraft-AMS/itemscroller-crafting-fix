package fi.dy.masa.itemscroller.recipes;

import javax.annotation.Nonnull;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.itemscroller.config.Configs;
import fi.dy.masa.itemscroller.config.Configs.SlotRange;
import fi.dy.masa.itemscroller.event.InputEventHandler;

public class CraftingRecipe
{
    private ItemStack result = InputEventHandler.EMPTY_STACK;
    private ItemStack[] recipe = new ItemStack[9];

    public CraftingRecipe()
    {
        this.ensureRecipeSizeAndClearRecipe(9);
    }

    public void ensureRecipeSize(int size)
    {
        if (this.getRecipeLength() != size)
        {
            this.recipe = new ItemStack[size];
        }
    }

    public void clearRecipe()
    {
        for (int i = 0; i < this.recipe.length; i++)
        {
            this.recipe[i] = InputEventHandler.EMPTY_STACK;
        }

        this.result = InputEventHandler.EMPTY_STACK;
    }

    public void ensureRecipeSizeAndClearRecipe(int size)
    {
        this.ensureRecipeSize(size);
        this.clearRecipe();
    }

    public void storeCraftingRecipe(GuiContainer gui, Slot slot)
    {
        SlotRange range = Configs.getCraftingGridSlots(gui, slot);

        if (range != null && slot.getHasStack())
        {
            if (InputEventHandler.areStacksEqual(this.getResult(), slot.getStack()) == false ||
                this.getRecipeLength() != range.getSlotCount())
            {
                int gridSize = range.getSlotCount();
                int numSlots = gui.inventorySlots.inventorySlots.size();

                this.ensureRecipeSizeAndClearRecipe(gridSize);

                for (int i = 0, s = range.getFirst(); i < gridSize && s < numSlots; i++, s++)
                {
                    Slot slotTmp = gui.inventorySlots.getSlot(s);
                    this.recipe[i] = slotTmp.getHasStack() ? slotTmp.getStack().copy() : InputEventHandler.EMPTY_STACK;
                }

                this.result = slot.getStack().copy();
            }
        }
    }

    public void copyRecipeFrom(CraftingRecipe other)
    {
        int size = other.getRecipeLength();
        ItemStack[] otherRecipe = other.getRecipe();

        this.ensureRecipeSizeAndClearRecipe(size);

        for (int i = 0; i < size; i++)
        {
            this.recipe[i] = InputEventHandler.isStackEmpty(otherRecipe[i]) == false ? otherRecipe[i].copy() : InputEventHandler.EMPTY_STACK;
        }

        this.result = InputEventHandler.isStackEmpty(other.getResult()) == false ? other.getResult().copy() : InputEventHandler.EMPTY_STACK;
    }

    public void readFromNBT(@Nonnull NBTTagCompound nbt)
    {
        if (nbt.hasKey("Result", Constants.NBT.TAG_COMPOUND) && nbt.hasKey("Ingredients", Constants.NBT.TAG_LIST))
        {
            NBTTagList tagIngredients = nbt.getTagList("Ingredients", Constants.NBT.TAG_COMPOUND);
            int count = tagIngredients.tagCount();
            int length = nbt.getInteger("Length");

            if (length > 0)
            {
                this.ensureRecipeSizeAndClearRecipe(length);
            }

            for (int i = 0; i < count; i++)
            {
                NBTTagCompound tag = tagIngredients.getCompoundTagAt(i);
                int slot = tag.getInteger("Slot");

                if (slot >= 0 && slot < this.recipe.length)
                {
                    this.recipe[slot] = ItemStack.loadItemStackFromNBT(tag);
                }
            }

            this.result = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("Result"));
        }
    }

    @Nonnull
    public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt)
    {
        if (this.isValid())
        {
            NBTTagCompound tag = new NBTTagCompound();
            this.result.writeToNBT(tag);

            nbt.setInteger("Length", this.recipe.length);
            nbt.setTag("Result", tag);

            NBTTagList tagIngredients = new NBTTagList();

            for (int i = 0; i < this.recipe.length; i++)
            {
                if (InputEventHandler.isStackEmpty(this.recipe[i]) == false)
                {
                    tag = new NBTTagCompound();
                    tag.setInteger("Slot", i);
                    this.recipe[i].writeToNBT(tag);
                    tagIngredients.appendTag(tag);
                }
            }

            nbt.setTag("Ingredients", tagIngredients);
        }

        return nbt;
    }

    public ItemStack getResult()
    {
        return this.result;
    }

    public int getRecipeLength()
    {
        return this.recipe.length;
    }

    public ItemStack[] getRecipe()
    {
        return this.recipe;
    }

    public boolean isValid()
    {
        return InputEventHandler.isStackEmpty(this.getResult()) == false;
    }
}