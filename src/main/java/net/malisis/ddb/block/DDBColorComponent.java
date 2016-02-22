/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.ddb.block;

import java.util.List;

import net.malisis.core.block.IBlockComponent;
import net.malisis.core.block.component.ColorComponent;
import net.malisis.core.item.MalisisItemBlock;
import net.malisis.ddb.Color;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class DDBColorComponent implements IBlockComponent
{
	public static PropertyEnum COLOR = PropertyEnum.create("color", Color.class);
	private boolean useColorMultiplier = true;

	public DDBColorComponent(boolean useColorMultiplier)
	{
		this.useColorMultiplier = useColorMultiplier;
	}

	public boolean useColorMultiplier()
	{
		return useColorMultiplier;
	}

	@Override
	public PropertyEnum getProperty()
	{
		return COLOR;
	}

	@Override
	public IBlockState setDefaultState(Block block, IBlockState state)
	{
		return state.withProperty(getProperty(), Color.WHITE);
	}

	@Override
	public String getUnlocalizedName(Block block, IBlockState state)
	{
		return block.getUnlocalizedName() + "." + getColor(state).getUnlocalizedName();
	}

	@Override
	public Item getItem(Block block)
	{
		return new MalisisItemBlock(block);
	}

	@Override
	public int damageDropped(Block block, IBlockState state)
	{
		return ((Color) state.getValue(getProperty())).getMetadata();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Block block, Item item, CreativeTabs tab, List list)
	{
		for (Color color : Color.values())
			list.add(new ItemStack(item, 1, color.getMetadata()));
	}

	@Override
	public int colorMultiplier(Block block, IBlockAccess world, BlockPos pos, int renderPass)
	{
		if (!useColorMultiplier)
			return 0xFFFFFF;
		return getRenderColor(block, world.getBlockState(pos));
	}

	/**
	 * Gets the render color for this {@link Block}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the render color
	 */
	@Override
	public int getRenderColor(Block block, IBlockState state)
	{
		return ItemDye.dyeColors[getColor(state).getDyeDamage()];
	}

	/**
	 * Get the {@link MapColor} for this {@link Block} and the given {@link IBlockState}.
	 *
	 * @param block the block
	 * @param state the state
	 * @return the map color
	 */
	@Override
	public MapColor getMapColor(Block block, IBlockState state)
	{
		return ((Color) state.getValue(getProperty())).getMapColor();
	}

	/**
	 * Get the {@link IBlockState} from the metadata
	 *
	 * @param block the block
	 * @param state the state
	 * @param meta the meta
	 * @return the state from meta
	 */
	@Override
	public IBlockState getStateFromMeta(Block block, IBlockState state, int meta)
	{
		return state.withProperty(getProperty(), Color.byMetadata(meta));
	}

	/**
	 * Get the metadata from the {@link IBlockState}
	 *
	 * @param block the block
	 * @param state the state
	 * @return the meta from state
	 */
	@Override
	public int getMetaFromState(Block block, IBlockState state)
	{
		return ((Color) state.getValue(getProperty())).getMetadata();
	}

	/**
	 * Gets the {@link Color color} for the {@link Block} at world coords.
	 *
	 * @param world the world
	 * @param pos the pos
	 * @return the Color, null if the block is not {@link ColorComponent}
	 */
	public static Color getColor(IBlockAccess world, BlockPos pos)
	{
		return world != null && pos != null ? getColor(world.getBlockState(pos)) : Color.WHITE;
	}

	/**
	 * Gets the {@link Color color} for the {@link IBlockState}.
	 *
	 * @param state the state
	 * @return the Color, null if the block is not {@link ColorComponent}
	 */
	public static Color getColor(IBlockState state)
	{
		ColorComponent cc = IBlockComponent.getComponent(ColorComponent.class, state.getBlock());
		if (cc == null)
			return Color.WHITE;

		PropertyEnum property = cc.getProperty();
		if (property == null || !state.getProperties().containsKey(property))
			return Color.WHITE;

		return (Color) state.getValue(property);
	}

}