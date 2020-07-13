# Security

This lab shows how to secure the Kafka deployment on OCP.

## Operator

* Deploy the Strimzi operator

```
oc apply -f 01-operator/
```

_(If you run this demo in other namespace than `myproject`, you have to change the namespace inside RBAC files in `./01-operator`.)_

## Install Keycloak / Red Hat Single Sign-On (RH-SSO)

* In OCP4, install Keycloak or RH-SSO operator from Operator Hub
* Deploy Keycloak / RH-SSO server

```
oc apply -f ./02-keycloak.yaml
```

* Create the internal and external realms in Keycloak / RH-SSO

```
oc apply -f ./03-keycloak-realm-internal.yaml
oc apply -f ./04-keycloak-realm-external.yaml
```

* You can login to the Keycloak / RH-SSO UI console and check that the realms were created _(username and password can be found in the secret named `credential-keycloak`)_

* For access from outside, we will need to trust the certificate of the Keycloak / RH-SSO service (route)
* Use OpenSSL to get the public key. Run the following command and copy-paste the CA certificate into a file `oauth.crt`_(The Keycloak / RH-SSO address in this command is just an example - your address will differ.)_:

```
openssl s_client -showcerts -connect keycloak-myproject.apps.jscholz.rhmw-integrations.net:443 -servername keycloak-myproject.apps.jscholz.rhmw-integrations.net
```

* Use `keytool` from Java to create a truststore for local client:

```
keytool -importcert -keystore oauth.truststore -file oauth.crt --storepass 123456
```

* Create a new Kubernetes secret with the file

```
kubectl create secret generic keycloak-external-tls --from-file=tls.crt=./oauth.crt
```

## Kafka

* Deploy Kafka with OAuth authentication.
Check the `05-kafka-oauth.yaml` file to see how it is configured.

```
oc apply -f 05-kafka-oauth.yaml
```

## Internal clients

* Deploy the internal clients:

```
oc apply -f 06-deployment.yaml
```

* Check that they work
* Look at the `06-deployment.yaml` file and notice
    * How the Keycloak client is created
    * How the ACLs for the users are set in `KafkaUser` resource
* You can check their source codes in `./06-oauth-clients`

## Kafka Connect

* Deploy Kafka Connect with OAuth

```
oc apply -f 07-kafka-connect.yaml
```

* You can use the following command to check that Connect works fine over OAuth

```
oc exec $(oc get pod -l strimzi.io/kind=KafkaConnect -o name) -ti -- tail -f /tmp/test.sink.txt
```

## Custom certificates

* Go to `./08-custom-certificates`
    * Generate new custom server certificates by running `./build.sh
    * Load them as Kubernetes secrets using `./load.sh`
    * For that you need to have the following things installed: OpenSSL, CFSSL, Java (keytool)

* Update the Kafka deployment to use the new custom certificate for the external interface

```
oc apply -f 09-kafka-custom-cert.yaml
```

* Notice the change in the file compared to `05-kafka-oauth.yaml`:

```yaml
        configuration:
          brokerCertChainAndKey:
            secretName: custom-cert-external
            certificate: tls.crt
            key: tls.key
```

* Use OpenSSL to check that the new certificate is in use now _(The KAfka address in this command is just an example - your address will differ.)_:

```
openssl s_client -showcerts -connect my-cluster-kafka-bootstrap-myproject.apps.jscholz.rhmw-integrations.net:443 -servername my-cluster-kafka-bootstrap-myproject.apps.jscholz.rhmw-integrations.net
```

## User ACL

* Look at the `10-external-user.yaml` which creates our external user and gives ACL rights for the right topic and group and deploy it:

```
oc apply -f 10-external-user.yaml
```

* Use the user in the external client to connect to the cluster with the code from `./10-external-client`

## Network policies

Update the Kafka resource with the new network policies configuration_
* 
```yaml
        networkPolicyPeers:
          - podSelector:
              matchLabels:
                app: kafka-consumer
          - podSelector:
              matchLabels:
                app: kafka-producer
```

```
oc apply -f 11-kafka-network-polcies.yaml
```

Restart the Hello World consumers / producers and see how they cannot connect.
Change the network policies and see how the connection is allowed.