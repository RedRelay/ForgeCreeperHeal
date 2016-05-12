package fr.eyzox.forgecreeperheal.blockdata;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

public class TileEntityBlockData extends DefaultBlockData{
	
	public static final String TAG_TILE_ENTITY = "tileEntity";
	
	private NBTTagCompound tileEntityTag;
	
	public TileEntityBlockData(final BlockPos pos, final IBlockState state, final NBTTagCompound tileEntityTag) {
		super(pos, state);
		this.tileEntityTag = tileEntityTag;
	}
	
	public TileEntityBlockData(final NBTTagCompound tag) {
		super(tag);
	}

	public NBTTagCompound getTileEntityTag() {
		return tileEntityTag;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
		if(tileEntityTag != null) {
			tag.setTag(TAG_TILE_ENTITY, tileEntityTag);
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		final NBTTagCompound tileEntityTag = tag.getCompoundTag(TAG_TILE_ENTITY);
		if(!tileEntityTag.hasNoTags()) {
			this.tileEntityTag = tileEntityTag;
		}else {
			this.tileEntityTag = null;
		}
	}

}