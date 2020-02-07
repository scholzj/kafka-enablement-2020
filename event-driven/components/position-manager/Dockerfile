FROM scholzj/centos-java-base:latest

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin

ADD target/position-manager.jar /

CMD ["/bin/run.sh", "/position-manager.jar"]