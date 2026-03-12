const mongoose = require('mongoose');

const voucherSchema = new mongoose.Schema({
    code: { type: String, required: true, unique: true, uppercase: true },
    name: { type: String, required: true },
    description: { type: String },
    type: { type: String, enum: ['percentage', 'fixed'], required: true },
    value: { type: Number, required: true, min: 0 },
    minOrderValue: { type: Number, default: 0 },
    maxDiscount: { type: Number }, // for percentage type
    usageLimit: { type: Number, default: null }, // null means unlimited
    usedCount: { type: Number, default: 0 },
    userLimit: { type: Number, default: 1 }, // how many times a user can use
    applicableProducts: [{ type: mongoose.Schema.Types.ObjectId, ref: 'Product' }],
    applicableCategories: [{ type: String }],
    startDate: { type: Date, required: true },
    endDate: { type: Date, required: true },
    isActive: { type: Boolean, default: true },
    createdBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User' }
}, { timestamps: true });

voucherSchema.index({ code: 1 });
voucherSchema.index({ isActive: 1, startDate: 1, endDate: 1 });

module.exports = mongoose.model('Voucher', voucherSchema);