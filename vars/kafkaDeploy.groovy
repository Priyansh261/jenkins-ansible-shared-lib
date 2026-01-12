def call(Map args = [:]) {

    def config

    stage('Load Config') {
        config = readYaml text: libraryResource(args.configFile)
        echo "Environment: ${config.ENVIRONMENT}"
    }

    stage('Clone Repo') {

        deleteDir()
        echo "Cloning Kafka code..."
        git branch: 'Priyanshunegi_ansible',
            url: 'https://github.com/OT-MyGurukulam/Ansible_33.git'
            credentialsId: 'github-creds'
    }

    if (config.KEEP_APPROVAL_STAGE == true) {
        stage('User Approval') {
            input message: "Approve Kafka deployment for ${config.ENVIRONMENT}?"
        }
    }

    stage('Ansible Playbook Execution') {
        echo "Running Ansible for Kafka..."
        sh """
        ansible-playbook \
          ${config.CODE_BASE_PATH}/site.yml \
          -i ${config.CODE_BASE_PATH}/inventory
        """
    }

    stage('Slack Notification') {
        slackSend(
            channel: config.SLACK_CHANNEL_NAME,
            message: "SUCCESS: ${config.ACTION_MESSAGE}"
        )
    }
}
