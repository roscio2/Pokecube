/**
 *
 */
package pokecube.core.client.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.opengl.GL11;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import pokecube.core.Mod_Pokecube_Helper;
import pokecube.core.PokecubeItems;
import pokecube.core.mod_Pokecube;
import pokecube.core.client.Resources;
import pokecube.core.interfaces.IMoveNames;
import pokecube.core.interfaces.IPokemob;
import pokecube.core.interfaces.Move_Base;
import pokecube.core.moves.MovesUtils;
import pokecube.core.network.PokecubePacketHandler;
import pokecube.core.network.PokecubePacketHandler.PokecubeServerPacket;
import pokecube.core.utils.PokecubeSerializer;
import pokecube.core.utils.PokecubeSerializer.TeleDest;
import pokecube.core.utils.Vector4;
import thut.api.maths.Vector3;

/** @author Manchou */
public class GuiDisplayPokecubeInfo extends Gui
{
    protected FontRenderer                fontRenderer;
    protected Minecraft                   minecraft;
    protected static int                  lightGrey = 0xDDDDDD;
    private static GuiDisplayPokecubeInfo instance;

    /**
     *
     */
    public GuiDisplayPokecubeInfo()
    {
        minecraft = (Minecraft) mod_Pokecube.getMinecraftInstance();
        fontRenderer = minecraft.fontRendererObj;
        instance = this;
    }

    public static GuiDisplayPokecubeInfo instance()
    {
        return instance;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        try
        {
            if (minecraft.currentScreen == null
                    && !((Minecraft) mod_Pokecube.getMinecraftInstance()).gameSettings.hideGUI)
                draw(event);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    private void draw(RenderWorldLastEvent event)
    {
        int currentItemIndex = mod_Pokecube.getPlayer(null).inventory.currentItem;

        int h = Mod_Pokecube_Helper.guiOffset[0];
        int w = Mod_Pokecube_Helper.guiOffset[1];

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        RenderHelper.disableStandardItemLighting();

        GL11.glNormal3f(0.0F, -1.0F, 0.0F);
        minecraft.entityRenderer.setupOverlayRendering();

        IPokemob[] pokemobs = getPokemobsToDisplay();
        if (indexPokemob < 0)
        {
            indexPokemob = 0;
        }
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        minecraft.entityRenderer.setupOverlayRendering();

        if (indexPokemob > pokemobs.length)
        {
            refreshCounter = 0;
            indexPokemob = 0;
            arrayRet = getPokemobsToDisplay();
        }
        if (indexPokemob >= pokemobs.length)
        {
            indexPokemob = 0;
        }
        if (indexPokemob >= pokemobs.length) { return; }
        int i = 0;

        IPokemob pokemob = pokemobs[indexPokemob];
        int n = pokemobs.length;
        if (pokemob != null)
        {

            pokemob.setMoveIndex(pokemob.getMoveIndex());

            if (pokemob.getMoveIndex() == 5)
            {
                GL11.glColor4f(0.0F, 1.0F, 0.4F, 1.0F);
            }
            else
            {
                GL11.glColor4f(0.0F, 1.0F, 0.4F, 1.0F);
            }
            // bind texture
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            this.drawTexturedModalRect(0 + h, 0 + w, 0, 0, 91, 13);
            String displayName = pokemob.getPokemonDisplayName();
            if (fontRenderer.getStringWidth(displayName) > 70)
            {

            }
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            fontRenderer.drawString(displayName, 2 + h, 2 + w, lightGrey);
            int moveIndex = 0;
            int index2 = 0;

            // Draw number of pokemon
            minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
            int num = fontRenderer.getStringWidth("" + n);
            this.drawTexturedModalRect(90 + h, 0 + w, 0, 0, num, 13);
            this.drawTexturedModalRect(90 + num + h, 0 + w, 81, 0, 10, 13);
            fontRenderer.drawString("" + n, 95 + h, 3 + w, lightGrey);

            pokemob.setMoveIndex(pokemob.getMoveIndex());

            for (moveIndex = 0; moveIndex < 4; moveIndex++)
            {
                Move_Base move = MovesUtils.getMoveFromName(pokemob.getMove(moveIndex));

                if (move != null)
                {
                    index2++;
                    if (pokemob.getMoveIndex() == moveIndex) GL11.glColor4f(0F, 0.1F, 1.0F, 1.0F);
                    else GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    // bind texture

                    minecraft.renderEngine.bindTexture(Resources.GUI_BATTLE);
                    this.drawTexturedModalRect(0 + h, 13 + 12 * moveIndex + w, 0, 13, 91, 12);
                    GL11.glPushMatrix();
                    Color moveColor = new Color(move.getType().colour);// TODO
                                                                       // see
                                                                       // why
                                                                       // both
                                                                       // of
                                                                       // these
                                                                       // colour
                                                                       // methods
                                                                       // are
                                                                       // needed.
                    GL11.glColor4f(moveColor.getRed() / 255f, moveColor.getGreen() / 255f, moveColor.getBlue() / 255f,
                            1.0F);
                    fontRenderer.drawString(MovesUtils.getTranslatedMove(move.getName()), 5 + 0 + h,
                            moveIndex * 12 + 14 + w, // white.getRGB());
                            move.getType().colour);
                    GL11.glPopMatrix();
                }
            }

        }
        i++;

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        RenderHelper.disableStandardItemLighting();

    }

    IPokemob[] arrayRet       = new IPokemob[0];
    int        refreshCounter = 0;

    public IPokemob[] getPokemobsToDisplay()
    {
        if (refreshCounter++ > 5)
        {
            refreshCounter = 0;
        }
        if (refreshCounter > 0) return arrayRet;

        EntityPlayer player = minecraft.thePlayer;
        List pokemobs = minecraft.theWorld.getLoadedEntityList();

        List<IPokemob> ret = new ArrayList<IPokemob>();
        int i = 0;
        for (Object object : pokemobs)
        {
            if (!(object instanceof IPokemob)) continue;
            IPokemob pokemob = (IPokemob) object;

            boolean owner = pokemob.getPokemonAIState(IPokemob.TAMED) && pokemob.getPokemonOwner() != null;

            if (owner)
            {
                owner = player.getEntityId() == pokemob.getPokemonOwner().getEntityId();
            }

            if (owner && !pokemob.getPokemonAIState(IPokemob.SITTING) && !pokemob.getPokemonAIState(IPokemob.GUARDING)
                    && !pokemob.getPokemonAIState(IPokemob.STAYING))
            {
                ret.add(pokemob);
            }
            // if (ret.size()>=5)
            // break;
        }
        arrayRet = ret.toArray(new IPokemob[ret.size()]);
        Arrays.sort(arrayRet, new Comparator<IPokemob>()
        {

            @Override
            public int compare(IPokemob o1, IPokemob o2)
            {
                Entity e1 = (Entity) o1;
                Entity e2 = (Entity) o2;

                if (e1.ticksExisted == e2.ticksExisted)
                {
                    if (o2.getLevel() == o1.getLevel())
                        return (o1.getPokemonDisplayName().compareTo(o2.getPokemonDisplayName()));
                    return o2.getLevel() - o1.getLevel();
                }
                return e1.ticksExisted - e2.ticksExisted;
            }
        });
        // Arrays.sort(arrayRet, PokemobAIThread.pokemobComparator);

        return arrayRet;
    }

    private int getColorForItem(ItemStack itemStack)
    {
        int color = lightGrey;

        if (itemStack.getItem() == PokecubeItems.pokemobEgg)
        {
            color = 0x78C848;
        }
        else if (itemStack.getItem() == PokecubeItems.getItem("pokecubeFilled"))
        {
            color = 0xEE0000;
        }
        else if (itemStack.getItem() == PokecubeItems.getItem("greatcubeFilled"))
        {
            color = 0x0B90CE;
        }
        else if (itemStack.getItem() == PokecubeItems.getItem("ultracubeFilled"))
        {
            color = 0xDCA937;
        }
        else if (itemStack.getItem() == PokecubeItems.getItem("mastercubeFilled"))
        {
            color = 0x332F6A;
        }

        return color;
    }

    int indexPokemob = 0;

    public void nextPokemob()
    {
        indexPokemob++;
        if (indexPokemob >= arrayRet.length) indexPokemob = 0;
    }

    public void previousPokemob()
    {
        indexPokemob--;
        if (indexPokemob < 0) indexPokemob = arrayRet.length - 1;
    }

    public void nextMove()
    {

        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null)
        {
            int index = (pokemob.getMoveIndex() + 1);
            if (index == 4) index = 5;
            if (index > 5) index = 0;
            pokemob.setMoveIndex(index);
        }
    }

    public void previousMove()
    {

        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null)
        {
            int index = pokemob.getMoveIndex();

            if (index == 5)
            {
                for (int i = 3; i > 0; i--)
                {
                    if (pokemob.getMove(i) != null)
                    {
                        index = i;
                        break;
                    }
                }
            }
            else
            {
                index--;
            }

            if (index % 5 >= 0) pokemob.setMoveIndex(index % 5);
            else pokemob.setMoveIndex(5);
        }
    }

    public void setMove(int num)
    {
        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null)
        {
            int index = num;
            if (index % 4 >= 0) pokemob.setMoveIndex(index % 4);
        }
    }

    public void pokemobBack()
    {
        IPokemob pokemob = getCurrentPokemob();
        if (pokemob != null) pokemob.returnToPokecube();
        else
        {
            EntityPlayer player = minecraft.thePlayer;
            Vector3 look = Vector3.getNewVectorFromPool().set(player.getLook(1));
            Vector3 temp = Vector3.getNewVectorFromPool().set(player).addTo(0, player.getEyeHeight(), 0);

            Entity target = temp.firstEntityExcluding(32, look, player.worldObj, false, player);
            temp.freeVectorFromPool();
            look.freeVectorFromPool();
            if (target != null && target instanceof IPokemob && ((IPokemob) target).getPokemonOwner() == player)
            {
                ((IPokemob) target).returnToPokecube();
            }
        }

        if (indexPokemob >= arrayRet.length) indexPokemob--;

        if (indexPokemob < 0) indexPokemob = 0;

    }

    public void pokemobAttack()
    {
        byte[] message = { (byte) 21, (byte) indexPokemob };

        EntityPlayer player = minecraft.thePlayer;
        Vector3 look = Vector3.getNewVectorFromPool().set(player.getLook(1));
        Vector3 temp = Vector3.getNewVectorFromPool().set(player).addTo(0, player.getEyeHeight(), 0);
        PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
        buffer.writeBytes(message);

        Entity target = temp.firstEntityExcluding(32, look, player.worldObj, true, player);

        temp.freeVectorFromPool();
        look.freeVectorFromPool();
        buffer.writeInt(target != null ? target.getEntityId() : 0);
        IPokemob targetPoke = null;
        boolean sameOwner = false;
        if (target instanceof IPokemob)
        {
            targetPoke = (IPokemob) target;
            sameOwner = ((IPokemob) target).getPokemonOwner() == player;
        }

        IPokemob pokemob = (IPokemob) getCurrentPokemob();

        if (pokemob != null)
        {
            if (pokemob.getMove(pokemob.getMoveIndex()) == null)
            {
                look.freeVectorFromPool();
                return;
            }

            if (target != null && !minecraft.thePlayer.isSneaking() && !sameOwner) pokemob.displayMessageToOwner(
                    "You tell " + pokemob.getPokemonDisplayName() + " to attack " + target.getCommandSenderName());
            buffer.writeInt(((Entity) pokemob).getEntityId());
            if (pokemob.getMove(pokemob.getMoveIndex()).equalsIgnoreCase(IMoveNames.MOVE_TELEPORT))
            {
                if (!GuiScrollableLists.instance().getState())
                {
                    GuiScrollableLists.instance().setState(true);
                    return;
                }
                else
                {
                    GuiScrollableLists.instance().setState(false);

                    Minecraft minecraft = (Minecraft) mod_Pokecube.getMinecraftInstance();
                    List<TeleDest> locations = PokecubeSerializer.getInstance()
                            .getTeleports(minecraft.thePlayer.getUniqueID().toString());

                    if (locations.size() > 0)
                    {
                        Vector4 location = locations
                                .get((GuiScrollableLists.instance().indexLocation) % locations.size()).loc;
                        buffer.writeInt((int) location.w);
                        buffer.writeFloat(location.x);
                        buffer.writeFloat(location.y);
                        buffer.writeFloat(location.z);
                    }
                }
            }
        }
        PokecubeServerPacket packet = PokecubePacketHandler.makeServerPacket(PokecubePacketHandler.CHANNEL_ID_STATS,
                buffer.array());
        PokecubePacketHandler.sendToServer(packet);
    }

    public void pokemobStance()
    {
        byte[] message = { (byte) 22 };
        PokecubeServerPacket packet = PokecubePacketHandler.makeServerPacket(PokecubePacketHandler.CHANNEL_ID_STATS,
                message);
        PokecubePacketHandler.sendToServer(packet);
    }

    public IPokemob getCurrentPokemob()
    {
        IPokemob pokemob = null;
        if (indexPokemob < arrayRet.length && indexPokemob >= 0 && arrayRet.length > 0)
        {
            pokemob = arrayRet[indexPokemob];
        }
        return pokemob;
    }

    public void moveGui(int x, int y)
    {
        Mod_Pokecube_Helper.guiOffset[0] += x;
        Mod_Pokecube_Helper.guiOffset[1] += y;
        if (Mod_Pokecube_Helper.guiOffset[0] < 0) Mod_Pokecube_Helper.guiOffset[0] = 0;
        if (Mod_Pokecube_Helper.guiOffset[1] < 0) Mod_Pokecube_Helper.guiOffset[1] = 0;
        saveConfig();
    }

    private void saveConfig()
    {
        Configuration config = Mod_Pokecube_Helper.config;
        config.load();

        config.get(Mod_Pokecube_Helper.CATEGORY_ADVANCED, "guiOffset", Mod_Pokecube_Helper.guiOffset,
                "offset of pokemon moves gui.").set(Mod_Pokecube_Helper.guiOffset);

        config.save();
    }
}