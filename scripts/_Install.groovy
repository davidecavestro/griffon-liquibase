//
// This script is executed by Griffon after plugin was installed to project.
// This script is a Gant script so you can use all special variables provided
// by Gant (such as 'baseDir' which points on project base dir). You can
// use 'ant' to access a global instance of AntBuilder
//
// For example you can create directory under project tree:
//
//    ant.mkdir(dir:"${basedir}/griffon-app/jobs")
//

// Update the following configuration if your addon
// requires a different prefix or exposes nodes in
// a different way.
// Remember to apply the reverse changes in _Uninstall.groovy
//
// check to see if we already have a LiquibaseGriffonAddon
// def configText = '''root.'LiquibaseGriffonAddon'.addon=true'''
// if(!(builderConfigFile.text.contains(configText))) {
//     println 'Adding LiquibaseGriffonAddon to Builder.groovy'
//     builderConfigFile.append("""
// $configText
// """)
// }

ant.mkdir(dir:"${basedir}/griffon-app/resources/migrations")

includeTargets << griffonScript("_GriffonCreateArtifacts")

argsMap = argsMap ?: [:]
argsMap.skipPackagePrompt = true

if(!new File("${basedir}/griffon-app/resources/migrations/RootChangelog.groovy").exists()) {
    createArtifact(
        name:   "RootChangelog",
        suffix: "",
        type:   "RootChangelog",
        path:   "griffon-app/resources/migrations")
}