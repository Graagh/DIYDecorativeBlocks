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

import java.util.stream.Stream;

import net.malisis.core.block.MalisisBlock;
import net.malisis.core.block.component.ColorComponent;
import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.block.component.PaneComponent;
import net.malisis.core.block.component.StairComponent;
import net.malisis.core.block.component.WallComponent;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.renderer.icon.provider.IconProviderBuilder;
import net.malisis.core.renderer.icon.provider.MegaTextureIconProvider;
import net.malisis.core.renderer.icon.provider.PropertyEnumIconProvider;
import net.malisis.ddb.BlockDescriptor;
import net.malisis.ddb.BlockPack;
import net.malisis.ddb.BlockType;
import net.malisis.ddb.DDB;
import net.malisis.ddb.DDBIcon;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Predicates;

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
		setSoundType(descriptor.getSoundType());

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
				break;
			case PANE:
				addComponent(new PaneComponent());
				break;
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

	@SideOnly(Side.CLIENT)
	public IIconProvider createIconProvider(Object object)
	{
		Icon defaultIcon = null;

		if (descriptor.type == BlockType.MEGATEXTURE)
		{
			defaultIcon = DDBIcon.getIcon(getName(), pack, descriptor.getTexture());
			MegaTextureIconProvider iconProvider = new MegaTextureIconProvider(defaultIcon);
			for (EnumFacing facing : EnumFacing.VALUES)
				iconProvider.setMegaTexture(facing, defaultIcon, descriptor.numBlocks);

			return iconProvider;
		}
		else if (descriptor.type == BlockType.CONNECTED)
		{
			Icon part1 = DDBIcon.getIcon(getName(), pack, descriptor.getTexture());
			Icon part2 = DDBIcon.getIcon(getName() + "2", pack, descriptor.getTexture() + "2");

			return IIconProvider.create(part1).wall(part2).build();
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

			return iconProvider;
		}
		else if (descriptor.type == BlockType.WALL)
		{
			String insideName = descriptor.getTexture("inside");
			String outsideName = descriptor.getTexture("outside");

			if (!StringUtils.isEmpty(insideName) && !StringUtils.isEmpty(outsideName))
			{
				Icon inside = DDBIcon.getIcon(getName() + "_inside", pack, insideName);
				Icon outside = DDBIcon.getIcon(getName() + "_outside", pack, outsideName);

				return IIconProvider.create(outside).wall(inside).build();
			}
			else
			{
				if (!StringUtils.isEmpty(insideName))
					defaultIcon = DDBIcon.getIcon(getName(), pack, insideName);
				else if (!StringUtils.isEmpty(outsideName))
					defaultIcon = DDBIcon.getIcon(getName(), pack, outsideName);
			}
		}

		String defaultName = Stream.of(EnumFacing.VALUES)
									.map(descriptor::getTexture)
									.filter(Predicates.not(StringUtils::isEmpty)::apply)
									.findFirst()
									.orElse(null);

		if (defaultName == null)
			return IIconProvider.create(DDBIcon.getIcon(name, pack, descriptor.getTexture())).build();

		IconProviderBuilder builder = IIconProvider.create(DDBIcon.getIcon(defaultName, pack, defaultName));

		for (EnumFacing side : EnumFacing.VALUES)
		{
			String textureName = descriptor.getTexture(side);
			if (textureName != null)
				builder.withSide(side, DDBIcon.getIcon(name + "_" + side.toString(), pack, textureName));
		}

		return builder.build();
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		if (!fullBlock)
			return false;

		return super.isOpaqueCube(state);
	}

	@Override
	public boolean canRenderInLayer(BlockRenderLayer layer)
	{
		if (descriptor.translucent)
			return layer == BlockRenderLayer.TRANSLUCENT;
		return layer == BlockRenderLayer.CUTOUT_MIPPED;
	}

	@Override
	public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		//Called for CONNECTED
		if (!isOpaqueCube(state))
			if (world.getBlockState(pos).getBlock() == world.getBlockState(pos.offset(side.getOpposite())).getBlock())
				return false;
		return super.shouldSideBeRendered(state, world, pos, side);
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
