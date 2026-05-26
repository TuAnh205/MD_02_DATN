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

const mailUser = process.env.MAIL_USER || "";
const mailPass = process.env.MAIL_PASS || "";
const mailFrom = process.env.MAIL_FROM || mailUser;
const mailHost = process.env.MAIL_HOST || "smtp.gmail.com";
const mailPort = Number(process.env.MAIL_PORT || 465);
const mailSecure =
  String(process.env.MAIL_SECURE || "true").toLowerCase() === "true";

const otpStore = new Map();
const OTP_TTL_MS = 10 * 60 * 1000;

// ================= ROLE INFERENCE CONFIG =================
const parseCsv = (s) =>
  String(s || "")
    .split(",")
    .map((x) => x.trim().toLowerCase())
    .filter(Boolean);

const ADMIN_EMAILS = parseCsv(process.env.ADMIN_EMAILS);
const SHOP_EMAILS = parseCsv(process.env.SHOP_EMAILS);
const ADMIN_EMAIL_DOMAINS = parseCsv(process.env.ADMIN_EMAIL_DOMAINS);

const inferRoleByEmail = (email, fallback = "user") => {
  if (!email) return fallback;
  const normalized = String(email).trim().toLowerCase();
  const domain = normalized.split("@")[1] || "";
  if (ADMIN_EMAILS.includes(normalized)) return "admin";
  if (SHOP_EMAILS.includes(normalized)) return "shop";
  if (ADMIN_EMAIL_DOMAINS.includes(domain)) return "admin";
  return fallback;
};

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

const generateOtpCode = () =>
  String(Math.floor(100000 + Math.random() * 900000));

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
  // First try Firebase Admin verification (accepts Firebase ID tokens)
  try {
    const decoded = await admin.auth().verifyIdToken(idToken);
    return {
      uid: decoded.uid,
      email: decoded.email,
      name: decoded.name || "User",
      verified: true,
    };
  } catch (err) {
    // If admin verification fails (aud mismatch), try Google OAuth verification.
    try {
      // Attempt to decode token to extract its audience if GOOGLE_CLIENT_ID isn't set
      const decoded = jwt.decode(idToken) || {};
      const tokenAud = decoded.aud || decoded.audience;
      // Build acceptable audiences array: include tokenAud and configured GOOGLE_CLIENT_ID
      const audiences = [];
      if (tokenAud) {
        if (Array.isArray(tokenAud)) audiences.push(...tokenAud);
        else audiences.push(String(tokenAud));
      }
      if (process.env.GOOGLE_CLIENT_ID) {
        const cid = String(process.env.GOOGLE_CLIENT_ID);
        if (!audiences.includes(cid)) audiences.push(cid);
      }
      if (audiences.length === 0) throw err;
      const client = new OAuth2Client(audiences[0]);
      const ticket = await client.verifyIdToken({ idToken, audience: audiences });
      const payload = ticket.getPayload();
      return {
        uid: payload.sub,
        email: payload.email,
        name: payload.name || "User",
        verified: payload.email_verified === true,
      };
    } catch (err2) {
      // If google verification also fails, throw the google error for clearer logs
      throw err2;
    }
  }
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
    message:
      "Dang ky bang email da duoc tat. Vui long su dung Google de tao tai khoan.",
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
    const normalizedEmail = String(googleData.email || "")
      .trim()
      .toLowerCase();

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
    return res
      .status(500)
      .json({ message: err.message || "Cannot send verification code" });
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

    const inferredRole = inferRoleByEmail(cached.payload.email, cached.payload.role);
    const user = new User({
      name: cached.payload.name,
      email: cached.payload.email,
      firebaseUid: cached.payload.firebaseUid,
      googleId: cached.payload.firebaseUid,
      role: inferredRole,
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
    return res
      .status(500)
      .json({ message: err.message || "Google registration failed" });
  }
};

// ================= REGISTER =================
exports.register = async (req, res) => {
  try {
    const { name, email, password, phone, role = "user" } = req.body;

    if (!name || !email || !password) {
      return res.status(400).json({ message: "Missing data" });
    }

    const normalizedEmail = email.trim().toLowerCase();
    const normalizedRole = role === "shop" ? "shop" : "user";

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
      role: normalizedRole,
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
    if (
      lowered.includes("badcredentials") ||
      lowered.includes("invalid login")
    ) {
      return res.status(500).json({
        message:
          "Gmail authentication failed. Set MAIL_USER to your Gmail and MAIL_PASS to a Gmail App Password (not normal account password).",
      });
    }

    res
      .status(500)
      .json({ message: err.message || "Cannot send verification code" });
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

      const inferredRole = inferRoleByEmail(normalizedEmail, 'user');
      user = new User({
        firebaseUid,
        email: normalizedEmail,
        name: name || "User",
        role: inferredRole,
        password: hashed,
      });
    } else {
      user.firebaseUid = firebaseUid;
      user.name = name || user.name;
      // Update role if needed based on configured lists
      user.role = inferRoleByEmail(normalizedEmail, user.role);

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
        message:
          "Tai khoan chua ton tai. Vui long dang ky bang email/mat khau.",
      });
    } else {
      user.firebaseUid = firebaseUid;
      if (!user.name) user.name = name;
      // Ensure role is inferred/updated if this email is in admin/shop lists
      user.role = inferRoleByEmail(email, user.role);
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
    const { name, phone, avatar } = req.body;

    const user = await User.findById(req.user.id);
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    if (name && name.trim()) user.name = name.trim();
    if (phone !== undefined) user.phone = phone.trim();
    if (avatar !== undefined) user.avatar = avatar.trim();

    await user.save();

    const updatedUser = await User.findById(req.user.id).select("-password");

    res.json({
      message: "Update success",
      user: updatedUser,
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
const escapeRegExp = (value) =>
  String(value).replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

exports.forgotPassword = async (req, res) => {
  try {
    const { email } = req.body;

    if (!email) {
      return res.status(400).json({ message: "Vui lòng nhập tên tài khoản / email." });
    }

    const normalized = String(email).trim();
    let user;

    if (normalized.includes("@")) {
      user = await User.findOne({
        email: {
          $regex: `^${escapeRegExp(normalized)}$`,
          $options: "i",
        },
      });
    } else {
      user = await User.findOne({
        name: {
          $regex: `^${escapeRegExp(normalized)}$`,
          $options: "i",
        },
      });
    }

    if (!user) {
      return res.status(404).json({ message: "Tài khoản không tồn tại." });
    }

    const token = crypto.randomBytes(20).toString("hex");
    user.resetPasswordToken = token;
    user.resetPasswordExpire = Date.now() + 15 * 60 * 1000; // 15 phút
    await user.save();

    res.json({
      message: "Tài khoản hợp lệ. Vui lòng nhập mật khẩu mới.",
      email: user.email,
      resetPasswordToken: token,
    });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};

// ================= RESET PASSWORD =================
exports.resetPassword = async (req, res) => {
  try {
    const { email, token, password } = req.body;

    if (!email || !token || !password) {
      return res.status(400).json({ message: "Thiếu dữ liệu để đổi mật khẩu." });
    }

    const user = await User.findOne({
      email: email.trim().toLowerCase(),
      resetPasswordToken: token,
    });

    if (!user || !user.resetPasswordExpire || user.resetPasswordExpire < Date.now()) {
      return res.status(400).json({
        message: "Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.",
      });
    }

    const hashed = await bcrypt.hash(password, 10);
    user.password = hashed;
    user.resetPasswordToken = undefined;
    user.resetPasswordExpire = undefined;
    await user.save();

    res.json({ message: "Đổi mật khẩu thành công." });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};
// ================= CHANGE PASSWORD =================
exports.changePassword = async (req, res) => {
  try {
    const { oldPassword, newPassword } = req.body;

    if (!oldPassword || !newPassword) {
      return res.status(400).json({ message: "Thiếu dữ liệu" });
    }

    const user = await User.findById(req.user.id);

    if (!user || !user.password) {
      return res.status(400).json({
        message: "Tài khoản này không có mật khẩu (Google login)",
      });
    }

    // check mật khẩu cũ
    const isMatch = await bcrypt.compare(oldPassword, user.password);
    if (!isMatch) {
      return res.status(400).json({ message: "Mật khẩu hiện tại không đúng" });
    }

    // hash mật khẩu mới
    const hashed = await bcrypt.hash(newPassword, 10);
    user.password = hashed;

    await user.save();

    res.json({ message: "Đổi mật khẩu thành công" });
  } catch (err) {
    res.status(500).json({ message: err.message });
  }
};