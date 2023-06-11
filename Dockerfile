FROM amazoncorretto:20 AS build
ENV VERSION 1.20

# Prepare foundation
RUN yum updateinfo -y && \
    yum install -y \
    git \
    tar \
    gzip \
    shadow-utils.x86_64 && \
    yum clean all

# Prepare env
RUN groupadd -g 1000 user && \
    useradd -m -s /bin/bash -u 1000 -g 1000 mc-user
USER mc-user
WORKDIR /home/mc-user/mc

# Build minecraft (spigot)
RUN curl -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
RUN java -jar BuildTools.jar --rev $VERSION

# Prepare startup
RUN echo eula=TRUE > eula.txt
RUN echo "java -Xms2G -Xmx2G -XX:+UseG1GC -jar /home/mc-user/mc/spigot-$VERSION.jar nogui" > start.sh
RUN chmod +x start.sh

# Install rcon-cli
# RUN curl -L https://github.com/gorcon/rcon-cli/releases/download/v0.10.3/rcon-0.10.3-amd64_linux.tar.gz | tar -xvz
# RUN mv rcon-*/rcon .
# RUN echo 'default:' > rcon.yaml && \
#     echo '  address: "127.0.0.1:16260"' >> rcon.yaml && \
#     echo '  password: "mc"' >> rcon.yaml && \
#     echo '  log: "rcon-default.log"' >> rcon.yaml && \
#     echo '  type: "rcon"' >> rcon.yaml && \
#     echo '  timeout: "10s"' >> rcon.yaml

FROM build
EXPOSE 25565
VOLUME ["/data"]
WORKDIR /data
USER mc-user
STOPSIGNAL SIGTERM
CMD ["/bin/sh", "/home/mc-user/mc/start.sh"]
