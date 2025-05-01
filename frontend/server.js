const path = require('path');
const express = require('express');
const app = express();

// serve the build output
app.use(express.static(path.join(__dirname, 'build')));

// all other routes should serve index.html so React Router works
app.get('/*', (_req, res) => {
  res.sendFile(path.join(__dirname, 'build', 'index.html'));
});

const PORT = process.env.PORT || 8080;
app.listen(PORT, () => {
  console.log(`Frontend listening on port ${PORT}`);
});
