String getCommitId() {
  // This may be called in two situations
  // 1) during a build in which case we have the full git info
  // 2) during a deploy in staging/prod in which case we do not have git, just the copied artifacts. In this case we grab it from build-info.json

  def exists = fileExists 'build-info.json'

  if (exists) {
    def props = readJSON file: 'build-info.json'
    gitHash = props['gitHash']
    assert gitHash != null
    return gitHash.trim()
  }
  else {
    return sh(script: 'git rev-parse HEAD', returnStdout: true).trim()
  }
}

def postGitHub(commitId, state, context, description, targetUrl) {
  def payload = JsonOutput.toJson(
      state: state,
      context: context,
      description: description,
      target_url: targetUrl
  )
  sh "curl -H \"Authorization: token ${gitHubApiToken}\" --request POST --data '${payload}' https://api.github.com/repos/${project}/statuses/${commitId} > /dev/null"
}

node {
    stage('Build') {
        def commitId = getCommitId()
        postGitHub commitId, 'pending', 'build', 'Build is running'

        try {
          sh  "${mvnHome}/bin/mvn clean package"
          postGitHub commitId, 'success', 'build', 'Build succeeded'
        } catch (error) {
          postGitHub commitId, 'failure', 'build', 'Build failed'
          throw error
        }
    }

    stage('Nexus Lifecycle Analysis') {
        def commitId = getCommitId()
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
