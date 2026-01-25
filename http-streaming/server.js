const express = require('express');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = 5000;

app.get("/download", (req, res) => {
    const filePath = path.join(__dirname, "Thetestdata_ZIP_20KB.zip");

    if (!fs.existsSync(filePath)) {
        return res.status(404).send("File not found");
    }

    const stat = fs.statSync(filePath);
    const fileSize = stat.size;

    res.writeHead(200, {
        "Content-Type": "application/octet-stream",
        "Content-Length": fileSize,
        "Transfer-Encoding": "chunked"
    });

    const readStream = fs.createReadStream(filePath, {
        highWaterMark: 10 * 1024, // 10KB chunks
    });

    console.log(fs.statSync(filePath).size);

    readStream.pipe(res);

    readStream.on("data", chunk => {
        console.log("Chunk size:", chunk.length);
    });

    readStream.on("error", err => {
        console.error(err);
        res.end();
    });
});

app.listen(PORT, () => {
  console.log(`Chunk streaming server running on port ${PORT}`);
});