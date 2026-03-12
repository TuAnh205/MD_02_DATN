const mongoose = require('mongoose');

const bannerSchema = new mongoose.Schema({
    title: { type: String, required: true },
    subtitle: { type: String },
    image: { type: String, required: true },
    link: { type: String },
    linkType: { type: String, enum: ['product', 'category', 'external', 'none'], default: 'none' },
    linkId: { type: mongoose.Schema.Types.ObjectId }, // product or category id
    position: { type: Number, default: 0 }, // for ordering
    isActive: { type: Boolean, default: true },
    startDate: { type: Date },
    endDate: { type: Date },
    clickCount: { type: Number, default: 0 }
}, { timestamps: true });

bannerSchema.index({ isActive: 1, position: 1 });

module.exports = mongoose.model('Banner', bannerSchema);