const express = require('express');
const { check, validationResult } = require('express-validator');
const app = express();
const PORT = 3000;
const dbClient = require('./database_connection');

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

app.use(express.json());
app.post('/register',[
  [
    check('name').isLength({ min: 5, max: 50 }).withMessage('Name must be between 5 and 50 characters'),
    check('email').isEmail().withMessage('Invalid email address'),
    check('password').isLength({ min: 6, max: 30 }).withMessage('Password must be between 6 and 30 characters')
  ]
] ,(req, res) => {

  const errors = validationResult(req);

  if (!errors.isEmpty()) {
    return res.status(400).json({ errors: errors.array() });
  }

  const {name, email, password} = req.body;

  try {

  checkIfUserExists(name, email)
  .then(() => {

    createNewUser(name, email, password)
    .then(() => {
      return res.status(201).json({success: `User with user name - ${name} & email - ${email}`});
    })
    .catch(() => {
      return res.status(500).json({ error: "Internal server error" });
    });
  })
  .catch(() => {
    return res.status(400).json({ error: "User already exists" });
  });

  } catch(error) {
    console.log("error: ", error);
    // return res.status(500).json({ error});
  }
});

app.listen(PORT, function () {
  console.log('Example app listening on port ' + PORT + '!');
});
