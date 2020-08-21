package me.TechsCode.InsaneReloader;

import me.TechsCode.base.SpigotTechPlugin;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;

public class InsaneReloader extends SpigotTechPlugin implements Listener {

    private final HashMap<File, Long> plugins;
    private File updatingPlugin;

    private boolean sendMsgs = false;

    public InsaneReloader(JavaPlugin plugin) {
        super(plugin);

        Bukkit.getPluginManager().registerEvents(this, this.getBootstrap());
        this.plugins = new HashMap<>();

        getScheduler().runTaskLater(() -> sendMsgs = true, 200L);
    }

    private void checkForChanges() {
        for (File pluginJar : Objects.requireNonNull(new File(getServerFolder().getAbsolutePath() + "/plugins").listFiles())) {
            if (!pluginJar.getName().endsWith(".jar")) continue;

            if (!plugins.containsKey(pluginJar)) {
                plugins.put(pluginJar, pluginJar.length());
                continue;
            }

            long lastFileSize = plugins.get(pluginJar);
            long fileSizeNow = pluginJar.length();

            boolean updating = lastFileSize != fileSizeNow;
            plugins.put(pluginJar, fileSizeNow);

            if (!updating && updatingPlugin != null && updatingPlugin.equals(pluginJar)) {
                onUpdatingComplete(pluginJar);
                updatingPlugin = null;
            } else if(updating) {
                updatingPlugin = pluginJar;
            }
        }
    }

    private void onUpdatingComplete(File file) {
        getScheduler().runTaskLater(() -> {
            File updatedFile = new File(file.getAbsolutePath());

            if(updatedFile.length() == file.length()) {
                String plugin = file.getName().replace(".jar", "");

                Plugin loadedPlugin = getPluginByName(plugin);

                sendToAll(updatedMessage(plugin));
                if(loadedPlugin != null && loadedPlugin.isEnabled()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plugman reload " + plugin);
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plugman load " + plugin);
                }
            }
        }, 90);
    }

    public Plugin getPluginByName(String name) {
        return Bukkit.getPluginManager().getPlugin(name);
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        if(!sendMsgs) return;

        sendToAll(loadedMessage(e.getPlugin().getName()));
    }

    private String loadedMessage(String plugin) {
        return getPrefix() + "§7The plugin §e" + plugin + " §7has §asuccessfully §7loaded!";
    }

    private String updatedMessage(String plugin) {
        return getPrefix() + "§7The plugin §e" + plugin + " §7just got updated!";
    }

    public void sendToAll(String msg) {
        Bukkit.getOnlinePlayers().forEach(all -> all.sendMessage(msg));
    }

    @Override
    protected void onEnable() {
        getScheduler().runTaskTimer(this::checkForChanges, 20, 20);
    }

    @Override
    protected void onDisable() {}
}
