import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LINEClient {

    public String authtoken;
    public String certificate;
    public String mid;
    public LINEClient(){}

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
        TalkService.Client client = new TalkService.Client(protocol);
        transport.open();
        mid = client.getProfile().mid;
        return client;
    }

    public AccountSupervisorService.Client getAccountSupervisor(String token) throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/JCH4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access",token);
        TProtocol protocol = new TCompactProtocol(transport);
        AccountSupervisorService.Client client = new AccountSupervisorService.Client(protocol);
        transport.open();
        return client;
    }

    public BuddyManagementService.Client getBuddyManagement(String token) throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/JBUDDY4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access",token);
        TProtocol protocol = new TCompactProtocol(transport);
        BuddyManagementService.Client client = new BuddyManagementService.Client(protocol);
        transport.open();
        return client;
    }

    public ChannelService.Client getChannel(String token) throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/JCH4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access",token);
        TProtocol protocol = new TCompactProtocol(transport);
        ChannelService.Client client = new ChannelService.Client(protocol);
        transport.open();
        return client;
    }

    public ShopService.Client getShop(String token) throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/JSHOP4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access",token);
        TProtocol protocol = new TCompactProtocol(transport);
        ShopService.Client client = new ShopService.Client(protocol);
        transport.open();
        return client;
    }

    public SnsAdaptorService.Client getSnsAdaptor(String token) throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/JSHOP4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access",token);
        TProtocol protocol = new TCompactProtocol(transport);
        SnsAdaptorService.Client client = new SnsAdaptorService.Client(protocol);
        transport.open();
        return client;
    }

    public UniversalNotificationService.Client getUniversalNotification(String token) throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/JSHOP4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access",token);
        TProtocol protocol = new TCompactProtocol(transport);
        UniversalNotificationService.Client client = new UniversalNotificationService.Client(protocol);
        transport.open();
        return client;
    }

    public TalkService.Client getPoll(String token) throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/P4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access",token);
        TProtocol protocol = new TCompactProtocol(transport);
        TalkService.Client client = new TalkService.Client(protocol);
        transport.open();
        return client;
    }
}
