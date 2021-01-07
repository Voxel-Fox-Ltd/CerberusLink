package uk.co.voxelfox.cerberuslink;

import java.util.List;

public class OnlineUsers {

    private List<String> onlineUsers;
    private String discordGuild;

    public OnlineUsers(List<String> onlineUsers, String discordGuild) {
        this.onlineUsers = onlineUsers;
        this.discordGuild = discordGuild;
    }

    public List<String> getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(List<String> onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public String getDiscordGuild() {
        return discordGuild;
    }

    public void setDiscordGuild(String discordGuild) {
        this.discordGuild = discordGuild;
    }

}
