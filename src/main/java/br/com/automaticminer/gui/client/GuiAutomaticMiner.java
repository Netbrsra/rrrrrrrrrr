package br.com.automaticminer.gui.client;

import br.com.automaticminer.inventory.ContainerAutomaticMiner;
import br.com.automaticminer.tile.TileAutomaticMiner;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;

public class GuiAutomaticMiner extends GuiContainer {
 private final TileAutomaticMiner te;

 public GuiAutomaticMiner(InventoryPlayer p, TileAutomaticMiner t){
  super(new ContainerAutomaticMiner(p,t));
  te=t; xSize=176; ySize=255;
 }

 @Override public void initGui(){
  super.initGui();
  buttonList.clear();
  buttonList.add(new GuiButton(0, guiLeft+8, guiTop+8, 52, 18, te.running ? "PARAR" : "INICIAR"));
  buttonList.add(new GuiButton(1, guiLeft+64, guiTop+8, 50, 18, te.down ? "BAIXO" : "RETO"));
  buttonList.add(new GuiButton(2, guiLeft+118, guiTop+8, 50, 18, te.rails ? "TRILHO: ON" : "TRILHO: OFF"));
  buttonList.add(new GuiButton(3, guiLeft+8, guiTop+154, 20, 18, "-"));
  buttonList.add(new GuiButton(4, guiLeft+148, guiTop+154, 20, 18, "+"));
 }

 @Override protected void actionPerformed(GuiButton b){
  if(b.id==0) te.running=!te.running;
  if(b.id==1) te.down=!te.down;
  if(b.id==2) te.rails=!te.rails;
  if(b.id==3) te.setTarget(te.target-64);
  if(b.id==4) te.setTarget(te.target+64);
  if(b.id<=2) b.displayString=b.id==0?(te.running?"PARAR":"INICIAR"):b.id==1?(te.down?"BAIXO":"RETO"):(te.rails?"TRILHO: ON":"TRILHO: OFF");
 }

 @Override protected void drawGuiContainerBackgroundLayer(float partial,int mx,int my){
  drawDefaultBackground();
  drawGradientRect(guiLeft,guiTop,guiLeft+xSize,guiTop+ySize,0xFF202020,0xFF101010);
  drawRect(guiLeft+4,guiTop+26,guiLeft+172,guiTop+96,0xFF303030);
  drawRect(guiLeft+4,guiTop+103,guiLeft+172,guiTop+173,0xFF303030);
  fontRenderer.drawString("COMBUSTIVEL",guiLeft+8,guiTop+28,0xFFFFD54F);
  fontRenderer.drawString("ITENS MINERADOS",guiLeft+8,guiTop+105,0xFF80CBC4);
  fontRenderer.drawString("Quantidade: "+te.target,guiLeft+36,guiTop+159,0xFFFFFFFF);
  fontRenderer.drawString("Feitos: "+te.done+"  Energia: "+te.energy,guiLeft+8,guiTop+177,0xFFFFFFFF);
  fontRenderer.drawString("Seu inventario",guiLeft+8,guiTop+163,0xFFAAAAAA);
 }

 @Override protected void drawGuiContainerForegroundLayer(int mx,int my){
  fontRenderer.drawString("MINERADOR AUTOMATICO",8,8,0xFFFFFFFF);
 }
}