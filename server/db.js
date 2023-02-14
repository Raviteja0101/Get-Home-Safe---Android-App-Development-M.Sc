var mysql = require('mysql');

var db = null;
var MYSQL_HOST = 'localhost';
var MYSQL_USER = 'root';
var MYSQL_PASSWORD = 'tgsd1234';
var MYSQL_DATABASE = 'get_home_safe';

exports.db = function () {
    if (db == null) {
        db = mysql.createPool({
            host: MYSQL_HOST,
            user: MYSQL_USER,
            password: MYSQL_PASSWORD,
            database: MYSQL_DATABASE,
            connectionLimit: 75
        });
    }
    return db;
};