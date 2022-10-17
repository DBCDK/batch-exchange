#!groovy

def workerNode = "devel11"

pipeline {
	agent {label workerNode}

	tools {
		maven 'Maven 3'
	}

    environment {
		GITLAB_PRIVATE_TOKEN = credentials("metascrum-gitlab-api-token")
	}

	options {
		timestamps()
		disableConcurrentBuilds()
	}

	stages {
		stage("clear workspace") {
			steps {
				deleteDir()
				checkout scm
			}
		}

		stage("verify") {
			steps {
				sh "mvn -B verify pmd:pmd javadoc:aggregate"

				script {
					def java = scanForIssues tool: [$class: 'Java']
					def javadoc = scanForIssues tool: [$class: 'JavaDoc']
					publishIssues issues: [java, javadoc]

					def pmd = scanForIssues tool: [$class: 'Pmd'], pattern: '**/target/pmd.xml'
					publishIssues issues: [pmd]
				}
			}
		}

		stage("deploy") {
			when {
                branch "master"
            }
			steps {
				sh "mvn -Dmaven.test.skip=true jar:jar deploy:deploy"
			}
		}
    }
}
