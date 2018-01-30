# LineJava
Naver's line API for Java

これらの作成にはLINE Corporationは一切関連しておりません

# 概要
Line API for JavaはPythonより高速なLineのBotの作成を可能にします

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
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/com/squareup/okhttp3/okhttp/3.9.1/okhttp-3.9.1.jar" path-in-jar="/" />
      <element id="extracted-dir" path="$MAVEN_REPOSITORY$/com/squareup/okio/okio/1.13.0/okio-1.13.0.jar" path-in-jar="/" />
    </root>
  </artifact>
</component>
```
