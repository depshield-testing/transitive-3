@Library('transitive-pipeline-library') _

node {
    stage('Build') {
        def commitId = utils.getCommitId()
        utils.postGitHub commitId, 'pending', 'build', 'Build is running'

        try {
          bat  "${mvnHome}/bin/mvn clean package"
          utils.postGitHub commitId, 'success', 'build', 'Build succeeded'
        } catch (error) {
          utils.postGitHub commitId, 'failure', 'build', 'Build failed'
          throw error
        }
    }

    stage('Nexus Lifecycle Analysis') {
        def commitId = utils.getCommitId()
        postGitHub commitId, 'pending', 'analysis', 'Nexus Lifecycle Analysis is running'

        try {
          def policyEvaluation = nexusPolicyEvaluation failBuildOnNetworkError: false, iqApplication: selectedApplication('nexusPlatformPlugin'), iqStage: 'build', jobCredentialsId: ''
          utils.postGitHub commitId, 'success', 'analysis', 'Nexus Lifecycle Analysis succeeded', "${policyEvaluation.applicationCompositionReportUrl}"
        } catch (error) {
          def policyEvaluation = error.policyEvaluation
          utils.postGitHub commitId, 'failure', 'analysis', 'Nexus Lifecycle Analysis failed', "${policyEvaluation.applicationCompositionReportUrl}"
          throw error
        }
    }
}
