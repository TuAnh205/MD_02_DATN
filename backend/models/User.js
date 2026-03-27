const mongoose = require("mongoose");

const addressSchema = new mongoose.Schema({
  name: { type: String, required: true },
  phone: { type: String, required: true },
  address: { type: String, required: true },
  city: { type: String, required: true },
  district: { type: String, required: true },
  ward: { type: String, required: true },
  isDefault: { type: Boolean, default: false },
});

const paymentMethodSchema = new mongoose.Schema({
  type: {
    type: String,
    enum: ["card", "paypal", "momo", "zalopay", "bank"],
    required: true,
  },
  details: { type: mongoose.Schema.Types.Mixed },
  isDefault: { type: Boolean, default: false },
});

const userSchema = new mongoose.Schema({
  name: { type: String, required: true },
  email: { type: String, required: true, unique: true, index: true },
  firebaseUid: { type: String, sparse: true, index: true },
  password: { type: String },
  phone: { type: String },
  avatar: { type: String },
  role: { type: String, enum: ["user", "shop", "admin"], default: "user" },
  isVerified: { type: Boolean, default: false },
  verificationToken: { type: String },
  resetPasswordToken: { type: String },
  resetPasswordExpire: { type: Date },
  googleId: { type: String },
  facebookId: { type: String },
  addresses: [addressSchema],
  paymentMethods: [paymentMethodSchema],
  preferences: {
    notifications: {
      orderStatus: { type: Boolean, default: true },
      promotions: { type: Boolean, default: true },
      newVouchers: { type: Boolean, default: true },
    },
  },
  createdAt: { type: Date, default: Date.now },
  updatedAt: { type: Date, default: Date.now },
});

userSchema.pre("save", function (next) {
  this.updatedAt = Date.now();
  next();
});

module.exports = mongoose.model("User", userSchema);
