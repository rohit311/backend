const { redis_main_const, OPCODES } = require('./constants.js');

function handleLengthEncoding(data, cursor) {
 	const byte = data[cursor];
 	const lengthType = (byte & 0b11000000) >> 6;
 	const lengthValues = [
 		[byte & 0b00111111, cursor + 1],
 		[((byte & 0b00111111) << 8) | data[cursor + 1], cursor + 2],
 		[data.readUInt32BE(cursor + 1), cursor + 5],
 	];
 	return (
 		lengthValues[lengthType] || new Error(`Invalid length encoding ${lengthType} at ${cursor}`)
 	);
}

function getKeysValues(data) {
  if (!data) {
    return "";
  }

  const { REDIS_MAGIC_STRING, REDIS_VERSION } = redis_main_const;
  let cursor = REDIS_MAGIC_STRING + REDIS_VERSION;
  while (cursor < data?.length) {
    if (data[cursor] === OPCODES.SELECTDB) {
      break;
    }
    cursor++;
  }
  cursor++;

  let length;
  [length, cursor] = handleLengthEncoding(data, cursor);
  cursor++;

  [length, cursor] = handleLengthEncoding(data, cursor);
  [length, cursor] = handleLengthEncoding(data, cursor);
  if (data[cursor] === OPCODES.EXPIRETIME) {
    cursor++;
    cursor += 4;
  }
  cursor++;
  const redisKeyLength = data[cursor];
  const redisKey = data.subarray(cursor + 1, cursor + 1 + redisKeyLength).toString();
  return redisKey;
}

module.exports = {
  getKeysValues,
};