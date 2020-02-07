FROM scholzj/centos-java-base:latest

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin

ADD target/share-position-generator.jar /

CMD ["/bin/run.sh", "/share-position-generator.jar"]