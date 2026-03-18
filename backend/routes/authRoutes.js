const express = require("express");
const router = express.Router();
const ctrl = require("../controllers/authController");
const auth = require("../middleware/auth");

router.post("/register", ctrl.register);
router.post("/login", ctrl.login);
router.get("/me", auth, ctrl.me);
router.put("/me", auth, ctrl.updateProfile);
router.post("/firebase-sync", ctrl.firebaseSync);

module.exports = router;
