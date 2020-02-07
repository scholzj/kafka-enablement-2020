FROM scholzj/centos-java-base:latest

ARG version=latest
ENV VERSION ${version}

COPY ./scripts/ /bin

ADD target/portfolio-viewer.jar /

CMD ["/bin/run.sh", "/portfolio-viewer.jar"]