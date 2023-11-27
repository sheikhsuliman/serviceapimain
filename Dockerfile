#
# Create minimal JRE
#
FROM azul/zulu-openjdk-alpine:11.0.3 AS jlink

RUN wget -q -O /tmp/newrelic-java.zip "https://download.newrelic.com/newrelic/java-agent/newrelic-agent/5.3.0/newrelic-java.zip"
RUN cd /tmp && unzip /tmp/newrelic-java.zip

ENV JAVA_MODULES="java.base,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.management.rmi,java.rmi,java.naming,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,jdk.unsupported"

RUN jlink --compress=2 --no-header-files --no-man-pages --strip-debug \
    --bind-services \
    --module-path $JAVA_HOME/jmods \
    --add-modules $JAVA_MODULES \
    --output /jlinked

#
# Create app docker image
#
FROM alpine:3.10

RUN rm -f /lib/libcrypto.so.1.1 /lib/libssl.so.1.1
RUN touch /tmp/placeholder

ENV DOCKER_HOME=/opt/app
WORKDIR ${DOCKER_HOME}

COPY --from=jlink /tmp/newrelic/newrelic.jar /newrelic/

COPY --from=jlink /jlinked /opt/jlinked/

## Add your application here, and change the CMD below to start it

ARG SPRING_PROFILES_ACTIVE=postgresql
ENV APP_NAME="swisscon-service-api"
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

ADD src/main/resources resources
ADD target/dependency-jars libs
ADD target/classes classes

# To reduce Tomcat startup time we added a system property pointing to "/dev/urandom" as a source of entropy.
# See: https://stackoverflow.com/a/58853599
ENV JAVA_OPTS="-XX:MaxRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom -javaagent:/newrelic/newrelic.jar"

ENTRYPOINT [ "sh", "-c", "/opt/jlinked/bin/java $JAVA_OPTS -Dnewrelic.environment=${ENVIRONMENT} -Dnewrelic.config.app_name=${ENVIRONMENT}-${APP_NAME} -cp resources:classes:libs/* com.siryus.swisscon.api.Application" ]
