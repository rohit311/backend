const http = require('http');
const url = require('url');
const httpCodes = require("./http_response_codes");

const server = http.createServer((req, res) => {
  try {
    if (req.url === "/") {
      res.writeHead(400, { 'Content-Type': 'text/plain' });
      res.end("Please specify status code for response");
    } else {
      const parsed = url.parse(req.url, true);
      const requestedCode = parsed.pathname.split('/')[1];
      res.writeHead(requestedCode, { 'Content-Type': 'text/plain' });

      if (!httpCodes[requestedCode]) {
        res.end("Unknown Code, Please refer - https://developer.mozilla.org/en-US/docs/Web/HTTP/Status");
      } else {
        res.end(`${requestedCode} ${httpCodes[requestedCode].message} \nUse Case - ${httpCodes[requestedCode].use_case}`);
      }
    }
  } catch(error) {
    res.writeHead(400, { 'Content-Type': 'text/plain' });
    res.end("Please specify status code for response");
  }


});

const PORT = 3000;
server.listen(PORT, () => {
  console.log(`Server listening on port - ${PORT}`);
});