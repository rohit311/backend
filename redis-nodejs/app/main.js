const net = require('net');
const fs = require('fs');
const { join } = require('path');
const { getKeysValues } = require('./parseRDB');

  const arguments = process.argv.slice(2);

  const config = new Map();
  let rdb;

  for (let i = 0; i < arguments.length; i++) {
  	const arg = arguments[i];
  	if (arg.startsWith('--')) {
  		config.set(arg.slice(2), arguments[i + 1]);
  		i += 1;
  	}
  }

if (config.get('dir') && config.get('dbfilename')) {
 	const dbPath = join(config.get('dir'), config.get('dbfilename'));
 	const isDbExists = fs.existsSync(dbPath);
 	if (isDbExists) {
		rdb = fs.readFileSync(dbPath);
 		if (!rdb) {
 			throw `Error reading DB at provided path: ${dbPath}`;
 		}
 	} else {
 		console.log(`DB doesn't exists at provided path: ${dbPath}`);
 	}
 }
 //

  const dataStore = new Map();
  const expiryList = new Map();

  // You can use print statements as follows for debugging, they'll be visible when running tests.
  console.log('Logs from your program will appear here!');

  const serializeRESP = (obj) => {
  	let resp = '';
  	switch (typeof obj) {
  		case 'object':
  			if (obj.constructor === Array) {
  				const arrLen = obj.length;
  				resp += `*${arrLen}\r\n`;
  				for (let i = 0; i < arrLen; i++) {
  					resp += serializeRESP(obj[i]);
  				}
  			}
  			return resp;
  		case 'string':
  			const strLen = obj.length;
  			resp += `$${strLen}\r\n`;
  			resp += `${obj}\r\n`;
  			return resp;
  		case 'number':
  		case 'bigint':
  		case 'boolean':
  		case 'undefined':
  		default:
  			break;
  	}
  };

  const parseRESP = (arrRESP) => {
  	for (let i = 0; i < arrRESP.length; i++) {
  		const element = arrRESP.shift();
  		switch (element[0]) {
  			case '*':
  				// array
  				const arrlen = element.slice(1);
  				const arr = [];
  				for (let j = 0; j < arrlen; j++) {
  					const parsedContent = parseRESP(arrRESP);
  					arr.push(parsedContent.content);
  					arrRESP = parsedContent.arrRESP;
  				}
  				return arr;

  			case '$':
  				// bulk string
  				const strlen = element.slice(1);
  				const str = arrRESP.shift();
  				return { content: str, arrRESP };
  			case ':':
  				// integer
  				const integer = element.slice(1);
  				return { content: Number(integer), arrRESP };
  			default:
  				break;
  		}
  	}
  };

  const parseRequest = (arrRequest) => {
  	const splitedRequest = arrRequest.split('\r\n');
  	const parsedRESP = parseRESP(splitedRequest);
  	const command = parsedRESP.shift();

  	switch (command.toUpperCase()) {
  		//TODO: add handling for two word commands
  		case 'CONFIG':
  			const scndPart = parsedRESP.shift();
  			if (scndPart) {
  				return {
  					commandName: command.toUpperCase() + ' ' + scndPart.toUpperCase(),
  					args: parsedRESP,
  				};
  			} else {
  				return {
  					commandName: command.toUpperCase(),
  				};
  			}

  		default:
  			return {
  				commandName: command.toUpperCase(),
  				args: parsedRESP,
  			};
  	}
  };

  const sendPongResponse = (connection) => {
  	connection.write('+PONG\r\n');
  };

  const sendEchoResponse = (connection, content) => {
  	connection.write(`+${content}\r\n`);
  };

  const handleSetRequest = (connection, key, value, px) => {
  	dataStore.set(key, value);
  	expiryList.set(key, Date.now() + Number(px));
  	connection.write('+OK\r\n');
  };
  const handleGetRequest = (connection, key) => {
  	const element = dataStore.get(key);
  	if (!element) {
  		connection.write(`$-1\r\n`);
  		return;
  	}

  	if (expiryList.get(key) <= Date.now()) {
  		dataStore.delete(key);
  		expiryList.delete(key);
  		connection.write(`$-1\r\n`);
  	} else {
  		connection.write(`+${element}\r\n`);
  	}
  };

  const handleConfigGetRequest = (connection, key) => {
  	const value = config.get(key);
  	connection.write(serializeRESP([key, value]));
  };

  // Uncomment this block to pass the first stage
  const server = net.createServer((connection) => {
  	console.log('connected');
  	connection.on('data', (stream) => {
  		const arrayRequest = stream.toString();
  		const parsedRequest = parseRequest(arrayRequest);
  		console.log(parsedRequest);
  		switch (parsedRequest.commandName) {
  			case 'ECHO':
  				sendEchoResponse(connection, parsedRequest.args[0]);
  				return;
  			case 'PING':
  				sendPongResponse(connection);
  				return;
  			case 'SET':
  				if (!parsedRequest.args[2]) {
  					handleSetRequest(connection, parsedRequest.args[0], parsedRequest.args[1]);
  					return;
  				}
  				switch (parsedRequest.args[2].toUpperCase()) {
  					case 'PX':
  						handleSetRequest(
  							connection,
  							parsedRequest.args[0],
  							parsedRequest.args[1],
  							parsedRequest.args[3],
  						);
  						break;

  					default:
  						break;
  				}
  				return;
  			case 'GET':
  				handleGetRequest(connection, parsedRequest.args[0]);
  				return;

  			case 'CONFIG GET':
  				handleConfigGetRequest(connection, parsedRequest.args[0]);
  				return;

        case 'KEYS':
          const redis_key = getKeysValues(rdb);
          connection.write(serializeRESP([redis_key]));
          return;
  			default:
  				connection.write('-ERR unsupported command\r\n');
  				return;
  		}
  	});
  });

  server.listen(6379, '127.0.0.1');