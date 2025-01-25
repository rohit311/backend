const express = require('express');
const { check, validationResult } = require('express-validator');
const app = express();
const PORT = 3000;
const authMiddleware = require('./authMiddleware');
const userUtils = require('./utils/user');
const todoUtils = require('./utils/todo');


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

    userUtils.checkIfUserExists(name, email)
    .then(() => {

      userUtils.createNewUser(name, email, password)
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
  }
});

app.post('/login', async (req, res) => {
  try {
    const {email, password} = req.body;
    const user = await userUtils.fetchUser(email);

    if (user.pwd !== password) {
      return res.status(400).json({ error: "Invalid creds" });
    }

    console.log("login api: ", user);


    const accessToken = authMiddleware.generateAccessToken(user);

    res.json({ accessToken: accessToken });
  } catch(error) {
    console.log("error: ", error);
  }
});

app.post('/todos', authMiddleware.authenticateToken, async (req, res) => {
  try {
    const query = "SELECT id,title, description FROM todos WHERE user_email = $1 ORDER BY id DESC LIMIT 1;";
    const values = [req.user.email];
    const result = await todoUtils.insertTodo(req, req.user.email);
    const todo = await todoUtils.fetchTodo(query, values);

    return res.status(201).json(todo);
  } catch(error) {
    console.log("error: ", error);
    return res.status(500).json({ error: "Internal server error" });
  }
});

app.put('/todo/:todoid', authMiddleware.authenticateToken, async (req, res) => {
  try {
    const todoId = req.params.todoid;
    const result = await todoUtils.updateTodo(todoId, req);

    if (result.rowCount != 1) {
      return res.status(400).json({error: "Record does not exist."});
    }

    return res.status(200).json(result.rows[0]);
  } catch(error) {
    return res.status(500).json({ error: "Internal server error" });
  }
});

app.delete('/todo/:todoid', authMiddleware.authenticateToken, async (req, res) => {
  try {
    const todoId = req.params.todoid;
    const result = await todoUtils.deleteTodo(todoId);

    if (result.rowCount != 1) {
      return res.status(400).json({error: "Record does not exist."});
    }

    return res.status(204).send();

  } catch(error) {
    console.log("error: 2", error);
    return res.status(500).json({ error: "Internal server error" });
  }
});

app.listen(PORT, function () {
  console.log('TODO app listening on port ' + PORT + '!');
});
