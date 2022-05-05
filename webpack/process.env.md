# process.env 사용시 에러 발생시

bundle-f11447b5f7563eae3512.js:3862 Uncaught ReferenceError: process is not defined

```
// webpack.config.js
const webpack = require('webpack')
const dotenv = require('dotenv')

// this will update the process.env with environment variables in .env file
dotenv.config();

module.exports = {
  //...
  plugins: [
    // ...
    new webpack.DefinePlugin({
       'process.env': JSON.stringify(process.env)
    })
    // ...
  ]
  //...
}
```