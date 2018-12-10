@Library('transitive-pipeline-library') _

node {
    stage('Build') {
        def commitId = transitive.getCommitId()
        transitive.postGitHub(commitId, 'pending', 'build', 'Build is running')

        try {
          bat  "${mvnHome}/bin/mvn clean package"
          transitive.postGitHub(commitId, 'success', 'build', 'Build succeeded')
        } catch (error) {
          transitive.postGitHub(commitId, 'failure', 'build', 'Build failed')
          throw error
        }
    }

    stage('Nexus Lifecycle Analysis') {
        def commitId = transitive.getCommitId()
        transitive.postGitHub(commitId, 'pending', 'analysis', 'Nexus Lifecycle Analysis is running')

        try {
          def policyEvaluation = nexusPolicyEvaluation failBuildOnNetworkError: false, iqApplication: selectedApplication('nexusPlatformPlugin'), iqStage: 'build', jobCredentialsId: ''
          transitive.postGitHub(commitId, 'success', 'analysis', 'Nexus Lifecycle Analysis succeeded', "${policyEvaluation.applicationCompositionReportUrl}")
        } catch (error) {
          def policyEvaluation = error.policyEvaluation
          transitive.postGitHub(commitId, 'failure', 'analysis', 'Nexus Lifecycle Analysis failed', "${policyEvaluation.applicationCompositionReportUrl}")
          throw error
        }
    }
}
