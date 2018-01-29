import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Main {

    public static HashMap<String, List<String>> read = new HashMap<>();
    public static HashMap<String, Boolean> sticker_status = new HashMap<>();
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    public static void main(String[] args) throws Exception {
        System.out.println(ANSI_CYAN + "(*´ヮ｀)< LineAPI for JAVA");
        System.out.println(ANSI_BLUE + "version " + ANSI_RESET + ":" + ANSI_GREEN + "1.0 beta");
        System.out.println(ANSI_RED + "codename " + ANSI_RESET + ":" + ANSI_YELLOW + " yuzu");
        System.out.println(ANSI_PURPLE + "author " + ANSI_RESET + ":" + ANSI_YELLOW + " tomo" + ANSI_RESET);
        String token = "";
        if(args.length == 0){
            String authtoken = loginByQRCode().authToken;
            token = authtoken;
            System.out.println("これからauthtokenを実行時の引数に追加してください");
            System.out.println("例：java -jar line.jar authtoken");
        }else{
            token = args[0];
        }
        System.out.println("Try this token : "+token);
        LINEClient client = new LINEClient(token);
        System.out.println("displayName : " + client.getProfile().displayName);
        System.out.println("mid : " + client.getProfile().mid);
        System.out.println("authtoken : "+ client.authtoken+"\n");
        List<String> inviteIds = client.getGroupIdsInvited();
        for (String inviteId : inviteIds) {
            client.acceptGroupInvitation(inviteId);
        }
        List<String> gids = client.getGroupIdsJoined();
        for (String gid : gids) {
            read.put(gid, new ArrayList<>());
            sticker_status.put(gid,false);
        }
        THttpClient shop_transport = new THttpClient("https://gd2.line.naver.jp/SHOP4");
        shop_transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        shop_transport.setCustomHeader("X-Line-Access", token);
        TProtocol shop_protocol = new TCompactProtocol(shop_transport);
        ShopService.Client shop_client = new ShopService.Client(shop_protocol);
        shop_transport.open();

        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/P4");
        transport.setCustomHeader("X-Line-Application", "DESKTOPWIN\t7.18.1\tYUZU\t11.2.5");
        transport.setCustomHeader("X-Line-Access", token);
        TProtocol protocol = new TCompactProtocol(transport);
        TalkService.Client poll_client = new TalkService.Client(protocol);
        transport.open();
        System.out.println(ANSI_CYAN + "起動完了！\n" + ANSI_RESET);
        while (true) {
            try {
                List<Operation> ops = poll_client.fetchOperations(client.getLastOpRevision(), 99);
                for (Operation op : ops) {
                    try {
                        if (op.type != OpType.END_OF_OPERATION && op.type != OpType.UPDATE_PROFILE) {
                            System.out.println(op.type);
                        }
                        switch (op.type) {
                            case NOTIFIED_ADD_CONTACT:
                                client.sendText(op.param1, client.getContact(op.param1).displayName + "さん、追加ありがとう！！\nグルに招待すると自動で参加します\nいろいろなグループに招待してね！！");
                                break;
                            case NOTIFIED_INVITE_INTO_GROUP:
                                client.acceptGroupInvitation(op.param1);
                                break;
                            case ACCEPT_GROUP_INVITATION:
                                read.put(op.param1, new ArrayList<>());
                                sticker_status.put(op.param1,false);
                                client.sendText(op.param1, "(*´ヮ｀)＜招待ありがとうございます\n参加させていただきました\n既読ボットです！\nまずはhelpでコマンド一覧をチェックしてね");
                                break;
                            case NOTIFIED_ACCEPT_GROUP_INVITATION:
                                client.sendText(op.param1, client.getContact(op.param2).displayName + "さん、" + client.getCompactGroup(op.param1).name + "へようこそ！\n柚です、よろしくね！");
                                break;
                            case INVITE_INTO_ROOM:
                                client.sendText(op.param1, "Roomでは使用できません\n" + client.getContact(op.param2).displayName + "さん、申し訳ありません！");
                                client.leaveRoom(op.param1);
                                break;
                            case NOTIFIED_UPDATE_GROUP:
                                String name = client.getContact(op.param2).displayName;
                                if (op.param3.equals("1")) {
                                    client.sendText(op.param1, name + "さんがグループ名を変更しました");
                                } else if (op.param3.equals("2")) {
                                    client.sendText(op.param1, name + "さんがグループ画像を変更しました");
                                } else if (op.param3.equals("4")) {
                                    if (client.getCompactGroup(op.param1).preventedJoinByTicket) {
                                        client.sendText(op.param1, name + "さんがURL招待をブロックしました");
                                    } else {
                                        client.sendText(op.param1, name + "さんがURL招待を許可しました");
                                    }
                                } else if (op.param3.equals("8")) {
                                    client.sendText(op.param1, name + "さんが通知設定を変更しました");
                                }
                                break;
                            case NOTIFIED_KICKOUT_FROM_GROUP:
                                if (op.param3.equals(client.mid)) {
                                    read.remove(op.param1);
                                    sticker_status.remove(op.param1);
                                }
                                break;
                            case SEND_MESSAGE:
                            case RECEIVE_MESSAGE:
                                if (op.message.contentType.equals(ContentType.NONE)) {
                                    if (op.message.text.toLowerCase().equals("help")) {
                                        String msg = "このボットのコマンドを教えるね！\n\n" +
                                                "[help]\nヘルプを送信\n" +
                                                "[whoread]\n誰が既読したか(初回時は正しい結果が出ないことがあります)\n" +
                                                "[reset]\n既読地点を更新します\n" +
                                                "[mid]\n自分のmidを送信します\n" +
                                                "[gid]\ngid(midの一種)を送信します\n" +
                                                "[ginfo]\nグループ情報を取得します\n" +
                                                "[geturl]\nグルのURLを発行します\n" +
                                                "[グル名 into gname]\nグループ名を変更します\n(グル名 > gnameでも可能です)\n" +
                                                "[howinv]\nグループに何人招待されているかを取得します\n" +
                                                "[stkinfo]\n送信されたスタンプの詳細情報を取得します\n" +
                                                "[speed]\nBotの反応時間を取得します\n" +
                                                "[sencon:mid]\n指定されたmidの連絡先を送信します(midは任意のものに置き換えてください)\n" +
                                                "[leave here]\nグループを退出します\n\n" +
                                                "[その他の機能]\n連絡先の詳細を送信\nTL共有時に情報の送信\n通話の開始と終了をお知らせ\nグループボード作成時に情報の送信\n\n" +
                                                "最終更新：2018-01-29 23:08\n" +
                                                "ビルド：15008 BETA\n" +
                                                "・質問やサポートはこちらから\nhttps://twitter.com/tomo_linebot";
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, msg);
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, msg);
                                        }
                                    } else if (op.message.text.contains("風呂")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, "(*´◇｀)＜おつとめご苦労さまでした。\n本日は、爽やかな香りの柚子風呂がございますよ❤\n…あ、わたくしが入っているわけではなく！\nぁゎゎ；");
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´◇｀)＜おつとめご苦労さまでした。\n本日は、爽やかな香りの柚子風呂がございますよ❤\n…あ、わたくしが入っているわけではなく！\nぁゎゎ；");
                                        }
                                    } else if (op.message.text.contains("ハラ減った")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, "(*´◇｀)＜お食事のご用意ができましたぁ♡\n本日は、懐石です♡");
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´◇｀)＜お食事のご用意ができましたぁ♡\n本日は、懐石です♡");
                                        }
                                    } else if (op.message.text.contains("おはよう")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, "(*´ｖ｀)＜おはようございます。\nまだちょっとねむねむですが、がんばりますっ！");
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ｖ｀)＜おはようございます。\nまだちょっとねむねむですが、がんばりますっ！");
                                        }
                                    } else if (op.message.text.contains("仕事") && op.message.text.contains("疲れ")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, "(*´◇｀)＜お風呂掃除～～♪ゴシゴシ。\nあったかお風呂をご用意してます～\n早く帰ってきてく～ださい♪");
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´◇｀)＜お風呂掃除～～♪ゴシゴシ。\nあったかお風呂をご用意してます～\n早く帰ってきてく～ださい♪");
                                        }
                                    } else if (op.message.text.toLowerCase().equals("speed")) {
                                        long systime = System.currentTimeMillis();
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, "(*´ヮ｀)＜お客様、測定結果です！\ndiff : " + (systime - op.createdTime) + "ミリ秒");
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜お客様、測定結果です！\ndiff : " + (systime - op.createdTime) + "ミリ秒");
                                        }
                                    } else if (op.message.text.toLowerCase().equals("gid")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, op.message.to);
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜ここはグループじゃないのでgidは存在しません");
                                        }
                                    } else if (op.message.text.toLowerCase().equals("mid")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, op.message._from);
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, op.message._from);
                                        }
                                    } else if (op.message.text.toLowerCase().equals("reset")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            read.get(op.message.to).clear();
                                            client.sendText(op.message.to, "(*´ヮ｀)＜既読一覧をリセットしました");
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                    } else if (op.message.text.equals("ginfo")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            Group group = client.getGroup(op.message.to);
                                            boolean notexists = false;
                                            if (!group.members.contains(group.creator)) {
                                                notexists = true;
                                            }
                                            if (group.preventedJoinByTicket == false) {
                                                if (notexists) {
                                                    client.sendText(op.message.to,
                                                            "[グループ情報]\n\nグル名：" + group.name + "\n\nGroupId：" + group.id + "\n\nグルの作成者(現在はおられません)：" + group.creator.displayName + "\n\n最も古くから参加してた人：" + group.members.get(0).displayName + "\n\nグル画：https://obs-sg.line-apps.com/" + group.pictureStatus + "\n\nURL招待：許可中");
                                                } else {
                                                    client.sendText(op.message.to,
                                                            "[グループ情報]\n\nグル名：" + group.name + "\n\nGroupId：" + group.id + "\n\nグルの作成者：" + group.creator.displayName + "\n\nグル画：https://obs-sg.line-apps.com/" + group.pictureStatus + "\n\nURL招待：許可中");
                                                }
                                            } else {
                                                if (notexists) {
                                                    client.sendText(op.message.to,
                                                            "[グループ情報]\n\nグル名：" + group.name + "\n\nGroupId：" + group.id + "\n\nグルの作成者(現在はおられません)：" + group.creator.displayName + "\n\n最も古くから参加してた人：" + group.members.get(0).displayName + "\n\nグル画：https://obs-sg.line-apps.com/" + group.pictureStatus + "\n\nURL招待：拒否中");
                                                } else {
                                                    client.sendText(op.message.to,
                                                            "[グループ情報]\n\nグル名：" + group.name + "\n\nGroupId：" + group.id + "\n\nグルの作成者：" + group.creator.displayName + "\n\nグル画：https://obs-sg.line-apps.com/" + group.pictureStatus + "\n\nURL招待：拒否中");
                                                }
                                            }
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                    } else if (op.message.text.equals("howinv")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            List<Contact> invs = client.getGroup(op.message.to).invitee;
                                            if (invs == null) {
                                                client.sendText(op.message.to, "(*´ヮ｀)＜誰も招待されていません");
                                            } else {
                                                client.sendText(op.message.to, "(*´ヮ｀)＜" + invs.size() + "人が招待中です\n(誤差があることがあります)");
                                            }
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                    } else if (op.message.text.replace(" ", "").replace("?", "").toLowerCase().equals("whoread")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            if (read.get(op.message.to).isEmpty()) {
                                                client.sendText(op.message.to, "(*´ヮ｀)＜誰も既読していません。");
                                            } else {
                                                String readers_data = "";
                                                for (int count1 = 0; count1 < read.get(op.message.to).size(); count1++) {
                                                    readers_data = readers_data + client.getContact(read.get(op.message.to).get(count1)).displayName + "さん、\n";
                                                }
                                                client.sendText(op.message.to, "既読したのは、\n" + readers_data + "です");
                                            }
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                    } else if (op.message.text.contains(" into gname")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            if (client.getGroup(op.message.to).name.equals(op.message.text.replace(" into gname", ""))) {
                                                client.sendText(op.message.to, "(*´ヮ｀)＜今とは違う名前にしてください");
                                            } else {
                                                client.setGroupName(op.message.to, op.message.text.replace(" into gname", ""));
                                                client.sendText(op.param1, "(*´ヮ｀)＜柚がグループ名を変更しました");
                                            }
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                    } else if (op.message.text.contains(" > gname")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            if (client.getGroup(op.message.to).name.equals(op.message.text.replace(" > gname", ""))) {
                                                client.sendText(op.message.to, "(*´ヮ｀)＜今とは違う名前にしてください");
                                            } else {
                                                client.setGroupName(op.message.to, op.message.text.replace(" > gname", ""));
                                                client.sendText(op.param1, "(*´ヮ｀)＜柚がグループ名を変更しました");
                                            }
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                    } else if (op.message.text.toLowerCase().contains("geturl")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, "(*´ヮ｀)＜グルのリンクを発行しました\n(URLを再形成したりこのBotを蹴ったりするとリンクは無効になります)\nline://ti/g/" + client.getGroupURL(op.message.to));
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                    } else if (op.message.text.toLowerCase().equals("stkinfo")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            sticker_status.remove(op.message.to);
                                            sticker_status.put(op.message.to, true);
                                            client.sendText(op.message.to, "詳細を見たいスタンプを送信してください");
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                        /*} else if (op.message.text.toLowerCase().equals("占い")) {
                                        List<String> colors = new ArrayList<>();
                                        colors.add("赤");
                                        colors.add("ピンク");
                                        colors.add("黄");
                                        colors.add("青");
                                        colors.add("水");
                                        colors.add("緑");
                                        colors.add("灰");
                                        Random rnd = new Random();
                                        String color = colors.get(rnd.nextInt(colors.size()));
                                        client.sendText(op.message.to, "(*´ヮ｀)＜あなたのラッキーカラーは" + color + "色です！");*/
                                    } else if (op.message.text.toLowerCase().equals("leave here")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, "(*´ヮ｀)＜グループを退出します\nあなたと出会えて本当に良かったです");
                                            read.remove(op.message.to);
                                            sticker_status.remove(op.message.to);
                                            client.leaveGroup(op.message.to);
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                    } else if (op.message.text.toLowerCase().contains("sencon:")) {
                                        if(op.message.toType == MIDType.GROUP) {
                                            String req_mid = op.message.text.toLowerCase().replace("sencon:", "");
                                            try {
                                                Contact contact = client.getContact(req_mid);
                                                Map<String, String> map = new HashMap<>();
                                                map.put("mid", contact.mid);
                                                map.put("displayName", contact.displayName);
                                                client.sendMessage(new Message().setTo(op.message.to).setContentType(ContentType.CONTACT).setContentMetadata(map));
                                            } catch (Exception e) {
                                                client.sendText(op.message.to, "(*´ヮ｀)＜指定されたユーザーは存在しません");
                                            }
                                        }else if(op.message.toType == MIDType.USER){
                                            client.sendText(op.message._from, "(*´ヮ｀)＜このコマンドはグループ以外でサポートされません");
                                        }
                                    }
                                } else if (op.message.contentType.equals(ContentType.CALL)) {
                                    // S (started) or E (ended)
                                    String status = op.message.contentMetadata.get("GC_EVT_TYPE");
                                    // When status was Ended, it means Duration(ms)
                                    String time_ms = op.message.contentMetadata.get("DURATION");
                                    if (status.equals("S")) {
                                        if (op.message.contentMetadata.get("GC_MEDIA_TYPE").equals("AUDIO")) {
                                            client.sendText(op.message.to, client.getContact(op.message._from).displayName + "さんが音声通話を開始しました");
                                        } else {
                                            client.sendText(op.message.to, client.getContact(op.message._from).displayName + "さんがビデオ通話を開始しました");
                                        }
                                    } else if (status.equals("E")) {
                                        if (op.message.contentMetadata.get("GC_MEDIA_TYPE").equals("AUDIO")) {
                                            client.sendText(op.message.to, "音声通話が終了しました\n通話時間：" + time_ms + "ミリ秒");
                                        } else {
                                            client.sendText(op.message.to, "ビデオ通話が終了しました\n通話時間：" + time_ms + "ミリ秒");
                                        }
                                    }
                                } else if (op.message.contentType.equals(ContentType.STICKER)) {
                                    if(op.message.toType == MIDType.GROUP) {
                                        //{STKTXT=[スタンプ], STKVER=100, STKID=407, STKPKGID=1}
                                        //Product(productId:ln_st_1, packageId:1, version:100, authorName:開発者名, onSale:false, validDays:0, saleType:0, copyright:著作権情報, descriptionText:説明, shopOrderId:-1, fromMid:null, toMid:null, validUntil:2147483647000, priceTier:0, price:0, currency:NLC, currencySymbol:NLC, paymentType:null, createDate:1333677960000, ownFlag:true, eventType:NO_EVENT, urlSchema:null, downloadUrl:null, buddyMid:null, publishSince:1333638000000, newFlag:false, missionFlag:false)
                                        if (sticker_status.get(op.message.to)) {
                                            Product product = shop_client.getProduct(Long.valueOf(op.message.contentMetadata.get("STKPKGID")), "日本語", "jp");
                                            client.sendText(op.message.to, "作者：" + product.authorName);
                                            client.sendText(op.message.to, "著作権情報：" + product.copyright);
                                            client.sendText(op.message.to, "説明：" + product.descriptionText);
                                            sticker_status.remove(op.message.to);
                                            sticker_status.put(op.message.to, false);
                                        }
                                    }
                                } else if (op.message.contentType.equals(ContentType.CONTACT)) {
                                    if(op.message.toType == MIDType.GROUP) {
                                        Contact contact = client.getContact(op.message.contentMetadata.get("mid"));
                                        client.sendText(op.message.to, "[mid]\n" + contact.mid + "\n\n[displayName]\n" + contact.displayName + "\n\n[statusMessage]\n" + contact.statusMessage + "\n\n[picturePath]\nhttps://profile.line-scdn.net" + contact.picturePath);
                                    }
                                } else if (op.message.contentType.equals(ContentType.POSTNOTIFICATION)) {
                                    if(op.message.toType == MIDType.GROUP) {
                                        if (op.message.contentMetadata.get("serviceType").equals("MH")) {
                                            client.sendText(op.message.to, client.getContact(op.message._from).displayName + "さんが" + op.message.contentMetadata.get("officialName") + "さんのTLを共有したよ");
                                            client.sendText(op.message.to, op.message.contentMetadata.get("postEndUrl"));
                                        } else if (op.message.contentMetadata.get("serviceType").equals("GB")) {
                                            client.sendText(op.message.to, client.getContact(op.message._from).displayName + "さんがグループノートに投稿したよ");
                                            client.sendText(op.message.to, op.message.contentMetadata.get("postEndUrl"));
                                        }
                                    }
                                }
                                break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public static String readAll(String path) throws IOException {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String string = reader.readLine();
            while (string != null){
                builder.append(string + System.getProperty("line.separator"));
                string = reader.readLine();
            }
        }

        return builder.toString();
    }

    public static LoginResult loginByQRCode() throws Exception{
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/api/v4/TalkService.do");
        transport.setCustomHeader("X-Line-Application", ApplicationType.DESKTOPWIN.name()+"\t8.0.2\tYUZU\t11.2.5");
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
        transport1.setCustomHeader("X-Line-Application", ApplicationType.DESKTOPWIN.name()+"\t8.0.2\tYUZU\t11.2.5");
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
        return loginresult;
    }

}