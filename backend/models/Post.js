const mongoose = require('mongoose');

const postSchema = new mongoose.Schema({
    title: { type: String, required: true },
    content: { type: String },
    author: { type: String },
    published: { type: Boolean, default: true },
    publishedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Post', postSchema);
