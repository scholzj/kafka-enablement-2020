FROM scholzj/centos-java-base:latest

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin

ADD target/position-aggregator.jar /

CMD ["/bin/run.sh", "/position-aggregator.jar"]