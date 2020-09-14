pipeline {
	agent {
		node {
			label "ssh-slave"
		}
	}

	parameters {
		booleanParam(name: 'PERFORM_RELEASE', defaultValue: false, description: "Performs release setting release version number, publish release artifacts and tags version.")
		string (name: 'NEXT_DEVELOPMENT_VERSION_NUMBER', defaultValue: '', description: "Input next development version as MAJOR.MINOR.PATCH.")
	}

	stages {
		stage("ensure versions") {
			stages {
				stage('check version') {
					steps {
						script {
							gradlePropertiesFile = readProperties  file: 'gradle.properties'
							env.PROJECT_VERSION = gradlePropertiesFile['version']
							if (!env.PROJECT_VERSION.endsWith('-SNAPSHOT')) {
								error("ERROR: Project version $PROJECT_VERSION must include SNAPSHOT")
							}
							env.BASE_VERSION = env.PROJECT_VERSION.replaceFirst(/-SNAPSHOT$/, "")
							env.BUILD_VERSION = env.BASE_VERSION + "-SNAPSHOT"
							env.VERSION = env.BUILD_VERSION // might be overwritten below
						}
						// Original bash expressions:  sed -r -i 's/^(version\s?=\s?)([^\n]+)/\1env.VERSION/' gradle.properties
						// We don't have any good set-version plugin for Gradle, see discussions here:
						// - https://discuss.gradle.org/t/preferred-way-to-build-a-release/27043/4
						// - https://discuss.gradle.org/t/a-programatic-way-to-set-the-version-of-any-gradle-project/23197/4
						// so we fall back on good old search-replace in files :-(
						// We do specifically want to have the version and commit it in version control for the version being built
						// for later reference, as opposed to solution where version is set as parameter.
						sh """
							env
							sed -r -i 's/^(version\\s?=\\s?)([^\\n]+)/\\1$VERSION/' gradle.properties
							git diff gradle.properties
							ls -al gradle.properties*
							cat gradle.properties*
						"""
						buildName "#${BUILD_NUMBER}-#${env.BASE_VERSION} (DEV)"
					}
				}
				stage('set release version') {
					when { expression { return params.PERFORM_RELEASE } }
					steps {
						// replace version with release version instead of build version with branch name
						// and update environment
						script {
							env.VERSION = env.BASE_VERSION
						}
						withCredentials([usernamePassword(credentialsId: env.CI_GLOBAL_ENV_CREDENTIAL_ID_NAME_CI_GITHUB_PERSONAL_ACCESS_TOKEN_SCOPE_REPO__V1, passwordVariable: 'GIT_PERSONAL_ACCESS_TOKEN', usernameVariable: 'GIT_USERNAME')]) {
							sh("""
									git config --local credential.helper "!f() { echo username=\\$GIT_USERNAME; echo password=\\$GIT_PERSONAL_ACCESS_TOKEN; }; f"
									sed -r -i 's/^(version\\s?=\\s?)([^\\n]+)/\\1$VERSION/' gradle.properties
									git diff gradle.properties
									git remote -v show
									git add gradle.properties
									git commit -m "Version number for release"
									git status
									git tag -a "v${VERSION}" -m "Release v${VERSION}"
									git log -n2
							""")
						}
						buildName "#${BUILD_NUMBER}-#${env.BASE_VERSION} (REL)"
					}
				}
			}
		} // end ensure versions



		stage('parallel') {
			failFast true
			parallel {
				stage('build test') {
					stages {
						stage('gradle build') {
							steps {
								withDockerContainer(args: '-v $HOME/.gradle:/home/gradle/.gradle -e GRADLE_USER_HOME=/home/gradle/.gradle', image: env.GRADLE_BUILD_DOCKER_IMAGE_VERSION) {
									sh 'gradle clean build compileTest -x test'
								}
							}
						}
						stage('unit tests') {
							steps {
								withDockerContainer(args: '-v $HOME/.gradle:/home/gradle/.gradle -e GRADLE_USER_HOME=/home/gradle/.gradle', image: env.GRADLE_BUILD_DOCKER_IMAGE_VERSION) {
									sh "gradle test"
								}
								junit testResults:  '**/build/test-results/test/TEST-*.xml'
							}
						}
					}
				}
			}
		}

		stage('publish private') {
			steps {
				withDockerContainer(args: '-v $HOME/.gradle:/home/gradle/.gradle -e GRADLE_USER_HOME=/home/gradle/.gradle', image: env.GRADLE_BUILD_DOCKER_IMAGE_VERSION) {
					sh "gradle publish"
				}
			}
		}
// 		stage('publish public') {
// 			when { expression { return params.PERFORM_RELEASE } }
// 			steps {
// 				withDockerContainer(args: '-v $HOME/.gradle:/home/gradle/.gradle -e GRADLE_USER_HOME=/home/gradle/.gradle', image: env.GRADLE_BUILD_DOCKER_IMAGE_VERSION) {
// 					sh "gradle publish -PpublishPublic"
// 				}
// 			}
// 		}
		stage("git publish") {
			when { expression { return params.PERFORM_RELEASE } }
			stages {
				stage("push release commit and tag") {
					steps {
						withCredentials([usernamePassword(credentialsId: env.CI_GLOBAL_ENV_CREDENTIAL_ID_NAME_CI_GITHUB_PERSONAL_ACCESS_TOKEN_SCOPE_REPO__V1, passwordVariable: 'GIT_PERSONAL_ACCESS_TOKEN', usernameVariable: 'GIT_USERNAME')]) {
							sh """
								git status
								git log -n2
								.ci/git-command-randon-failure-hack.sh git push origin --tags HEAD:$GIT_BRANCH
							"""
						}
					}
				}

				stage("set next dev. version") {
					steps {
						withCredentials([usernamePassword(credentialsId: env.CI_GLOBAL_ENV_CREDENTIAL_ID_NAME_CI_GITHUB_PERSONAL_ACCESS_TOKEN_SCOPE_REPO__V1, passwordVariable: 'GIT_PERSONAL_ACCESS_TOKEN', usernameVariable: 'GIT_USERNAME')]) {
							sh """
								git config --local credential.helper "!f() { echo username=$GIT_USERNAME; echo password=$GIT_PERSONAL_ACCESS_TOKEN; }; f"
								sed -r -i 's/^(version\\s?=\\s?)([^\\n]+)/\\1${params.NEXT_DEVELOPMENT_VERSION_NUMBER}-SNAPSHOT/' gradle.properties
								git add gradle.properties
								git diff
								git commit -m "Version bump to next development cycle"
								git status
								git log -n2
								.ci/git-command-randon-failure-hack.sh git push origin HEAD:$GIT_BRANCH
							"""
						}
					}
				}
			}
		}
	}
	post {
		unsuccessful {
			// aborted, failure or unstable status:
			slackSend message: "*${currentBuild.currentResult}*:<${BUILD_URL}|${JOB_NAME} #${BUILD_NUMBER}>", notifyCommitters: true
		}
	}
}
