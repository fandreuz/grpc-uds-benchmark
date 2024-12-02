import com.google.protobuf.gradle.id
import me.champeau.jmh.JMHTask

plugins {
  application
  id("com.google.protobuf") version "0.9.4"
  id("me.champeau.jmh") version "0.7.2"
  id("com.diffplug.spotless") version "7.0.0.BETA4"
  id("io.freefair.lombok") version "8.11"
}

val grpcVersion = "1.68.2"
val protobufVersion = "4.29.0"
val protocVersion = protobufVersion

repositories { mavenCentral() }

dependencies {
  implementation("com.google.protobuf:protobuf-java-util:$protocVersion")
  implementation("io.grpc:grpc-protobuf:$grpcVersion")
  implementation("io.grpc:grpc-services:$grpcVersion")
  implementation("io.grpc:grpc-stub:$grpcVersion")
  implementation("io.grpc:grpc-netty:$grpcVersion")
  implementation("io.netty:netty-all:4.1.115.Final")
  compileOnly("org.apache.tomcat:annotations-api:6.0.53")

  testImplementation(libs.junit.jupiter)
  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

protobuf {
  protoc { artifact = "com.google.protobuf:protoc:$protocVersion" }
  plugins { id("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion" } }
  generateProtoTasks { ofSourceSet(sourceSets.main.name).forEach { it.plugins { id("grpc") {} } } }
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

application { mainClass = "com.fandreuz.grpc.uds.Server" }

tasks.named<Test>("test") { useJUnitPlatform() }

spotless {
  java {
    removeUnusedImports()
    indentWithSpaces()
    palantirJavaFormat()
  }
  kotlinGradle { ktfmt() }
}
