package me.TechsCode.InsaneReloader;

import me.TechsCode.base.SpigotTechPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class InsaneReloader extends SpigotTechPlugin {

    private Map<File, Long> plugins;
    private File updatingPlugin;

    public InsaneReloader(JavaPlugin plugin) {
        super(plugin);
    }

    private void checkForChanges(){
        if(plugins == null){
            plugins = new HashMap<>();
        }

        for(File pluginJar : Objects.requireNonNull(new File(getServerFolder().getAbsolutePath() + "/plugins").listFiles())){
            if(!pluginJar.getName().endsWith(".jar")) continue;

            if(!plugins.containsKey(pluginJar)){
                plugins.put(pluginJar, pluginJar.length());
                continue;
            }

            long lastFileSize = plugins.get(pluginJar);
            long fileSizeNow = pluginJar.length();

            boolean updating = lastFileSize != fileSizeNow;
            plugins.put(pluginJar, fileSizeNow);

            if(updatingPlugin != null && updatingPlugin.equals(pluginJar)){
                if(!updating){
                    onUpdatingComplete(pluginJar);
                    updatingPlugin = null;
                }
            } else {
                if(updating){
                    updatingPlugin = pluginJar;
                }
            }
        }
    }

    private void onUpdatingComplete(File file){
        String plugin = file.getName().replace(".jar", "");

        for(Player all : Bukkit.getOnlinePlayers()){
            all.sendMessage(getPrefix()+"§7Plugin §e"+plugin+" §7just got updated");
        }

        getScheduler().runTaskLater(() -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plugman reload "+plugin), 60);
    }

    @Override
    protected void onEnable() {
        getScheduler().runTaskTimer(this::checkForChanges, 20, 20);
    }

    @Override
    protected void onDisable() {}
}
