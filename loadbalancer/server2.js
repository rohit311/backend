const express = require("express");
const app = express();

app.get('/', (req, res) => {
  res.send('Hello from server 2!');
});

app.listen(8082, () => {
  console.log('Backend server running on port 8082');
});
