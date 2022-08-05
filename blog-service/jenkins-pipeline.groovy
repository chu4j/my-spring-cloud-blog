podTemplate(
        yaml: '''
    apiVersion: v1
    kind: Pod
    metadata:
      labels: 
        app-label: blog-service
    spec:
      replicas: 2
      selector:
        matchLabels:
          app: blog-service
    ''',
        workspaceVolume: persistentVolumeClaimWorkspaceVolume(claimName: 'jenkins-pvc', readOnly: false)
) {
    node(POD_LABEL) {
        stage('Build Project') {
            echo '----print all env variable----'
            sh 'env'
            echo '----print all custom params----'
            for (param in params) {
                echo "${param.key}  -->  ${param.value}"
            }
            git 'https://github.com/zgbjty/my-spring-cloud-blog.git'
            withMaven(
                    options: [artifactsPublisher(fingerprintFilesDisabled: true, archiveFilesDisabled: true),
                              junitPublisher(disabled: true),
                              findbugsPublisher(disabled: true),
                              openTasksPublisher(disabled: true),
                              dependenciesFingerprintPublisher(disabled: true),
                              concordionPublisher(disabled: true),
                              invokerPublisher(disabled: true),
                              jgivenPublisher(disabled: true),
                              jacocoPublisher(disabled: true),
                              mavenLinkerPublisher(disabled: true),
                              pipelineGraphPublisher(disabled: true)],
                    maven: 'maven3',
                    mavenLocalRepo: '$JENKINS_AGENT_WORKDIR/.m2/repository',
                    mavenSettingsConfig: 'd8292f9d-502a-47f2-b3d9-9d253090d09d'
            ) {
                // sh 'mvn -B -f ./blog-service/pom.xml clean install -DskipTests=true -Dmaven.repo.local=$JENKINS_AGENT_WORKDIR/.m2/repository'
                sh "mvn -B -f ./blog-service/pom.xml clean package -DskipTests=true -Dspring.profiles.active=${params.Profile}"
            }
        }
    }
}