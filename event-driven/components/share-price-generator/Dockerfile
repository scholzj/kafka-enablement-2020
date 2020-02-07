FROM scholzj/centos-java-base:latest

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin

ADD target/share-price-generator.jar /

CMD ["/bin/run.sh", "/share-price-generator.jar"]