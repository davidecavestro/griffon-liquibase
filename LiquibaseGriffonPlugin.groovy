/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author Davide Cavestro
 */
class LiquibaseGriffonPlugin {
    // the plugin version
    String version = '0.2'
    // the version or versions of Griffon the plugin is designed for
    String griffonVersion = '1.2.0 > *'
    // the other plugins this plugin depends on
    Map dependsOn = [gsql: '1.1.1', spring: '1.2.0']
    // resources that are included in plugin packaging
    List pluginIncludes = []
    // the plugin license
    String license = 'Apache Software License 2.0'
    // Toolkit compatibility. No value means compatible with all
    // Valid values are: swing, javafx, swt, pivot, qt
    List toolkits = []
    // Platform compatibility. No value means compatible with all
    // Valid values are:
    // linux, linux64, windows, windows64, macosx, macosx64, solaris
    List platforms = []
    // URL where documentation can be found
    String documentation = ''
    // URL where source can be found
    String source = 'https://github.com/davidecavestro/griffon-liquibase'

    List authors = [
            [
                    name: 'Davide Cavestro',
                    email: 'davide.cavestro@gmail.com'
            ]
    ]
    String title = 'Executes database migration scripts using Liquibase'
    // accepts Markdown syntax. See http://daringfireball.net/projects/markdown/ for details
    String description = '''
Provides integration with [Liquibase][1] for database migrations (parsing groovy changesets
through [groovy-liquibase-dsl][2]).
This plugin enables the execution of Liquibase migration scripts at application startup. Hence you have to
include your changesets on the migration changelog file.

Usage
-----
Upon installation the plugin will generate the following artifacts in `$appdir/griffon-app/resources/migrations`:

 * RootChangelog.groovy - contains the root changelog definition.

The root changelog can be used to directly define the migration changesets. Since it can lead to huge contents from
version 0.2 you can split changelogs in multiple files and include them from the root changelog.


    databaseChangeLog() {
        include(file: 'migrations/20130304-initial-schema.groovy', relativeToChangelog: false)
        include(file: 'migrations/20130305-other-changes.groovy', relativeToChangelog: false)
        ...
    }

Every changelog can define multiple changesets, following the [liquibase syntax][3]

    databaseChangeLog() {
        changeSet(id:'initial-schema', author: 'yourname') {
            //cross dbms directives
            createTable (tableName: "person") {
                column (name:"id", type:"bigint", autoIncrement:"true") {
                    constraints (primaryKey:"true", nullable:"false")
                }
                column (name:"firstname", type:"varchar(50)")
                column (name:"lastname", type:"varchar(50)") {
                    constraints (nullable:"false")
                }
            }
            //dbms-specific sql syntax
            sql(stripComments: true, splitStatements: true, endDelimiter: ';') {
                """
                CREATE TABLE mytable (
                    id LONG NOT NULL AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR (4000),
                    description VARCHAR (10000),
                );
                """
            }
        }
        ...
    }



Configuration
-------------
Should you prefer moving _RootChangelog.groovy_ to a different path you should also add to _Config.groovy_ the
configuration property `griffon.liquibase.rootChangeLogPath` pointing to the right path (where the default value
is `"classpath:migrations/RootChangelog.groovy"`).

TODO
-------------
* Documentation
* Tests
* Provide a way to work on multiple datasources
* Provide more liquibase configuration hooks (changeset parameters, contexts, default tablespace, etc)


[1]: http://liquibase.org/
[2]: https://github.com/tlberglund/groovy-liquibase
[3]: http://www.liquibase.org/manual/home
'''
}
