package com.syj.newsfriend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.jivesoftware.database.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 
/**
 * <b>function:</b> 聊天记录db操作类
 * @author hoojo
 * @createDate 2012-9-19 下午04:15:43
 * @file DbChatLogsManager.java
 * @package com.iflashbuy.openfire.chat.logs
 * @project OpenfirePlugin
 * @blog http://blog.csdn.net/IBM_hoojo
 * @email hoojo_@126.com
 * @version 1.0
 */
public class DbChatLogsManager {
 
    private static final Logger Log = LoggerFactory.getLogger(DbChatLogsManager.class);
    private static final DbChatLogsManager CHAT_LOGS_MANAGER = new DbChatLogsManager();
    
    private DbChatLogsManager() {
    }
    
    public static DbChatLogsManager getInstance() {
        return CHAT_LOGS_MANAGER;
    }
    
    private static final String LOGS_COUNT = "SELECT count(1) FROM offriendsLogs";
    private static final String LOGS_LAST_MESSAGE_ID = "SELECT max(messageId) FROM offriendsLogs";
    private static final String LOGS_FIND_BY_ID = "SELECT messageId, sessionJID, sender, receiver, createDate, length, content FROM offriendsLogs where messageId = ?";
    private static final String LOGS_REMOVE = "UPDATE offriendsLogs set state = 1 where messageId = ?";
    private static final String FRIENDSLOGS_DELETE="DELETE FROM offriendsLogs WHERE messageId = ?";
    private static final String FRIENDSLOGS_QUERY="SELECT messageId, sessionJID, sender, receiver, createDate, length, detail FROM offriendsLogs where receiver = ";
    
    private static final String LOGS_INSERT = "INSERT INTO offriendsLogs(messageId, sessionJID, sender, receiver, createDate, length,detail, state) VALUES(?,?,?,?,?,?,?,?)";
    private static final String LOGS_QUERY = "SELECT messageId, sessionJID, sender, receiver, createDate, length FROM offriendsLogs where state = 0";
    private static final String LOGS_SEARCH = "SELECT * FROM offriendsLogs where state = 0";
    private static final String LOGS_LAST_CONTACT = "SELECT distinct receiver FROM offriendsLogs where state = 0 and sender = ?";
    private static final String LOGS_ALL_CONTACT = "SELECT distinct sessionJID FROM offriendsLogs where state = 0";
    
    /**
     * <b>function:</b> 获取最后一个id
     * @author hoojo
     * @createDate 2012-9-19 下午08:13:33
     * @return 最后一个记录id
     */
    public int getLastId() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = -1;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOGS_LAST_MESSAGE_ID);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = 0;
            }
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            return 0;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
        return count;
    }
    
    /**
     * <b>function:</b> 获取总数量
     * @author hoojo
     * @createDate 2012-9-19 下午08:14:59
     * @return 总数量
     */
    public int getCount() {
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = -1;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOGS_COUNT);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            } else {
                count = 0;
            }
        } catch (SQLException sqle) {
            Log.error(sqle.getMessage(), sqle);
            return 0;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
        return count;
    }
    
    /**
     * <b>function:</b> 删除聊天记录信息
     * @author hoojo
     * @createDate 2012-9-19 下午08:25:48
     * @param id 聊天信息id
     * @return
     */
    public boolean remove(Long id) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(FRIENDSLOGS_DELETE);
            pstmt.setLong(1, id);
            return pstmt.execute();
            
        } catch (SQLException sqle) {
            Log.error("chatLogs remove exception: {}", sqle);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
    
    /**
     * <b>function:</b> 添加聊天记录信息
     * @author hoojo
     * @createDate 2012-9-19 下午08:37:52
     * @param logs ChatLogs 聊天记录对象
     * @return 是否添加成功
     */
    public boolean add(ChatLogs logs) {
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOGS_INSERT);
            int i = 1;
            pstmt.setLong(i++, logs.getMessageId());
            pstmt.setString(i++, logs.getSessionJID());
            pstmt.setString(i++, logs.getSender());
            pstmt.setString(i++, logs.getReceiver());
            pstmt.setTimestamp(i++, logs.getCreateDate());
            
            pstmt.setInt(i++, logs.getLength());
            pstmt.setString(i++, logs.getDetail());
            pstmt.setInt(i++, 0);
          
            return pstmt.execute();
        } catch (SQLException sqle) {
            Log.error("chatLogs add exception: {}", sqle);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
    
    /**
     * <b>function:</b> 通过id查询聊天记录信息
     * @author hoojo
     * @createDate 2012-9-19 下午09:32:19
     * @param id 消息id
     * @return ChatLogs
     */
    public ChatLogs find(int id) {
        Connection con = null;
        PreparedStatement pstmt = null;
        ChatLogs logs = null;
        
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOGS_FIND_BY_ID);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs = new ChatLogs();
                logs.setMessageId(rs.getInt("messageId"));
                logs.setCreateDate(rs.getTimestamp("createDate"));
                logs.setLength(rs.getInt("length"));
                logs.setSessionJID(rs.getString("sessionJID"));
                logs.setSender(rs.getString("sender"));
                logs.setReceiver(rs.getString("receiver"));
            }
            return logs;
        } catch (SQLException sqle) {
            Log.error("chatLogs find exception: {}", sqle);
            return logs;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
    
    /**
     * <b>function:</b> 多条件搜索查询，返回List&lt;ChatLogs>集合
     * @author hoojo
     * @createDate 2012-9-19 下午09:34:45
     * @param entity ChatLogs
     * @return 返回List&lt;ChatLogs>集合
     */
    public List<ChatLogs> query(String reciver) {
        Connection con = null;
        Statement pstmt = null;
        ChatLogs logs = null;
 
        List<ChatLogs> result = new ArrayList<ChatLogs>();
        
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.createStatement();
            
            String sql = FRIENDSLOGS_QUERY;
          sql+="'"+reciver+"'";
            sql += " order by createDate asc";
            System.out.println(sql);
            ResultSet rs = pstmt.executeQuery(sql);
            while (rs.next()) {
                logs = new ChatLogs();
                logs.setMessageId(rs.getInt("messageId"));
                logs.setCreateDate(rs.getTimestamp("createDate"));
                logs.setLength(rs.getInt("length"));
                logs.setSessionJID(rs.getString("sessionJID"));
                logs.setSender(rs.getString("sender"));
                logs.setReceiver(rs.getString("receiver"));
                logs.setDetail(rs.getString("detail"));
                result.add(logs);
            }
            return result;
        } catch (SQLException sqle) {
            Log.error("chatLogs search exception: {}", sqle);
            return result;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
    
    /**
     * <b>function:</b> 多条件搜索查询，返回List<Map>集合
     * @author hoojo
     * @createDate 2012-9-19 下午09:33:28
     * @param entity ChatLogs
     * @return List<HashMap<String, Object>>
     */
    public List<HashMap<String, Object>> search(ChatLogs entity) {
        Connection con = null;
        Statement pstmt = null;
 
        List<HashMap<String, Object>> result = new ArrayList<HashMap<String, Object>>();
        
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.createStatement();
            
            String sql = LOGS_SEARCH;
            if (entity != null) {
                if (!StringUtils.isEmpty(entity.getSender()) && !StringUtils.isEmpty(entity.getReceiver())) {
                    sql += " and (sender = '" + entity.getSender() + "' and receiver = '" + entity.getReceiver() + "')";
                    sql += " or (receiver = '" + entity.getSender() + "' and sender = '" + entity.getReceiver() + "')";
                    
                } else {
                    if (!StringUtils.isEmpty(entity.getSender())) {
                        sql += " and sender = '" + entity.getSender() + "'";
                    } 
                    if (!StringUtils.isEmpty(entity.getReceiver())) {
                        sql += " and receiver = '" + entity.getReceiver() + "'";
                    }
                }
             
                if (entity.getCreateDate() != null) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String crateatDate = df.format(new Date(entity.getCreateDate().getTime()));
                    sql += " and to_char(createDate, 'yyyy-mm-dd') = '" + crateatDate + "'";
                }
            }
            sql += " order by createDate asc";
            ResultSet rs = pstmt.executeQuery(sql);
            
            ResultSetMetaData rsmd = rs.getMetaData();
            /** 获取结果集的列数*/  
            int columnCount = rsmd.getColumnCount();  
            while (rs.next()) {  
                HashMap<String, Object> map = new HashMap<String, Object>();  
                /** 把每一行以(key, value)存入HashMap, 列名做为key,列值做为value */  
                for (int i = 1; i <= columnCount; ++i) {  
                    String columnVal = rs.getString(i);  
                    if (columnVal == null) {  
                        columnVal = "";  
                    }  
                    map.put(rsmd.getColumnName(i), columnVal);  
                }  
                /** 把装有一行数据的HashMap存入list */  
                result.add(map);  
            }  
            return result;
        } catch (SQLException sqle) {
            Log.error("chatLogs search exception: {}", sqle);
            return result;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
    
    /**
     * <b>function:</b> 最近联系人
     * @author hoojo
     * @createDate 2013-3-24 下午4:38:51
     * @param entity 聊天记录实体
     * @return 最近联系人集合
     */
    public List<String> findLastContact(ChatLogs entity) {
        Connection con = null;
        PreparedStatement pstmt = null;
        List<String> result = new ArrayList<String>();
        
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOGS_LAST_CONTACT);
            pstmt.setString(1, entity.getSender());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("receiver"));
            }
            return result;
        } catch (SQLException sqle) {
            Log.error("chatLogs find exception: {}", sqle);
            return result;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
    
    /**
     * <b>function:</b> 查找所有聊天用户
     * @author hoojo
     * @createDate 2013-3-24 下午4:37:40
     * @return 所有聊天用户sessionJID集合
     */
    public List<String> findAllContact() {
        Connection con = null;
        PreparedStatement pstmt = null;
        List<String> result = new ArrayList<String>();
        
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(LOGS_ALL_CONTACT);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("sessionJID"));
            }
            return result;
        } catch (SQLException sqle) {
            Log.error("chatLogs find exception: {}", sqle);
            return result;
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
    }
}