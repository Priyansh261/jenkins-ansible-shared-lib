def call(Map args = [:]) {

    // Load config file
    def config = readYaml text: libraryResource(args.configFile)

    pipeline {
        agent any

        stages {

            stage('Clone Repo') {
                steps {
                    echo "Cloning Kafka code..."
                    git branch: 'Priyanshunegi_ansible',
                        url: 'https://github.com/OT-MyGurukulam/Ansible_33.git'
                }
            }

            stage('User Approval') {
                when {
                    expression { config.KEEP_APPROVAL_STAGE == true }
                }
                steps {
                    input message: "Approve Kafka deployment for ${config.ENVIRONMENT}?"
                }
            }

            stage('Ansible Playbook Execution') {
                steps {
                    echo "Running Ansible for Kafka..."
                    sh """
                    ansible-playbook \
                      ${config.CODE_BASE_PATH}/site.yml \
                      -i ${config.CODE_BASE_PATH}/inventory
                    """
                }
            }
        }

        post {
            success {
                slackSend(
                    channel: config.SLACK_CHANNEL_NAME,
                    message: "SUCCESS: ${config.ACTION_MESSAGE}"
                )
            }
            failure {
                slackSend(
                    channel: config.SLACK_CHANNEL_NAME,
                    message: "FAILED: Kafka deployment failed"
                )
            }
        }
    }
}
