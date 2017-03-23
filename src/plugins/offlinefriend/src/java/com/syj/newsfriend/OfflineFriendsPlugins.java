package com.syj.newsfriend;

import java.io.File;
import java.io.StringReader;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.plugin.PresencePlugin;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.syj.newsfriend.ChatLogs.ChatLogsConstants;



/**
 * 用来处理用户不在线，添加好友的请求
 * 
 * @author Administrator
 *
 */
public class OfflineFriendsPlugins implements Plugin, PacketInterceptor {
	private PresencePlugin plugin;
	private InterceptorManager interceptorManager;
	private static DbChatLogsManager logsManager;

	public OfflineFriendsPlugins() {
		super();
		// TODO Auto-generated constructor stub
		
		interceptorManager = InterceptorManager.getInstance();
		logsManager=DbChatLogsManager.getInstance();
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		// TODO Auto-generated method stub
		SAXReader xmlReader = new SAXReader();
		try {

			if(packet instanceof Presence){
				Presence presence2 = (Presence) packet.createCopy();
				if(presence2.getType()==Presence.Type.subscribe&&incoming&&processed){
				if (packet.getFrom()!=null) {
					String sender = packet.getFrom().toString();
					List<ChatLogs> logs = logsManager.query(sender.split("/")[0]);
					if(logs!=null){
					for (ChatLogs chatLogs : logs) {
						System.out.println(chatLogs.getDetail());
						Presence presence = new Presence(xmlReader
								.read(new StringReader(
										chatLogs.getDetail().replaceAll("\"", "\'")))
								.getRootElement());
					session.process(presence);
					System.out.println("處理了");
					logsManager.remove(chatLogs.getMessageId());
					}
					}
				}
				// sessionManager.
			}
			
			}
			//判断是否为添加好友的请求，如过好友不在线，存储
			if(packet instanceof Presence){
				Presence presence = (Presence) packet.createCopy();
				if(presence.getType()==Presence.Type.subscribe&&incoming&&processed){
					JID reciver = presence.getTo();
					try {
						if(reciver!=null&&!reciver.toString().contains("conference")){
						plugin = (PresencePlugin) XMPPServer.getInstance().getPluginManager().getPlugin("presence");
						Presence onlineReciver = plugin.getPresence(null, reciver.toBareJID());
						//用戶不在線情況
						if(onlineReciver==null){
							ChatLogs chatLogs = new ChatLogs();
							chatLogs.setCreateDate(new Timestamp( new Date().getTime()));
							chatLogs.setDetail(presence.toXML());
							chatLogs.setLength(presence.toXML().length());
							long messageID = SequenceManager.nextID(ChatLogsConstants.CHAT_LOGS);
							chatLogs.setMessageId(messageID);
							chatLogs.setReceiver(presence.getTo().toString());
							chatLogs.setSender(presence.getFrom().toString());
							chatLogs.setSessionJID(presence.getID());
							logsManager.add(chatLogs);
						}}
					} catch (UserNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}

		} catch (DocumentException e) {
			e.printStackTrace();
		}

		// ClientSession session = sessionManager.getSession(packet.getFrom());
	}

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		// TODO Auto-generated method stub
		System.out.println("这个插件已经用上了");
		interceptorManager.addInterceptor(this);
		
	}

	@Override
	public void destroyPlugin() {
		// TODO Auto-generated method stub
interceptorManager.removeInterceptor(this);
	}

}
