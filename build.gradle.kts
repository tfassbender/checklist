plugins {
    java
    id("io.quarkus")
}

repositories {
    mavenCentral()
}

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("${quarkusPlatformGroupId}:${quarkusPlatformArtifactId}:${quarkusPlatformVersion}"))
    implementation("io.quarkus:quarkus-arc")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-smallrye-jwt")
    implementation("io.quarkus:quarkus-smallrye-jwt-build")
    implementation("io.quarkus:quarkus-security")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.rest-assured:rest-assured")
}

group = "net.tfassbender"
version = "1.0.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

tasks.withType<Test> {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

// Frontend build integration
val frontendDir = "frontend"
val frontendBuildDir = "$frontendDir/dist"
val resourcesDir = "src/main/resources/META-INF/resources"

tasks.register<Exec>("npmInstall") {
    workingDir = file(frontendDir)
    commandLine("cmd", "/c", "npm", "install")
    inputs.file("$frontendDir/package.json")
    outputs.dir("$frontendDir/node_modules")
}

tasks.register<Exec>("buildFrontend") {
    dependsOn("npmInstall")
    workingDir = file(frontendDir)
    commandLine("cmd", "/c", "npm", "run", "build")
    inputs.dir("$frontendDir/src")
    inputs.file("$frontendDir/index.html")
    inputs.file("$frontendDir/vite.config.ts")
    inputs.file("$frontendDir/tailwind.config.js")
    outputs.dir(frontendBuildDir)
}

tasks.register<Copy>("copyFrontend") {
    dependsOn("buildFrontend")
    from(frontendBuildDir)
    into(resourcesDir)
}

tasks.named("processResources") {
    dependsOn("copyFrontend")
}

tasks.register<Exec>("devFrontend") {
    workingDir = file(frontendDir)
    commandLine("cmd", "/c", "npm", "run", "dev")
}
