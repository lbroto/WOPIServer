plugins {
	java
	eclipse
	id("org.springframework.boot") version "3.2.4"
	id("io.spring.dependency-management") version "1.1.2"
}

group = "org.wopiserver"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_21
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("commons-cli:commons-cli:1.5.0")
	implementation("commons-io:commons-io:2.16.1")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
