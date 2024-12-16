const redis_main_const = {
  REDIS_MAGIC_NUMBER: 5,
  REDIS_VERSION: 4
};

const OPCODES = {
  EOF: 0xff,
  SELECTDB: 0xfe,
  EXPIRETIME: 0xfd,
  EXPIRETIMEMS: 0xfc,
  RESIZEDB: 0xfb,
  AUX: 0xfa,
};

module.exports = {
  redis_main_const,
  OPCODES,
};
