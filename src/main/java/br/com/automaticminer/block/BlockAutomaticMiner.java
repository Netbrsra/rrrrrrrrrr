package br.com.automaticminer.block;

import br.com.automaticminer.AutomaticMiner;
import br.com.automaticminer.tile.TileAutomaticMiner;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockAutomaticMiner extends BlockContainer {
 public BlockAutomaticMiner(){super(Material.IRON);setHardness(5F);setResistance(10F);setHarvestLevel("pickaxe",2);}
 @Override public TileEntity createNewTileEntity(World w,int m){return new TileAutomaticMiner();}
 @Override public boolean onBlockActivated(World w,BlockPos p,IBlockState s,EntityPlayer pl,EnumHand h,EnumFacing f,float x,float y,float z){
  if(!w.isRemote)pl.openGui(AutomaticMiner.INSTANCE,0,w,p.getX(),p.getY(),p.getZ()); return true;
 }
}
