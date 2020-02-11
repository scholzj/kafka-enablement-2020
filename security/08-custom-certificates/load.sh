#!/usr/bin/env bash

oc delete secret custom-cert-internal
oc create secret generic custom-cert-internal \
  --from-file=tls.crt=internal-bundle.crt \
  --from-file=tls.key=internal.key

oc delete secret custom-cert-external
oc create secret generic custom-cert-external \
  --from-file=tls.crt=external-bundle.crt \
  --from-file=tls.key=external.key

oc delete secret custom-cert
oc create secret generic custom-cert \
  --from-file=internal.crt=internal-bundle.crt \
  --from-file=internal.key=internal.key \
  --from-file=external.crt=external-bundle.crt \
  --from-file=external.key=external.key \
  --from-file=ca.crt=ca.pem