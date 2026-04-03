const express = require('express');
const fs = require('fs');
const path = require('path');
const router = express.Router();

// GET /api/locations
router.get('/', (req, res) => {
  const filePath = path.join(__dirname, '../data/vietnam_provinces.json');
  fs.readFile(filePath, 'utf8', (err, data) => {
    if (err) {
      return res.status(500).json({ message: 'Không đọc được dữ liệu địa lý' });
    }
    try {
      const locations = JSON.parse(data);
      res.json({ locations });
    } catch (e) {
      res.status(500).json({ message: 'Lỗi dữ liệu địa lý' });
    }
  });
});

module.exports = router;
