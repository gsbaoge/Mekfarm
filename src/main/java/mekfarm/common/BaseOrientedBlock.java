package mekfarm.common;

import mekfarm.MekfarmMod;
import net.darkhax.tesla.api.ITeslaHolder;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

/**
 * Created by CF on 2016-11-02.
 */
public abstract class BaseOrientedBlock<T extends TileEntity> extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    private String blockId;
    private Class<T> teClass;
    private int guiId;

    protected BaseOrientedBlock(String blockId, Class<T> teClass, int guiId) {
        super(Material.ROCK);
        this.blockId = blockId;
        this.teClass = teClass;
        this.guiId = guiId;

        this.setRegistryName(blockId);
        this.setUnlocalizedName(MekfarmMod.MODID + "." + blockId);
        this.setCreativeTab(CreativeTabs.FOOD);

        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    public void register() {
        GameRegistry.register(this);
        GameRegistry.register(new ItemBlock(this), this.getRegistryName());
        GameRegistry.registerTileEntity(this.teClass, this.getRegistryName() + "_tile");
    }

    @SideOnly(Side.CLIENT)
    public void registerRenderer() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this)
                , 0
                , new ModelResourceLocation(this.getRegistryName(), "inventory")
        );
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        try {
            return this.teClass.newInstance();
        } catch (InstantiationException e) {
            MekfarmMod.logger.error(e);
            return null;
        } catch (IllegalAccessException e) {
            MekfarmMod.logger.error(e);
            return null;
        }
    }

    protected T getTileEntity(World world, BlockPos pos) {
        TileEntity entity = world.getTileEntity(pos);
        try {
            return this.teClass.cast(entity);
        }
        catch (ClassCastException ex) {
            MekfarmMod.logger.warn("Error trying to get tile entity at position: " + pos.toString() + ". [" + ex.getMessage() + "]");
            return null;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            T entity = this.getTileEntity(world, pos);
            if (entity != null) {
                ITeslaHolder tesla = entity.getCapability(TeslaCapabilities.CAPABILITY_HOLDER, side);
                if (tesla != null) {
                    player.addChatMessage(new TextComponentString(TextFormatting.GREEN + "Stored Energy: " + tesla.getStoredPower()));
                }
            }
        }

        // Only execute on the server
        if (!world.isRemote && (this.guiId > 0)) {
            player.openGui(MekfarmMod.instance, this.guiId, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, getFacingFromEntity(pos, placer)), 2);
    }

    public static EnumFacing getFacingFromEntity(BlockPos clickedBlock, EntityLivingBase entity) {
        return EnumFacing.getFacingFromVector((float) (entity.posX - clickedBlock.getX()), 0, (float) (entity.posZ - clickedBlock.getZ()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);
        if (enumfacing.getAxis() == EnumFacing.Axis.Y) { enumfacing = EnumFacing.NORTH; }
        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity t = worldIn.getTileEntity(pos);
        if ((t != null) && (t.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null))) {
            IItemHandler handler = t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (handler != null) {
                for (int i = 0; i < handler.getSlots(); ++i) {
                    ItemStack stack = handler.getStackInSlot(i);
                    if ((stack != null) && (stack.stackSize > 0))
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), handler.getStackInSlot(i));
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }
}