package com.builderbot;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class BuilderBot extends JavaPlugin {

    private BotTask botTask;
    private ClaimManager claimManager;

    @Override
    public void onEnable() {
        getLogger().info("BuilderBot enabled! Auto builder is ready.");
        claimManager = new ClaimManager(this);
        botTask = new BotTask(this, claimManager);
        // Auto start building every 5 minutes
        botTask.startScheduler();
    }

    @Override
    public void onDisable() {
        if (botTask != null) botTask.stop();
        getLogger().info("BuilderBot disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("builderbot")) return false;
        if (!sender.hasPermission("builderbot.admin")) {
            sender.sendMessage(ChatColor.RED + "Ban khong co quyen su dung lenh nay!");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "Dung: /builderbot <start|stop|status|build>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "start":
                botTask.startScheduler();
                sender.sendMessage(ChatColor.GREEN + "[BuilderBot] Da bat bot tu dong xay dung!");
                break;
            case "stop":
                botTask.stop();
                sender.sendMessage(ChatColor.RED + "[BuilderBot] Da dung bot.");
                break;
            case "build":
                sender.sendMessage(ChatColor.AQUA + "[BuilderBot] Dang tim vi tri va xay cong trinh ngau nhien...");
                botTask.buildNow();
                break;
            case "status":
                sender.sendMessage(ChatColor.AQUA + "[BuilderBot] Trang thai: " +
                    (botTask.isRunning() ? ChatColor.GREEN + "DANG CHAY" : ChatColor.RED + "DA DUNG"));
                sender.sendMessage(ChatColor.AQUA + "Tong cong trinh da xay: " + ChatColor.WHITE + botTask.getBuildCount());
                break;
            default:
                sender.sendMessage(ChatColor.YELLOW + "Dung: /builderbot <start|stop|status|build>");
        }
        return true;
    }
}
