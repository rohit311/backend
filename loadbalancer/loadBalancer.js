const express = require("express");
const axios = require('axios');

const app = express();

// Backend servers
const servers = [
  'http://localhost:8081',
  'http://localhost:8082'
];

let currentIndex = 0;

function getNextServer() {
  currentIndex++;

  if (currentIndex > servers.length) {
    currentIndex = 0;
  }

  return servers[currentIndex];
}

async function checkServerHealth() {

  for (let i = 0;i < servers.length; i++) {
    try {
      const result = await axios.get(servers[i] + '/health');

      if (result.status !== 200) {
        servers.splice(i, 1);
        i--;
      }
  } catch(e) {
    console.log('error:', e);
  }
  }

  setInterval(async () => {
    let serverAdded = false;
    for (let i = 0; i < servers.length; i++) {
      const result = await axios.get(servers[i] + '/');
      if (result.status === 200 && !servers.includes(servers[i])) {
        servers.push(servers[i]);
        serverAdded = true;
      }
    }

    if (serverAdded) {
      console.log('Server added back to pool');
    }
  }, 5000);
}

checkServerHealth();

// Log requests
app.use((req, res, next) => {
  console.log(`${req.method} request to ${req.url}`);
  next();
});

// Handler for incoming requests
app.get('*', async (req, res) => {

  // Get next backend server
  const server = getNextServer();

  // Forward request
  try {
    const result = await axios.get(server + req.url);
    res.status(result.status).send(result.data);
  } catch (err) {
    res.status(500).send('Failed to connect to backend');
  }
});

app.listen(80, () => {
  console.log('Load balancer running on port 80');
});
