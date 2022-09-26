# teach-yourself-mtls

```
export NAME=server
export NAME=client
export DAYS=3650
export PASS=hogehoge
```

* サーバーとクライアントで二回繰り返す
* `DAYS=3650` 当然実環境で使用してはいけない
* `PASS` keytoolは6文字以上を要求する


## CA証明書の作成

```
openssl req \
    -new \
    -x509 \
    -newkey rsa:4096 \
    -days $DAYS \
    -subj '/CN=localhost ca' \
    -keyout "$NAME"ca.key \
    -out "$NAME"ca.crt \
    -passout pass:$PASS
```

* `-new` 新規作成
* `-x509` 証明書を作成する
* `-newkey rsa:4096` RSA4096bitの秘密鍵
* `-subj` subject - なんでもいい。
* `-keyout` 秘密鍵を作成して出力する(`-key`で使用する鍵を指定出来る)
* `-passout pass:hoge` 実際のパスワードとして`hoge`を指定


## CSRの作成

```
openssl req \
    -new \
    -newkey rsa:4096 \
    -subj '/CN=localhost' \
    -keyout $NAME.key \
    -out $NAME.csr \
    -passout pass:$PASS
```

* `-x509`がないのでCSRが作成される
* `-subj` subject - サーバーのドメイン。今回は `localhost`


## 証明書作成

```
openssl x509 \
    -req \
    -in $NAME.csr \
    -CA "$NAME"ca.crt \
    -CAkey "$NAME"ca.key \
    -CAcreateserial \
    -days $DAYS \
    -passin pass:$PASS \
    -out $NAME.crt
```

* `-req` CSRに対する処理であることを示す
* `-in` 入力ファイル。`-req`なのでCSRを指定
* `-CA` CA証明書
* `-CAkey` CA秘密鍵
* `-CAcreateserial` 証明書のシリアルナンバーを新規作成する


## CA証明書のJava keystore形式への変換

```
keytool -import \
    -trustcacerts \
    -noprompt \
    -file "$NAME"ca.crt \
    -keystore "$NAME"ca.keystore \
    -storepass $PASS
```


## 証明書のJava keystore形式への変換

```
openssl pkcs12 \
    -export \
    -in $NAME.crt \
    -inkey $NAME.key \
    -passin pass:$PASS \
    -out $NAME.p12 \
    -passout pass:$PASS \
    -CAfile "$NAME"ca.crt
keytool -importkeystore \
    -srckeystore $NAME.p12 \
    -srcstoretype PKCS12 \
    -srcstorepass $PASS \
    -destkeystore $NAME.keystore \
    -deststorepass $PASS
```

* 直接秘密鍵のPEMを取り込むことは出来ないらしい
    * まずPEMをPKCS12形式に変換し、その後JKSを作成する
    * わけがわからないよ。本当かどうか要調査


## curl

```
# server.ktの`it.needClientAuth = true`をコメントアウト
curl -v --cacert serverca.crt https://localhost:8080

# `it.needClientAuth = true`
curl -v --cacert serverca.crt https://localhost:8080
# エラーになる
curl -v --cacert serverca.crt --key client.key --cert client.crt:$PASS https://localhost:8080
# クライアント証明をしたので成功する
```


## 参考文献

* https://qiita.com/kentarok/items/15c08350274fa5578aaf#3-%E3%82%AF%E3%83%A9%E3%82%A4%E3%82%A2%E3%83%B3%E3%83%88%E8%A8%BC%E6%98%8E%E6%9B%B8%E3%81%AE%E6%BA%96%E5%82%99%E7%A2%BA%E8%AA%8D
* https://docs.oracle.com/en/java/javase/19/docs/specs/man/keytool.html
* https://docs.oracle.com/en/database/other-databases/nosql-database/21.2/security/import-key-pair-java-keystore.html
* https://stackoverflow.com/questions/906402/how-to-import-an-existing-x-509-certificate-and-private-key-in-java-keystore-to
* https://qiita.com/kunichiko/items/12cbccaadcbf41c72735#%E8%A8%BC%E6%98%8E%E6%9B%B8%E7%BD%B2%E5%90%8D%E4%BB%98%E3%81%8D%E3%81%AE%E5%85%AC%E9%96%8B%E9%8D%B5%E3%82%92%E4%BD%9C%E6%88%90%E3%81%99%E3%82%8B
* https://qiita.com/takech9203/items/5206f8e2572e95209bbc
* https://gist.github.com/crpietschmann/35024f1da2a5beb0466e616ce1d7a876
* https://stackoverflow.com/questions/19014541/curl-to-pass-ssl-certifcate-and-password
* https://qiita.com/riversun/items/2909019123b28471ea79
