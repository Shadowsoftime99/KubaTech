/*
 * KubaTech - Gregtech Addon
 * Copyright (C) 2022  kuba6000
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package kubatech.loaders.item.items;

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.*;
import java.text.NumberFormat;
import kubatech.api.enums.ItemList;
import kubatech.api.utils.ModUtils;
import kubatech.api.utils.StringUtils;
import kubatech.loaders.item.IItemProxyGUI;
import kubatech.savedata.PlayerData;
import kubatech.savedata.PlayerDataManager;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class TeaUltimate extends TeaCollection implements IItemProxyGUI {
    public TeaUltimate() {
        super("ultimate_tea");
    }

    private static String name = "";
    private static long timeCounter = 0;
    private static int colorCounter = 0;

    public static String getUltimateTeaDisplayName(String displayName) {
        long current = System.currentTimeMillis();
        if (current - timeCounter > 100) {
            timeCounter = current;
            name = StringUtils.applyRainbow(
                    "ULTIMATE", colorCounter++, EnumChatFormatting.BOLD.toString() + EnumChatFormatting.OBFUSCATED);
        }
        return String.format(displayName, name + EnumChatFormatting.RESET);
    }

    @Override
    public String getDisplayName(ItemStack stack) {
        if (!ModUtils.isClientSided) return super.getDisplayName(stack);
        if (checkTeaOwner(stack, Minecraft.getMinecraft().thePlayer.getCommandSenderName())) {
            return getUltimateTeaDisplayName(super.getDisplayName(stack));
        }
        return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "" + EnumChatFormatting.ITALIC + "???????";
    }

    @Override
    public ModularWindow createWindow(ItemStack stack, EntityPlayer player) {
        ModularWindow.Builder builder = ModularWindow.builder(200, 150);
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
        final PlayerData playerData = PlayerDataManager.getPlayer(player.getCommandSenderName());
        IDrawable tab1 = new ItemDrawable(ItemList.LegendaryUltimateTea.get(1)).withFixedSize(18, 18, 4, 6);
        IDrawable tab2 = new ItemDrawable(new ItemStack(Blocks.crafting_table)).withFixedSize(18, 18, 4, 6);
        IDrawable tab3 = new ItemDrawable(new ItemStack(Items.golden_apple)).withFixedSize(18, 18, 4, 6);
        builder.widget(new TabContainer()
                .setButtonSize(28, 32)
                .addTabButton(new TabButton(0)
                        .setBackground(false, ModularUITextures.VANILLA_TAB_TOP_START.getSubArea(0, 0, 1f, 0.5f), tab1)
                        .setBackground(true, ModularUITextures.VANILLA_TAB_TOP_START.getSubArea(0, 0.5f, 1f, 1f), tab1)
                        .setPos(0, -28))
                .addTabButton(new TabButton(1)
                        .setBackground(false, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0, 1f, 0.5f), tab2)
                        .setBackground(true, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0.5f, 1f, 1f), tab2)
                        .setPos(28, -28))
                .addTabButton(new TabButton(2)
                        .setBackground(false, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0, 1f, 0.5f), tab3)
                        .setBackground(true, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0.5f, 1f, 1f), tab3)
                        .setPos(56, -28))
                .addPage(new MultiChildWidget()
                        .addChild(new TextWidget(new Text("STATUS")
                                        .format(EnumChatFormatting.BOLD)
                                        .format(EnumChatFormatting.GOLD)
                                        .shadow())
                                .setPos(10, 5))
                        .addChild(new DynamicTextWidget(() -> new Text("Tea: "
                                                + (playerData == null
                                                        ? "ERROR"
                                                        : NumberFormat.getInstance()
                                                                .format(playerData.teaAmount)))
                                        .color(Color.GREEN.normal))
                                .setPos(20, 20)))
                .addPage(new MultiChildWidget()
                        .addChild(new TextWidget(new Text("EXCHANGE")
                                        .format(EnumChatFormatting.BOLD)
                                        .format(EnumChatFormatting.GOLD)
                                        .shadow())
                                .setPos(10, 5))
                        .addChild(new ButtonWidget()
                                .setOnClick((Widget.ClickData clickData, Widget widget) -> {
                                    if (!(player instanceof EntityPlayerMP)) return;
                                    if (playerData == null || playerData.teaAmount < 50_000L) return;
                                    playerData.teaAmount -= 50_000L;
                                    playerData.markDirty();
                                    if (player.inventory.addItemStackToInventory(
                                            ItemList.TeaAcceptorResearchNote.get(1))) return;
                                    player.entityDropItem(ItemList.TeaAcceptorResearchNote.get(1), 0.5f);
                                })
                                .setBackground(new ItemDrawable().setItem(ItemList.TeaAcceptorResearchNote.get(1)))
                                .addTooltip("Tea Acceptor Research Note")
                                .addTooltip(new Text("Cost: "
                                                + NumberFormat.getInstance().format(50_000) + " Tea")
                                        .color(Color.GRAY.normal))
                                .setPos(20, 20)))
                .addPage(new MultiChildWidget()
                        .addChild(new TextWidget(new Text("BENEFITS")
                                        .format(EnumChatFormatting.BOLD)
                                        .format(EnumChatFormatting.GOLD)
                                        .shadow())
                                .setPos(10, 5))
                        .addChild(new ButtonWidget()
                                .setOnClick((Widget.ClickData clickData, Widget widget) -> {
                                    if (!(player instanceof EntityPlayerMP)) return;
                                    if (playerData == null) return;
                                    playerData.autoRegen = !playerData.autoRegen;
                                    playerData.markDirty();
                                })
                                .setBackground(new ItemDrawable().setItem(new ItemStack(Items.potionitem, 1, 8193)))
                                .addTooltip("Regeneration I")
                                .addTooltip("For 1 minute")
                                .addTooltip(new Text("Cost: "
                                                + NumberFormat.getInstance().format(75_000) + " Tea")
                                        .color(Color.GRAY.normal))
                                // .addTooltip( //Find a way to run that on server, or different approach
                                //        new Text("Autobuy: " + (playerData == null ? "ERROR" : playerData.autoRegen))
                                //                .color(Color.GREY.normal))
                                .setPos(20, 20))));
        return builder.build();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer entity) {
        if (world.isRemote) return stack;
        if (!(entity instanceof EntityPlayerMP)) return stack;
        if (!checkTeaOwner(stack, entity.getCommandSenderName())) return stack;
        openHeldItemGUI(entity);
        return stack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrentItem) {
        if (world.isRemote) return;
        if (!(entity instanceof EntityPlayerMP)) return;
        super.onUpdate(stack, world, entity, slot, isCurrentItem);
        if (checkTeaOwner(stack, entity.getCommandSenderName())) {
            PlayerData playerData = PlayerDataManager.getPlayer(entity.getCommandSenderName());
            if (playerData == null) return;
            playerData.teaAmount++;
            playerData.markDirty();

            if (playerData.autoRegen && playerData.teaAmount > 75_000) {
                if (((EntityPlayerMP) entity).getActivePotionEffect(Potion.regeneration) == null) {
                    ((EntityPlayerMP) entity).addPotionEffect(new PotionEffect(Potion.regeneration.id, 1200, 0, true));
                    playerData.teaAmount -= 75_000;
                }
            }
        }
    }
}
