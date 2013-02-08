package liujing.magdownload.gwt.client.message;
import com.google.gwt.i18n.client.Messages;


public interface PortalMessage extends Messages
{
    @DefaultMessage("一个下载工具,它可以帮你下载<ul class=\"ulist\"><li>在线杂志<li>在线漫画</ul>")
    String introduction();

    @DefaultMessage("Contact author <a href=\"mailto:liujing.break@gmail.com\">liujing.break@gmail.com</a> | 新浪微博 <a href=\"http://www.weibo.com/ljbreak\">http://www.weibo.com/ljbreak</a>")
    String address();
}
