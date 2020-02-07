FROM scholzj/centos-java-base:latest

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin

ADD target/pricer.jar /

CMD ["/bin/run.sh", "/pricer.jar"]