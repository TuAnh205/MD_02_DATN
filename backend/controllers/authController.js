const User = require("../models/User");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const crypto = require("crypto");
const admin = require("firebase-admin");

// ================= CONFIG =================
const jwtSecret = process.env.JWT_SECRET || "secret_jwt_key";
const jwtExpire = process.env.JWT_EXPIRE || "7d";

// ================= FIREBASE INIT =================
if (!admin.apps.length) {
  admin.initializeApp();
}

// ================= TOKEN =================
const generateToken = (user) => {
  return jwt.sign(
    {
      id: user._id,
      role: user.role,
    },
    jwtSecret,
    { expiresIn: jwtExpire },
  );
};

// ================= REGISTER =================
exports.register = async (req, res) => {
  try {
    const { name, email, password, phone } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json({ message: "Missing data" });
    }

    const normalizedEmail = email.trim().toLowerCase();

    const exist = await User.findOne({ email: normalizedEmail });
    if (exist) {
      return res.status(400).json({ message: "Email already registered" });
    }

    const hashed = await bcrypt.hash(password, 10);

    const user = new User({
      name,
      email: normalizedEmail,
      password: hashed,
      phone,
      role: "user",
    });

    await user.save();

    const token = generateToken(user);

    res.status(201).json({
      message: "Register success",
      token,
      user,
    });
  } catch (err) {
    console.error("REGISTER ERROR:", err);
    res.status(500).json({ message: err.message });
  }
};

// ================= LOGIN =================
exports.login = async (req, res) => {
  try {
    const { email, password } = req.body;

    const normalizedEmail = email.trim().toLowerCase();

    const user = await User.findOne({ email: normalizedEmail });

    if (!user) {
      return res.status(401).json({ message: "Invalid credentials" });
    }

    // 🔥 Nếu là account Google mà chưa có password
    if (!user.password) {
      return res.status(400).json({
        message: "Tài khoản này đăng nhập bằng Google",
      });
    }

    const match = await bcrypt.compare(password, user.password);

    if (!match) {
      return res.status(401).json({ message: "Invalid credentials" });
    }

    const token = generateToken(user);

    res.json({
      message: "Login success",
      token,
      user,
    });
  } catch (err) {
    console.error("LOGIN ERROR:", err);
    res.status(500).json({ message: err.message });
  }
};

// ================= FIREBASE SYNC =================
exports.firebaseSync = async (req, res) => {
  try {
    console.log("🔥 FIREBASE SYNC RUNNING");
    console.log("BODY:", req.body);

    const { firebaseUid, email, name } = req.body;

    if (!firebaseUid || !email) {
      return res
        .status(400)
        .json({ message: "firebaseUid and email required" });
    }

    const normalizedEmail = email.trim().toLowerCase();

    let user = await User.findOne({
      $or: [{ firebaseUid }, { email: normalizedEmail }],
    });

    let generatedPassword;

    if (!user) {
      generatedPassword = crypto.randomBytes(6).toString("hex");
      const hashed = await bcrypt.hash(generatedPassword, 10);

      user = new User({
        firebaseUid,
        email: normalizedEmail,
        name: name || "User",
        role: "user",
        password: hashed,
      });
    } else {
      user.firebaseUid = firebaseUid;
      user.name = name || user.name;

      if (!user.password) {
        generatedPassword = crypto.randomBytes(6).toString("hex");
        user.password = await bcrypt.hash(generatedPassword, 10);
      }
    }

    await user.save();

    const token = generateToken(user);

    res.json({
      message: "User synced successfully",
      token, // 🔥 QUAN TRỌNG
      user: {
        id: user._id,
        name: user.name,
        email: user.email,
        role: user.role,
      },
      temporaryPassword: generatedPassword || null,
    });
  } catch (err) {
    console.error("FIREBASE SYNC ERROR:", err);
    res.status(500).json({ message: err.message });
  }
};

// ================= GOOGLE LOGIN =================
exports.googleLogin = async (req, res) => {
  try {
    const { idToken } = req.body;

    if (!idToken) {
      return res.status(400).json({ message: "Missing idToken" });
    }

    const decoded = await admin.auth().verifyIdToken(idToken);

    const firebaseUid = decoded.uid;
    const email = decoded.email;
    const name = decoded.name || "User";

    let user = await User.findOne({ email });

    if (!user) {
      const randomPass = crypto.randomBytes(6).toString("hex");
      const hashed = await bcrypt.hash(randomPass, 10);

      user = new User({
        firebaseUid,
        email,
        name,
        password: hashed,
        role: "user",
      });

      await user.save();
    }

    const token = generateToken(user);

    res.json({
      message: "Google login success",
      token,
      user,
    });
  } catch (err) {
    console.error("GOOGLE LOGIN ERROR:", err);
    res.status(401).json({ message: "Google login failed" });
  }
};
// ================= GET PROFILE =================
exports.me = async (req, res) => {
  try {
    const user = await User.findById(req.user.id).select("-password");
    res.json(user);
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= UPDATE PROFILE =================
exports.updateProfile = async (req, res) => {
  try {
    const { name, phone } = req.body;

    const user = await User.findById(req.user.id);

    if (name) user.name = name;
    if (phone) user.phone = phone;

    await user.save();

    res.json({
      message: "Update success",
      user,
    });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= SET PASSWORD =================
exports.setPassword = async (req, res) => {
  try {
    const { password } = req.body;

    const user = await User.findById(req.user.id);

    const hashed = await bcrypt.hash(password, 10);
    user.password = hashed;

    await user.save();

    res.json({ message: "Password set success" });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= FORGOT PASSWORD =================
exports.forgotPassword = async (req, res) => {
  res.json({ message: "Forgot password API (demo)" });
};

// ================= RESET PASSWORD =================
exports.resetPassword = async (req, res) => {
  res.json({ message: "Reset password API (demo)" });
};
