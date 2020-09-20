package de.newH1VE.griefergames.chat;

import static net.labymod.utils.ModColor.BOLD;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import de.newH1VE.griefergames.antiScammer.Scammer;
import net.labymod.core.LabyModCore;
import net.labymod.utils.ModColor;
import net.labymod.utils.UUIDFetcher;

public class AntiScammer extends Chat {
    ModColor modcolor = null;
    TextFormatting chatformat = null;


    ITextComponent resetMsg = new TextComponentString(" ")
            .setStyle(new Style().setColor(TextFormatting.RESET));


    // regex for message/payment idenfication
    private static Pattern msgUserGlobalChatRegex = Pattern.compile("^([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})");
    private static Pattern msgUserGlobalChatClanRegex = Pattern
            .compile("^(\\[[^\\]]+\\])\\s([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})\\s\\u00bb");
    private static Pattern privateMessageRegex = Pattern
            .compile("^\\[([A-Za-z\\-]+\\+?) \\\u2503 ((\\u007E)?\\w{1,16}) -> mir\\](.*)$");
    private static Pattern privateMessageSentRegex = Pattern
            .compile("^\\[mir -> ([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})\\](.*)$");
    private static Pattern playerPaymentReceiveRegexp = Pattern.compile(
            "^([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16}) hat dir \\$((?:[1-9]\\d{0,2}(?:,\\d{1,3})*|0)(?:\\.\\d+)?) gegeben\\.$");
    private static Pattern antiMagixPrefixRegex = Pattern.compile("([A-Za-z\\-]+\\+?) \\u2503 ((\\u007E)?\\w{1,16})");


    List<String> scammerList = new ArrayList<String>();
    List<Scammer> onlineScammerList = new ArrayList<Scammer>();
    List<Scammer> localScammerList = new ArrayList<Scammer>();

    private static final File onlineScammerFile = new File("LabyMod/antiScammer/onlineScammer.json");
    private static final File localScammerFile = new File("LabyMod/antiScammer/localScammer.json");

    public AntiScammer() {
        // do nothing :D
    }

    public void setScammerList(List<String> scammerList) {
        this.scammerList = scammerList;
    }

    public List<Scammer> getOnlineScammerList() {
        return onlineScammerList;
    }

    public void setOnlineScammerList(List<Scammer> onlineScammerList) {
        this.onlineScammerList = onlineScammerList;
    }

    public List<Scammer> getLocalScammerList() {
        return localScammerList;
    }

    public void setLocalScammerList(List<Scammer> localScammerList) {
        this.localScammerList = localScammerList;
    }

    public List<String> getScammerList() {
        return scammerList;
    }

    @Override
    public String getName() {
        return "antiScammer";
    }

    @Override
    public boolean doAction(String unformatted, String formatted) {
        return true;
    }

    @Override
    public boolean commandMessage(String unformatted) {
        return true;
    }


    @Override
    public boolean doActionModifyChatMessage(ITextComponent msg) {


        String unformatted = msg.getUnformattedText();
        String formatted = msg.getFormattedText();

        return doAction(unformatted, formatted);
    }

    @Override
    public ITextComponent modifyChatMessage(ITextComponent msg) {

        modcolor = getGG().getPrefixcolor();
        chatformat = getGG().getChatformat();
        String prefix = ModColor.RESET + " " + ModColor.GOLD + " [" + ModColor.RESET + "" + modcolor + "" + BOLD
                + "SCAMMER" + ModColor.RESET + " " + ModColor.GOLD + "] " + ModColor.RESET + "";

        String unformatted = msg.getUnformattedText();
        String formatted = msg.getFormattedText();

        ITextComponent newMsg = new TextComponentString("");

        if (doAction(unformatted, formatted)) {
            String userName = "";

            if (isGlobalMessage(unformatted)) {
                userName = getUserFromGlobalMessage(unformatted);
            }

            if (isPrivateMessage(unformatted)) {

                userName = getUserFromPrivateMessage(unformatted);
            }

            if (userName.trim().length() > 0 && scammerList.contains(userName.toLowerCase())) {

                ITextComponent befScammerMsg = new TextComponentString("[")
                        .setStyle(new Style().setColor(TextFormatting.GOLD));
                ITextComponent scammerMsg = new TextComponentString("SCAMMER")
                        .setStyle(new Style().setColor(chatformat).setBold(true));
                ITextComponent aftScammerMsg = new TextComponentString("] ")
                        .setStyle(new Style().setColor(TextFormatting.GOLD));
                ITextComponent scammersign = new TextComponentString("").appendSibling(befScammerMsg)
                        .appendSibling(scammerMsg).appendSibling(aftScammerMsg);


                if (isGlobalMessage(unformatted) && getGG().isMessageEnabled()) {
                    getApi().displayMessageInChat(
                            prefix + ModColor.YELLOW + BOLD + "Der Spieler " + ModColor.DARK_RED + BOLD + userName
                                    + ModColor.YELLOW + BOLD + " ist als Scammer hinterlegt. Achtung! " + prefix);
                }

                if (getGG().isPrefixEnabled()) {
                    newMsg.appendSibling(scammersign).appendSibling(resetMsg).appendSibling(msg);

                    return newMsg;
                }

            }

            return msg;
        }

        return super.modifyChatMessage(msg);
    }

    @Override
    public boolean doActionCommandMessage(String unformatted) {

        if (unformatted.toLowerCase().startsWith("/scammer reload")) {
            printPrefixLine();
            getApi().displayMessageInChat(ModColor.WHITE + "Liste wird geladen, bitte warten...");
            Thread thread = new Thread() {
                public void run() {
                    try {

                        onlineScammerList = getHelper().loadScammerFile(onlineScammerFile);
                        localScammerList = getHelper().loadScammerFile(localScammerFile);
                        scammerList = getHelper().joinScammerLists();

                        getApi().displayMessageInChat(ModColor.WHITE + "Liste wurde neu geladen.");
                        printPrefixLine();

                    } catch (Exception e) {
                        getApi().displayMessageInChat(ModColor.WHITE + "Liste konnte nicht geladen werden.");
                        printPrefixLine();

                        System.err.println(e);
                    }
                }
            };

            thread.start();

            return true;
        } else if (unformatted.toLowerCase().startsWith("/scammer update")) {
            printPrefixLine();
            getApi().displayMessageInChat(ModColor.WHITE + "Liste wird aktualisiert, bitte warten...");
            printPrefixLine();

            try {

                getHelper().updateScammerLists();
                scammerList = getHelper().joinScammerLists();

                printPrefixLine();
                getApi().displayMessageInChat(ModColor.WHITE + "Liste wurde aktualisiert.");
                printPrefixLine();

            } catch (Exception e) {
                printPrefixLine();
                getApi().displayMessageInChat(ModColor.WHITE + "Liste konnte nicht aktualisiert werden.");
                printPrefixLine();
                System.err.println(e);

            }

            return true;
        } else if (unformatted.toLowerCase().startsWith("/scammer add")) {
            String[] commandArray = unformatted.split(" ");
            printPrefixLine();
            if (commandArray.length > 3) {

                getApi().displayMessageInChat(
                        ModColor.WHITE + "Bitte immer nur einen Namen angeben (/scammer add NAME)");
                printPrefixLine();
                return true;
            } else if (commandArray.length == 3) {
                final String playerName = commandArray[2].trim();
                if (scammerList.contains(playerName.toLowerCase())) {

                    getApi().displayMessageInChat(ModColor.WHITE + "Dieser Scammer ist bereits hinterlegt!");
                    printPrefixLine();
                    return true;
                } else {
                    final UUID playerUUID = UUIDFetcher.getUUID(playerName);
                    if (playerUUID != null) {
                        getApi().displayMessageInChat(
                                ModColor.WHITE + "UUID f\u00FCr " + playerName + " wird ermittelt, bitte warten...");

                        Thread thread = new Thread() {
                            public void run() {
                                try {
                                    scammerList.add(playerName.toLowerCase());
                                    localScammerList.add(new Scammer(playerName, playerUUID.toString()));

                                    for (Scammer scammer : localScammerList) {
                                        System.out.println(scammer.name);
                                    }

                                    getHelper().saveScammerFile(localScammerList, localScammerFile);

                                    getApi().displayMessageInChat(
                                            ModColor.WHITE + playerName + " wurde als Scammer hinterlegt!");
                                    printPrefixLine();
                                } catch (Exception e) {
                                    getApi().displayMessageInChat(
                                            ModColor.WHITE + playerName + " konnte nicht hinterlegt werden.");
                                    printPrefixLine();
                                    System.err.println(e);
                                }
                            }
                        };

                        thread.start();
                        return true;
                    } else {
                        getApi().displayMessageInChat(
                                ModColor.WHITE + "Der Spielername " + playerName + " konnte nicht gefunden werden!");
                        printPrefixLine();
                        return true;
                    }
                }
            } else {
                getApi().displayMessageInChat(ModColor.WHITE + "Bitte einen Namen angeben (/scammer add NAME)");
                printPrefixLine();
                return true;
            }
        } else if (unformatted.toLowerCase().startsWith("/scammer remove")) {
            String[] commandArray = unformatted.split(" ");
            printPrefixLine();
            if (commandArray.length > 3) {
                getApi().displayMessageInChat(
                        ModColor.WHITE + "Bitte immer nur einen Namen angeben (/scammer remove NAME)");
                printPrefixLine();
            } else if (commandArray.length == 3) {
                final String playerName = commandArray[2].trim();
                if (scammerList.contains(playerName.toLowerCase())) {
                    final UUID playerUUID = UUIDFetcher.getUUID(playerName);
                    if (playerUUID != null) {
                        int localListIndex = -1;
                        for (Scammer scammer : localScammerList) {
                            if (scammer.name.equalsIgnoreCase(playerName.toLowerCase())) {
                                localListIndex = localScammerList.indexOf(scammer);
                            }
                        }

                        if (localListIndex < 0) {
                            getApi().displayMessageInChat(ModColor.WHITE + playerName
                                    + " kann nicht gel\u00F6scht werden, da er Online als Scammer hinterlegt wurde!");
                            printPrefixLine();
                        } else {
                            getApi().displayMessageInChat(
                                    ModColor.WHITE + playerName + " wird gel\u00F6scht, bitte warten...");

                            final int scammerListIndex = localListIndex;

                            Thread thread = new Thread() {
                                public void run() {
                                    try {
                                        scammerList.remove(playerName.toLowerCase());
                                        localScammerList.remove(scammerListIndex);

                                        getHelper().saveScammerFile(localScammerList, localScammerFile);

                                        getApi().displayMessageInChat(
                                                ModColor.WHITE + playerName + " wurde als Scammer entfernt!");
                                        printPrefixLine();
                                    } catch (Exception e) {
                                        getApi().displayMessageInChat(
                                                ModColor.WHITE + playerName + " konnte nicht entfernt werden.");
                                        printPrefixLine();
                                        System.err.println(e);
                                    }
                                }
                            };

                            thread.start();
                        }
                    } else {
                        getApi().displayMessageInChat(
                                ModColor.WHITE + "Der Spielername " + playerName + " konnte nicht gefunden werden!");
                        printPrefixLine();
                    }
                } else {
                    getApi().displayMessageInChat(
                            ModColor.WHITE + "Der Spielername " + playerName + " ist nicht auf der lokalen Liste vorhanden!");
                    printPrefixLine();
                }
            } else {
                getApi().displayMessageInChat(ModColor.WHITE + "Bitte einen Namen angeben (/scammer remove NAME)");
                printPrefixLine();
            }

            return true;
        } else if (unformatted.toLowerCase().startsWith("/scammer help")) {

            printPrefixLine();
            getHelper().printMenu();
            printPrefixLine();

            return true;

        } else if (unformatted.toLowerCase().startsWith("/scammer all")) {
            printPrefixLine();

            for (Scammer scammerobj : localScammerList) {
                getApi().displayMessageInChat(ModColor.WHITE + scammerobj.name);
            }

            printPrefixLine();

            return true;
        } else if (unformatted.toLowerCase().startsWith("/scammer check")) {
            printPrefixLine();
            String[] commandArray = unformatted.split(" ");
            if (commandArray.length > 3) {

                getApi().displayMessageInChat(
                        ModColor.WHITE + "Bitte immer nur einen Namen angeben (/scammer check NAMEoder sehe alle Scammer auf diesem Server über /scammer check *)");
                printPrefixLine();
            } else if (commandArray.length == 3) {
                getApi().displayMessageInChat(ModColor.WHITE + "Der eingegebene Name wird \u00FCberpr\u00FCft. Bitte warten ...");

                final String playerName = commandArray[2].trim();

                if (playerName.equals("*")) {
                    List<String> scammerOnServer = getHelper().getScammerOnServer();
                    if (scammerOnServer.size() > 0) {
                        getApi().displayMessageInChat(ModColor.WHITE + "Folgende Scammer befinden sich auf diesem Server:");
                        for (String scammerName : scammerOnServer) {
                            getApi().displayMessageInChat(ModColor.GOLD + "[" + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER"
                                    + ModColor.GOLD + "]" + ModColor.WHITE + " " + scammerName);

                        }
                        printPrefixLine();
                        return true;
                    }
                    if (scammerOnServer.size() == 0) {
                        getApi().displayMessageInChat(ModColor.WHITE + "Auf diesem Server befinden sich keine Scammer!");
                        printPrefixLine();
                        return true;
                    }
                } else {

                    try {
                        final UUID playerUUID = UUIDFetcher.getUUID(playerName);
                        //getApi().displayMessageInChat("Angefragt UUID: " + playerUUID);

                        for (Scammer scammer : onlineScammerList) {
                            if (scammer.uuid.equals(playerUUID.toString())) {
                                if (playerName.equalsIgnoreCase(scammer.name)) {
                                    scammer.name = playerName;
                                    getHelper().saveScammerFile(onlineScammerList, onlineScammerFile);

                                    getApi().displayMessageInChat(ModColor.WHITE + "Der Spieler" + playerName + " ist als " + ModColor.GOLD + "["
                                            + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER" + ModColor.GOLD + "]" + ModColor.WHITE
                                            + " auf der [SCAMMER]Radar Liste hinterlegt!");
                                    printPrefixLine();

                                    return true;
                                }

                                getApi().displayMessageInChat(ModColor.WHITE + "Der Spieler" + playerName + " ist als " + ModColor.GOLD + "["
                                        + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER" + ModColor.GOLD + "]" + ModColor.WHITE
                                        + " auf der [SCAMMER]Radar hinterlegt! Sein vorheriger Name war: "
                                        + scammer.name + ". Der Spielername wurde geupdated.");
                                printPrefixLine();

                                return true;
                            }
                        }

                        for (Scammer scammer : localScammerList) {
                            if (scammer.uuid.equals(playerUUID.toString())) {
                                if (playerName.equalsIgnoreCase(scammer.name)) {
                                    scammer.name = playerName;
                                    getHelper().saveScammerFile(localScammerList, localScammerFile);

                                    getApi().displayMessageInChat(ModColor.WHITE + "Der Spieler " + playerName + " ist als " + ModColor.GOLD + "["
                                            + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER" + ModColor.GOLD + "]" + ModColor.WHITE
                                            + " auf deiner LOKALEN Liste hinterlegt!");
                                    printPrefixLine();

                                    return true;
                                }

                                getApi().displayMessageInChat(ModColor.WHITE + "Der Spieler " + playerName + " ist als " + ModColor.GOLD + "["
                                        + getGG().getPrefixcolor() + BOLD.toString() + "SCAMMER" + ModColor.GOLD + "]" + ModColor.WHITE
                                        + " auf deiner LOKALEN Liste hinterlegt! Sein vorheriger Name war: "
                                        + scammer.name + ". Der Spielername wurde geupdated.");
                                printPrefixLine();

                                return true;
                            }
                        }

                    } catch (Exception ex) {
                        getApi().displayMessageInChat(
                                ModColor.WHITE + "Dieser Spielername " + playerName + " exisitiert nicht bei Mojang.");
                        printPrefixLine();
                        ex.printStackTrace();
                        return true;
                    }

                }

                getApi().displayMessageInChat(
                        ModColor.WHITE + "Die UUID von " + playerName + " ist nicht auf den Listen hinterlegt!");
                printPrefixLine();
                return true;
            }

            return true;
        }

        if (unformatted.startsWith("/assets/minecraft/griefergames/scammer"))
            return true;

        return false;
    }

    public void printPrefixLine() {
        modcolor = getGG().getPrefixcolor();
        String prefix = ModColor.RESET + "" + ModColor.GOLD + " [" + ModColor.RESET + "" + modcolor + "" + BOLD
                + "SCAMMER" + ModColor.RESET + "" + ModColor.GOLD + "] " + ModColor.RESET + "";

        getApi().displayMessageInChat(ModColor.BLACK + BOLD.toString() + "\u00a74\u00a7l\u00a7m----------" + prefix
                + ModColor.BLACK + BOLD.toString() + "\u00a74\u00a7l\u00a7m----------");
    }

    private boolean isGlobalMessage(String unformatted) {
        Matcher matcher = msgUserGlobalChatRegex.matcher(unformatted);
        Matcher matcher2 = msgUserGlobalChatClanRegex.matcher(unformatted);
        Matcher matcher3 = playerPaymentReceiveRegexp.matcher(unformatted);

        if (matcher3.find()) {
            return false;
        }

        if (matcher.find() && !getUserFromGlobalMessage(unformatted)
                .equalsIgnoreCase(LabyModCore.getMinecraft().getPlayer().getName().trim())) {
            return true;
        } else if (matcher2.find() && !getUserFromGlobalMessage(unformatted)
                .equalsIgnoreCase(LabyModCore.getMinecraft().getPlayer().getName().trim())) {
            return true;
        }
        return false;
    }

    private String getUserFromGlobalMessage(String unformatted) {
        String displayName = "";
        Matcher msgUserGlobalChat = msgUserGlobalChatRegex.matcher(unformatted);
        Matcher msgUserGlobalChatClan = msgUserGlobalChatClanRegex.matcher(unformatted);
        if (msgUserGlobalChat.find()) {
            displayName = msgUserGlobalChat.group(2);
        } else if (msgUserGlobalChatClan.find()) {
            displayName = msgUserGlobalChatClan.group(3);
        }
        return displayName;
    }


    private String getUserFromPrivateMessage(String unformatted) {
        String displayName = "";

        Matcher privateMessage = privateMessageRegex.matcher(unformatted);
        if (privateMessage.find()) {
            displayName = privateMessage.group(2);
        }

        Matcher privateSentMessage = privateMessageSentRegex.matcher(unformatted);
        if (privateSentMessage.find()) {
            displayName = privateSentMessage.group(2);
        }

        return displayName;
    }

    private boolean isPrivateMessage(String unformatted) {
        Matcher privateMessage = privateMessageRegex.matcher(unformatted);
        Matcher matcher3 = playerPaymentReceiveRegexp.matcher(unformatted);
        Matcher privateSentMessage = privateMessageSentRegex.matcher(unformatted);

        if (matcher3.find()) {
            return false;
        }

        if (unformatted.trim().length() > 0 && privateMessage.find())
            return true;

        if (unformatted.trim().length() > 0 && privateSentMessage.find())
            return true;

        return false;
    }
}