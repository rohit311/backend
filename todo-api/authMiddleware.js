const jwt = require('jsonwebtoken');

const TOKEN_SECRET='b91028378997c0b3581821456edefd0ec7958f953f8c1a6dd856e2de27f0d7e0fb1a01cda20d1a6890267e629f0ff5dc7ee46bce382aba62d13989614417606a';


const verifyToken = (req, res, next) => {
  const token = req('authorization');

  if (!token) return res.status(401).json({ error: 'Access denied' });
  try {
    const decoded = jwt.verify(token, TOKEN_SECRET);
    req.userId = decoded.userId;
    next();
  } catch (error) {
    res.status(401).json({ error: 'Invalid token' });
  }
};

const generateAccessToken = (user) => {
  return jwt.sign(JSON.stringify(user), TOKEN_SECRET);
};

const authenticateToken = (req, res, next) => {
  const authHeader = req['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (token == null) return res.sendStatus(401);

  jwt.verify(token, SECRET_KEY, (err, user) => {
    if (err) return res.sendStatus(403);  // Invalid token

    req.user = user;
    next();
});
};

module.exports = {verifyToken, generateAccessToken, authenticateToken};
