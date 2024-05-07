def readConfigYamlBeforehand(String path, String nodeLabel) {
    def conf
    timestamps {
        def sanitizedPath = path.replaceAll(/^\.\//, '')
        node(nodeLabel) {
            dir('read_config_tmp') {
                try {
                    checkout([
                        $class: 'GitSCM',
                        branches: scm.extensions + [$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: sanitizedPath]]]
                        userRemoteConfigs: scm.userRemoteConfigs
                    ])
                    readYaml(file: sanitizedPath)
                } catch(e) {
                    error("Config file is not read correctly. path: ${sanitizedPath}. error: ${e.getCause().getMessage()}")
                }
            }
            deleteDir()
        }
    }
}