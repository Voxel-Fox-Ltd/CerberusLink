package uk.co.voxelfox.cerberuslink;

import com.earth2me.essentials.Essentials;
import com.google.gson.Gson;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Guild;
import github.scarsz.discordsrv.objects.managers.AccountLinkManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public final class CerberusLink extends JavaPlugin {

    static Essentials ess = (Essentials) Bukkit.getServer().getPluginManager().getPlugin("Essentials");
    private DiscordSRV discordSRV;
    private BukkitTask looper;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        discordSRV = DiscordSRV.getPlugin();
        looper = Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            getLogger().log(Level.FINE, "Getting online users.");
            List<String> users = getOnlineDiscordUserIds();
            getLogger().log(Level.FINE, String.format("Sending list of %d online users to https://cerberus.voxelfox.co.uk/webhooks/minecraft_server_activity", users.size()));
            try {
                sendPostRequest(users);
            } catch (IOException e) {
                getLogger().log(Level.FINE, String.format("Failed sending message to Cerberus - %s", e.getMessage()));
                getLogger().log(Level.FINE, "Disabling loop.");
                looper.cancel();
                return;
            }
            getLogger().log(Level.FINE, "Sent list of online users successfully.");
        },20, 20 * 60);
    }

    private void sendPostRequest(List<String> discordIds) throws IOException {
        URL url = new URL("https://cerberus.voxelfox.co.uk/webhooks/minecraft_server_activity");
        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        Gson json = new Gson();
        String jsonOutput = json.toJson(new OnlineUsers(discordIds, getDiscordGuildId()));
        byte[] out = jsonOutput.getBytes(StandardCharsets.UTF_8);
        int length = out.length;
        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        http.setRequestProperty("Authorization", getConfig().getString("authorization"));
        http.connect();
        try(OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
        try(BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
            String inputLine;
            while((inputLine = in.readLine()) != null) {
                getLogger().log(Level.FINE, inputLine);
            }
        }
        http.disconnect();
    }

    private List<String> getOnlineDiscordUserIds() {
        List<String> onlineDiscordUserIds = new ArrayList<>();
        for(World w: Bukkit.getWorlds()) {
            for(Player p: w.getPlayers()) {
                if(!ess.getUser(p).isAfk()) {
                    String username = getDiscordUserId(p.getUniqueId());
                    if (username != null) {
                        onlineDiscordUserIds.add(username);
                    }
                }
            }
        }
        return onlineDiscordUserIds;
    }

    private String getDiscordUserId(UUID uuid) {
        if(uuid == null) {
            return null;
        }
        AccountLinkManager linkManager = discordSRV.getAccountLinkManager();
        if(linkManager != null) {
            return linkManager.getDiscordId(uuid);
        }
        return null;
    }

    private String getDiscordGuildId() {
        Guild mainGuild = discordSRV.getMainGuild();
        if(mainGuild == null) {
            return null;
        }
        String guildId = mainGuild.getId();
        if(guildId == null) {
            return null;
        }
        return guildId;
    }

    @Override
    public void onDisable() {
        if(looper != null && !looper.isCancelled()) {
            looper.cancel();
        }
    }
}
