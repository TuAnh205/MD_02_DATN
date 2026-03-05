const Post = require('../models/Post');

exports.listPosts = async (req, res) => {
    try {
        const posts = await Post.find({ published: true }).sort('-publishedAt');
        res.json(posts);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

exports.getPost = async (req, res) => {
    try {
        const post = await Post.findById(req.params.id);
        if (!post) return res.status(404).json({ message: 'Post not found' });
        res.json(post);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};

// Admin: create post
exports.createPost = async (req, res) => {
    try {
        const { title, content, author, published } = req.body;
        const p = new Post({ title, content, author, published });
        await p.save();
        res.status(201).json(p);
    } catch (err) {
        res.status(500).json({ message: 'Server error', error: err.message });
    }
};
