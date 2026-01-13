# CDI勉強会 TIPS04 Dependentスコープのインスタンスがメモリ解放されない 

## セットアップ

1. git cloneする。

```
git clone https://github.com/jjugnsjeecditips202602/febtips04.git
```

2. ビルドする

```
cd febtips04
mvn clean package
```

targetフォルダ直下に「febtips04-0.0.1-SNAPSHOT.war」が作成されていることを確認

3. WildFlyまたはJBoss EAPにwarをデプロイ

jboss-cliまたは管理コンソールでwarをデプロイする。

jboss-cliでデプロイする際のコマンド例

```
deploy C:\dev\febtips04\target\febtips04-0.0.1-SNAPSHOT.war --force
```

APサーバーの標準出力に次のような文言が表示されることを確認

```
18:24:12,521 INFO  [org.jboss.as.server.deployment] (MSC service thread 1-2) WFLYSRV0027: "febtips04-0.0.1-SNAPSHOT.war" (runtime-name: "febtips04-0.0.1-SNAPSHOT.war") のデプロイメントを開始しました。
18:24:13,808 INFO  [org.jboss.weld.deployer] (MSC service thread 1-2) WFLYWELD0003: Weld デプロイメント febtips04-0.0.1-SNAPSHOT.war を処理しています
18:24:14,352 INFO  [org.jboss.resteasy.resteasy_jaxrs.i18n] (ServerService Thread Pool -- 101) RESTEASY002225: Deploying jakarta.ws.rs.core.Application: class io.github.futokiyo.febtips04.rest.JakartaRESTActivator
18:24:14,357 INFO  [org.wildfly.extension.undertow] (ServerService Thread Pool -- 101) WFLYUT0021: 登録された web コンテキスト: '/febtips04-0.0.1-SNAPSHOT' (サーバー 'default-server' 用)
18:24:14,455 INFO  [org.jboss.as.server] (management-handler-thread - 6) WFLYSRV0016: デプロイメント "febtips04-0.0.1-SNAPSHOT.war" がデプロイメント "febtips04-0.0.1-SNAPSHOT.war" に置き換えられました。
```


## 動作確認

ブラウザのURLに、
```
http://localhost:8080/febtips04-0.0.1-SNAPSHOT/rest4/beanlist
```
と打ち込み、「com.arjuna.ats.jta.cdi.NarayanaTransactionManager size:1 scope:ApplicationScoped」で始まるBeanの一覧が表示されることを確認。



次に、ブラウザのURLに、
```
http://localhost:8080/febtips04-0.0.1-SNAPSHOT/rest4/rqst
```

と打ち込み、

```
RequestScoped Object (CDI.current().select(RqstSampleForDynamicLookup.class).get()) rqstSampleForDynamicLookup.idUuid:d46f48a9-7716-4114-93f5-e1df5ef812ca


rqstSampleForDynamicLookup equals rqstSampleForDynamicLookupByInjection


rqstSampleForInjection:idUuid:734d1c94-bce9-48c1-a710-79cb5d1de7d1 -> injectedDpndntSampleUuid:97e109d9-2408-45fa-9148-f89f203e9eff.


DpndntSampleForApplicationScopedInjection.idUuid:0cd498aa-6e23-416f-81bc-98a0281fe076


total:1024MB, free:533MB, usage:490MB
```
というフォーマットの内容が表示されることを確認し、ブラウザの再読み込みボタンを20回クリックする。

ブラウザに画面が表示されており、free:に表示されている空き領域に余裕があることを確認。

次に、ブラウザのURLに、
```
http://localhost:8080/febtips04-0.0.1-SNAPSHOT/rest4/dpndnt
```

と打ち込み、

```
Dependent Object (CDI.current().select(Sample.class).get()) sample.idUuid:49a0f297-a855-450e-9c1c-de557b7e50dc


total:1024MB, free:694MB, usage:329MB
```

というフォーマットの内容が表示されることを確認し、ブラウザの再読み込みボタンを数回クリックする。クリックするごとにfreeの表示が減り続けることを確認する。

再読み込みを行い続けるとOutOfMemoryが発生することをWildFly(またはJBoss EAP)のコンソールで確認する。

```
Caused by: java.lang.OutOfMemoryError: Java heap space
```

次に、ブラウザのURLに、
```
http://localhost:8080/febtips04-0.0.1-SNAPSHOT/rest4/memoryusage
```

と打ち込み、freeの表示が低いままで、なかなか増えないこと（DependentスコープのBeanインスタンスのメモリが解放されないこと）を確認する。


