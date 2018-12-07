stage('Build') {
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

def postGitHub(commitId, state, context, description, targetUrl) {
  def payload = JsonOutput.toJson(
    state: state,
    context: context,
    description: description,
    target_url: targetUrl
  )
  sh "curl -H \"Authorization: token ${gitHubApiToken}\" --request POST --data '${payload}'
  https://api.github.com/repos/${project}/statuses/${commitId} > /dev/null"
}
