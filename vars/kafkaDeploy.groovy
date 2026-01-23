def call(Map args = [:]) {

    def config

    stage('Load Config') {
        config = readYaml text: libraryResource(args.configFile)
        echo "Environment: ${config.ENVIRONMENT}"
    }

    stage('Clone Repo') {
    echo "Cloning Kafka code..."

    dir('ansible-src') {
        git branch: 'Priyanshunegi_ansible',
            url: 'https://github.com/OT-MyGurukulam/Ansible_33.git',
            credentialsId: 'github-creds'
    }
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
          -u ${config.ansible_user} \
          ${config.CODE_BASE_PATH}/site.yml \
          -i ${config.CODE_BASE_PATH}/aws_ec2.yml \
          --private-key=${config.private_key_path}
        """
    }

    stage('Slack Notification') {
        slackSend(
        channel: config.SLACK_CHANNEL_NAME,
        color: currentBuild.currentResult == 'SUCCESS' ? 'good' : 'danger',
        message: """
Kafka Deployment Completed

Job: ${env.JOB_NAME}
Build #: ${env.BUILD_NUMBER}
Environment: ${config.ENVIRONMENT}
Result: ${currentBuild.currentResult}

Build URL:
${env.BUILD_URL}
"""
    )
    }
}
