const redis = require("redis");
const redisClient = redis.createClient();

(async () => {
  await redisClient.connect();
})();

console.log("Connecting to the Redis");

redisClient.on("ready", () => {
    console.log("Connected!");
});

redisClient.on("error", (err) => {
    console.log("Error in the Connection");
});