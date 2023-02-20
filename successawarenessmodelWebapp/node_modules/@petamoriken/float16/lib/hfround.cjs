"use strict";

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.hfround = hfround;
var _converter = require("./_util/converter.cjs");
var _primordials = require("./_util/primordials.cjs");
function hfround(x) {
  const number = +x;
  if (!(0, _primordials.NumberIsFinite)(number) || number === 0) {
    return number;
  }
  const x16 = (0, _converter.roundToFloat16Bits)(number);
  return (0, _converter.convertToNumber)(x16);
}