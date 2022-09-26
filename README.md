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
