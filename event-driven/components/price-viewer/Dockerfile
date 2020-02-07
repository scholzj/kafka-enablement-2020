FROM scholzj/centos-java-base:latest

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin

ADD target/price-viewer.jar /

CMD ["/bin/run.sh", "/price-viewer.jar"]