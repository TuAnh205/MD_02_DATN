const express = require("express");
const router = express.Router();
const ctrl = require("../controllers/authController");
const auth = require("../middleware/auth");

router.post("/register", ctrl.register);
router.post("/register-shop", ctrl.registerShop);
router.post("/login", ctrl.login);
router.post("/google-login", ctrl.googleLogin);
router.post("/forgot-password", ctrl.forgotPassword);
router.post("/reset-password", ctrl.resetPassword);
router.get("/me", auth, ctrl.me);
router.put("/me", auth, ctrl.updateProfile);
router.post("/firebase-sync", ctrl.firebaseSync);
router.post("/set-password", auth, ctrl.setPassword);

module.exports = router;
