plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("io.spring.dependency-management") version "1.1.6"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework:spring-context:6.1.14")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

	implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
	implementation("ch.qos.logback:logback-classic:1.4.12")


	testImplementation("io.kotest:kotest-runner-junit5:5.6.2") // Kotest Runner
	testImplementation("io.kotest:kotest-assertions-core:5.6.2") // Kotest Assertions
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3") // Coroutines test
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
