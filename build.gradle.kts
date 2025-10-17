
/*
 * Copyright 2022 Intershop Communications AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import io.gitee.pkmer.enums.PublishingType

plugins {
    // project plugins
    `java-library`

    // test coverage
    jacoco

    // ide plugin
    idea
    eclipse

    // publish plugin
    `maven-publish`

    // artifact signing - necessary on Maven Central
    signing

    id("com.dorongold.task-tree") version "4.0.1"

    id("io.gitee.pkmer.pkmerboot-central-publisher") version "1.1.1"
}

// release configuration
group = "com.intershop.version"
description = "semantic version"
// apply gradle property 'projectVersion' to project.version, default to 'LOCAL'
val projectVersion : String? by project
version = projectVersion ?: "LOCAL"

val sonatypeUsername: String? by project
val sonatypePassword: String? by project

repositories {
    gradlePluginPortal()
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }

    withJavadocJar()
    withSourcesJar()
}

// set correct project status
if (project.version.toString().endsWith("-SNAPSHOT")) {
    status = "snapshot'"
}

jacoco {
    toolVersion = "0.8.12"
}

tasks {
    withType<Test> {
        useJUnitPlatform()
    }

    withType<JacocoReport> {
        reports {
            xml.required.set(true)
            html.required.set(true)

            html.outputLocation.set( File(project.layout.buildDirectory.asFile.get(), "jacocoHtml") )
        }

        dependsOn(test)
    }
}

val stagingRepoDir = project.layout.buildDirectory.dir("stagingRepo")

publishing {
    publications {
        create<MavenPublication>("intershopMvn") {
            from(components["java"])
        }
        withType<MavenPublication>().configureEach {
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https:/github.com/IntershopCommunicationsAG/${project.name}")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                        distribution.set("repo")
                    }
                }

                organization {
                    name.set("Intershop Communications AG")
                    url.set("https://intershop.com")
                }

                developers {
                    developer {
                        id.set("team")
                        name.set("CoreTeam@Intershop")
                        email.set("ENGCORETEAM@intershop.de")
                    }
                }

                scm {
                    connection.set("https://github.com/IntershopCommunicationsAG/${project.name}.git")
                    developerConnection.set("git@github.com:IntershopCommunicationsAG/${project.name}.git")
                    url.set("https://github.com/IntershopCommunicationsAG/${project.name}")
                }
            }
        }
    }
    repositories {
        maven {
            name = "LOCAL"
            url = stagingRepoDir.get().asFile.toURI()
        }
    }
}

pkmerBoot {
    sonatypeMavenCentral{
        // the same with publishing.repositories.maven.url in the configuration.
        stagingRepository = stagingRepoDir

        /**
         * get username and password from
         * <a href="https://central.sonatype.com/account"> central sonatype account</a>
         */
        username = sonatypeUsername
        password = sonatypePassword

        // Optional the publishingType default value is PublishingType.AUTOMATIC
        publishingType = PublishingType.USER_MANAGED
    }
}

signing {
    sign(publishing.publications["intershopMvn"])
}

dependencies {
    implementation("org.apache.commons:commons-collections4:4.5.0")

    testImplementation("org.junit.jupiter:junit-jupiter:6.0.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

