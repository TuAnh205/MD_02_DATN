const mongoose = require("mongoose");
const User = require("./backend/models/User");
(async () => {
  const uri = process.env.MONGO_URI || "mongodb://localhost:27017/md02_datn";
  try {
    await mongoose.connect(uri, {
      useNewUrlParser: true,
      useUnifiedTopology: true,
    });
    console.log("Connected to", uri);
    const users = await User.find({}, "name email phone role").limit(10).lean();
    console.log("Users:", users);
  } catch (e) {
    console.error("Error:", e.message || e);
  } finally {
    await mongoose.disconnect();
  }
})();
