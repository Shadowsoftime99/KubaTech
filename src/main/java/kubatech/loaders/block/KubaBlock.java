package kubatech.loaders.block;

import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.builder.UIBuilder;
import com.gtnewhorizons.modularui.common.builder.UIInfo;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import kubatech.loaders.BlockLoader;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class KubaBlock extends Block {

    public static final Function<IModularUIContainerCreator, UIInfo<?, ?>> TileEntityUIFactory =
            containerConstructor -> UIBuilder.of()
                    .container((player, world, x, y, z) -> {
                        TileEntity te = world.getTileEntity(x, y, z);
                        if (te instanceof ITileWithModularUI) {
                            UIBuildContext buildContext = new UIBuildContext(player);
                            ModularWindow window = ((ITileWithModularUI) te).createWindow(buildContext);
                            return containerConstructor.createUIContainer(
                                    new ModularUIContext(buildContext, te::markDirty), window);
                        }
                        return null;
                    })
                    .gui(((player, world, x, y, z) -> {
                        if (!world.isRemote) return null;
                        TileEntity te = world.getTileEntity(x, y, z);
                        if (te instanceof ITileWithModularUI) {
                            UIBuildContext buildContext = new UIBuildContext(player);
                            ModularWindow window = ((ITileWithModularUI) te).createWindow(buildContext);
                            return new ModularGui(containerConstructor.createUIContainer(
                                    new ModularUIContext(buildContext, te::markDirty), window));
                        }
                        return null;
                    }))
                    .build();

    public static final UIInfo<?, ?> defaultTileEntityUI = TileEntityUIFactory.apply(ModularUIContainer::new);

    static final HashMap<Integer, BlockProxy> blocks = new HashMap<>();
    private static int idCounter = 0;

    public KubaBlock(Material p_i45394_1_) {
        super(p_i45394_1_);
    }

    public ItemStack registerProxyBlock(BlockProxy block) {
        blocks.put(idCounter, block);
        block.itemInit(idCounter);
        return new ItemStack(BlockLoader.kubaItemBlock, 1, idCounter++);
    }

    private BlockProxy getBlock(int id) {
        return blocks.get(id);
    }

    @Override
    public boolean hasTileEntity(int meta) {
        return getBlock(meta) instanceof IProxyTileEntityProvider;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void getSubBlocks(Item p_149666_1_, CreativeTabs p_149666_2_, List p_149666_3_) {
        for (int i = 0; i < blocks.size(); i++) p_149666_3_.add(new ItemStack(p_149666_1_, 1, i));
    }

    @Override
    public void registerBlockIcons(IIconRegister p_149651_1_) {
        blocks.values().forEach(b -> b.registerIcon(p_149651_1_));
    }

    @Override
    public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
        return blocks.get(p_149691_2_).getIcon(p_149691_1_);
    }

    @Override
    public String getLocalizedName() {
        return "KUBABLOCK";
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        if (!hasTileEntity(metadata)) return null;
        return ((IProxyTileEntityProvider) getBlock(metadata)).createTileEntity(world);
    }

    @Override
    public boolean onBlockActivated(
            World p_149727_1_,
            int p_149727_2_,
            int p_149727_3_,
            int p_149727_4_,
            EntityPlayer p_149727_5_,
            int p_149727_6_,
            float p_149727_7_,
            float p_149727_8_,
            float p_149727_9_) {
        return getBlock(p_149727_1_.getBlockMetadata(p_149727_2_, p_149727_3_, p_149727_4_))
                .onActivated(p_149727_1_, p_149727_2_, p_149727_3_, p_149727_4_, p_149727_5_);
    }

    @Override
    public void onBlockPlacedBy(
            World p_149689_1_,
            int p_149689_2_,
            int p_149689_3_,
            int p_149689_4_,
            EntityLivingBase p_149689_5_,
            ItemStack p_149689_6_) {
        getBlock(p_149689_6_.getItemDamage())
                .onBlockPlaced(p_149689_1_, p_149689_2_, p_149689_3_, p_149689_4_, p_149689_5_);
    }

    @FunctionalInterface
    public interface IModularUIContainerCreator {
        ModularUIContainer createUIContainer(ModularUIContext context, ModularWindow mainWindow);
    }

    @FunctionalInterface
    public interface IModularUIProvider {
        UIInfo<?, ?> getUI();
    }
}
