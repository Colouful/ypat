module.exports = {
  presets: [
    [
      '@vue/cli-plugin-babel/preset',
      process.env.UNI_PLATFORM === 'mp-weixin'
        ? { useBuiltIns: false }  // 禁用 core-js polyfill，避免 mp-weixin 报 DOMException
        : {}
    ]
  ],
  plugins: [
    '@babel/plugin-proposal-optional-chaining',
    '@babel/plugin-proposal-nullish-coalescing-operator'
  ],
  sourceType: 'unambiguous'  // 允许 ESM 和 CJS 混用
}