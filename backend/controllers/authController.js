const User = require("../models/User");
const bcrypt = require("bcryptjs");
const jwt = require("jsonwebtoken");
const crypto = require("crypto");
const admin = require("firebase-admin");
const nodemailer = require("nodemailer");
const { OAuth2Client } = require("google-auth-library");

// ================= CONFIG =================
const jwtSecret = process.env.JWT_SECRET || "secret_jwt_key";
const jwtExpire = process.env.JWT_EXPIRE || "7d";
const googleClientId = process.env.GOOGLE_CLIENT_ID || "";

const mailUser = process.env.MAIL_USER || "";
const mailPass = process.env.MAIL_PASS || "";
const mailFrom = process.env.MAIL_FROM || mailUser;
const mailHost = process.env.MAIL_HOST || "smtp.gmail.com";
const mailPort = Number(process.env.MAIL_PORT || 465);
const mailSecure = String(process.env.MAIL_SECURE || "true").toLowerCase() === "true";

const otpStore = new Map();
const OTP_TTL_MS = 10 * 60 * 1000;

const googleClient = googleClientId ? new OAuth2Client(googleClientId) : null;

const gmailRegex = /^[^\s@]+@gmail\.com$/i;
const placeholderRegex = /(your_|replace_me)/i;

// ================= FIREBASE INIT =================
if (!admin.apps.length) {
  admin.initializeApp();
}

const transporter = nodemailer.createTransport({
  host: mailHost,
  port: mailPort,
  secure: mailSecure,
  auth: {
    user: mailUser,
    pass: mailPass,
  },
});

const generateOtpCode = () => String(Math.floor(100000 + Math.random() * 900000));

const validateMailConfig = () => {
  if (!mailUser || !mailPass) {
    throw new Error("MAIL_USER/MAIL_PASS not configured");
  }

  if (placeholderRegex.test(mailUser) || placeholderRegex.test(mailPass)) {
    throw new Error(
      "MAIL_USER/MAIL_PASS are still placeholder values. Use your Gmail and a 16-character App Password.",
    );
  }

  if (!/@gmail\.com$/i.test(mailUser)) {
    throw new Error("MAIL_USER must be a Gmail address when using Gmail SMTP");
  }
};

const sendOtpMail = async (email, code) => {
  validateMailConfig();

  await transporter.sendMail({
    from: mailFrom,
    to: email,
    subject: "Ma xac nhan dang ky tai khoan",
    text: `Ma xac nhan cua ban la: ${code}. Ma co hieu luc trong 10 phut.`,
    html: `<p>Ma xac nhan cua ban la: <b>${code}</b></p><p>Ma co hieu luc trong 10 phut.</p>`,
  });
};

const verifyGoogleToken = async (idToken) => {
  if (googleClient) {
    const ticket = await googleClient.verifyIdToken({
      idToken,
      audience: googleClientId,
    });

    const payload = ticket.getPayload();
    return {
      uid: payload.sub,
      email: payload.email,
      name: payload.name || "User",
      verified: Boolean(payload.email_verified),
    };
  }

  const decoded = await admin.auth().verifyIdToken(idToken);
  return {
    uid: decoded.uid,
    email: decoded.email,
    name: decoded.name || "User",
    verified: true,
  };
};

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

exports.emailRegistrationDisabled = async (req, res) => {
  return res.status(410).json({
    message: "Dang ky bang email da duoc tat. Vui long su dung Google de tao tai khoan.",
  });
};

// ================= GOOGLE REGISTER: SEND CODE =================
exports.sendGoogleRegistrationCode = async (req, res) => {
  try {
    const { idToken, role = "user" } = req.body;

    if (!idToken) {
      return res.status(400).json({ message: "Missing idToken" });
    }

    const googleData = await verifyGoogleToken(idToken);
    const normalizedEmail = String(googleData.email || "").trim().toLowerCase();

    if (!normalizedEmail) {
      return res.status(400).json({ message: "Google account has no email" });
    }

    const exist = await User.findOne({ email: normalizedEmail });
    if (exist) {
      return res.status(400).json({ message: "Email already registered" });
    }

    const code = generateOtpCode();
    const storeKey = `google-register:${normalizedEmail}`;

    otpStore.set(storeKey, {
      code,
      expiresAt: Date.now() + OTP_TTL_MS,
      payload: {
        email: normalizedEmail,
        name: googleData.name || "User",
        firebaseUid: googleData.uid,
        role: role === "shop" ? "shop" : "user",
      },
    });

    await sendOtpMail(normalizedEmail, code);

    return res.json({
      message: "Verification code sent",
      email: normalizedEmail,
    });
  } catch (err) {
    console.error("SEND GOOGLE REGISTRATION CODE ERROR:", err);
    return res.status(500).json({ message: err.message || "Cannot send verification code" });
  }
};

// ================= GOOGLE REGISTER: VERIFY CODE =================
exports.verifyGoogleRegistrationCode = async (req, res) => {
  try {
    const { email, code } = req.body;

    if (!email || !code) {
      return res.status(400).json({ message: "email and code are required" });
    }

    const normalizedEmail = String(email).trim().toLowerCase();
    const storeKey = `google-register:${normalizedEmail}`;
    const cached = otpStore.get(storeKey);

    if (!cached) {
      return res.status(400).json({ message: "Verification code not found" });
    }

    if (Date.now() > cached.expiresAt) {
      otpStore.delete(storeKey);
      return res.status(400).json({ message: "Verification code expired" });
    }

    if (String(code).trim() !== String(cached.code).trim()) {
      return res.status(400).json({ message: "Invalid verification code" });
    }

    const exist = await User.findOne({ email: normalizedEmail });
    if (exist) {
      otpStore.delete(storeKey);
      return res.status(400).json({ message: "Email already registered" });
    }

    const user = new User({
      name: cached.payload.name,
      email: cached.payload.email,
      firebaseUid: cached.payload.firebaseUid,
      googleId: cached.payload.firebaseUid,
      role: cached.payload.role,
      isVerified: true,
      password: null,
    });

    await user.save();
    otpStore.delete(storeKey);

    const token = generateToken(user);

    return res.status(201).json({
      message: "Google registration success",
      token,
      user,
    });
  } catch (err) {
    console.error("VERIFY GOOGLE REGISTRATION CODE ERROR:", err);
    return res.status(500).json({ message: err.message || "Google registration failed" });
  }
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

// ================= SEND EMAIL VERIFICATION CODE =================
exports.sendVerificationCode = async (req, res) => {
  try {
    const { name, email, password, phone, role = "user" } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json({ message: "Missing data" });
    }

    const normalizedEmail = email.trim().toLowerCase();

    if (!gmailRegex.test(normalizedEmail)) {
      return res.status(400).json({ message: "Only Gmail is supported" });
    }

    const exist = await User.findOne({ email: normalizedEmail });
    if (exist) {
      return res.status(400).json({ message: "Email already registered" });
    }

    const hashed = await bcrypt.hash(password, 10);
    const code = generateOtpCode();

    otpStore.set(normalizedEmail, {
      code,
      expiresAt: Date.now() + OTP_TTL_MS,
      payload: {
        name,
        email: normalizedEmail,
        password: hashed,
        phone,
        role: role === "shop" ? "shop" : "user",
      },
    });

    await sendOtpMail(normalizedEmail, code);

    res.json({ message: "Verification code sent" });
  } catch (err) {
    console.error("SEND VERIFICATION CODE ERROR:", err);
    const lowered = String(err.message || "").toLowerCase();
    if (lowered.includes("badcredentials") || lowered.includes("invalid login")) {
      return res.status(500).json({
        message:
          "Gmail authentication failed. Set MAIL_USER to your Gmail and MAIL_PASS to a Gmail App Password (not normal account password).",
      });
    }

    res.status(500).json({ message: err.message || "Cannot send verification code" });
  }
};

// ================= VERIFY EMAIL CODE =================
exports.verifyEmailCode = async (req, res) => {
  try {
    const { email, code } = req.body;

    if (!email || !code) {
      return res.status(400).json({ message: "email and code are required" });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const cached = otpStore.get(normalizedEmail);

    if (!cached) {
      return res.status(400).json({ message: "Verification code not found" });
    }

    if (Date.now() > cached.expiresAt) {
      otpStore.delete(normalizedEmail);
      return res.status(400).json({ message: "Verification code expired" });
    }

    if (String(code).trim() !== cached.code) {
      return res.status(400).json({ message: "Invalid verification code" });
    }

    const exist = await User.findOne({ email: normalizedEmail });
    if (exist) {
      otpStore.delete(normalizedEmail);
      return res.status(400).json({ message: "Email already registered" });
    }

    const user = new User({
      ...cached.payload,
      isVerified: true,
    });

    await user.save();
    otpStore.delete(normalizedEmail);

    const token = generateToken(user);

    res.status(201).json({
      message: "Email verified and account created",
      token,
      user,
    });
  } catch (err) {
    console.error("VERIFY EMAIL CODE ERROR:", err);
    res.status(500).json({ message: err.message || "Verify email failed" });
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

// ================= REGISTER SHOP =================
exports.registerShop = async (req, res) => {
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

    const shop = new User({
      name,
      email: normalizedEmail,
      password: hashed,
      phone,
      role: "shop",
    });

    await shop.save();

    const token = generateToken(shop);

    res.status(201).json({
      message: "Shop register success",
      token,
      user: shop,
    });
  } catch (err) {
    console.error("REGISTER SHOP ERROR:", err);
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

    const googleData = await verifyGoogleToken(idToken);

    const firebaseUid = googleData.uid;
    const email = googleData.email;
    const name = googleData.name;

    if (!email) {
      return res.status(400).json({ message: "Google account has no email" });
    }

    let user = await User.findOne({ email: email.trim().toLowerCase() });

    if (!user) {
      return res.status(400).json({
        message: "Tai khoan chua ton tai. Vui long dang ky bang email/mat khau.",
      });
    } else {
      user.firebaseUid = firebaseUid;
      if (!user.name) user.name = name;
      user.isVerified = true;
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
