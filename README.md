## サーバーCA証明書の作成

```
openssl req \
    -new \
    -x509 \
    -days 7 \
    -subj 'CN=/localhost' \
    -keyout server.key \
    -out server.csr
```
