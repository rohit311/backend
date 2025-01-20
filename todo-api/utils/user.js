const dbClient = require('./database');

const checkIfUserExists = (userName, email) => {
  return new Promise((resolve, reject) => {
    dbClient.query(`SELECT user_name, email FROM users WHERE user_name = $1 OR email = $2`,[userName, email],
      (err, result) => {
        if (err) {
          throw err;
        } else {
          console.log('Query result:', result.rows);
          if (result.rows.length > 0) {
            reject();
          } else {
            resolve();
          }
        }
      }
    );
  });

};

const createNewUser = (userName, email, password) => {
  const INSERT_STATEMENT = 'INSERT INTO users(user_name,email,pwd) VALUES ($1,$2,$3)';
  const values = [userName, email, password];

  return new Promise((resolve, reject) => {
    dbClient.query(INSERT_STATEMENT, values, (err, res) => {
      if (err) {
        reject(err);
      } else {
        resolve();
      }
    });
  });
};

const fetchUser = (email) => {
  return new Promise((resolve, reject) => {
    dbClient.query(`SELECT user_name, email, pwd FROM users WHERE email = $1`, [email],
      (err, result) => {

        if (err) {
          throw err;
        } else {
          if (result.rows.length > 0) {
            resolve(result.rows[0]);
          } else {
            reject();
          }
        }
      }
    );
  });
};

module.exports = {checkIfUserExists, createNewUser, fetchUser};