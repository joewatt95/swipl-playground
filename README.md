# swipl-playground

A [ClojureScript](https://clojurescript.org/) [Hoplon](https://hoplon.io/)
project implementing a web-based playground for SWI-Prolog, powered by
[SWI-Prolog's WebAssembly builds](https://github.com/SWI-Prolog/npm-swipl-wasm).

## Dependencies

- java
- pnpm

## Usage
### Setup
```shell
pnpm install
```

### Development
```shell
  npx shadow-cljs watch app
```

2. Go to <http://localhost:8000> in your browser. You should see the SWI-Prolog
   playground.

3. If you edit and save a file, it will recompile the code and reload the
   browser to show the updated version.

### Compile an optimized version

```shell
 nvm use && npx shadow-cljs release app
```

## License

Copyright © 2023 Joe
