#!/usr/bin/env bash

export MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

PASSWORD="123456"

# Create dir
mkdir $MY_DIR/keys/

# Generate the CA
cfssl genkey -initca $MY_DIR/ca.json | cfssljson -bare $MY_DIR/keys/ca

# Generate server keys
cfssl gencert -ca $MY_DIR/keys/ca.pem -ca-key $MY_DIR/keys/ca-key.pem $MY_DIR/server-0.json | cfssljson -bare $MY_DIR/keys/server-0
cfssl gencert -ca $MY_DIR/keys/ca.pem -ca-key $MY_DIR/keys/ca-key.pem $MY_DIR/server-1.json | cfssljson -bare $MY_DIR/keys/server-1
cfssl gencert -ca $MY_DIR/keys/ca.pem -ca-key $MY_DIR/keys/ca-key.pem $MY_DIR/server-2.json | cfssljson -bare $MY_DIR/keys/server-2

# Generate Connect keys - ext
cfssl gencert -ca $MY_DIR/keys/ca.pem -ca-key $MY_DIR/keys/ca-key.pem $MY_DIR/connect-0.json | cfssljson -bare $MY_DIR/keys/connect-0
cfssl gencert -ca $MY_DIR/keys/ca.pem -ca-key $MY_DIR/keys/ca-key.pem $MY_DIR/connect-1.json | cfssljson -bare $MY_DIR/keys/connect-1
cfssl gencert -ca $MY_DIR/keys/ca.pem -ca-key $MY_DIR/keys/ca-key.pem $MY_DIR/connect-2.json | cfssljson -bare $MY_DIR/keys/connect-2

# Generate full chain server CRTs
cat $MY_DIR/keys/server-0.pem $MY_DIR/keys/ca.pem > $MY_DIR/keys/server-0-full-chain.pem
cat $MY_DIR/keys/server-1.pem $MY_DIR/keys/ca.pem > $MY_DIR/keys/server-1-full-chain.pem
cat $MY_DIR/keys/server-2.pem $MY_DIR/keys/ca.pem > $MY_DIR/keys/server-2-full-chain.pem

# Generate full chain Connecr CRTs
cat $MY_DIR/keys/connect-0.pem $MY_DIR/keys/ca.pem > $MY_DIR/keys/connect-0-full-chain.pem
cat $MY_DIR/keys/connect-1.pem $MY_DIR/keys/ca.pem > $MY_DIR/keys/connect-1-full-chain.pem
cat $MY_DIR/keys/connect-2.pem $MY_DIR/keys/ca.pem > $MY_DIR/keys/connect-2-full-chain.pem

# Generate user keys
cfssl gencert -ca $MY_DIR/keys/ca.pem -ca-key $MY_DIR/keys/ca-key.pem $MY_DIR/user1.json | cfssljson -bare $MY_DIR/keys/user1
cfssl gencert -ca $MY_DIR/keys/ca.pem -ca-key $MY_DIR/keys/ca-key.pem $MY_DIR/user2.json | cfssljson -bare $MY_DIR/keys/user2
cfssl gencert -ca $MY_DIR/keys/ca.pem -ca-key $MY_DIR/keys/ca-key.pem $MY_DIR/user-connect.json | cfssljson -bare $MY_DIR/keys/user-connect

# Convert CA to Java Keystore format (truststrore)
rm $MY_DIR/keys/truststore
keytool -importcert -keystore $MY_DIR/keys/truststore -storepass $PASSWORD -storetype JKS -alias ca -file $MY_DIR/keys/ca.pem -noprompt

# Convert keys to PKCS12
openssl pkcs12 -export -out $MY_DIR/keys/server-0.p12 -in $MY_DIR/keys/server-0-full-chain.pem -inkey $MY_DIR/keys/server-0-key.pem -password pass:$PASSWORD
openssl pkcs12 -export -out $MY_DIR/keys/server-1.p12 -in $MY_DIR/keys/server-1-full-chain.pem -inkey $MY_DIR/keys/server-1-key.pem -password pass:$PASSWORD
openssl pkcs12 -export -out $MY_DIR/keys/server-2.p12 -in $MY_DIR/keys/server-2-full-chain.pem -inkey $MY_DIR/keys/server-2-key.pem -password pass:$PASSWORD
openssl pkcs12 -export -out $MY_DIR/keys/connect-0.p12 -in $MY_DIR/keys/connect-0-full-chain.pem -inkey $MY_DIR/keys/connect-0-key.pem -password pass:$PASSWORD
openssl pkcs12 -export -out $MY_DIR/keys/connect-1.p12 -in $MY_DIR/keys/connect-1-full-chain.pem -inkey $MY_DIR/keys/connect-1-key.pem -password pass:$PASSWORD
openssl pkcs12 -export -out $MY_DIR/keys/connect-2.p12 -in $MY_DIR/keys/connect-2-full-chain.pem -inkey $MY_DIR/keys/connect-2-key.pem -password pass:$PASSWORD
openssl pkcs12 -export -out $MY_DIR/keys/user1.p12 -in $MY_DIR/keys/user1.pem -inkey $MY_DIR/keys/user1-key.pem -password pass:$PASSWORD
openssl pkcs12 -export -out $MY_DIR/keys/user2.p12 -in $MY_DIR/keys/user2.pem -inkey $MY_DIR/keys/user2-key.pem -password pass:$PASSWORD
openssl pkcs12 -export -out $MY_DIR/keys/user-connect.p12 -in $MY_DIR/keys/user-connect.pem -inkey $MY_DIR/keys/user-connect-key.pem -password pass:$PASSWORD

# Convert PKCS12 keys to keystores
rm $MY_DIR/keys/*.keystore
keytool -importkeystore -srckeystore $MY_DIR/keys/server-0.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -destkeystore $MY_DIR/keys/server-0.keystore -deststoretype JKS -deststorepass $PASSWORD -noprompt
keytool -importkeystore -srckeystore $MY_DIR/keys/server-1.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -destkeystore $MY_DIR/keys/server-1.keystore -deststoretype JKS -deststorepass $PASSWORD -noprompt
keytool -importkeystore -srckeystore $MY_DIR/keys/server-2.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -destkeystore $MY_DIR/keys/server-2.keystore -deststoretype JKS -deststorepass $PASSWORD -noprompt
keytool -importkeystore -srckeystore $MY_DIR/keys/connect-0.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -destkeystore $MY_DIR/keys/connect-0.keystore -deststoretype JKS -deststorepass $PASSWORD -noprompt
keytool -importkeystore -srckeystore $MY_DIR/keys/connect-1.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -destkeystore $MY_DIR/keys/connect-1.keystore -deststoretype JKS -deststorepass $PASSWORD -noprompt
keytool -importkeystore -srckeystore $MY_DIR/keys/connect-2.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -destkeystore $MY_DIR/keys/connect-2.keystore -deststoretype JKS -deststorepass $PASSWORD -noprompt
keytool -importkeystore -srckeystore $MY_DIR/keys/user1.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -destkeystore $MY_DIR/keys/user1.keystore -deststoretype JKS -deststorepass $PASSWORD -noprompt
keytool -importkeystore -srckeystore $MY_DIR/keys/user2.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -destkeystore $MY_DIR/keys/user2.keystore -deststoretype JKS -deststorepass $PASSWORD -noprompt
keytool -importkeystore -srckeystore $MY_DIR/keys/user-connect.p12 -srcstoretype PKCS12 -srcstorepass $PASSWORD -destkeystore $MY_DIR/keys/user-connect.keystore -deststoretype JKS -deststorepass $PASSWORD -noprompt