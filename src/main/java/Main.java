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
    public static HashMap<String, String> lang = new HashMap<>();
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static JSONObject ja;
    private static JSONObject en;
    private static String default_lang = "en";

    public static void main(String[] args) throws Exception {
        System.out.println(ANSI_CYAN + "(*´ヮ｀)< LineAPI for JAVA");
        System.out.println(ANSI_BLUE + "version " + ANSI_RESET + ":" + ANSI_GREEN + "1.0 beta");
        System.out.println(ANSI_RED + "codename " + ANSI_RESET + ":" + ANSI_YELLOW + " yuzu");
        System.out.println(ANSI_PURPLE + "author " + ANSI_RESET + ":" + ANSI_YELLOW + " tomo" + ANSI_RESET);
        LoadLangResourceFile("ja");
        LoadLangResourceFile("en");
        if(Locale.getDefault().getLanguage().equals("ja")){
            default_lang = "ja";
        }else{
            default_lang = "en";
        }

        String token = "";
        if (args.length == 0) {
            String authtoken = loginByQRCode().authToken;
            token = authtoken;
            System.out.println("これからauthtokenを実行時の引数に追加してください");
            System.out.println("例：java -jar line.jar authtoken");
        } else {
            token = args[0];
        }
        System.out.println("Try this token : " + token);
        LINEClient client = new LINEClient(token);
        System.out.println("displayName : " + client.getProfile().displayName);
        System.out.println("mid : " + client.getProfile().mid);
        System.out.println("authtoken : " + client.authtoken + "\n");
        List<String> inviteIds = client.getGroupIdsInvited();
        for (String inviteId : inviteIds) {
            client.acceptGroupInvitation(inviteId);
        }
        List<String> gids = client.getGroupIdsJoined();
        for (String gid : gids) {
            read.put(gid, new ArrayList<>());
            sticker_status.put(gid, false);
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
                        switch (op.type) {
                            case NOTIFIED_ADD_CONTACT:
                                client.sendText(op.param1, getString("thanks_for_adding",getLangCodeByGid(op.message.to)).replace("%s",client.getContact(op.param1).displayName));
                                break;
                            case NOTIFIED_INVITE_INTO_GROUP:
                                client.acceptGroupInvitation(op.param1);
                                break;
                            case ACCEPT_GROUP_INVITATION:
                                read.put(op.param1, new ArrayList<>());
                                sticker_status.put(op.param1, false);
                                client.sendText(op.param1, getString("thanks_for_inviting",getLangCodeByGid(op.message.to)));
                                break;
                            case NOTIFIED_ACCEPT_GROUP_INVITATION:
                                client.sendText(op.param1, getString("welcome_this_group",getLangCodeByGid(op.message.to)).replace("%s",client.getContact(op.param2).displayName).replace("%g",client.getCompactGroup(op.param1).name));
                                break;
                            case INVITE_INTO_ROOM:
                                client.sendText(op.param1, getString("welcome_this_group",getLangCodeByGid(op.message.to)).replace("%s",client.getContact(op.param2).displayName));
                                client.leaveRoom(op.param1);
                                break;
                            case NOTIFIED_UPDATE_GROUP:
                                String name = client.getContact(op.param2).displayName;
                                if (op.param3.equals("1")) {
                                    client.sendText(op.param1, getString("changed_group_name",getLangCodeByGid(op.message.to)).replace("%s",name));
                                } else if (op.param3.equals("2")) {
                                    client.sendText(op.param1, getString("changed_group_image",getLangCodeByGid(op.message.to)).replace("%s",name));
                                } else if (op.param3.equals("4")) {
                                    if (client.getCompactGroup(op.param1).preventedJoinByTicket) {
                                        client.sendText(op.param1, getString("blocked_invitng_by_url",getLangCodeByGid(op.message.to)).replace("%s",name));
                                    } else {
                                        client.sendText(op.param1, getString("allowed_invitng_by_url",getLangCodeByGid(op.message.to)).replace("%s",name));
                                    }
                                } else if (op.param3.equals("8")) {
                                    client.sendText(op.param1, getString("changed_notification_setting",getLangCodeByGid(op.message.to)).replace("%s",name));
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
                                        String msg = getString("help_message",getLangCodeByGid(op.message.to))
                                                .replace("%d","2018-01-31 02:19")
                                                .replace("%b","15018")
                                                .replace("%t","BETA")
                                                .replace("%supporturl","https://twitter.com/tomo_linebot");
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, msg);
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, msg);
                                        }
                                    } else if (op.message.text.toLowerCase().contains("setlang:")) {
                                        String lang_code = "ja";
                                        if(op.message.text.toLowerCase().replace("setlang:","").equals("ja")){
                                            lang_code = "ja";
                                        } else if(op.message.text.toLowerCase().replace("setlang:","").equals("en")){
                                            lang_code = "en";
                                        }
                                        if(lang.containsKey(op.message.to)){
                                            lang.remove(op.message.to);
                                            lang.put(op.message.to,lang_code);
                                        }else{
                                            lang.put(op.message.to,lang_code);
                                        }
                                        client.sendText(op.message.to,"Changed Language.");
                                    } else if (op.message.text.contains(getString("bath",getLangCodeByGid(op.message.to)))) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, getString("good_for_working",getLangCodeByGid(op.message.to)));
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("good_for_working",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.contains(getString("imhungry",getLangCodeByGid(op.message.to)))) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, getString("prepaired_dinner",getLangCodeByGid(op.message.to)));
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("prepaired_dinner",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.contains(getString("good_morning",getLangCodeByGid(op.message.to)))) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, getString("good_morning_msg",getLangCodeByGid(op.message.to)));
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("good_morning_msg",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.contains(getString("job",getLangCodeByGid(op.message.to))) && op.message.text.toLowerCase().contains(getString("tired",getLangCodeByGid(op.message.to)))) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, getString("bath_cleaning",getLangCodeByGid(op.message.to)));
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("bath_cleaning",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.toLowerCase().equals("speed")) {
                                        long systime = System.currentTimeMillis();
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, getString("speed_result",getLangCodeByGid(op.message.to)).replace("%s",(systime - op.createdTime)+""));
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("speed_result",getLangCodeByGid(op.message.to)).replace("%s",(systime - op.createdTime)+""));
                                        }
                                    } else if (op.message.text.toLowerCase().equals("gid")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, op.message.to);
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("does_not_exists_gid",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.toLowerCase().equals("mid")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, op.message._from);
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, op.message._from);
                                        }
                                    } else if (op.message.text.toLowerCase().equals("reset")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            read.get(op.message.to).clear();
                                            client.sendText(op.message.to, getString("reset_readers_list",getLangCodeByGid(op.message.to)));
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.toLowerCase().equals("ginfo")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            Group group = client.getGroup(op.message.to);
                                            boolean notexists = false;
                                            if (!group.members.contains(group.creator)) {
                                                notexists = true;
                                            }
                                            if (group.preventedJoinByTicket == false) {
                                                if (notexists) {
                                                    client.sendText(op.message.to,
                                                            getString("ginfo_1",getLangCodeByGid(op.message.to)).replace("%s",group.name).replace("%t",group.id).replace("%u",group.creator.displayName).replace("%v",group.members.get(0).displayName).replace("%w",group.pictureStatus));
                                                } else {
                                                    client.sendText(op.message.to,
                                                            getString("ginfo_2",getLangCodeByGid(op.message.to)).replace("%s",group.name).replace("%t",group.id).replace("%u",group.creator.displayName).replace("%v",group.pictureStatus));
                                                }
                                            } else {
                                                if (notexists) {
                                                    client.sendText(op.message.to,
                                                            getString("ginfo_3",getLangCodeByGid(op.message.to)).replace("%s",group.name).replace("%t",group.id).replace("%u",group.creator.displayName).replace("%v",group.members.get(0).displayName).replace("%w",group.pictureStatus));
                                                } else {
                                                    client.sendText(op.message.to,
                                                            getString("ginfo_4",getLangCodeByGid(op.message.to)).replace("%s",group.name).replace("%t",group.id).replace("%u",group.creator.displayName).replace("%v",group.pictureStatus));
                                                }
                                            }
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.toLowerCase().equals("howinv")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            List<Contact> invs = client.getGroup(op.message.to).invitee;
                                            if (invs == null) {
                                                client.sendText(op.message.to, getString("no_bady_invited",getLangCodeByGid(op.message.to)));
                                            } else {
                                                client.sendText(op.message.to, getString("invited_users",getLangCodeByGid(op.message.to)).replace("%s",invs.size()+""));
                                            }
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.replace(" ", "").replace("?", "").toLowerCase().equals("whoread")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            if (read.get(op.message.to).isEmpty()) {
                                                client.sendText(op.message.to, getString("no_bady_read",getLangCodeByGid(op.message.to)));
                                            } else {
                                                String readers_data = "";
                                                for (int count1 = 0; count1 < read.get(op.message.to).size(); count1++) {
                                                    readers_data = readers_data + getString("invited_users",getLangCodeByGid(op.message.to)).replace("%s",client.getContact(read.get(op.message.to).get(count1)).displayName);
                                                }
                                                client.sendText(op.message.to, getString("read_users",getLangCodeByGid(op.message.to)).replace("%s",readers_data));
                                            }
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.contains(" into gname")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            if (client.getGroup(op.message.to).name.equals(op.message.text.replace(" into gname", ""))) {
                                                client.sendText(op.message.to, getString("use_other_name",getLangCodeByGid(op.message.to)));
                                            } else {
                                                client.setGroupName(op.message.to, op.message.text.replace(" into gname", ""));
                                                client.sendText(op.param1, getString("i_changed_group_name",getLangCodeByGid(op.message.to)));
                                            }
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.contains(" > gname")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            if (client.getGroup(op.message.to).name.equals(op.message.text.replace(" > gname", ""))) {
                                                client.sendText(op.message.to, getString("use_other_name",getLangCodeByGid(op.message.to)));
                                            } else {
                                                client.setGroupName(op.message.to, op.message.text.replace(" > gname", ""));
                                                client.sendText(op.param1, getString("i_changed_group_name",getLangCodeByGid(op.message.to)));
                                            }
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.toLowerCase().contains("geturl")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, getString("i_reissued_glink",getLangCodeByGid(op.message.to)).replace("%s",client.getGroupURL(op.message.to)));
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.toLowerCase().equals("stkinfo")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            sticker_status.remove(op.message.to);
                                            sticker_status.put(op.message.to, true);
                                            client.sendText(op.message.to, getString("send_stk",getLangCodeByGid(op.message.to)));
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
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
                                        if (op.message.toType == MIDType.GROUP) {
                                            client.sendText(op.message.to, getString("good_for_meeting_you",getLangCodeByGid(op.message.to)));
                                            read.remove(op.message.to);
                                            sticker_status.remove(op.message.to);
                                            client.leaveGroup(op.message.to);
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
                                        }
                                    } else if (op.message.text.toLowerCase().contains("sencon:")) {
                                        if (op.message.toType == MIDType.GROUP) {
                                            String req_mid = op.message.text.toLowerCase().replace("sencon:", "");
                                            try {
                                                Contact contact = client.getContact(req_mid);
                                                Map<String, String> map = new HashMap<>();
                                                map.put("mid", contact.mid);
                                                map.put("displayName", contact.displayName);
                                                client.sendMessage(new Message().setTo(op.message.to).setContentType(ContentType.CONTACT).setContentMetadata(map));
                                            } catch (Exception e) {
                                                client.sendText(op.message.to, getString("user_does_not_exist",getLangCodeByGid(op.message.to)));
                                            }
                                        } else if (op.message.toType == MIDType.USER) {
                                            client.sendText(op.message._from, getString("this_command_does_not_supported",getLangCodeByGid(op.message.to)));
                                        }
                                    }
                                } else if (op.message.contentType.equals(ContentType.CALL)) {
                                    // S (started) or E (ended)
                                    String status = op.message.contentMetadata.get("GC_EVT_TYPE");
                                    // When status was Ended, it means Duration(ms)
                                    String time_ms = op.message.contentMetadata.get("DURATION");
                                    if (status.equals("S")) {
                                        if (op.message.contentMetadata.get("GC_MEDIA_TYPE").equals("AUDIO")) {
                                            client.sendText(op.message.to, getString("started_group_call",getLangCodeByGid(op.message.to)).replace("%s",client.getContact(op.message._from).displayName));
                                        } else {
                                            client.sendText(op.message.to, getString("started_group_video_call",getLangCodeByGid(op.message.to)).replace("%s",client.getContact(op.message._from).displayName));
                                        }
                                    } else if (status.equals("E")) {
                                        if (op.message.contentMetadata.get("GC_MEDIA_TYPE").equals("AUDIO")) {
                                            client.sendText(op.message.to, getString("ended_group_call",getLangCodeByGid(op.message.to)).replace("%s",time_ms));
                                        } else {
                                            client.sendText(op.message.to, getString("ended_group_video_call",getLangCodeByGid(op.message.to)).replace("%s",time_ms));
                                        }
                                    }
                                } else if (op.message.contentType.equals(ContentType.STICKER)) {
                                    if (op.message.toType == MIDType.GROUP) {
                                        //{STKTXT=[スタンプ], STKVER=100, STKID=407, STKPKGID=1}
                                        //Product(productId:ln_st_1, packageId:1, version:100, authorName:開発者名, onSale:false, validDays:0, saleType:0, copyright:著作権情報, descriptionText:説明, shopOrderId:-1, fromMid:null, toMid:null, validUntil:2147483647000, priceTier:0, price:0, currency:NLC, currencySymbol:NLC, paymentType:null, createDate:1333677960000, ownFlag:true, eventType:NO_EVENT, urlSchema:null, downloadUrl:null, buddyMid:null, publishSince:1333638000000, newFlag:false, missionFlag:false)
                                        if (sticker_status.get(op.message.to)) {
                                            Product product = shop_client.getProduct(Long.valueOf(op.message.contentMetadata.get("STKPKGID")), getString("lang",getLangCodeByGid(op.message.to)), getString("country",getLangCodeByGid(op.message.to)));
                                            client.sendText(op.message.to, getString("authorName",getLangCodeByGid(op.message.to)).replace("%s",product.authorName));
                                            client.sendText(op.message.to, getString("copyright",getLangCodeByGid(op.message.to)).replace("%s",product.copyright));
                                            client.sendText(op.message.to, getString("descriptionText",getLangCodeByGid(op.message.to)).replace("%s",product.descriptionText));
                                            sticker_status.remove(op.message.to);
                                            sticker_status.put(op.message.to, false);
                                        }
                                    }
                                } else if (op.message.contentType.equals(ContentType.CONTACT)) {
                                    if (op.message.toType == MIDType.GROUP) {
                                        Contact contact = client.getContact(op.message.contentMetadata.get("mid"));
                                        client.sendText(op.message.to, "[mid]\n" + contact.mid + "\n\n[displayName]\n" + contact.displayName + "\n\n[statusMessage]\n" + contact.statusMessage + "\n\n[picturePath]\nhttps://profile.line-scdn.net" + contact.picturePath);
                                    }
                                } else if (op.message.contentType.equals(ContentType.POSTNOTIFICATION)) {
                                    if (op.message.toType == MIDType.GROUP) {
                                        if (op.message.contentMetadata.get("serviceType").equals("MH")) {
                                            client.sendText(op.message.to, getString("posted_group_note",getLangCodeByGid(op.message.to)).replace("%s",client.getContact(op.message._from).displayName).replace("%t",op.message.contentMetadata.get("officialName")));
                                            client.sendText(op.message.to, op.message.contentMetadata.get("postEndUrl"));
                                        } else if (op.message.contentMetadata.get("serviceType").equals("GB")) {
                                            client.sendText(op.message.to, getString("posted_group_note",getLangCodeByGid(op.message.to)).replace("%s",client.getContact(op.message._from).displayName));
                                            client.sendText(op.message.to, op.message.contentMetadata.get("postEndUrl"));
                                        }
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String readAll(String path) throws IOException {
        StringBuilder builder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String string = reader.readLine();
            while (string != null) {
                builder.append(string + System.getProperty("line.separator"));
                string = reader.readLine();
            }
        }

        return builder.toString();
    }

    public static void LoadLangResourceFile(String lang_code){
        InputStream is = ClassLoader.getSystemResourceAsStream(lang_code+".json");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String l = null;
        String lang_data = "";
        try {
            while ((l = br.readLine()) != null) {
                lang_data = lang_data + l;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(lang_code.equals("ja")){
            ja = new JSONObject(lang_data);
        }else if(lang_code.equals("en")){
            en = new JSONObject(lang_data);
        }
    }

    public static String getString(String key,String lang){
        if(lang.equals("ja")){
            return ja.getString(key);
        }else if(lang.equals("en")){
            return en.getString(key);
        }else{
            return "";
        }
    }

    public static String getLangCodeByGid(String gid){
        if(lang.containsKey(gid)){
            if(lang.get(gid).equals("ja")){
                return "ja";
            }else{
                return "en";
            }
        }else{
            return "ja";
        }
    }

    public static LoginResult loginByQRCode() throws Exception {
        THttpClient transport = new THttpClient("https://gd2.line.naver.jp/api/v4/TalkService.do");
        transport.setCustomHeader("X-Line-Application", ApplicationType.DESKTOPWIN.name() + "\t8.0.2\tYUZU\t11.2.5");
        TProtocol protocol = new TCompactProtocol(transport);
        TalkService.Client client = new TalkService.Client(protocol);
        transport.open();
        AuthQrcode qr = client.getAuthQrcode(true, "YUZU");
        System.out.println(getString("click_this_link_in_two_minutes",default_lang));
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
        transport1.setCustomHeader("X-Line-Application", ApplicationType.DESKTOPWIN.name() + "\t8.0.2\tYUZU\t11.2.5");
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