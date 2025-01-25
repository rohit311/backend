const dbClient = require('./database');


const insertTodo = (req, userEmail) => {
  const {title, description} = req.body;
  const INSERT_STATEMENT = 'INSERT INTO todos(title, description, user_email) VALUES ($1, $2, $3)';
  const VALUES = [title, description, userEmail];

  return new Promise((resolve, reject) => {
    dbClient.query(INSERT_STATEMENT, VALUES, (err, res) => {

      if (err) {
        reject(err);
      } else {
        resolve(res);
      }
    });
  });
};

const updateTodo = (todoId, req) => {
  const {title, description} = req.body;
  const UPDATE_STATEMENT = 'UPDATE todos SET title = $1, description = $2 WHERE id = $3 RETURNING id, title, description;';
  const VALUES = [title, description, todoId];

  return new Promise((resolve, reject) => {
    dbClient.query(UPDATE_STATEMENT, VALUES, (err, res) => {

      if (err) {
        reject(err);
      } else {
        console.log("result: ", res);
        resolve(res);
      }
    });
  });
}

const deleteTodo = (todoId) => {
  const DELETE_STATEMENT = 'DELETE FROM todos WHERE id = $1';
  const VALUES = [todoId];

  return new Promise((resolve, reject) => {
    dbClient.query(DELETE_STATEMENT, VALUES, (err, res) => {

      if (err) {
        reject(err);
      } else {
        resolve(res);
      }
    });
  });
};

const fetchTodo = (query, values) => {
  return new Promise((resolve, reject) => {
    dbClient.query(query, values,
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

module.exports = {insertTodo, updateTodo, deleteTodo, fetchTodo};
