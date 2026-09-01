package br.com.automaticminer.tile;

import br.com.automaticminer.init.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.*;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.util.NonNullList;
import net.minecraft.inventory.ItemStackHelper;

public class TileAutomaticMiner extends TileEntity implements ITickable,IInventory {
 private NonNullList<ItemStack> inv=NonNullList.withSize(54, ItemStack.EMPTY);
 public boolean running=false, down=false, rails=false, torches=false;
 public int target=64, done=0, energy=0, ticks=0;
 public EnumFacing direction=EnumFacing.NORTH;

 public TileAutomaticMiner(){}

 @Override public void update(){
  if(world==null||world.isRemote||!running)return;
  if(++ticks<10)return;ticks=0;
  if(done>=target){running=false;markDirty();return;}
  if(energy<2&&!loadFuel()){running=false;markDirty();return;}
  boolean ok=down?mineDown():mineForward();
  if(!ok){running=false;markDirty();return;}
  energy-=2;done++;
  markDirty();
 }

 private boolean loadFuel(){
  for(int i=0;i<27;i++){
   ItemStack s=inv.get(i); if(s.isEmpty())continue;
   int add=0; Item it=s.getItem();
   if(it==Items.COAL)add=64;
   else if(it==Items.LAVA_BUCKET)add=256;
   else if(it==Item.getItemFromBlock(Blocks.LOG)||it==Item.getItemFromBlock(Blocks.LOG2))add=64;
   else if(it==Item.getItemFromBlock(Blocks.PLANKS))add=16;
   if(add>0){energy+=add;s.shrink(1);if(it==Items.LAVA_BUCKET)insert(new ItemStack(Items.BUCKET));return true;}
  }return false;
 }

 private boolean mineForward(){
  BlockPos next=pos.offset(direction);
  // Se houver buraco no chão da próxima posição, tenta preencher antes de avançar.
  if(world.isAirBlock(next.down()) && !placeBridgeBlock(next.down())) return false;
  if(!breakBlock(next)||!breakBlock(next.up()))return false;
  moveTo(next);
  if(rails)placeRail(pos.down());
  if(torches&&done%8==0)placeTorch(pos.up());
  return true;
 }

 private boolean mineDown(){
  BlockPos a=pos.down(), b=a.down();
  if(!breakBlock(a)||!breakBlock(b))return false;
  moveTo(a);
  return true;
 }

 private boolean breakBlock(BlockPos p){
  IBlockState st=world.getBlockState(p);
  if(world.isAirBlock(p))return true;
  if(st.getBlock()==Blocks.BEDROCK||st.getBlockHardness(world,p)<0)return false;
  for(ItemStack drop:st.getBlock().getDrops(world,p,st,0)) if(!insert(drop))return false;
  world.setBlockToAir(p); return true;
 }

 private boolean insert(ItemStack stack){
  for(int i=27;i<54&&!stack.isEmpty();i++){
   if(inv.get(i).isEmpty()){inv.get(i)=stack.copy();return true;}
   if(ItemStack.areItemsEqual(inv.get(i),stack)&&ItemStack.areItemStackTagsEqual(inv.get(i),stack)){
    int room=Math.min(getInventoryStackLimit(),inv.get(i).getMaxStackSize())-inv.get(i).getCount();
    int n=Math.min(room,stack.getCount());if(n>0){inv.get(i).grow(n);stack.shrink(n);}
   }
  }return stack.isEmpty();
 }

 private boolean placeBridgeBlock(BlockPos p){
  Item cobble=Item.getItemFromBlock(Blocks.COBBLESTONE);
  Item stone=Item.getItemFromBlock(Blocks.STONE);
  for(int i=27;i<54;i++){
   ItemStack s=inv.get(i);
   if(!s.isEmpty() && (s.getItem()==cobble || s.getItem()==stone)){
    Block b=Block.getBlockFromItem(s.getItem());
    world.setBlockState(p,b.getDefaultState());
    s.shrink(1);
    return true;
   }
  }
  return false;
 }
 private boolean take(Item item){
  for(int i=27;i<54;i++)if(!inv.get(i).isEmpty()&&inv.get(i).getItem()==item){inv.get(i).shrink(1);return true;}return false;
 }
 private void placeRail(BlockPos p){if(world.isAirBlock(p)&&take(Item.getItemFromBlock(Blocks.RAIL)))world.setBlockState(p,Blocks.RAIL.getDefaultState());}
 private void placeTorch(BlockPos p){if(world.isAirBlock(p)&&take(Item.getItemFromBlock(Blocks.TORCH)))world.setBlockState(p,Blocks.TORCH.getDefaultState());}

 private void moveTo(BlockPos n){
  IBlockState me=world.getBlockState(pos);
  world.setBlockState(n,me);
  TileEntity te=world.getTileEntity(n);
  if(te instanceof TileAutomaticMiner){
   TileAutomaticMiner m=(TileAutomaticMiner)te;
   m.inv=this.inv;m.running=running;m.down=down;m.rails=rails;m.torches=torches;m.target=target;m.done=done;m.energy=energy;m.direction=direction;
  }
  world.setBlockToAir(pos);
 }

 public void setTarget(int v){target=Math.max(1,Math.min(v,100000));done=0;markDirty();}
 public void toggle(){running=!running;markDirty();}

 @Override public NBTTagCompound writeToNBT(NBTTagCompound t){
  super.writeToNBT(t);t.setBoolean("run",running);t.setBoolean("down",down);t.setBoolean("rails",rails);t.setBoolean("torches",torches);
  t.setInteger("target",target);t.setInteger("done",done);t.setInteger("energy",energy);t.setInteger("dir",direction.getIndex());
  NBTTagList l=new NBTTagList();for(int i=0;i<54;i++)if(!inv.get(i).isEmpty()){NBTTagCompound s=new NBTTagCompound();s.setByte("slot",(byte)i);inv.get(i).writeToNBT(s);l.appendTag(s);}t.setTag("items",l);return t;
 }
 @Override public void readFromNBT(NBTTagCompound t){
  super.readFromNBT(t);running=t.getBoolean("run");down=t.getBoolean("down");rails=t.getBoolean("rails");torches=t.getBoolean("torches");
  target=t.getInteger("target");done=t.getInteger("done");energy=t.getInteger("energy");direction=EnumFacing.getFront(t.getInteger("dir"));
  for(int i=0;i<54;i++)inv.get(i)=ItemStack.EMPTY;NBTTagList l=t.getTagList("items",10);for(int j=0;j<l.tagCount();j++){NBTTagCompound s=l.getCompoundTagAt(j);inv.get(s.getByte("slot")&255)=new ItemStack(s);}
 }
 @Override public String getName(){return "Automatic Miner";}@Override public boolean hasCustomName(){return false;}
 @Override public int getSizeInventory(){return 54;}@Override public boolean isEmpty(){for(ItemStack s:inv)if(!s.isEmpty())return false;return true;}
 @Override public ItemStack getStackInSlot(int i){return inv.get(i);}
 @Override public ItemStack decrStackSize(int i,int c){return net.minecraft.inventory.ItemStackHelper.getAndSplit(inv,i,c);}
 @Override public ItemStack removeStackFromSlot(int i){return net.minecraft.inventory.ItemStackHelper.getAndRemove(inv,i);}
 @Override public void setInventorySlotContents(int i,ItemStack s){inv.get(i)=s;if(!s.isEmpty()&&s.getCount()>getInventoryStackLimit())s.setCount(getInventoryStackLimit());}
 @Override public int getInventoryStackLimit(){return 64;}@Override public boolean isUsableByPlayer(net.minecraft.entity.player.EntityPlayer p){return true;}
 @Override public void openInventory(net.minecraft.entity.player.EntityPlayer p){}@Override public void closeInventory(net.minecraft.entity.player.EntityPlayer p){}
 @Override public boolean isItemValidForSlot(int i,ItemStack s){return true;}@Override public int getField(int i){return 0;}@Override public void setField(int i,int v){}@Override public int getFieldCount(){return 0;}
 @Override public void clear(){for(int i=0;i<54;i++)inv.get(i)=ItemStack.EMPTY;}
}
