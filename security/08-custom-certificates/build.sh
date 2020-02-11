#!/usr/bin/env bash

# Generate CA with Root, Intermediate and Strimzi / Clients CAs
cfssl genkey -initca ca.json | cfssljson -bare ca
cfssl genkey intermediate.json | cfssljson -bare intermediate
cfssl sign -config config.json -profile CA -ca ca.pem -ca-key ca-key.pem intermediate.csr intermediate.json | cfssljson -bare intermediate

cfssl genkey internal.json | cfssljson -bare internal
cfssl sign -config config.json -profile server -ca intermediate.pem -ca-key intermediate-key.pem internal.csr internal.json | cfssljson -bare internal
cfssl genkey external.json | cfssljson -bare external
cfssl sign -config config.json -profile server -ca intermediate.pem -ca-key intermediate-key.pem external.csr external.json | cfssljson -bare external

# Create CRT bundles
cat internal.pem > internal-bundle.crt
cat intermediate.pem >> internal-bundle.crt
cat ca.pem >> internal-bundle.crt
cat external.pem > external-bundle.crt
cat intermediate.pem >> external-bundle.crt
cat ca.pem >> external-bundle.crt

# Convert keys to PKCS8
openssl pkcs8 -topk8 -nocrypt -in ca-key.pem -out ca.key
openssl pkcs8 -topk8 -nocrypt -in intermediate-key.pem -out intermediate.key
openssl pkcs8 -topk8 -nocrypt -in external-key.pem -out external.key
openssl pkcs8 -topk8 -nocrypt -in internal-key.pem -out internal.key

# Create truststores
keytool -keystore ca.truststore -storepass 123456 -noprompt -alias ca -import -file ca.pem -storetype JKS
keytool -keystore internal.truststore -storepass 123456 -noprompt -alias internal -import -file internal.pem -storetype JKS
keytool -keystore external.truststore -storepass 123456 -noprompt -alias external -import -file external.pem -storetype JKS