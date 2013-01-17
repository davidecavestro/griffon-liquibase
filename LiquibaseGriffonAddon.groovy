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



import griffon.plugins.datasource.DataSourceConnector
import griffon.plugins.datasource.DataSourceHolder
import groovy.sql.Sql
import liquibase.Liquibase
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.exception.LiquibaseException
import liquibase.parser.ChangeLogParserFactory
import liquibase.parser.ext.GroovyLiquibaseChangeLogParser
import liquibase.resource.ResourceAccessor
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

import java.sql.Connection
import java.sql.SQLException

/**
 * @author Davide Cavestro
 */

class LiquibaseGriffonAddon {
    GriffonApplication app

    // lifecycle methods

    // called once, after the addon is created
    void addonInit(GriffonApplication app) {
        this.app = app
    }

    Map events = [
        DataSourceConnectEnd: {dsName, ds->
            PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver(app.class.classLoader)
            ResourceAccessor resourceAccessor = new SpringResourceAccessor (pathResolver: pathResolver)

            ChangeLogParserFactory parserFactory = ChangeLogParserFactory.instance
            ChangeLogParserFactory.getInstance().register(new GroovyLiquibaseChangeLogParser())

            def changeLogFilePath = app.config.griffon?.liquibase?.rootChangeLogPath
            if (!changeLogFilePath) {//default root changelog path
                changeLogFilePath = "classpath:migrations/RootChangelog.groovy"
            }

            //FIXME provide a way to work on multiple datasources
            DataSourceConnector.instance.resolveDataSourceProvider(app).withSql('default') {String dataSourceName, Sql sql->
                Connection c = sql.getConnection()
                Liquibase liquibase = null;
                try {
                    liquibase = createLiquibase(c, changeLogFilePath, resourceAccessor);
                    def contexts = ''
                    liquibase.update(contexts);
                } catch (SQLException e) {
                    throw new DatabaseException(e);
                } finally {
                    if (liquibase != null) {
                        liquibase.forceReleaseLocks();
                    }
                    if (c != null) {
                        try {
                            c.rollback();
                            c.close();
                        } catch (SQLException e) {
                            //nothing to do
                        }
                    }
                }
            }
        }
    ]


    protected Liquibase createLiquibase(Connection c, String changeLog, ResourceAccessor resourceAccessor) throws LiquibaseException {
        Liquibase liquibase = new Liquibase(changeLog, resourceAccessor, createDatabase(c));
        return liquibase;
    }
    /**
     * Subclasses may override this method add change some database settings such as
     * default schema before returning the database object.
     * @param c
     * @return a Database implementation retrieved from the {@link DatabaseFactory}.
     * @throws DatabaseException
     */
    protected Database createDatabase(Connection c) throws DatabaseException {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
        return database;
    }

    /**
     * A resource accessor backed by a Spring path resolver
     */
    class SpringResourceAccessor implements ResourceAccessor {

        PathMatchingResourcePatternResolver pathResolver

        @Override
        InputStream getResourceAsStream(String location) throws IOException {
            pathResolver.getResource(location).inputStream
        }

        @Override
        Enumeration<URL> getResources(String locationPattern) throws IOException {
            pathResolver.getResources(locationPattern).collect {Resource resource->
                resource.URL
            }
        }

        @Override
        ClassLoader toClassLoader() {
            return pathResolver.classLoader
        }
    }
}
