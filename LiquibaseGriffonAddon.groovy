import griffon.plugins.datasource.DataSourceHolder
import groovy.sql.Sql
import liquibase.Liquibase
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.exception.DatabaseException
import liquibase.exception.LiquibaseException
import liquibase.parser.ChangeLogParser
import liquibase.parser.ChangeLogParserFactory
import liquibase.parser.ext.GroovyLiquibaseChangeLogParser
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.resource.ResourceAccessor
import org.springframework.core.io.Resource
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

import java.sql.Connection
import java.sql.SQLException

class LiquibaseGriffonAddon {
    GriffonApplication app

    // lifecycle methods

    // called once, after the addon is created
    void addonInit(GriffonApplication app) {
        this.app = app
    }

    // called once, after all addons have been inited
    //void addonPostInit(GriffonApplication app) {
    //}

    // called many times, after creating a builder
    //void addonBuilderInit(GriffonApplication app, FactoryBuilderSupport builder) {
    //}

    // called many times, after creating a builder and after
    // all addons have been inited
    //void addonBuilderPostInit(GriffonApplication app, FactoryBuilderSupport builder) {
    //}

    // to add MVC Groups use create-mvc

    // builder fields, these are added to all builders.
    // closures can either be literal { it -> println it}
    // or they can be method closures: this.&method

    // adds methods to all builders
    //Map methods = [
    //    methodName: { /*Closure*/ }
    //]

    // adds properties to all builders
    //Map props = [
    //    propertyName: [
    //        get: { /* optional getter closure */ },
    //        set: {val-> /* optional setter closure */ },
    //  ]
    //]

    // adds new factories to all builders
    //Map factories = [
    //    factory : /*instance that extends Factory*/
    //]


    // adds application event handlers
    Map events = [
    //    "StartupStart": {app -> /* event hadler code */ }
            DataSourceConnectEnd: {dsName, ds->
                PathMatchingResourcePatternResolver pathResolver = new PathMatchingResourcePatternResolver(app.class.classLoader)
                ResourceAccessor resourceAccessor = new SpringResourceAccessor (pathResolver: pathResolver)

                ChangeLogParserFactory parserFactory = ChangeLogParserFactory.instance
                ChangeLogParserFactory.getInstance().register(new GroovyLiquibaseChangeLogParser())

                def changeLogFilePath = app.config.griffon?.liquibase?.rootChangeLogPath
                if (!changeLogFilePath) {
                    changeLogFilePath = "classpath*:migrations/rootChangelog.groovy"
                }

                DataSourceHolder.instance.withSql('default', {String dataSourceName, Sql sql->
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
                })
            }
    ]


    protected Liquibase createLiquibase(Connection c, String changeLog, ResourceAccessor resourceAccessor) throws LiquibaseException {
        Liquibase liquibase = new Liquibase(changeLog, resourceAccessor, createDatabase(c));
        /*
        if (parameters != null) {
            for(Map.Entry<String, String> entry: parameters.entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
                }
            }

        if (isDropFirst()) {
            liquibase.dropAll();
        }
        */
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
/*        if (this.defaultSchema != null) {
            database.setDefaultSchemaName(this.defaultSchema);
        }
*/
        return database;
    }

    /*
    protected DatabaseChangeLog loadChangelog (ChangeLogParserFactory parserFactory, ResourceAccessor resourceAccessor, String path, ChangeLogParameters changeLogParameters) {
        ChangeLogParser parser = parserFactory.getParser(path, resourceAccessor)
        parser.parse(path, changeLogParameters, resourceAccessor)
    }
    */

    // handle synthetic node properties or
    // intercept existing ones
    //List attributeDelegates = [
    //    {builder, node, attributes -> /*handler code*/ }
    //]

    // called before a node is instantiated
    //List preInstantiateDelegates = [
    //    {builder, attributes, value -> /*handler code*/ }
    //]

    // called after the node was instantiated
    //List postInstantiateDelegates = [
    //    {builder, attributes, node -> /*handler code*/ }
    //]

    // called after the node has been fully
    // processed, including child content
    //List postNodeCompletionDelegates = [
    //    {builder, parent, node -> /*handler code*/ }
    //]

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
