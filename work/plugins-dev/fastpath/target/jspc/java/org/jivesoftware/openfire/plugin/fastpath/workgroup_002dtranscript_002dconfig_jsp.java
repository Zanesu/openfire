/*
 * Generated by the Jasper component of Apache Tomcat
 * Version: JspC/ApacheTomcat8
 * Generated at: 2016-12-19 07:14:04 UTC
 * Note: The last modified time of this file was set to
 *       the last modified time of the source file after
 *       generation to assist with modification tracking.
 */
package org.jivesoftware.openfire.plugin.fastpath;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import org.jivesoftware.xmpp.workgroup.utils.ModelUtil;
import org.jivesoftware.xmpp.workgroup.WorkgroupManager;
import org.jivesoftware.xmpp.workgroup.Workgroup;
import org.xmpp.packet.JID;
import org.jivesoftware.xmpp.workgroup.DbProperties;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;

public final class workgroup_002dtranscript_002dconfig_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static final javax.servlet.jsp.JspFactory _jspxFactory =
          javax.servlet.jsp.JspFactory.getDefaultFactory();

  private static java.util.Map<java.lang.String,java.lang.Long> _jspx_dependants;

  private javax.el.ExpressionFactory _el_expressionfactory;
  private org.apache.tomcat.InstanceManager _jsp_instancemanager;

  public java.util.Map<java.lang.String,java.lang.Long> getDependants() {
    return _jspx_dependants;
  }

  public void _jspInit() {
    _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
    _jsp_instancemanager = org.apache.jasper.runtime.InstanceManagerFactory.getInstanceManager(getServletConfig());
  }

  public void _jspDestroy() {
  }

  public void _jspService(final javax.servlet.http.HttpServletRequest request, final javax.servlet.http.HttpServletResponse response)
        throws java.io.IOException, javax.servlet.ServletException {

    final javax.servlet.jsp.PageContext pageContext;
    javax.servlet.http.HttpSession session = null;
    final javax.servlet.ServletContext application;
    final javax.servlet.ServletConfig config;
    javax.servlet.jsp.JspWriter out = null;
    final java.lang.Object page = this;
    javax.servlet.jsp.JspWriter _jspx_out = null;
    javax.servlet.jsp.PageContext _jspx_page_context = null;


    try {
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			"workgroup-error.jsp", true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;

      out.write('\r');
      out.write('\n');
 try { 
      out.write("\r\n\r\n ");

     String wgID = request.getParameter("wgID");
     final WorkgroupManager workgroupManager = WorkgroupManager.getInstance();
     Workgroup workgroup = workgroupManager.getWorkgroup(new JID(wgID));

     boolean delete = request.getParameter("removeChanges") != null;
     boolean save = request.getParameter("save") != null && !delete;

     boolean emailConfigured = false;

     String property = JiveGlobals.getProperty("mail.configured");
     if (ModelUtil.hasLength(property)) {
        emailConfigured = true;
     }

     DbProperties props = workgroup.getProperties();
     String context = "jive.transcript";
     String from = "";
     String subject = "";
     String message = "";
     String fromEmail = "";
     if (save) {
         from = request.getParameter("from");
         fromEmail = request.getParameter("fromEmail");
         subject = request.getParameter("subject");
         message = request.getParameter("message");
         if (ModelUtil.hasLength(from) && ModelUtil.hasLength(subject) && ModelUtil.hasLength(message) && ModelUtil.hasLength(fromEmail)) {
             props.setProperty(context + ".from", from);
             props.setProperty(context + ".fromEmail", fromEmail);
             props.setProperty(context + ".subject", subject);
             props.setProperty(context + ".message", message);
         }
     }
     else if(delete){
        props.deleteProperty(context + ".from");
        props.deleteProperty(context + ".fromEmail");
        props.deleteProperty(context + ".subject");
        props.deleteProperty(context + ".message");
         System.out.println("DELTED");
     }
     else {
         from = props.getProperty(context + ".from");
         subject = props.getProperty(context + ".subject");
         message = props.getProperty(context + ".message");
         fromEmail = props.getProperty(context + ".fromEmail");
         if (from == null) {
             from = "";
         }

         if (subject == null) {
             subject = "";
         }

         if (message == null) {
             message = "";
         }

         if (fromEmail == null) {
             fromEmail = "";
         }
     }
 
      out.write("\r\n<html>\r\n    <head>\r\n        <title>");
      out.print( "Transcript Settings for "+wgID);
      out.write("</title>\r\n        <meta name=\"subPageID\" content=\"workgroup-transcript-config\"/>\r\n        <meta name=\"extraParams\" content=\"wgID=");
      out.print( wgID );
      out.write("\"/>\r\n        <!--<meta name=\"helpPage\" content=\"set_header_and_message_text_for_transcript_emails.html\"/>-->\r\n    </head>\r\n    <body>\r\n\r\n    Specify specific configuration when a user requests a transcript from the system.\r\n    <br/><br/>\r\n\r\n    ");
 if(save) { 
      out.write("\r\n   <div class=\"success\">\r\n    Transcript Settings have been updated\r\n    </div>\r\n    ");
}
      out.write("\r\n\r\n\r\n       ");
 if(!emailConfigured){ 
      out.write("\r\n            <div class=\"error\">\r\n                Transcripts cannot be sent until you configure your <a href=\"../../system-email.jsp\">email settings</a>.\r\n            </div>\r\n    ");
 } 
      out.write("\r\n\r\n    <p/>\r\n    <form action=\"workgroup-transcript-config.jsp\" method=\"get\" name=\"offline\">\r\n    <input type=\"hidden\" name=\"wgID\" value=\"");
      out.print( wgID );
      out.write("\" />\r\n    <div>\r\n        <div class=\"jive-contentBoxHeader\">\r\n        Conversation Transcript Configuration\r\n        </div>\r\n        <table width=\"100%\" cellpadding=\"3\" cellspacing=\"3\" border=\"0\" class=\"jive-contentBox\">\r\n                <tr valign=\"top\">\r\n                    <td>From:</td>\r\n                    <td>\r\n                        <input type=\"text\" size=\"40\" name=\"from\" value=\"");
      out.print( from );
      out.write("\" /><br/><span class=\"jive-description\">Specify who the transcript is from, such as ACME Company.</span>\r\n                    </td>\r\n                </tr>\r\n\r\n                  <tr valign=\"top\">\r\n                    <td>Email Address:</td>\r\n                    <td>\r\n                        <input type=\"text\" size=\"40\" name=\"fromEmail\" value=\"");
      out.print( fromEmail );
      out.write("\" /><br/><span class=\"jive-description\">Specify the email address the message will be from. Ex. support@acme.com</span>\r\n                    </td>\r\n                </tr>\r\n\r\n               <tr valign=\"top\">\r\n\r\n                    <td>Subject:</td>\r\n                    <td>\r\n                        <input type=\"text\" size=\"40\" name=\"subject\" value=\"");
      out.print( subject );
      out.write("\"/><br/><span class=\"jive-description\">The subject that will appear to the user.</span>\r\n                    </td>\r\n                </tr>\r\n                <!--  End Of Subject Line -->\r\n                 <tr valign=\"top\">\r\n\r\n                    <td>Message:</td>\r\n                    <td>\r\n                        <textarea name=\"message\" cols=\"40\" rows=\"3\">");
      out.print( message );
      out.write("</textarea><br/><span class=\"jive-description\">Text to prepend to the transcript being sent.</span>\r\n                    </td>\r\n                </tr>\r\n                    <input type=\"hidden\" name=\"save\" value=\"save\">\r\n                 <tr>\r\n                <td colspan=\"1\"> <input type=\"button\" name=\"save\" value=\"Save Changes\" onclick=\"return saveSettings();\" /></td>\r\n                <td colspan=\"1\"> <input type=\"submit\" name=\"removeChanges\" value=\"Remove Changes\"  /></td>\r\n                <td>&nbsp;</td>\r\n                </tr>\r\n            ");
      out.write("\r\n            </table>\r\n       </div>\r\n       </form>\r\n       <script>\r\n       function saveSettings(){\r\n            var from = document.offline.from.value;\r\n            var subject = document.offline.subject.value;\r\n            var message = document.offline.message.value;\r\n\r\n            if(!Jtrim(from) || !Jtrim(subject) || !Jtrim(message)){\r\n                alert(\"All fields are required.\");\r\n                document.offline.from.focus();\r\n                return;\r\n            }\r\n               document.offline.submit();\r\n       }\r\n\r\n\r\n\r\n        function Jtrim(st) {\r\n            var len = st.length;\r\n            var begin = 0, end = len - 1;\r\n            while (st.charAt(begin) == \" \" && begin < len) {\r\n                begin++;\r\n            }\r\n            while (st.charAt(end) == \" \" && end > begin) {\r\n                end--;\r\n            }\r\n            return st.substring(begin, end + 1);\r\n        }\r\n       </script>\r\n\r\n</body>\r\n</html>\r\n\r\n");
 } catch(Exception ex){ex.printStackTrace();} 
    } catch (java.lang.Throwable t) {
      if (!(t instanceof javax.servlet.jsp.SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          try {
            if (response.isCommitted()) {
              out.flush();
            } else {
              out.clearBuffer();
            }
          } catch (java.io.IOException e) {}
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
        else throw new ServletException(t);
      }
    } finally {
      _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}