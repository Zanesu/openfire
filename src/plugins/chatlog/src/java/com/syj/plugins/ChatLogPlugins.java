package com.syj.plugins;

import java.io.File;

import java.io.File;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.dom4j.Element;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.ElementUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketExtension;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Presence.Show;

import com.sun.jersey.api.uri.UriComponent.Type;
import com.syj.plugins.ChatLogs;
import com.syj.plugins.DbChatLogsManager;
import com.syj.plugins.ChatLogs.ChatLogsConstants;

public class ChatLogPlugins implements Plugin, PacketInterceptor {
	private static final Logger log = LoggerFactory.getLogger(ChatLogPlugins.class);

	private static PluginManager pluginManager;
	private static DbChatLogsManager logsManager;

	public ChatLogPlugins() {
		interceptorManager = InterceptorManager.getInstance();
		logsManager = DbChatLogsManager.getInstance();
	}

	// Hook for intercpetorn
	private InterceptorManager interceptorManager;

	/**
	 * <b>function:</b> 拦截消息核心方法，Packet就是拦截消息对象
	 * 
	 * @author hoojo
	 * @createDate 2013-3-27 下午04:49:11
	 */
	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		if (session != null) {
			//System.out.println(packet.toXML());
			debug(packet, incoming, processed, session);
			if (packet instanceof Presence) {
				Presence presence = (Presence) packet;
				if (presence.getType() != null) {
					if (presence.getType().toString() == Presence.Type.subscribe.toString()) {
						//保存離線消息
						
						// XMPPServer.getInstance().getRoutingTable().routePacket(iq.getTo().asBareJID(),
						// presence, true);
					}
				}
			}
		}
		JID recipient = packet.getTo();
		if (recipient != null) {
			String username = recipient.getNode();
			// if (recipient.toBareJID().contains("@conference")) {
			// return;
			// }
			// 广播消息或是不存在/没注册的用户.
			// if (username == null ||
			// !UserManager.getInstance().isRegisteredUser(recipient)) {
			if (username == null || !(packet instanceof Message)) {
				return;
			} else if (!XMPPServer.getInstance().getServerInfo().getXMPPDomain().equals(recipient.getDomain())) {
				// 非当前openfire服务器信息
				return;
			} else if ("".equals(recipient.getResource())) {
			}
		}
		this.doAction(packet, incoming, processed, session);
	}

	/**
	 * <b>function:</b> 执行保存/分析聊天记录动作
	 * 
	 * @author hoojo
	 * @createDate 2013-3-24 下午12:20:56
	 * @param packet
	 *            数据包
	 * @param incoming
	 *            true表示发送方
	 * @param session
	 *            当前用户session
	 */
	private void doAction(Packet packet, boolean incoming, boolean processed, Session session) {
		Packet copyPacket = packet.createCopy();

		if (packet instanceof Message) {
			Message message = (Message) copyPacket;
					// 一对一聊天，单人模式
			if (message.getType() == Message.Type.chat) {
				// log.info("单人聊天信息：{}", message.toXML());
				 debug("单人聊天信息：" + message.toXML());

				// 程序执行中；是否为结束或返回状态（是否是当前session用户发送消息）
				if (processed || !incoming) {
					return;
				}
				logsManager.add(this.get(packet, incoming, session));

				// 群聊天，多人模式
			} else if (message.getType() == Message.Type.groupchat) {
				String body = message.getBody();
				if (body != null) {
					// logsManager.add(this.get(packet, incoming, session));
					// List<?> els = message.getElement().elements("x");
					// if (els != null && !els.isEmpty()) {
					// log.info("群聊天信息：{}", message.toXML());
					 debug("群聊天信息：" + message.toXML());

				} else {
					// log.info("群系统信息：{}", message.toXML());
					// debug("群系统信息：" + message.toXML());
					if (processed || !incoming) {
						return;
					}

				}

				// 其他信息
			} else {
				// log.info("其他信息：{}", message.toXML());
				// debug("其他信息：" + message.toXML());
			}
		} else if (packet instanceof IQ) {
			IQ iq = (IQ) copyPacket;
			if (iq.getType() == IQ.Type.set && iq.getChildElement() != null
					&& "session".equals(iq.getChildElement().getName())) {
				log.info("用户登录成功：{}", iq.toXML());
				debug("用户登录成功：" + iq.toXML());
			}
		} else if (packet instanceof Presence) {
			Presence presence = (Presence) copyPacket;

			if (presence.getType() == Presence.Type.unavailable) {
				log.info("用户退出服务器成功：{}", presence.toXML());
				debug("用户退出服务器成功：" + presence.toXML());
			}
		}
	}

	/**
	 * <b>function:</b> 创建一个聊天记录实体对象，并设置相关数据
	 * 
	 * @author hoojo
	 * @createDate 2013-3-27 下午04:44:54
	 * @param packet
	 *            数据包
	 * @param incoming
	 *            如果为ture就表明是发送者
	 * @param session
	 *            当前用户session
	 * @return 聊天实体
	 */
	private ChatLogs get(Packet packet, boolean incoming, Session session) {
		Message message = (Message) packet;
		ChatLogs logs = new ChatLogs();

		JID jid = session.getAddress();
		if (incoming) { // 发送者
			logs.setSender(jid.getNode());
			JID recipient = message.getTo();
			logs.setReceiver(recipient.getNode());
		}
		logs.setContent(message.getBody());
		logs.setCreateDate(new Timestamp(new Date().getTime()));
		logs.setDetail(message.toXML());
		logs.setLength(logs.getContent().length());
		logs.setState(0);
		logs.setSessionJID(jid.toString());
		// 生成主键id，利用序列生成器
		long messageID = SequenceManager.nextID(ChatLogsConstants.CHAT_LOGS);
		logs.setMessageId(messageID);

		return logs;
	}

	/**
	 * <b>function:</b> 调试信息
	 * 
	 * @author hoojo
	 * @createDate 2013-3-27 下午04:44:31
	 * @param packet
	 *            数据包
	 * @param incoming
	 *            如果为true就表明是发送者
	 * @param processed
	 *            执行
	 * @param session
	 *            当前用户session
	 */
	private void debug(Packet packet, boolean incoming, boolean processed, Session session) {
		String info = "[ packetID: " + packet.getID() + ", to: " + packet.getTo() + ", from: " + packet.getFrom()
				+ ", incoming: " + incoming + ", processed: " + processed + " ]";

		long timed = System.currentTimeMillis();
		// debug("################### start ###################" + timed);
		// debug("id:" + session.getStreamID() + ", address: " +
		// session.getAddress());
		// debug("info: " + info);
		// debug("xml: " + packet.toXML());
		// debug("################### end #####################" + timed);

		log.info("id:" + session.getStreamID() + ", address: " + session.getAddress());
		log.info("info: {}", info);
		log.info("plugin Name: " + pluginManager.getName(this) + ", xml: " + packet.toXML());
	}

	private void debug(Object message) {
		if (true) {
			System.out.println(message);
		}
	}

	@Override
	public void destroyPlugin() {
		interceptorManager.removeInterceptor(this);
		debug("销毁聊天记录插件成功！");
	}

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		interceptorManager.addInterceptor(this);
		pluginManager = manager;

		debug("安装聊天记录插件成功！");
	}

}
