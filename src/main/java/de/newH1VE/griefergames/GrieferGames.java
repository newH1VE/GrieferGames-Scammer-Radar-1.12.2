package de.newH1VE.griefergames;


import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.newH1VE.griefergames.Events.OnKeyEvent;
import de.newH1VE.griefergames.antiScammer.ListUpdater;
import de.newH1VE.griefergames.chat.AntiScammer;
import de.newH1VE.griefergames.Enum.ColorEnum;
import de.newH1VE.griefergames.Helper.Helper;

import net.labymod.api.LabyModAddon;
import net.labymod.api.events.MessageModifyChatEvent;
import net.labymod.api.events.MessageReceiveEvent;
import net.labymod.api.events.MessageSendEvent;
import net.labymod.gui.elements.DropDownMenu;
import net.labymod.settings.elements.*;
import net.labymod.utils.Consumer;
import net.labymod.utils.Material;
import net.labymod.utils.ModColor;
import net.labymod.utils.ServerData;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class GrieferGames extends LabyModAddon {
    private static GrieferGames griefergames;
    private String serverIp = "";
    private String secondServerIp = "";
    private Helper helper;
    private boolean modenabled = true;
    private boolean prefixenabled = true;
    private boolean messageenabled = false;
    private boolean doAntiScammer = false;
    private boolean tablistenabled = true;
    private static ListUpdater listupdater = new ListUpdater();
    private static AntiScammer antiscammer = new AntiScammer();
    private static Pattern msgStartsWithTime = Pattern.compile("^\\[(\\d{2}\\:){2}\\d{2}\\][^$]*$");
    private static Pattern msgStartsWithAMP = Pattern.compile("^\\[AMP\\][^$]*$");
    private ModColor prefixcolor = ModColor.DARK_RED;
    private TextFormatting chatformat = TextFormatting.DARK_RED;
    private ColorEnum colorenum = ColorEnum.DARK_RED;
    private Minecraft mc;
    private static final File onlineScammerFile = new File("LabyMod/antiScammer/onlineScammer.json");
    private static final File localScammerFile = new File("LabyMod/antiScammer/localScammer.json");

    public void setHelper(Helper helper) {
        this.helper = helper;
    }

    public Helper getHelper() {
        return helper;
    }

    public static AntiScammer getAntiscammer()
    {
        return antiscammer;
    }

    public boolean isTabListEnabled() {
        return tablistenabled;
    }

    public void setTabListEnabled(boolean tabListUpdate) {
        this.tablistenabled = tablistenabled;
    }

    public boolean isMessageEnabled() {
        return messageenabled;
    }

    public boolean isPrefixEnabled() {
        return prefixenabled;
    }

    public void setPrefixEnabled(boolean prefixenabled) {
        this.prefixenabled = prefixenabled;
    }

    public boolean isModEnabled() {
        return modenabled;
    }

    private void setModEnabled(boolean modenabled) {
        this.modenabled = modenabled;
    }

    private void setMessageEnabled(boolean messenabled) {
        this.messageenabled = messenabled;
    }

    public static GrieferGames getGrieferGames() {
        return griefergames;
    }

    private static void setGrieferGames(GrieferGames griefergames) {
        GrieferGames.griefergames = griefergames;
    }

    private String getServerIp() {
        return serverIp;
    }

    private void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    private String getSecondServerIp() {
        return secondServerIp;
    }

    private void setSecondServerIp(String secondServerIp) {
        this.secondServerIp = secondServerIp;
    }

    private boolean getDoAntiScammer() {
        return doAntiScammer;
    }

    private void setDoAntiScammer(boolean doAntiScammer) {
        this.doAntiScammer = doAntiScammer;
    }

    public ColorEnum getColorenum() {
        return colorenum;
    }

    public void setColorenum(ColorEnum colorenum) {
        this.colorenum = colorenum;
    }

    public TextFormatting getChatformat() {
        return chatformat;
    }

    public void setChatformat(TextFormatting chatformat) {
        this.chatformat = chatformat;
    }

    public ModColor getPrefixcolor() {
        return prefixcolor;
    }

    public void setPrefixcolor(String prefixcolor) {
        System.out.println(prefixcolor);

        if (prefixcolor.equals(ColorEnum.BLACK.toString())) {
            this.prefixcolor = ModColor.BLACK;
            setChatformat(TextFormatting.BLACK);
            setColorenum(ColorEnum.BLACK);

        }


        if (prefixcolor.equals(ColorEnum.DARK_GRAY.toString())) {
            this.prefixcolor = ModColor.DARK_GRAY;
            setChatformat(TextFormatting.DARK_GRAY);
            setColorenum(ColorEnum.DARK_GRAY);
        }


        if (prefixcolor.equals(ColorEnum.DARK_RED.toString())) {
            this.prefixcolor = ModColor.DARK_RED;
            setChatformat(TextFormatting.DARK_RED);
            setColorenum(ColorEnum.DARK_RED);
        }


        if (prefixcolor.equals(ColorEnum.GRAY.toString())) {
            this.prefixcolor = ModColor.GRAY;
            setChatformat(TextFormatting.GRAY);
            setColorenum(ColorEnum.GRAY);

        }


        if (prefixcolor.equals(ColorEnum.GREEN.toString())) {
            this.prefixcolor = ModColor.GREEN;
            setChatformat(TextFormatting.GREEN);
            setColorenum(ColorEnum.GREEN);
        }


        if (prefixcolor.equals(ColorEnum.PINK.toString())) {
            this.prefixcolor = ModColor.PINK;
            setChatformat(TextFormatting.LIGHT_PURPLE);
            setColorenum(ColorEnum.PINK);
        }


        if (prefixcolor.equals(ColorEnum.YELLOW.toString())) {
            this.prefixcolor = ModColor.YELLOW;
            setChatformat(TextFormatting.YELLOW);
            setColorenum(ColorEnum.YELLOW
            );
        }


        if (prefixcolor.equals(ColorEnum.BLUE.toString())) {
            this.prefixcolor = ModColor.BLUE;
            setChatformat(TextFormatting.BLUE);
            setColorenum(ColorEnum.BLUE);
        }


        if (prefixcolor.equals(ColorEnum.DARK_GREEN.toString())) {
            this.prefixcolor = ModColor.DARK_GREEN;
            setChatformat(TextFormatting.DARK_GREEN);
            setColorenum(ColorEnum.DARK_GREEN);

        }


    }


    @Override
    public void onEnable() {

        System.out.println("[GrieferGames AntiScammer] enabled.");

        // restrict access by time
        //final LocalDateTime deactivate = LocalDateTime.of(2020, 9, 10, 23, 59);

        //setup helper
        setHelper(new Helper());

        // get new scammer list from webserver
        try {
            listupdater.updateScammerFile(onlineScammerFile);

        } catch(Exception ex)
        {
            ex.printStackTrace();
        }


        // initial load of scammerFiles
        getHelper().loadScammerFile(onlineScammerFile);
        getHelper().loadScammerFile(localScammerFile);
        getAntiscammer().setScammerList(getHelper().joinScammerLists());

        // set ip
        setServerIp("griefergames.net");
        setSecondServerIp("griefergames.de");

        // save instance
        setGrieferGames(this);

        // initial update of both scammer lists/files
        try {
           getHelper().updateScammerLists();
        } catch (Exception e) {
            System.err.println(e);
        }

        this.getApi().getEventManager().registerOnJoin(new Consumer<ServerData>() {
            @Override
            public void accept(ServerData serverData) {
                boolean doAntiScammer = (serverData.getIp().toLowerCase().indexOf(getServerIp()) >= 0
                        || serverData.getIp().toLowerCase().indexOf(getSecondServerIp()) >= 0);
                setDoAntiScammer(doAntiScammer);
            }
        });

        this.getApi().getEventManager().register(new MessageModifyChatEvent() {
            public Object onModifyChatMessage(Object o) {
               // LocalDateTime now = LocalDateTime.now();
               // if(now.isAfter(deactivate))
               // setModEnabled(false);

                if (isModEnabled())
                    return modifyChatMessage(o);

                return o;
            }

        });


        // register eventlistener for tablist updates
        getApi().registerForgeListener(new OnKeyEvent());


        getApi().getEventManager().register(new MessageSendEvent() {
            public boolean onSend(String message) {
               // LocalDateTime now = LocalDateTime.now();
               // if(now.isAfter(deactivate))
               //     setModEnabled(false);

                if (!(isModEnabled() && getDoAntiScammer()))
                    return false;

                if (antiscammer.doActionCommandMessage(message)) {
                    return antiscammer.commandMessage(message);
                }

                return false;
            }
        });

        getApi().getEventManager().register(new MessageReceiveEvent() {
            public boolean onReceive(String formatted, String unformatted) {
               // LocalDateTime now = LocalDateTime.now();
               // if(now.isAfter(deactivate))
               //     setModEnabled(false);

                if (!(isModEnabled() && getDoAntiScammer()))
                    return false;

                if (antiscammer.doActionReceiveMessage(formatted, unformatted)) {
                    return antiscammer.receiveMessage(formatted, unformatted);
                }

                return false;
            }
        });

    }

    public Object modifyChatMessage(Object o) {
        if (!(isModEnabled() && getDoAntiScammer()))
            return o;

        try {
            ITextComponent msg = (ITextComponent) o;

            ITextComponent time = new TextComponentString("");

            Matcher matcher = msgStartsWithTime.matcher(msg.getUnformattedText());

            if (matcher.find()) {
                for (int i = 0; i < msg.getSiblings().size() - 1; i++) {
                    time.appendSibling(msg.getSiblings().get(i));
                }

                msg = msg.getSiblings().get(msg.getSiblings().size() - 1);
            }

            Matcher matcherAMP = msgStartsWithAMP.matcher(msg.getUnformattedText());

            if (matcher.find()) {
                for (int i = 0; i < msg.getSiblings().size() - 1; i++) {
                    time.appendSibling(msg.getSiblings().get(i));
                }

                msg = msg.getSiblings().get(msg.getSiblings().size() - 1);
            }

            if (antiscammer.doActionModifyChatMessage(msg)) {
                msg = antiscammer.modifyChatMessage(msg);
                if (time.getUnformattedText().trim().length() > 0) {
                    msg = time.appendSibling(msg);
                }
            }

            return msg;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return o;
    }

    @Override
    public void onDisable() {
        System.out.println("[GrieferGames AntiScammer] disabled.");

    }

    @Override
    public void loadConfig() {
        if (getConfig().has("enabled"))
            setModEnabled(getConfig().get("enabled").getAsBoolean());

        if (getConfig().has("prefix enabled"))
            setPrefixEnabled(getConfig().get("prefix enabled").getAsBoolean());

        if (getConfig().has("message enabled"))
            setMessageEnabled(getConfig().get("message enabled").getAsBoolean());

        if (getConfig().has("prefix color"))
            setPrefixcolor(getConfig().get("prefix color").getAsString());


    }


    @Override
    public List<SettingsElement> getSubSettings() {
        return super.getSubSettings();
    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {
        final BooleanElement modEnabledBtn = new BooleanElement("Schutz aktivieren",
                new ControlElement.IconData(Material.DIAMOND_CHESTPLATE), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean modEnabled) {
                setModEnabled(modEnabled);
                getConfig().addProperty("enabled", modEnabled);
                saveConfig();
            }
        }, isModEnabled());
        list.add(modEnabledBtn);

        list.add(new HeaderElement("Anzeige Scammerwarnung"));

        final BooleanElement modPrefixEnabled = new BooleanElement("Scammer Warnung \u00FCber Prefix",
                new ControlElement.IconData(Material.THIN_GLASS), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean prefEnabled) {
                setPrefixEnabled(prefEnabled);
                getConfig().addProperty("prefix enabled", prefEnabled);
                saveConfig();
            }
        }, isPrefixEnabled());
        list.add(modPrefixEnabled);

        final BooleanElement modMessageEnabled = new BooleanElement("Scammer Warnung \u00FCber Nachricht",
                new ControlElement.IconData(Material.PAPER), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean messEnabled) {
                setMessageEnabled(messEnabled);
                getConfig().addProperty("message enabled", messEnabled);
                saveConfig();
            }
        }, isMessageEnabled());
        list.add(modMessageEnabled);

        final BooleanElement modTabListEnabled = new BooleanElement("Scammer Warnung in der Tablist",
                new ControlElement.IconData(Material.THIN_GLASS), new Consumer<Boolean>() {
            @Override
            public void accept(Boolean tabEnabled) {
                setTabListEnabled(tabEnabled);
                getConfig().addProperty("tablist enabled", tabEnabled);
                saveConfig();
            }
        }, isTabListEnabled());
        list.add(modTabListEnabled);


        list.add(new HeaderElement("Farbe Scammer Prefix"));

        final DropDownMenu<ColorEnum> dropDownMenu = new DropDownMenu<ColorEnum>("Farben" /* Display name */, 0, 0, 0, 0)
                .fill(ColorEnum.values());
        DropDownElement<ColorEnum> dropDown = new DropDownElement<ColorEnum>("Farben", dropDownMenu);

        dropDownMenu.setSelected(colorenum);

        dropDown.setChangeListener(new Consumer<ColorEnum>() {
            @Override
            public void accept(ColorEnum color) {
                System.out.println("New selected alignment: " + color.toString());
                dropDownMenu.setSelected(color);
                setPrefixcolor(color.toString());
                getConfig().addProperty("prefix color", color.toString());
                saveConfig();
            }
        });

        list.add(dropDown);


    }
}
