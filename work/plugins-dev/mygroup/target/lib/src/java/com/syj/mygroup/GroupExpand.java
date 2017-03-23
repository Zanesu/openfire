package com.syj.mygroup;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.dom4j.Node;
import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Packet;

public class GroupExpand implements Plugin,PacketInterceptor{
	private static final Logger log = LoggerFactory.getLogger(GroupExpand.class);

	private XMPPServer server;

	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		InterceptorManager.getInstance().addInterceptor(this);
		server = XMPPServer.getInstance();
	}

	@Override
	public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
			throws PacketRejectedException {
		if (incoming && processed && packet instanceof IQ) {
			JID fromJID = packet.getFrom();
			if (fromJID == null)
				return;
			JID jid = null;
			if ((jid = packet.getTo()) == null)
				return;
			if (jid.getDomain() == null || !jid.getDomain().startsWith("conference"))
				return;
			IQ iq = (IQ) packet;
			if (!iq.getType().equals(org.xmpp.packet.IQ.Type.get))
				return;
			Element iqele = iq.getElement();
			Iterator<Node> iter = iqele.nodeIterator();
			boolean flag = false;
			while (iter.hasNext()) {
				Node node = iter.next();
				flag = node.asXML().contains("<query xmlns=\"myjoinedroom\"");
				if (flag)
					break;
			}
			if (!flag)
				return;
			// 获取该用户的所有群组返回
			/*
			 * <iq from='macbeth.shakespeare.lit' id='disco2'
			 * to='hag66@shakespeare.lit/pda' type='result'> <rooms
			 * xmlns='myjoinedroom'> <room> <jid>1</jid>
			 * <name>sadfsad@sdf.sdf</name>
			 * 
			 * ........
			 * 
			 * </room> </rooms> </iq>
			 */
			IQ responseIQ = new IQ(org.xmpp.packet.IQ.Type.result, iq.getID());
			responseIQ.setTo(fromJID);
			Element queryEle = responseIQ.setChildElement("rooms", "myjoinedroom");
			List<Map<String, String>> mucs = getMUCInfo(fromJID.toBareJID());
			if (mucs != null && mucs.size() > 0) {
				Element itemEle = null;
				for (Map<String, String> map : mucs) {
					itemEle = queryEle.addElement("room");
					itemEle.addElement("serviceId").setText(map.get("serviceId"));
					itemEle.addElement("name").setText(map.get("roomName"));
					itemEle.addElement("roomid").setText(map.get("roomid"));
					itemEle.addElement("nickName").setText(map.get("nickName"));
					itemEle.addElement("memberJID").setText(map.get("memberJID"));
				}
			}

			server.getRoutingTable().routePacket(fromJID, responseIQ, true);
		}
	}

	public List<Map<String, String>> getMUCInfo(String jid) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		String sql = "select ofmucroom.serviceID, ofmucroom.name, ofmucroom.roomid ,ofmucmember.nickname,ofmucmember.jid from ofmucroom join ofmucmember on ofmucroom.roomID = ofmucmember.roomID and ofmucmember.jid = ?";
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Map<String, String> map = null;
		try {
			connection = DbConnectionManager.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, jid);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				map = new HashMap<String, String>();
				map.put("serviceId", resultSet.getString(1) == null ? "" : resultSet.getString(1));
				map.put("roomName", resultSet.getString(2) == null ? "" : resultSet.getString(2));
				map.put("roomid", resultSet.getString(3) == null ? "" : resultSet.getString(3));
				map.put("nickName", resultSet.getString(4) == null ? "" : resultSet.getString(4));
				map.put("memberJID", resultSet.getString(5) == null ? "" : resultSet.getString(5));
				list.add(map);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
			log.error(e1.getMessage());
		} finally {
			DbConnectionManager.closeConnection(resultSet, statement, connection);
		}
		return list;
	}

	@Override
	public void destroyPlugin() {
		// TODO Auto-generated method stub

	}
}
