import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class LINEClient {

    public String authtoken;
    public String certificate;
    public String mid;
    public TalkService.Client client;

    public LINEClient(String token)throws Exception{
        authtoken = token;
        loginWithAuthToken(token);
    }

    public LoginResult loginByQRCode() throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/api/v4/TalkService.do");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        TProtocol protocol = new TCompactProtocol(transport);
        TalkService.Client client = new TalkService.Client(protocol);
        transport.open();
        AuthQrcode qr = client.getAuthQrcode(true, "YUZU");
        System.out.println("(*´ヮ｀)＜二分以内にこのリンクを踏んでくださいね！");
        System.out.println("line://au/q/" + qr.verifier);
        URL myURL = new URL("https://gd2.line.naver.jp/Q");
        HttpURLConnection conn = (HttpURLConnection) myURL.openConnection();
        conn.setRequestProperty("X-Line-Access", qr.verifier);
        conn.setRequestMethod("GET");
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setInstanceFollowRedirects(true);
        conn.connect();
        InputStream in;
        try {
            in = conn.getInputStream();
        } catch (Exception e) {
            in = conn.getErrorStream();
        }
        String encoding = conn.getContentEncoding();
        if (null == encoding) {
            encoding = "UTF-8";
        }
        InputStreamReader inReader = new InputStreamReader(in, encoding);
        BufferedReader bufReader = new BufferedReader(inReader);
        String result = "";
        String line = null;
        while ((line = bufReader.readLine()) != null) {
            result = result + line;
        }
        JSONObject jo = new JSONObject(result);
        THttpClient transport1 = new THttpClient("https://gd2.line.naver.jp/api/v4p/rs");
        transport1.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        TProtocol protocol1 = new TCompactProtocol(transport1);
        TalkService.Client client1 = new TalkService.Client(protocol1);
        transport1.open();
        loginRequest req = new loginRequest()
                .setAccessLocation("8.8.8.8")
                .setE2eeVersion(0)
                .setType(LoginType.QRCODE.getValue())
                .setKeepLoggedIn(true)
                .setSystemName("YUZU")
                .setIdentityProvider(IdentityProvider.LINE.getValue())
                .setVerifier(jo.getJSONObject("result").getString("verifier"));
        LoginResult loginresult = client1.loginZ(req);
        bufReader.close();
        inReader.close();
        in.close();
        transport.close();
        authtoken = loginresult.authToken;
        certificate = loginresult.certificate;
        return loginresult;
    }

    public TalkService.Client loginWithAuthToken(String token) throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/S4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access",token);
        TProtocol protocol = new TCompactProtocol(transport);
        TalkService.Client myclient = new TalkService.Client(protocol);
        transport.open();
        client = myclient;
        mid = myclient.getProfile().mid;
        return myclient;
    }

    public void acceptGroupInvitation(String groupId) throws Exception{
        client.acceptGroupInvitation(0,groupId);
    }

    public void leaveRoom(String roomId) throws Exception{
        client.leaveRoom(0,roomId);
    }

    public void leaveGroup(String groupId) throws Exception{
        client.leaveGroup(0,groupId);
    }

    public Contact getContact(String id) throws Exception{
        return client.getContact(id);
    }

    public Group getGroup(String groupId) throws Exception{
        return client.getGroup(groupId);
    }

    public Group getCompactGroup(String groupId) throws Exception{
        return client.getCompactGroup(groupId);
    }

    public List<String> getGroupIdsInvited() throws Exception{
        return client.getGroupIdsInvited();
    }

    public List<String> getGroupIdsJoined() throws Exception{
        return client.getGroupIdsJoined();
    }

    public Profile getProfile() throws Exception{
        return client.getProfile();
    }

    public long getLastOpRevision() throws Exception{
        return client.getLastOpRevision();
    }

    public void sendText(String to,String text) throws Exception{
        client.sendMessage(0,new Message().setTo(to).setText(text));
    }

    public void sendMessage(Message msg) throws Exception{
        client.sendMessage(0,msg);
    }

    public void setGroupName(String groupId,String name) throws Exception{
        Group group = client.getGroup(groupId);
        client.updateGroup(0,new Group().setId(groupId).setPictureStatus(group.pictureStatus).setPreventedJoinByTicket(group.preventedJoinByTicket).setName(name));
    }

    public String getGroupURL(String groupMid) throws Exception{
        return client.reissueGroupTicket(groupMid);
    }
}
