import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

    public static HashMap<String, List<String>> read = new HashMap<>();
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    public static void main(String[] args) throws Exception{
        System.out.println(ANSI_CYAN+"(*´ヮ｀)< LineAPI for JAVA");
        System.out.println(ANSI_BLUE+"version "+ANSI_RESET+":"+ANSI_GREEN+"1.0 beta");
        System.out.println(ANSI_RED+"codename "+ANSI_RESET+":"+ANSI_YELLOW+" yuzu");
        System.out.println(ANSI_PURPLE+"author "+ANSI_RESET+":"+ANSI_YELLOW+" tomo"+ANSI_RESET);
        //LoginResult result = new LINEClient().loginByQRCode();
        //System.out.println("authToken:"+result.authToken);
        //System.out.println("certificate:"+result.certificate);
        //System.out.println(System.currentTimeMillis()/1000 - 1504080453143L/1000);
        LINEClient lineclient = new LINEClient();
        TalkService.Client client = lineclient.loginWithAuthToken("token");
        System.out.println("displayName : " + client.getProfile().displayName);
        System.out.println("mid : " + client.getProfile().mid);
        System.out.println("");
        List<String> inviteIds = client.getGroupIdsInvited();
        for (String inviteId : inviteIds) {
            client.acceptGroupInvitation(0, inviteId);
        }
        List<String> gids = client.getGroupIdsJoined();
        for (String gid : gids) {
            read.put(gid, new ArrayList<>());
        }
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/P4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access", "token");
        TProtocol protocol = new TCompactProtocol(transport);
        TalkService.Client poll_client = new TalkService.Client(protocol);
        transport.open();
        System.out.println(ANSI_CYAN+"起動完了！\n"+ANSI_RESET);
        while (true) {
            /*if (transport.isOpen()) {
                transport.close();
                transport.open();
            } else {
                transport.open();
            }*/
            List<Operation> ops = poll_client.fetchOperations(client.getLastOpRevision(), 99);
            for (Operation op : ops) {
                try {
                    switch (op.type) {
                        case NOTIFIED_ADD_CONTACT:
                            client.sendMessage(0, new Message().setTo(op.param1).setText(client.getContact(op.param1).displayName + "さん、追加ありがとう！！\nグルに招待すると自動で参加します\nいろいろなグループに招待してね！！"));
                            break;
                        case NOTIFIED_INVITE_INTO_GROUP:
                            client.acceptGroupInvitation(0, op.param1);
                            break;
                        case ACCEPT_GROUP_INVITATION:
                            if (client.getGroup(op.param1).members.size() < 10) {
                                client.sendMessage(0, new Message().setTo(op.param1).setText("10人以上のグループに招待してください"));
                                client.leaveGroup(0, op.param1);
                            } else {
                                read.put(op.param1, new ArrayList<>());
                                client.sendMessage(0, new Message().setTo(op.param1).setText("(*´ヮ｀)＜招待ありがとうございます\n参加させていただきました\n既読ボットです！\nまずはhelpでコマンド一覧をチェックしてね"));
                            }
                            break;
                        case NOTIFIED_ACCEPT_GROUP_INVITATION:
                            client.sendMessage(0, new Message().setTo(op.param1).setText(client.getContact(op.param2).displayName + "さん、よろしく！"));
                            break;
                        case INVITE_INTO_ROOM:
                            client.sendMessage(0, new Message().setTo(op.param1).setText("Roomでは使用できません\n" + client.getContact(op.param2).displayName + "さん、申し訳ありません！"));
                            client.leaveRoom(0, op.param1);
                            break;
                        case NOTIFIED_UPDATE_GROUP:
                            String name = client.getContact(op.param2).displayName;
                            if (op.param3.equals("1")) {
                                client.sendMessage(0, new Message().setTo(op.param1).setText(name + "さんがグループ名を変更しました"));
                            } else if (op.param3.equals("2")) {
                                client.sendMessage(0, new Message().setTo(op.param1).setText(name + "さんがグループ画像を変更しました"));
                            } else if (op.param3.equals("4")) {
                                if (client.getCompactGroup(op.param1).preventedJoinByTicket) {
                                    client.sendMessage(0, new Message().setTo(op.param1).setText(name + "さんがURL招待をブロックしました"));
                                } else {
                                    client.sendMessage(0, new Message().setTo(op.param1).setText(name + "さんがURL招待を許可しました"));
                                }
                            } else if (op.param3.equals("8")) {
                                client.sendMessage(0, new Message().setTo(op.param1).setText(name + "さんが通知設定を変更しました"));
                            }
                            break;
                        case NOTIFIED_KICKOUT_FROM_GROUP:
                            if (op.param3.equals(lineclient.mid)) {
                                read.remove(op.param1);
                            }
                            break;
                        case SEND_MESSAGE:
                        case RECEIVE_MESSAGE:
                            if (op.message.contentType.equals(ContentType.NONE)) {
                                System.out.println(op.message.text);
                                if (op.message.text.toLowerCase().equals("help")) {
                                    String msg = "このボットのコマンドを教えるね！\n\n[help]\nヘルプを送信\n[whoread]\n誰が既読したか(初回時は正しい結果が出ないことがあります)\n[reset]\n既読地点を更新します\n[gid]\ngid(midの一種)を送信します\n[mid]\n自分のmidを送信します\n[speed]\nBotの反応時間を取得します\n[leave here]\nグループを退出します\n\n[その他の機能]\n連絡先送信\nTL共有時に情報の送信\nグループボード作成時に情報の送信\n\n・質問やサポートはこちらから\nhttps://twitter.com/tomo_linebot";
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText(msg));
                                } else if (op.message.text.toLowerCase().equals("speed")) {
                                    long systime = System.currentTimeMillis();
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText("UNIXTime : " + systime));
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText("createTime : " + op.createdTime));
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText("diff : " + (systime - op.createdTime) + "ミリ秒"));
                                } else if (op.message.text.toLowerCase().equals("gid")) {
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText(op.message.to));
                                } else if (op.message.text.toLowerCase().equals("mid")) {
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText(op.message._from));
                                } else if (op.message.text.toLowerCase().equals("loginz")) {
                                    login(client, op.message.to);
                                } else if (op.message.text.toLowerCase().equals("reset")) {
                                    read.get(op.message.to).clear();
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText("既読一覧をリセットしました"));
                                } else if (op.message.text.replace(" ", "").replace("?", "").toLowerCase().equals("whoread")) {
                                    if (read.get(op.message.to).isEmpty()) {
                                        client.sendMessage(0, new Message().setTo(op.message.to).setText("誰も既読していません。"));
                                    } else {
                                        String readers_data = "";
                                        for (int count1 = 0; count1 < read.get(op.message.to).size(); count1++) {
                                            readers_data = readers_data + client.getContact(read.get(op.message.to).get(count1)).displayName + "さん、\n";
                                        }
                                        client.sendMessage(0, new Message().setTo(op.message.to).setText("既読したのは、\n" + readers_data + "です"));
                                    }
                                } else if (op.message.text.toLowerCase().equals("leave here")) {
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText("グループを退出します\nあなたと出会えて本当に良かったです"));
                                    client.leaveGroup(0, op.message.to);
                                }
                            } else if (op.message.contentType.equals(ContentType.CONTACT)) {
                                Contact contact = client.getContact(op.message.contentMetadata.get("mid"));
                                client.sendMessage(0, new Message().setTo(op.message.to).setText("[mid]\n" + contact.mid + "\n\n[displayName]\n" + contact.displayName + "\n\n[statusMessage]\n" + contact.statusMessage + "\n\n[picturePath]\nhttps://profile.line-scdn.net" + contact.picturePath));
                            } else if (op.message.contentType.equals(ContentType.POSTNOTIFICATION)) {
                                if (op.message.contentMetadata.get("serviceType").equals("MH")) {
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText(client.getContact(op.message._from).displayName + "さんが" + op.message.contentMetadata.get("officialName") + "さんのTLを共有したよ"));
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText(op.message.contentMetadata.get("postEndUrl")));
                                } else if (op.message.contentMetadata.get("serviceType").equals("GB")) {
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText(client.getContact(op.message._from).displayName + "さんがグループノートに投稿したよ"));
                                    client.sendMessage(0, new Message().setTo(op.message.to).setText(op.message.contentMetadata.get("postEndUrl")));
                                }
                            }
                            break;
                        case NOTIFIED_READ_MESSAGE:
                            // param1 group
                            // param2 mid
                            if (read.containsKey(op.param1)) {
                                List<String> list = read.get(op.param1);
                                if (!list.contains(op.param2)) {
                                    list.add(op.param2);
                                    read.put(op.param1, list);
                                }
                            } else {
                                read.put(op.param1, new ArrayList<>());
                                List<String> list = read.get(op.param1);
                                list.add(op.param2);
                                read.put(op.param1, list);
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void login(TalkService.Client client2, String mid1) throws Exception {
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/api/v4/TalkService.do");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        TProtocol protocol = new TCompactProtocol(transport);
        TalkService.Client client = new TalkService.Client(protocol);
        transport.open();
        AuthQrcode qr = client.getAuthQrcode(true, "YUZU");
        client2.sendMessage(0, new Message().setTo(mid1).setText("(*´ヮ｀)＜二分以内にこのリンクを踏んでくださいね！"));
        client2.sendMessage(0, new Message().setTo(mid1).setText("line://au/q/" + qr.verifier));
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
        String authtoken = loginresult.authToken;
        System.out.println("AUTHTOKEN * " + authtoken);
        client2.sendMessage(0, new Message().setTo(mid1).setText("(*´ヮ｀)＜authtokenはコンソールで確認してくださいね！"));
    }

}