const { Pool } = require('pg');

const dbClient = new Pool({
	user: 'postgres',
	password: 'postgres',
	host: 'localhost',
	port: '5432',
	database: 'todo_api',
  max: 20, // maximum number of connections in the pool
  idleTimeoutMillis: 30000, // how long a client is allowed to remain idle before being closed
  connectionTimeoutMillis: 2000, // how long to wait for a connection to be established
});


dbClient
	.connect()
	.then(() => {
		console.log('Connected to PostgreSQL database');
	})
	.catch((err) => {
		console.error('Error connecting to PostgreSQL database', err);
	});

module.exports = dbClient;
