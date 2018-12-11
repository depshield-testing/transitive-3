@Library('transitive-pipeline-library') _

withEnv([
    'ENVIRONMENT=dev',
    'project=depshield-testing/transitive-3'
  ]) {
  withCredentials([
    string(credentialsId: 'gitHubApiToken', variable: 'gitHubApiToken')
  ]) {
  node {
    stage('Build') {
        try {
          bat  "mvn clean package"
        } catch (error) {
          throw error
        }
    }

    stage('Nexus Lifecycle Analysis') {
        try {
          def policyEvaluation = nexusPolicyEvaluation failBuildOnNetworkError: true, iqApplication: 'transitive-3', iqScanPatterns: [[scanPattern: '**/target/mod-boot*.jar']], iqStage: 'build', jobCredentialsId: 'nexus-iq'
          def policyEvaluation2 = nexusPolicyEvaluation failBuildOnNetworkError: true, iqApplication: 'transitive-3', iqScanPatterns: [[scanPattern: '**/target/mod-orm*.jar']], iqStage: 'build', jobCredentialsId: 'nexus-iq'
        } catch (error) {
          throw error
        }
    }
  }
  }
}
