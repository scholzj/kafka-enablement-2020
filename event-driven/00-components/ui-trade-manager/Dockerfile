FROM scholzj/centos-java-base:latest

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin

ADD target/ui-trade-manager.jar /

CMD ["/bin/run.sh", "/ui-trade-manager.jar"]