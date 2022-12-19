package ru.leymooo.botfilter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import ru.leymooo.botfilter.config.Settings;

public class BotFilterCommand extends Command
{

    public BotFilterCommand()
    {
        super( "botfilter", null, "bf", "antibot", "gg" );
    }

    @Override
    public void execute(CommandSender sender, String[] args)
    {
        if ( sender instanceof ProxiedPlayer )
        {
            sendStat( sender );
            return;
        }
        if ( args.length == 0 )
        {
            sender.sendMessage( "§r--------------- §bBotFilter §cv" + Settings.IMP.BOT_FILTER_VERSION + "§r-----------------" );
            sender.sendMessage( "§r> §lbotfilter reload §6- §a重载配置文件" );
            sender.sendMessage( "§r> §lbotfilter stat §6- §a显示统计数据" );
            sender.sendMessage( "§r> §lbotfilter export §6- §a导出通过假人验证的玩家列表" );
            sender.sendMessage( "§r> §lbotfilter protection on/off §6- §a手动启用或禁用'受到攻击'模式" );
            sender.sendMessage( "§r--------------- §bBotFilter §r-----------------" );
        } else if ( args[0].equalsIgnoreCase( "reload" ) )
        {
            BungeeCord.getInstance().getBotFilter().disable();
            BungeeCord.getInstance().setBotFilter( new BotFilter( false ) );
            sender.sendMessage( "§a指令已执行" );
        } else if ( args[0].equalsIgnoreCase( "stat" ) || args[0].equalsIgnoreCase( "stats" ) || args[0].equalsIgnoreCase( "info" ) )
        {
            sendStat( sender );
        } else if ( args[0].equalsIgnoreCase( "export" ) )
        {
            export( sender, args );
            sender.sendMessage( "§a指令已执行" );
        } else if ( args[0].equalsIgnoreCase( "protection" ) )
        {
            if ( args.length >= 2 )
            {
                boolean enable = args[1].equalsIgnoreCase( "on" );
                BungeeCord.getInstance().getBotFilter().setForceProtectionEnabled( enable );
                sender.sendMessage( "§a保护已 " + ( enable ? "启用" : "§c禁用" ) );
            }
        }
    }

    private void sendStat(CommandSender sender)
    {
        BotFilter botFilter = BungeeCord.getInstance().getBotFilter();
        sender.sendMessage( "§r----------------- §bBotFilter §cv" + Settings.IMP.BOT_FILTER_VERSION + " §r-----------------" );
        sender.sendMessage( "§r> §l受到攻击: " + ( botFilter.isUnderAttack() ? "§c是" : "§a否" ) );
        sender.sendMessage( "§r> §l验证中的玩家/假人: " + botFilter.getOnlineOnFilter() );
        sender.sendMessage( "§r> §l已通过验证的玩家: " + botFilter.getUsersCount() );
        sender.sendMessage( "§r> §lDownload BotFilter(RUS): http://www.rubukkit.org/threads/137038/" );
        sender.sendMessage( "§r> §lDownload BotFilter(ENG): https://github.com/DionaMC/BungeeCord-BotFilter-ENG" );
        sender.sendMessage( "§r> §l下载 BotFilter(简体中文): https://github.com/Loyisa/BungeeCord-BotFilter-ZHCN" );
    }

    private void export(CommandSender sender, String[] args)
    {
        BotFilter botFilter = BungeeCord.getInstance().getBotFilter();

        if ( args.length == 1 )
        {
            sender.sendMessage( "§r> §lbotfilter export [TIME_IN_SECONDS] §6- §aExport the list of players who "
                    + "passed the check within the specified time. Use \"ALL\" to get a list of all times" );
            sender.sendMessage( "§r> §lbotfilter export [TIME_IN_SECONDS] JOIN §6- §aExport the list of players who "
                    + "logged into the server at a specified time. (including players who passed the check)" );
            return;
        }
        if ( args[1].equalsIgnoreCase( "all" ) )
        {
            List<String> out = new ArrayList<>( botFilter.getUsersCount() );
            botFilter.getUserCache().values().forEach( value ->
                out.add( value.getName() + "|" + value.getIp() + "|" + value.getLastCheck() + "|" + value.getLastJoin() )
            );
            exportToFile( out, args.length >= 3 && args[2].equalsIgnoreCase( "join" ) );
            return;
        }
        try
        {
            int seconds = Integer.parseInt( args[1] );
            boolean join = args.length >= 3 && args[2].equalsIgnoreCase( "join" );
            Calendar calendar = Calendar.getInstance();
            calendar.add( Calendar.SECOND, -seconds );
            long until = calendar.getTimeInMillis();

            List<String> out = new ArrayList<>( botFilter.getUsersCount() );
            botFilter.getUserCache().values().forEach( value ->
                {
                    if ( join )
                    {
                        if ( value.getLastJoin() >= until )
                        {
                            out.add( value.getName() + "|" + value.getIp() + "|" + value.getLastCheck() + "|" + value.getLastJoin() );
                        }
                    } else if ( value.getLastCheck() >= until )
                    {
                        out.add( value.getName() + "|" + value.getIp() + "|" + value.getLastCheck() + "|" + value.getLastJoin() );
                    }
                }
            );
            exportToFile( out, join );
        } catch ( Exception e )
        {
            sender.sendMessage( "§cPlease input a number value" );
        }
    }

    private void exportToFile(List<String> out, boolean join)
    {
        Path outFile = new File( "BotFilter", "whitelist.out." + ( join ? "join" : "" ) + ".txt" ).toPath();
        try
        {
            Files.write( outFile, out, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING );
        } catch ( IOException e )
        {
            BungeeCord.getInstance().getLogger().log( Level.WARNING, "[BotFilter] Could not export ips to file", e );
        }
    }

}
