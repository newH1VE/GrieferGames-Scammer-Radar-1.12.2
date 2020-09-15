package de.newH1VE.griefergames.Events;

import de.newH1VE.griefergames.GrieferGames;
import net.labymod.core.LabyModCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;

import java.util.Collection;
import java.util.List;

public class OnKeyEvent {

    @SubscribeEvent
    public void onPress(InputEvent.KeyInputEvent event) {

        if (Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isPressed() && GrieferGames.getGrieferGames().isModEnabled() && GrieferGames.getGrieferGames().isTabListEnabled()) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    modifyTabList();

                }
            });

            thread.start();
        }
    }


    public void modifyTabList() {

        List<String> scammerList = GrieferGames.getAntiscammer().getScammerList();
        TextFormatting chatformat = GrieferGames.getGrieferGames().getChatformat();
        NetHandlerPlayClient nethandlerplayclient = LabyModCore.getMinecraft().getPlayer().connection;
        Collection<NetworkPlayerInfo> playerMap = nethandlerplayclient.getPlayerInfoMap();

        try {
            for (NetworkPlayerInfo player : playerMap) {
                boolean found = false;
                if (player.getDisplayName() != null) {
                    ITextComponent playerDisplayName = player.getDisplayName();

                    String oldMessage = playerDisplayName.getFormattedText().replaceAll("\u00A7", "§");
                    if (oldMessage.indexOf("§k") != -1) {
                        oldMessage = oldMessage.replaceAll("§k", "");
                        oldMessage = oldMessage.replaceAll("§", "\u00A7");
                        playerDisplayName = new TextComponentString(oldMessage);
                    }

                    if (playerDisplayName.getUnformattedText().startsWith("[AMP]")) {
                        playerDisplayName = playerDisplayName.getSiblings().get(playerDisplayName.getSiblings().size() - 1);
                    }

                    String fullItem = playerDisplayName.getUnformattedText();
                    String playerName = player.getGameProfile().getName();
                    ITextComponent newPlayerDisplayName = playerDisplayName;


                    for (String scammer : scammerList) {
                        if (playerName.equalsIgnoreCase(scammer)) {
                            found = true;
                            if (!fullItem.startsWith("[SCAMMER]")) {
                                ITextComponent befsign = new TextComponentString("[").setStyle(new Style().setColor(TextFormatting.YELLOW));
                                ITextComponent afsign = new TextComponentString("] ").setStyle(new Style().setColor(TextFormatting.YELLOW));
                                ITextComponent sign = new TextComponentString("SCAMMER").setStyle(new Style().setColor(chatformat).setBold(true));
                                newPlayerDisplayName = befsign.appendSibling(sign).appendSibling(afsign).appendSibling(playerDisplayName);
                            }
                        }
                    }

                    if (fullItem.startsWith("[SCAMMER]") && found == false) {
                        newPlayerDisplayName = playerDisplayName.getSiblings().get(playerDisplayName.getSiblings().size() - 1);
                    }
                    player.setDisplayName(newPlayerDisplayName);
                }
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

}
