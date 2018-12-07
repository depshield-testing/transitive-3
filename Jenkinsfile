pipeline {
  stages {
    stage('Build') {
      steps {
        postGitHub commitId, 'pending', 'build', 'Build is running'

        try {
          sh  "${mvnHome}/bin/mvn clean package"
          postGitHub commitId, 'success', 'build', 'Build succeeded'
        } catch (error) {
          postGitHub commitId, 'failure', 'build', 'Build failed'
          throw error
        }
      }
    }

    stage('Nexus Lifecycle Analysis') {
      steps {
        postGitHub commitId, 'pending', 'analysis', 'Nexus Lifecycle Analysis is running'

        try {
          def policyEvaluation = nexusPolicyEvaluation failBuildOnNetworkError: false, iqApplication: selectedApplication('nexusPlatformPlugin'), iqStage: 'build', jobCredentialsId: ''
          postGitHub commitId, 'success', 'analysis', 'Nexus Lifecycle Analysis succeeded', "${policyEvaluation.applicationCompositionReportUrl}"
        } catch (error) {
          def policyEvaluation = error.policyEvaluation
          postGitHub commitId, 'failure', 'analysis', 'Nexus Lifecycle Analysis failed', "${policyEvaluation.applicationCompositionReportUrl}"
          throw error
        }
      }
    }
  }
}
