package ru.leymooo.botfilter.caching;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.Protocol;
import net.md_5.bungee.protocol.ProtocolConstants;
import net.md_5.bungee.protocol.packet.KeepAlive;
import net.md_5.bungee.protocol.packet.Kick;
import net.md_5.bungee.protocol.packet.PluginMessage;
import ru.leymooo.botfilter.config.Settings;
import ru.leymooo.botfilter.packets.DefaultSpawnPosition;
import ru.leymooo.botfilter.packets.EmptyChunkPacket;
import ru.leymooo.botfilter.packets.JoinGame;
import ru.leymooo.botfilter.packets.PlayerAbilities;
import ru.leymooo.botfilter.packets.PlayerPositionAndLook;
import ru.leymooo.botfilter.packets.SetExp;
import ru.leymooo.botfilter.packets.SetSlot;
import ru.leymooo.botfilter.packets.TimeUpdate;
import ru.leymooo.botfilter.utils.Dimension;

/**
 * @author Leymooo
 */
public class PacketUtils
{

    private static int[] VERSION_REWRITE = new int[1024];
    public static final CachedCaptcha captchas = new CachedCaptcha();
    private static final CachedPacket[] cachedPackets = new CachedPacket[12];
    private static final HashMap<KickType, CachedPacket> kickMessagesGame = new HashMap<>( 3 );
    private static final HashMap<KickType, CachedPacket> kickMessagesLogin = new HashMap<>( 4 );
    public static int PROTOCOLS_COUNT = ProtocolConstants.SUPPORTED_VERSION_IDS.size();
    public static int CLIENTID = new Random().nextInt( Integer.MAX_VALUE - 100 ) + 50;
    public static int KEEPALIVE_ID = 9876;
    public static CachedExpPackets expPackets;

    /**
     * 0 - Checking_fall, 1 - checking_captcha, 2 - sus
     */
    public static CachedTitle[] titles = new CachedTitle[3];
    public static CachedMessage[] messages = new CachedMessage[3];

    public static ByteBuf createPacket(DefinedPacket packet, int id, int protocol)
    {
        final ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        DefinedPacket.writeVarInt( id, buffer );
        packet.write( buffer, ProtocolConstants.Direction.TO_CLIENT, protocol );
        buffer.capacity( buffer.readableBytes() );
        return buffer;
    }

    public static void init()
    {
        Arrays.fill( VERSION_REWRITE, -1 );
        for ( int i = 0; i < ProtocolConstants.SUPPORTED_VERSION_IDS.size(); i++ )
        {
            VERSION_REWRITE[ProtocolConstants.SUPPORTED_VERSION_IDS.get( i )] = i;
        }
        if ( expPackets != null )
        {
            expPackets.release();
        }
        for ( CachedPacket packet : cachedPackets )
        {
            if ( packet != null )
            {
                packet.release();
            }
        }
        for ( CachedTitle title : titles )
        {
            if ( title != null )
            {
                title.release();
            }
        }
        for ( CachedPacket packet : kickMessagesGame.values() )
        {
            packet.release();
        }
        for ( CachedMessage message : messages )
        {
            if ( message != null )
            {
                message.release();
            }
        }
        kickMessagesGame.clear();

        expPackets = new CachedExpPackets();

        titles[0] = new CachedTitle( Settings.IMP.MESSAGES.CHECKING_TITLE, 5, 90, 15 );
        titles[1] = new CachedTitle( Settings.IMP.MESSAGES.CHECKING_TITLE_CAPTCHA, 5, 35, 10 );
        titles[2] = new CachedTitle( Settings.IMP.MESSAGES.CHECKING_TITLE_SUS, 5, 20, 10 );

        Dimension dimension = Dimension.OVERWORLD;
        int dimensionType = Settings.IMP.DIMENSIONS.TYPE;
        if ( dimensionType == 1 )
        {
            dimension = Dimension.THE_NETHER;
        } else if ( dimensionType == 2 )
        {
            dimension = Dimension.THE_END;
        }
        DefinedPacket[] packets =
        {
            new JoinGame( CLIENTID, dimension ), //0
            new EmptyChunkPacket( 0, 0 ), //1
            new TimeUpdate( 1, 23700 ), //2
            new PlayerAbilities( (byte) 6, 0f, 0f ), //3
            new PlayerPositionAndLook( 7.00, 450, 7.00, 90f, 38f, 9876, false ), //4
            new SetSlot( 0, 36, 358, 1, 0 ), //5 map 1.8+
            new SetSlot( 0, 36, -1, 0, 0 ), //6 map reset
            new KeepAlive( KEEPALIVE_ID ), //7
            new PlayerPositionAndLook( 7.00, 450, 7.00, 90f, 10f, 9876, false ), //8
            new SetExp( 0, 0, 0 ), //9
            createPluginMessage(), //10
            new DefaultSpawnPosition( 7, 450, 7, 123 ) //11
        };

        for ( int i = 0; i < packets.length; i++ )
        {
            PacketUtils.cachedPackets[i] = new CachedPacket( packets[i], Protocol.BotFilter, Protocol.GAME );
        }


        messages = new CachedMessage[]
        {
            new CachedMessage( Settings.IMP.MESSAGES.CHECKING_CAPTCHA_WRONG.replaceFirst( "%s", "2" ).replaceFirst( "%s", "次" ) ),
            new CachedMessage( Settings.IMP.MESSAGES.CHECKING_CAPTCHA_WRONG.replaceFirst( "%s", "1" ).replaceFirst( "%s", "次" ) ),
            new CachedMessage( Settings.IMP.MESSAGES.CHECKING ),
            new CachedMessage( Settings.IMP.MESSAGES.CHECKING_CAPTCHA ),
            new CachedMessage( Settings.IMP.MESSAGES.SUCCESSFULLY )
        };


        Protocol kickGame = Protocol.GAME;
        Protocol kickLogin = Protocol.LOGIN;

        CachedPacket failedMessage = new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_NOT_PLAYER ), kickGame );
        kickMessagesGame.put( KickType.PING, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_BIG_PING ), kickGame ) );
        kickMessagesGame.put( KickType.FAILED_CAPTCHA, failedMessage );
        kickMessagesGame.put( KickType.FAILED_FALLING, failedMessage );
        kickMessagesGame.put( KickType.TIMED_OUT, failedMessage );
        kickMessagesGame.put( KickType.COUNTRY, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_COUNTRY ), kickGame ) );
        kickMessagesGame.put( KickType.BIG_PACKET, new CachedPacket( createKickPacket( "§c验证失败。请向服务器管理员报告。(数据包大小异常)" ), kickGame ) );
        kickMessagesLogin.put( KickType.PING, new CachedPacket( createKickPacket( String.join( "", Settings.IMP.SERVER_PING_CHECK.KICK_MESSAGE ) ), kickLogin ) );
        kickMessagesLogin.put( KickType.MANYCHECKS, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_MANY_CHECKS ), kickLogin ) );
        kickMessagesLogin.put( KickType.COUNTRY, new CachedPacket( createKickPacket( Settings.IMP.MESSAGES.KICK_COUNTRY ), kickLogin ) );
    }

    private static DefinedPacket createKickPacket(String message)
    {
        return new Kick( ComponentSerializer.toString(
            TextComponent.fromLegacyText(
                ChatColor.translateAlternateColorCodes( '&',
                    message.replace( "%prefix%", Settings.IMP.MESSAGES.PREFIX ).replace( "%nl%", "\n" ) ) ) ) );
    }


    private static DefinedPacket createPluginMessage()
    {
        ByteBuf brand = ByteBufAllocator.DEFAULT.heapBuffer();
        DefinedPacket.writeString( "BotFilter (https://vk.cc/8hr1pU)", brand );
        DefinedPacket packet = new PluginMessage( "MC|Brand", DefinedPacket.toArray( brand ), false );
        brand.release();
        return packet;
    }

    public static int getPacketId(DefinedPacket packet, int version, Protocol... protocols)
    {
        for ( Protocol protocol : protocols )
        {
            try
            {
                return protocol.TO_CLIENT.getId( packet.getClass(), version );
            } catch ( Exception ignore )
            {
            }
        }

        return -1;
    }

    public static void releaseByteBuf(ByteBuf buf)
    {
        if ( buf != null && buf.refCnt() != 0 )
        {
            while ( buf.refCnt() != 0 )
            {
                buf.release();
            }
        }
    }

    public static void fillArray(ByteBuf[] buffer, DefinedPacket packet, Protocol... protocols)
    {
        fillArray( buffer, packet, 0, Integer.MAX_VALUE, protocols );
    }

    public static void fillArray(ByteBuf[] buffer, DefinedPacket packet, int from, int to, Protocol... protocols)
    {
        if ( packet == null )
        {
            return;
        }
        int oldPacketId = -1;
        ByteBuf oldBuf = null;
        for ( int version : ProtocolConstants.SUPPORTED_VERSION_IDS )
        {
            if ( version < from || version > to )
            {
                continue;
            }
            int versionRewrited = rewriteVersion( version );
            int newPacketId = PacketUtils.getPacketId( packet, version, protocols );
            if ( newPacketId == -1 )
            {
                continue;
            }
            if ( newPacketId != oldPacketId )
            {
                oldPacketId = newPacketId;
                oldBuf = PacketUtils.createPacket( packet, oldPacketId, version );
                buffer[versionRewrited] = oldBuf;
            } else
            {
                ByteBuf newBuf = PacketUtils.createPacket( packet, oldPacketId, version );
                if ( newBuf.equals( oldBuf ) )
                {
                    buffer[versionRewrited] = oldBuf;
                    newBuf.release();
                } else
                {
                    oldBuf = newBuf;
                    buffer[versionRewrited] = oldBuf;
                }
            }
        }
    }

    public static int rewriteVersion(int version)
    {
        int rewritten = VERSION_REWRITE[version];
        if ( rewritten == -1 )
        {
            throw new IllegalArgumentException( "不支持的版本" );
        }
        return rewritten;
    }

    public static void spawnPlayer(Channel channel, int version, boolean disableFall, boolean captcha)
    {
        channel.write( getCachedPacket( PacketsPosition.LOGIN ).get( version ), channel.voidPromise() );
        channel.write( getCachedPacket( PacketsPosition.PLUGIN_MESSAGE ).get( version ), channel.voidPromise() );
        channel.write( getCachedPacket( PacketsPosition.CHUNK ).get( version ), channel.voidPromise() );
        if ( disableFall )
        {
            channel.write( getCachedPacket( PacketsPosition.PLAYERABILITIES ).get( version ), channel.voidPromise() );
        }
        if ( version >= ProtocolConstants.MINECRAFT_1_19_3 )
        {
            channel.write( getCachedPacket( PacketsPosition.DEFAULT_SPAWN_POSITION ).get( version ), channel.voidPromise() );
        }
        if ( captcha )
        {
            channel.write( getCachedPacket( PacketsPosition.PLAYERPOSANDLOOK_CAPTCHA ).get( version ), channel.voidPromise() );
        } else
        {
            channel.write( getCachedPacket( PacketsPosition.PLAYERPOSANDLOOK ).get( version ), channel.voidPromise() );
        }
        channel.write( getCachedPacket( PacketsPosition.TIME ).get( version ), channel.voidPromise() );
        //channel.flush(); Не очищяем поскольку это будет в другом месте
    }

    public static void kickPlayer(KickType kick, Protocol protocol, ChannelWrapper wrapper, int version)
    {
        if ( !wrapper.getHandle().isActive() || wrapper.isClosed() || wrapper.isClosing() )
        {
            return;
        }
        if ( protocol == Protocol.GAME )
        {
            wrapper.close( kickMessagesGame.get( kick ).get( version ) );
        } else
        {
            wrapper.close( kickMessagesLogin.get( kick ).get( version ) );
        }

    }

    public static CachedPacket getCachedPacket(int pos)
    {
        return cachedPackets[pos];
    }

    public static enum KickType
    {
        MANYCHECKS,
        FAILED_CAPTCHA,
        FAILED_FALLING,
        TIMED_OUT,
        COUNTRY,
        LEAVED, //left
        // THROTTLE,
        BIG_PACKET,
        PING;
    }

}
