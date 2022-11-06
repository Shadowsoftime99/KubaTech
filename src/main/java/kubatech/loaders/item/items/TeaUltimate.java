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
import com.gtnewhorizons.modularui.api.drawable.ItemDrawable;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.widget.*;
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
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class TeaUltimate extends TeaCollection implements IItemProxyGUI {
    public TeaUltimate() {
        super("ultimate_tea");
    }

    private static String name = "";
    private static long timeCounter = 0;
    private static int colorCounter = 0;

    @Override
    public String getDisplayName(ItemStack stack) {
        if (!ModUtils.isClientSided) return super.getDisplayName(stack);
        if (checkTeaOwner(stack, Minecraft.getMinecraft().thePlayer.getCommandSenderName())) {
            long current = System.currentTimeMillis();
            if (current - timeCounter > 100) {
                timeCounter = current;
                name = StringUtils.applyRainbow(
                        "ULTIMATE", colorCounter++, EnumChatFormatting.BOLD.toString() + EnumChatFormatting.OBFUSCATED);
            }
            return String.format(super.getDisplayName(stack), name + EnumChatFormatting.RESET);
        }
        return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "" + EnumChatFormatting.ITALIC + "???????";
    }

    @Override
    public ModularWindow createWindow(ItemStack stack, EntityPlayer player) {
        ModularWindow.Builder builder = ModularWindow.builder(200, 150);
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
        final PlayerData playerData = PlayerDataManager.getPlayer(player.getCommandSenderName());
        builder.widget(new TabContainer()
                .setButtonSize(28, 32)
                .addTabButton(new TabButton(0)
                        .setBackground(false, ModularUITextures.VANILLA_TAB_TOP_START.getSubArea(0, 0, 1f, 0.5f))
                        .setBackground(true, ModularUITextures.VANILLA_TAB_TOP_START.getSubArea(0, 0.5f, 1f, 1f))
                        .setPos(0, -28))
                .addTabButton(new TabButton(1)
                        .setBackground(false, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0, 1f, 0.5f))
                        .setBackground(true, ModularUITextures.VANILLA_TAB_TOP_MIDDLE.getSubArea(0, 0.5f, 1f, 1f))
                        .setPos(28, -28))
                .addPage(new MultiChildWidget()
                        .addChild(new TextWidget(new Text("STATUS")
                                        .format(EnumChatFormatting.BOLD)
                                        .format(EnumChatFormatting.GOLD))
                                .setPos(10, 5))
                        .addChild(new DynamicTextWidget(
                                        () -> new Text("Tea: " + (playerData == null ? "ERROR" : playerData.teaAmount))
                                                .color(Color.GREEN.normal))
                                .setPos(20, 20)))
                .addPage(new MultiChildWidget()
                        .addChild(new TextWidget(new Text("EXCHANGE")
                                        .format(EnumChatFormatting.BOLD)
                                        .format(EnumChatFormatting.GOLD))
                                .setPos(10, 5))
                        .addChild(new ButtonWidget()
                                .setOnClick((Widget.ClickData clickData, Widget widget) -> {
                                    if (!(player instanceof EntityPlayerMP)) return;
                                    if (playerData == null || playerData.teaAmount < 50000L) return;
                                    playerData.teaAmount -= 50000L;
                                    playerData.markDirty();
                                    if (player.inventory.addItemStackToInventory(
                                            ItemList.TeaAcceptorResearchNote.get(1))) return;
                                    player.entityDropItem(ItemList.TeaAcceptorResearchNote.get(1), 0.5f);
                                })
                                .setBackground(new ItemDrawable().setItem(ItemList.TeaAcceptorResearchNote.get(1)))
                                .addTooltip("Tea Acceptor Research Note")
                                .addTooltip(new Text("Cost: 50000 Tea").color(Color.GREY.normal))
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
        }
    }
}
