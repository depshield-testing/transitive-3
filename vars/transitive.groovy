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
    stdout = bat(returnStdout:true , script: 'git rev-parse HEAD').trim()
    result = stdout.readLines().drop(1).join(" ")
    return result
//    def p = "git rev-parse HEAD".execute()
//    def bri = new BufferedReader (new InputStreamReader(p.getInputStream()))
//    return bri.readLine().trim()
    //bat "git rev-parse HEAD"

    //bat(script: 'git rev-parse HEAD', returnStdout: true).trim()
  }
}

def postGitHub(commitId, state, context, description, targetUrl) {
  String payload = String.format("{\\\"state\\\":\\\"%s\\\",\\\"context\\\":\\\"%s\\\",\\\"description\\\":\\\"%s\\\",\\\"target_url\\\":\\\"%s\\\"}",
    state, context, description, targetUrl)
//  def payload = "{\"state\":\"" + state + "\"}"  //''{ "state":"' + state + '","context":"' + context + '","description":"' + description + '","target_url":"' + targetUrl + '"}'
  bat "curl -H \"Authorization: token ${gitHubApiToken}\" --request POST --data \"${payload}\" https://api.github.com/repos/${project}/statuses/${commitId}"
}
