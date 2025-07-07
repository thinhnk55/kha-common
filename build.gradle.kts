plugins {
    `java-library`
    `maven-publish`
}

val groupId = "com.github.thinhnk55"
val artifactId = "kha-common"
val versionId = "1.0.13"

group = groupId
version = versionId


repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

val vertxVersion = "5.0.0"
val jacksonVersion = "2.19.1"
val slf4jVersion = "2.0.16"
val logbackVersion = "1.5.18"
val hikariVersion = "6.2.1"
val flywayVersion = "11.9.1"
val jdbiVersion = "3.49.5"
val postgresqlVersion = "42.7.7"
val minioVersion = "8.5.17"
val uuidCreatorVersion = "6.0.0"
val redissonVersion = "3.49.0"
val lombokVersion = "1.18.36"
val junitVersion = "5.9.1"
val nimbusVersion = "10.3"
val bouncyCastleVersion = "1.80"
val jCasbinVersion = "1.81.0"
val guavaVersion = "33.4.8-jre"
val slugifyVersion = "3.0.7"
val icu4jVersion = "77.1"

dependencies {
    implementation("io.vertx:vertx-core:$vertxVersion")
    implementation("io.vertx:vertx-web:$vertxVersion")
    implementation("io.vertx:vertx-auth-common:$vertxVersion")
    implementation("io.vertx:vertx-auth-jwt:$vertxVersion")

    implementation("org.slf4j:slf4j-api:$slf4jVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")
    implementation("org.jdbi:jdbi3-core:$jdbiVersion")
    implementation("org.jdbi:jdbi3-jackson2:$jdbiVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")
    implementation("io.minio:minio:$minioVersion")

    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("com.nimbusds:nimbus-jose-jwt:$nimbusVersion")
	implementation("org.bouncycastle:bcprov-jdk18on:$bouncyCastleVersion")
	implementation("org.bouncycastle:bcpkix-jdk18on:$bouncyCastleVersion")
	implementation("org.casbin:jcasbin:$jCasbinVersion")

    implementation("org.redisson:redisson:$redissonVersion")
    implementation("com.github.f4b6a3:uuid-creator:$uuidCreatorVersion")
    implementation("com.github.slugify:slugify:$slugifyVersion")
    implementation("com.google.guava:guava:$guavaVersion")
    implementation("com.ibm.icu:icu4j:$icu4jVersion")

    compileOnly("org.projectlombok:lombok:$lombokVersion")
    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = groupId
            artifactId = artifactId
            version = version
        }
    }
    repositories {
        maven {
            url = uri("https://jitpack.io")
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

// Configure JavaDoc to suppress warnings for Lombok-generated constructors
tasks.javadoc {
    options {
        (this as StandardJavadocDocletOptions).apply {
            addStringOption("Xdoclint:all,-missing", "-quiet")
        }
    }
}
