plugins {
    `java-library`
    `maven-publish`
}

group = "com.github.thinhnk55"
version = "1.0.1"

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }

}

dependencies {
    implementation("io.vertx:vertx-web:4.5.14")
    implementation("io.vertx:vertx-web-client:4.5.14")
    implementation("io.vertx:vertx-core:4.5.14")
    implementation("org.slf4j:slf4j-api:2.0.16")
    implementation("ch.qos.logback:logback-classic:1.5.16")
    implementation("org.slf4j:jcl-over-slf4j:2.0.17")
    implementation("org.slf4j:jul-to-slf4j:2.0.17")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.18.3")
    implementation("org.redisson:redisson:3.45.1")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation("com.github.f4b6a3:uuid-creator:6.0.0")
    implementation("com.nimbusds:nimbus-jose-jwt:10.0.2")
    implementation("org.bouncycastle:bcprov-jdk18on:1.80")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.80")
    implementation("org.casbin:jcasbin:1.81.0")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
}

java {
    withSourcesJar()
    withJavadocJar()
}
