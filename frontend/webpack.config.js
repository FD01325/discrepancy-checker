const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
  entry: './src/index.js',
  output: {
    path: path.resolve(__dirname, 'dist'),
    filename: 'bundle.js',
  },
  module: {
    rules: [
      {
        test: /\.(js|jsx)$/, // JavaScript and JSX files
        exclude: /node_modules/,
        use: {
          loader: 'babel-loader',
        },
      },
      {
        test: /\.css$/, // CSS files
        use: ['style-loader', 'css-loader'],
      },
      {
        test: /\.(png|jpg|gif|svg)$/, // Matches image files
        use: {
          loader: 'file-loader',
          options: {
            name: '[name].[hash].[ext]',
            outputPath: 'assets/', // Output folder for images
          },
        },
      },
    ],
  },
  plugins: [
    new HtmlWebpackPlugin({
      template: './public/index.html',
    }),
  ],
  devServer: {
    contentBase: './dist',  // For Webpack 4, use 'contentBase' instead of 'static'
    port: 3000,             // Development server port
    hot: true,              // Enable Hot Module Replacement
    compress: true,         // Enable gzip compression
    historyApiFallback: true,  // Enable single-page application routing
  },
  resolve: {
    extensions: ['.js', '.jsx'],
  },
};
