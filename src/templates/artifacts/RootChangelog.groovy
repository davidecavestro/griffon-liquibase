databaseChangeLog() {
/*
 * include here your PLAIN SQL changesets
 * SQL syntax is based on https://github.com/tlberglund/groovy-liquibase
 */
/*
    changeSet(id:'initial-schema', author: 'yourname') {
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
*/
}
