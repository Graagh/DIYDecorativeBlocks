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

import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.ColorComponent;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.block.component.StairComponent;
import net.malisis.core.block.component.WallComponent;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.ConnectedIconsProvider;
import net.malisis.core.renderer.icon.provider.MegaTextureIconProvider;
import net.malisis.core.renderer.icon.provider.PropertyEnumIconProvider;
import net.malisis.core.renderer.icon.provider.SidesIconProvider;
import net.malisis.core.renderer.icon.provider.WallIconProvider;
import net.malisis.ddb.BlockDescriptor;
import net.malisis.ddb.BlockPack;
import net.malisis.ddb.BlockType;
import net.malisis.ddb.DDB;
import net.malisis.ddb.DDBIcon;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Ordinastie
 *
 */
public class DDBBlock extends MalisisBlock
{
	protected BlockPack pack;
	protected BlockDescriptor descriptor;

	public DDBBlock(BlockPack pack, BlockDescriptor descriptor)
	{
		super(descriptor.getMaterial());
		this.pack = pack;
		this.descriptor = descriptor;
		this.fullBlock = descriptor.opaque && !descriptor.translucent;
		this.lightOpacity = fullBlock ? 255 : 0;
		this.lightValue = Math.max(0, Math.min(15, descriptor.lightValue));

		setName(pack.getName() + "_" + descriptor.name);
		setHardness(descriptor.hardness);
		setStepSound(descriptor.getSoundType());

		setCreativeTab(DDB.tab);

		switch (descriptor.type)
		{
			case DIRECTIONAL:
				addComponent(new DirectionalComponent());
				break;
			case STAIRS:
				addComponent(new StairComponent());
				break;
			case COLORED:
				addComponent(new ColorComponent(descriptor.useColorMultiplier));
				break;
			case WALL:
				addComponent(new WallComponent());
			default:
				break;
		}
	}

	@Override
	public String getName()
	{
		return name;
	}

	public BlockType getBlockType()
	{
		return descriptor.type;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		MalisisIcon defaultIcon = null;

		if (descriptor.type == BlockType.MEGATEXTURE)
		{
			defaultIcon = new DDBIcon(getName(), pack, descriptor.getTexture());
			MegaTextureIconProvider iconProvider = new MegaTextureIconProvider(defaultIcon);
			for (EnumFacing facing : EnumFacing.VALUES)
				iconProvider.setMegaTexture(facing, defaultIcon, descriptor.numBlocks);

			this.iconProvider = iconProvider;
			return;
		}
		else if (descriptor.type == BlockType.CONNECTED)
		{
			MalisisIcon part1 = new DDBIcon(getName(), pack, descriptor.getTexture());
			MalisisIcon part2 = new DDBIcon(getName() + "2", pack, descriptor.getTexture() + "2");

			iconProvider = new ConnectedIconsProvider(part1, part2);
			return;
		}
		else if (descriptor.type == BlockType.COLORED && !descriptor.useColorMultiplier)
		{
			//DDBIcon defaultIcon = new DDBIcon(name, pack, descriptor.getTexture());
			PropertyEnumIconProvider<EnumDyeColor> iconProvider = new PropertyEnumIconProvider<>(ColorComponent.COLOR, EnumDyeColor.class);
			for (EnumDyeColor color : EnumDyeColor.values())
			{
				String name = getName() + "_" + color.getUnlocalizedName();
				DDBIcon icon = new DDBIcon(name, pack, descriptor.getTexture() + "_" + color.getUnlocalizedName());
				iconProvider.setIcon(color, icon);
			}

			this.iconProvider = iconProvider;
			return;
		}
		else if (descriptor.type == BlockType.WALL)
		{
			String insideName = descriptor.getTexture("inside");
			String outsideName = descriptor.getTexture("outside");

			if (!StringUtils.isEmpty(insideName) && !StringUtils.isEmpty(outsideName))
			{
				DDBIcon inside = new DDBIcon(getName() + "_inside", pack, insideName);
				DDBIcon outside = new DDBIcon(getName() + "_outside", pack, outsideName);
				this.iconProvider = new WallIconProvider(inside, outside);
				return;
			}
			else
			{
				if (!StringUtils.isEmpty(insideName))
					defaultIcon = new DDBIcon(getName(), pack, insideName);
				else if (!StringUtils.isEmpty(outsideName))
					defaultIcon = new DDBIcon(getName(), pack, outsideName);
			}
		}

		MalisisIcon[] sideIcons = new DDBIcon[6];
		for (EnumFacing dir : EnumFacing.VALUES)
		{
			String textureName = descriptor.getTexture(dir);

			if (textureName != null)
				sideIcons[dir.ordinal()] = new DDBIcon(name + "_" + dir.toString(), pack, textureName);
			else if (defaultIcon == null)
				defaultIcon = new DDBIcon(name, pack, descriptor.getTexture());
		}

		iconProvider = new SidesIconProvider(defaultIcon, sideIcons);
	}

	@Override
	public boolean isOpaqueCube()
	{
		if (!fullBlock)
			return false;

		return super.isOpaqueCube();
	}

	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer)
	{
		if (descriptor.translucent)
			return layer == EnumWorldBlockLayer.TRANSLUCENT;
		return layer == EnumWorldBlockLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		//Called for CONNECTED
		if (!isOpaqueCube())
			if (world.getBlockState(pos).getBlock() == world.getBlockState(pos.offset(side.getOpposite())).getBlock())
				return false;
		return super.shouldSideBeRendered(world, pos, side);
	}

	//	@Override
	//	public boolean shouldSmartCull()
	//	{
	//		return descriptor.type != BlockType.CONNECTED;
	//	}

	public void registerRecipes()
	{
		IRecipe recipe = descriptor.recipe != null ? descriptor.recipe.createRecipe(this) : null;
		if (recipe != null)
			GameRegistry.addRecipe(recipe);

		if (descriptor.furnaceRecipe != null)
			descriptor.furnaceRecipe.addFurnaceRecipe(this);
	}
}
