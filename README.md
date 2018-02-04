# LineJava
Naver's line API for Java

これらの作成にはLINE Corporationは一切関連しておりません

For PC Intellij IDEA (https://www.jetbrains.com/idea/download)


    A : How use in VPS?


    B : 

    sudo apt-get install python-software-properties


    sudo add-apt-repository ppa:webupd8team/java


    sudo apt-get update
    
    
    sudo apt-get install openjdk-7-jdk


    java -version
    
    
    Then, cd to Examples->, java -jar line.jar

# 概要
Line API for JavaはPythonより高速なLineのBotの作成を可能にします

# 使い方
Main.javaを編集します

ここにあるのはあくまで一例です

```
// LINEClientの作成
LINEClient client = new LINEClient("authtoken");
//To send text msg
client.sendText("to", "message");
//テキスト送信にも使用可能だが、より高度なメッセージの送信に使う
client.sendMessage(new Message().setTo("to").setText("message"));
//Mention
ArrayList<String> list = new ArrayList<>();
list.add(mid);
list.add(mid);
client.sendMessageWithMention(op.message.to,"message",list,"\n");
//or...
client.sendMessageWithMention(op.message.to,"message",list);
```

LINEClient.java内にある関数が利用可能です

詳しくはそちらをご覧ください

Javaで動くものなら組み込めるのでMCBE ServerのNUkkitやJupiterにも組み込めるよ！


# ビルド
これは[Intellij IDEA](https://www.jetbrains.com/idea/download)を使うことを前提とされています

お使いのPCにインストールされてない場合はインストールしてください

まず、このプロジェクトをダウンロードしてください

それから、ideaで開いてください

ideaで開くと.ideaというフォルダーが作成されます。

そのフォルダーを開いてarfifactsという名前のフォルダーを作成してください

そのフィルダーの中に下のコードをline.xmlを作成して貼り付けてください

```
<component name="ArtifactManager">
  <artifact type="jar" name="line">
    <output-path>$PROJECT_DIR$/out/artifacts/line</output-path>
    <root id="archive" name="line.jar">
      <element id="module-output" name="LineJava" />
      <element id="directory" name="META-INF">
        <element id="file-copy" path="$PROJECT_DIR$/META-INF/MANIFEST.MF" />
      </element>
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/commons-lang/commons-lang/2.5/commons-lang-2.5.jar" path-in-jar="/" />
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/commons-codec/commons-codec/1.9/commons-codec-1.9.jar" path-in-jar="/" />
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/commons-logging/commons-logging/1.2/commons-logging-1.2.jar" path-in-jar="/" />
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/org/apache/httpcomponents/httpclient/4.4.1/httpclient-4.4.1.jar" path-in-jar="/" />
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/org/apache/httpcomponents/httpcore/4.4.1/httpcore-4.4.1.jar" path-in-jar="/" />
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/org/apache/thrift/libthrift/0.9.1/libthrift-0.9.1.jar" path-in-jar="/" />
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/org/slf4j/slf4j-api/1.7.12/slf4j-api-1.7.12.jar" path-in-jar="/" />
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/org/slf4j/slf4j-simple/1.5.8/slf4j-simple-1.5.8.jar" path-in-jar="/" />
    </root>
  </artifact>
</component>
```

それからBuild > Build Arfifactsを選択してビルドします

それからJarが出力されたパスにcdして、java -jar line.jarをすればLogin用のURLが形成されます

このURLをスマホ版LINEで踏んでログインすればauthtokenを取得できます

authtokenの情報はご自身で大切に保管してください(authtokenは漏らさないように)

二度目以降はjava -jar line.jar authtokenとするとまたリンクを踏む必要がなくなります

![Build Without Run Config](https://raw.githubusercontent.com/kaoru-nishida/LineJava/master/ScreenShots/ScreenShot_2018-01-30_14-27-18-01.jpeg)

Run/Debug設定を設定するとより簡単に実行できるようになります

![Run/Debug Config](https://raw.githubusercontent.com/kaoru-nishida/LineJava/master/ScreenShots/ScreenShot_2018-01-30_14-30-36.png)

このように設定して上部バーの実行ボタンの横でLINEを選択すると実行ボタンを押すだけで実行できるようなります

![Run With Eun Config](https://raw.githubusercontent.com/kaoru-nishida/LineJava/master/ScreenShots/ScreenShot_2018-01-30_14-30-20.png)