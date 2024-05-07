def getTargetFiles(String credentialsId, List directories, List extentions) {
    /*
     * Get all files from the file updated by git. Except removed or deleted.
     *
     * @param credentialsId(String): Credentials ID of github on jenkins cred manager.
     * @param directories(List): Directories the file should have.
     * @param extentions(List): Extentions of target files.
     */
    def updatedFiles = getUpdatedFiles(credentialsId, variable)
    def extractedFiles = extractSpecifiedDirFiles(updatedFiles, directories)

    def files = extensions.collect { "it.contains('${it}')" }.join(" || ")
    def targetFiles = extractedFiles.findAll { eval(condition) }
    def files = targetFiles.collect {
        it.replaceAll(removePath, "")
    }

    return files
}

def getUpdatedFiles(String credentialsId) {
    /*
     * Get updated files in PR on git. Except removed or deleted.
     *
     * @param credentialsId(String): Credentials ID of github on jenkins cred manager.
     */
    withCredentials([[$class: 'StringBinding', credentialsId: credentialsId, variable: 'GITHUB_TOKEN']]) {
        def repo
        if ("${env.GIT_URL}".contains("https://")) {
            repo = "${env.GIT_URL}".split("https://github.com/")[1].replace(".git", "")
        }
        else {
            repo = "${env.GIT_URL}".split("git@github.com:")[1].replace(".git", "")
        }
        def gitURL = "https://github.com/api/v3/repos/${repo}/pulls/${env.CHANGE_ID}/files?page=1&per_page=1000"
        def response = sh(returnStdout: true, script: "curl -H 'Accept: application/json' -H 'Authorization: Bearer ${GITHUB_TOKEN}' ${gitURL}")
        def props = readJSON text: "${response}"
        def changeList = [];

        props.each {
            if(it.status != "removed") {
                changeList.add(it.filename)
            }
        }
        return changeList;
    }
}

def extractSpecifiedDirFiles(List files, List directories){
    /*
     * Extract files that exist in specified directory names.
     *
     * @param files(List): Files of analitic target.
     * @param directories(List): Drectories the file should have.
     */
     def extractedFiles = []

     directories.each { directory ->
        extractedFiles.addAll(files.findAll { file ->
            file.contains(directory)
        })
     }
     extractedFiles.unique()
     return extractedFiles
}