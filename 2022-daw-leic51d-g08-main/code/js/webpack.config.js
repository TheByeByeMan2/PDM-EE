module.exports = {
    mode: 'development',
    resolve: {
        extensions: [".js", ".ts", ".tsx"]
    },
    devServer: {
        historyApiFallback: true,
        compress: false, 
        proxy: {
            "/api":{
                "target":"http://localhost:8080"
            }
        }
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/
            }
        ]
    }
}