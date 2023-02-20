import{i as v,r as y,T as z,a as Ti,Z as Oi,t as be,b as Gs,c as Ks,d as G,e as Js,x as Xs,f as Zs,w as Qs,g as eo,h as to,_ as io}from"./indexhtml.26664944.js";/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const so=v`
  :host {
    --lumo-size-xs: 1.625rem;
    --lumo-size-s: 1.875rem;
    --lumo-size-m: 2.25rem;
    --lumo-size-l: 2.75rem;
    --lumo-size-xl: 3.5rem;

    /* Icons */
    --lumo-icon-size-s: 1.25em;
    --lumo-icon-size-m: 1.5em;
    --lumo-icon-size-l: 2.25em;
    /* For backwards compatibility */
    --lumo-icon-size: var(--lumo-icon-size-m);
  }
`,Si=document.createElement("template");Si.innerHTML=`<style>${so.toString().replace(":host","html")}</style>`;document.head.appendChild(Si.content);const Ni=v`
  :host {
    /* Sizing */
    --lumo-button-size: var(--lumo-size-m);
    min-width: calc(var(--lumo-button-size) * 2);
    height: var(--lumo-button-size);
    padding: 0 calc(var(--lumo-button-size) / 3 + var(--lumo-border-radius-m) / 2);
    margin: var(--lumo-space-xs) 0;
    box-sizing: border-box;
    /* Style */
    font-family: var(--lumo-font-family);
    font-size: var(--lumo-font-size-m);
    font-weight: 500;
    color: var(--_lumo-button-color, var(--lumo-primary-text-color));
    background-color: var(--_lumo-button-background-color, var(--lumo-contrast-5pct));
    border-radius: var(--lumo-border-radius-m);
    cursor: var(--lumo-clickable-cursor);
    -webkit-tap-highlight-color: transparent;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }

  /* Set only for the internal parts so we don't affect the host vertical alignment */
  [part='label'],
  [part='prefix'],
  [part='suffix'] {
    line-height: var(--lumo-line-height-xs);
  }

  [part='label'] {
    padding: calc(var(--lumo-button-size) / 6) 0;
  }

  :host([theme~='small']) {
    font-size: var(--lumo-font-size-s);
    --lumo-button-size: var(--lumo-size-s);
  }

  :host([theme~='large']) {
    font-size: var(--lumo-font-size-l);
    --lumo-button-size: var(--lumo-size-l);
  }

  /* For interaction states */
  :host::before,
  :host::after {
    content: '';
    /* We rely on the host always being relative */
    position: absolute;
    z-index: 1;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    background-color: currentColor;
    border-radius: inherit;
    opacity: 0;
    pointer-events: none;
  }

  /* Hover */

  @media (any-hover: hover) {
    :host(:hover)::before {
      opacity: 0.02;
    }
  }

  /* Active */

  :host::after {
    transition: opacity 1.4s, transform 0.1s;
    filter: blur(8px);
  }

  :host([active])::before {
    opacity: 0.05;
    transition-duration: 0s;
  }

  :host([active])::after {
    opacity: 0.1;
    transition-duration: 0s, 0s;
    transform: scale(0);
  }

  /* Keyboard focus */

  :host([focus-ring]) {
    box-shadow: 0 0 0 2px var(--lumo-primary-color-50pct);
  }

  :host([theme~='primary'][focus-ring]) {
    box-shadow: 0 0 0 1px var(--lumo-base-color), 0 0 0 3px var(--lumo-primary-color-50pct);
  }

  /* Types (primary, tertiary, tertiary-inline */

  :host([theme~='tertiary']),
  :host([theme~='tertiary-inline']) {
    background-color: transparent !important;
    min-width: 0;
  }

  :host([theme~='tertiary']) {
    padding: 0 calc(var(--lumo-button-size) / 6);
  }

  :host([theme~='tertiary-inline'])::before {
    display: none;
  }

  :host([theme~='tertiary-inline']) {
    margin: 0;
    height: auto;
    padding: 0;
    line-height: inherit;
    font-size: inherit;
  }

  :host([theme~='tertiary-inline']) [part='label'] {
    padding: 0;
    overflow: visible;
    line-height: inherit;
  }

  :host([theme~='primary']) {
    background-color: var(--_lumo-button-primary-background-color, var(--lumo-primary-color));
    color: var(--_lumo-button-primary-color, var(--lumo-primary-contrast-color));
    font-weight: 600;
    min-width: calc(var(--lumo-button-size) * 2.5);
  }

  :host([theme~='primary'])::before {
    background-color: black;
  }

  @media (any-hover: hover) {
    :host([theme~='primary']:hover)::before {
      opacity: 0.05;
    }
  }

  :host([theme~='primary'][active])::before {
    opacity: 0.1;
  }

  :host([theme~='primary'][active])::after {
    opacity: 0.2;
  }

  /* Colors (success, error, contrast) */

  :host([theme~='success']) {
    color: var(--lumo-success-text-color);
  }

  :host([theme~='success'][theme~='primary']) {
    background-color: var(--lumo-success-color);
    color: var(--lumo-success-contrast-color);
  }

  :host([theme~='error']) {
    color: var(--lumo-error-text-color);
  }

  :host([theme~='error'][theme~='primary']) {
    background-color: var(--lumo-error-color);
    color: var(--lumo-error-contrast-color);
  }

  :host([theme~='contrast']) {
    color: var(--lumo-contrast);
  }

  :host([theme~='contrast'][theme~='primary']) {
    background-color: var(--lumo-contrast);
    color: var(--lumo-base-color);
  }

  /* Disabled state. Keep selectors after other color variants. */

  :host([disabled]) {
    pointer-events: none;
    color: var(--lumo-disabled-text-color);
  }

  :host([theme~='primary'][disabled]) {
    background-color: var(--lumo-contrast-30pct);
    color: var(--lumo-base-color);
  }

  :host([theme~='primary'][disabled]) [part] {
    opacity: 0.7;
  }

  /* Icons */

  [part] ::slotted(vaadin-icon),
  [part] ::slotted(iron-icon) {
    display: inline-block;
    width: var(--lumo-icon-size-m);
    height: var(--lumo-icon-size-m);
  }

  /* Vaadin icons are based on a 16x16 grid (unlike Lumo and Material icons with 24x24), so they look too big by default */
  [part] ::slotted(vaadin-icon[icon^='vaadin:']),
  [part] ::slotted(iron-icon[icon^='vaadin:']) {
    padding: 0.25em;
    box-sizing: border-box !important;
  }

  [part='prefix'] {
    margin-left: -0.25em;
    margin-right: 0.25em;
  }

  [part='suffix'] {
    margin-left: 0.25em;
    margin-right: -0.25em;
  }

  /* Icon-only */

  :host([theme~='icon']:not([theme~='tertiary-inline'])) {
    min-width: var(--lumo-button-size);
    padding-left: calc(var(--lumo-button-size) / 4);
    padding-right: calc(var(--lumo-button-size) / 4);
  }

  :host([theme~='icon']) [part='prefix'],
  :host([theme~='icon']) [part='suffix'] {
    margin-left: 0;
    margin-right: 0;
  }

  /* RTL specific styles */

  :host([dir='rtl']) [part='prefix'] {
    margin-left: 0.25em;
    margin-right: -0.25em;
  }

  :host([dir='rtl']) [part='suffix'] {
    margin-left: -0.25em;
    margin-right: 0.25em;
  }

  :host([dir='rtl'][theme~='icon']) [part='prefix'],
  :host([dir='rtl'][theme~='icon']) [part='suffix'] {
    margin-left: 0;
    margin-right: 0;
  }
`;y("vaadin-button",Ni,{moduleId:"lumo-button"});/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/window.JSCompiler_renameProperty=function(i,e){return i};/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/let oo=/(url\()([^)]*)(\))/g,no=/(^\/[^\/])|(^#)|(^[\w-\d]*:)/,we,E;function le(i,e){if(i&&no.test(i)||i==="//")return i;if(we===void 0){we=!1;try{const t=new URL("b","http://a");t.pathname="c%20d",we=t.href==="http://a/c%20d"}catch{}}if(e||(e=document.baseURI||window.location.href),we)try{return new URL(i,e).href}catch{return i}return E||(E=document.implementation.createHTMLDocument("temp"),E.base=E.createElement("base"),E.head.appendChild(E.base),E.anchor=E.createElement("a"),E.body.appendChild(E.anchor)),E.base.href=e,E.anchor.href=i,E.anchor.href||i}function vt(i,e){return i.replace(oo,function(t,s,o,n){return s+"'"+le(o.replace(/["']/g,""),e)+"'"+n})}function yt(i){return i.substring(0,i.lastIndexOf("/")+1)}/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const Ii=!window.ShadyDOM||!window.ShadyDOM.inUse;Boolean(!window.ShadyCSS||window.ShadyCSS.nativeCss);const ro=Ii&&"adoptedStyleSheets"in Document.prototype&&"replaceSync"in CSSStyleSheet.prototype&&(()=>{try{const i=new CSSStyleSheet;i.replaceSync("");const e=document.createElement("div");return e.attachShadow({mode:"open"}),e.shadowRoot.adoptedStyleSheets=[i],e.shadowRoot.adoptedStyleSheets[0]===i}catch{return!1}})();let ao=window.Polymer&&window.Polymer.rootPath||yt(document.baseURI||window.location.href),Oe=window.Polymer&&window.Polymer.sanitizeDOMValue||void 0,Ba=window.Polymer&&window.Polymer.setPassiveTouchGestures||!1,de=window.Polymer&&window.Polymer.strictTemplatePolicy||!1,lo=window.Polymer&&window.Polymer.allowTemplateFromDomModule||!1,zi=window.Polymer&&window.Polymer.legacyOptimizations||!1,Mi=window.Polymer&&window.Polymer.legacyWarnings||!1,co=window.Polymer&&window.Polymer.syncInitialRender||!1,et=window.Polymer&&window.Polymer.legacyUndefined||!1,ho=window.Polymer&&window.Polymer.orderedComputed||!1,uo=!0;const po=function(i){uo=i};let Rt=window.Polymer&&window.Polymer.removeNestedTemplates||!1,ki=window.Polymer&&window.Polymer.fastDomIf||!1,fo=window.Polymer&&window.Polymer.suppressTemplateNotifications||!1,Va=window.Polymer&&window.Polymer.legacyNoObservedAttributes||!1,_o=window.Polymer&&window.Polymer.useAdoptedStyleSheetsWithBuiltCSS||!1;/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/let mo=0;const g=function(i){let e=i.__mixinApplications;e||(e=new WeakMap,i.__mixinApplications=e);let t=mo++;function s(o){let n=o.__mixinSet;if(n&&n[t])return o;let r=e,a=r.get(o);if(!a){a=i(o),r.set(o,a);let l=Object.create(a.__mixinSet||n||null);l[t]=!0,a.__mixinSet=l}return a}return s};/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/let bt={},Li={};function Ft(i,e){bt[i]=Li[i.toLowerCase()]=e}function Bt(i){return bt[i]||Li[i.toLowerCase()]}function go(i){i.querySelector("style")&&console.warn("dom-module %s has style outside template",i.id)}class K extends HTMLElement{static get observedAttributes(){return["id"]}static import(e,t){if(e){let s=Bt(e);return s&&t?s.querySelector(t):s}return null}attributeChangedCallback(e,t,s,o){t!==s&&this.register()}get assetpath(){if(!this.__assetpath){const e=window.HTMLImports&&HTMLImports.importForElement?HTMLImports.importForElement(this)||document:this.ownerDocument,t=le(this.getAttribute("assetpath")||"",e.baseURI);this.__assetpath=yt(t)}return this.__assetpath}register(e){if(e=e||this.id,e){if(de&&Bt(e)!==void 0)throw Ft(e,null),new Error(`strictTemplatePolicy: dom-module ${e} re-registered`);this.id=e,Ft(e,this),go(this)}}}K.prototype.modules=bt;customElements.define("dom-module",K);/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const vo="link[rel=import][type~=css]",yo="include",Vt="shady-unscoped";function wt(i){return K.import(i)}function Ut(i){let e=i.body?i.body:i;const t=vt(e.textContent,i.baseURI),s=document.createElement("style");return s.textContent=t,s}function bo(i){const e=i.trim().split(/\s+/),t=[];for(let s=0;s<e.length;s++)t.push(...wo(e[s]));return t}function wo(i){const e=wt(i);if(!e)return console.warn("Could not find style data in module named",i),[];if(e._styles===void 0){const t=[];t.push(...Ct(e));const s=e.querySelector("template");s&&t.push(...De(s,e.assetpath)),e._styles=t}return e._styles}function De(i,e){if(!i._styles){const t=[],s=i.content.querySelectorAll("style");for(let o=0;o<s.length;o++){let n=s[o],r=n.getAttribute(yo);r&&t.push(...bo(r).filter(function(a,l,d){return d.indexOf(a)===l})),e&&(n.textContent=vt(n.textContent,e)),t.push(n)}i._styles=t}return i._styles}function Co(i){let e=wt(i);return e?Ct(e):[]}function Ct(i){const e=[],t=i.querySelectorAll(vo);for(let s=0;s<t.length;s++){let o=t[s];if(o.import){const n=o.import,r=o.hasAttribute(Vt);if(r&&!n._unscopedStyle){const a=Ut(n);a.setAttribute(Vt,""),n._unscopedStyle=a}else n._style||(n._style=Ut(n));e.push(r?n._unscopedStyle:n._style)}}return e}function Ua(i){let e=i.trim().split(/\s+/),t="";for(let s=0;s<e.length;s++)t+=xo(e[s]);return t}function xo(i){let e=wt(i);if(e&&e._cssText===void 0){let t=Eo(e),s=e.querySelector("template");s&&(t+=Ao(s,e.assetpath)),e._cssText=t||null}return e||console.warn("Could not find style data in module named",i),e&&e._cssText||""}function Ao(i,e){let t="";const s=De(i,e);for(let o=0;o<s.length;o++){let n=s[o];n.parentNode&&n.parentNode.removeChild(n),t+=n.textContent}return t}function Eo(i){let e="",t=Ct(i);for(let s=0;s<t.length;s++)e+=t[s].textContent;return e}/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const _=window.ShadyDOM&&window.ShadyDOM.noPatch&&window.ShadyDOM.wrap?window.ShadyDOM.wrap:window.ShadyDOM?i=>ShadyDOM.patch(i):i=>i;/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/function tt(i){return i.indexOf(".")>=0}function M(i){let e=i.indexOf(".");return e===-1?i:i.slice(0,e)}function Di(i,e){return i.indexOf(e+".")===0}function ce(i,e){return e.indexOf(i+".")===0}function Se(i,e,t){return e+t.slice(i.length)}function ja(i,e){return i===e||Di(i,e)||ce(i,e)}function re(i){if(Array.isArray(i)){let e=[];for(let t=0;t<i.length;t++){let s=i[t].toString().split(".");for(let o=0;o<s.length;o++)e.push(s[o])}return e.join(".")}else return i}function Hi(i){return Array.isArray(i)?re(i).split("."):i.toString().split(".")}function w(i,e,t){let s=i,o=Hi(e);for(let n=0;n<o.length;n++){if(!s)return;let r=o[n];s=s[r]}return t&&(t.path=o.join(".")),s}function jt(i,e,t){let s=i,o=Hi(e),n=o[o.length-1];if(o.length>1){for(let r=0;r<o.length-1;r++){let a=o[r];if(s=s[a],!s)return}s[n]=t}else s[e]=t;return o.join(".")}/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const Ne={},Po=/-[a-z]/g,To=/([A-Z])/g;function Ri(i){return Ne[i]||(Ne[i]=i.indexOf("-")<0?i:i.replace(Po,e=>e[1].toUpperCase()))}function He(i){return Ne[i]||(Ne[i]=i.replace(To,"-$1").toLowerCase())}/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/let Oo=0,Fi=0,q=[],So=0,it=!1,Bi=document.createTextNode("");new window.MutationObserver(No).observe(Bi,{characterData:!0});function No(){it=!1;const i=q.length;for(let e=0;e<i;e++){let t=q[e];if(t)try{t()}catch(s){setTimeout(()=>{throw s})}}q.splice(0,i),Fi+=i}const qa={after(i){return{run(e){return window.setTimeout(e,i)},cancel(e){window.clearTimeout(e)}}},run(i,e){return window.setTimeout(i,e)},cancel(i){window.clearTimeout(i)}},$a={run(i){return window.requestAnimationFrame(i)},cancel(i){window.cancelAnimationFrame(i)}},Io={run(i){return window.requestIdleCallback?window.requestIdleCallback(i):window.setTimeout(i,16)},cancel(i){window.cancelIdleCallback?window.cancelIdleCallback(i):window.clearTimeout(i)}},xt={run(i){return it||(it=!0,Bi.textContent=So++),q.push(i),Oo++},cancel(i){const e=i-Fi;if(e>=0){if(!q[e])throw new Error("invalid async handle: "+i);q[e]=null}}};/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const zo=xt,Vi=g(i=>{class e extends i{static createProperties(s){const o=this.prototype;for(let n in s)n in o||o._createPropertyAccessor(n)}static attributeNameForProperty(s){return s.toLowerCase()}static typeForProperty(s){}_createPropertyAccessor(s,o){this._addPropertyToAttributeMap(s),this.hasOwnProperty(JSCompiler_renameProperty("__dataHasAccessor",this))||(this.__dataHasAccessor=Object.assign({},this.__dataHasAccessor)),this.__dataHasAccessor[s]||(this.__dataHasAccessor[s]=!0,this._definePropertyAccessor(s,o))}_addPropertyToAttributeMap(s){this.hasOwnProperty(JSCompiler_renameProperty("__dataAttributes",this))||(this.__dataAttributes=Object.assign({},this.__dataAttributes));let o=this.__dataAttributes[s];return o||(o=this.constructor.attributeNameForProperty(s),this.__dataAttributes[o]=s),o}_definePropertyAccessor(s,o){Object.defineProperty(this,s,{get(){return this.__data[s]},set:o?function(){}:function(n){this._setPendingProperty(s,n,!0)&&this._invalidateProperties()}})}constructor(){super(),this.__dataEnabled=!1,this.__dataReady=!1,this.__dataInvalid=!1,this.__data={},this.__dataPending=null,this.__dataOld=null,this.__dataInstanceProps=null,this.__dataCounter=0,this.__serializing=!1,this._initializeProperties()}ready(){this.__dataReady=!0,this._flushProperties()}_initializeProperties(){for(let s in this.__dataHasAccessor)this.hasOwnProperty(s)&&(this.__dataInstanceProps=this.__dataInstanceProps||{},this.__dataInstanceProps[s]=this[s],delete this[s])}_initializeInstanceProperties(s){Object.assign(this,s)}_setProperty(s,o){this._setPendingProperty(s,o)&&this._invalidateProperties()}_getProperty(s){return this.__data[s]}_setPendingProperty(s,o,n){let r=this.__data[s],a=this._shouldPropertyChange(s,o,r);return a&&(this.__dataPending||(this.__dataPending={},this.__dataOld={}),this.__dataOld&&!(s in this.__dataOld)&&(this.__dataOld[s]=r),this.__data[s]=o,this.__dataPending[s]=o),a}_isPropertyPending(s){return!!(this.__dataPending&&this.__dataPending.hasOwnProperty(s))}_invalidateProperties(){!this.__dataInvalid&&this.__dataReady&&(this.__dataInvalid=!0,zo.run(()=>{this.__dataInvalid&&(this.__dataInvalid=!1,this._flushProperties())}))}_enableProperties(){this.__dataEnabled||(this.__dataEnabled=!0,this.__dataInstanceProps&&(this._initializeInstanceProperties(this.__dataInstanceProps),this.__dataInstanceProps=null),this.ready())}_flushProperties(){this.__dataCounter++;const s=this.__data,o=this.__dataPending,n=this.__dataOld;this._shouldPropertiesChange(s,o,n)&&(this.__dataPending=null,this.__dataOld=null,this._propertiesChanged(s,o,n)),this.__dataCounter--}_shouldPropertiesChange(s,o,n){return Boolean(o)}_propertiesChanged(s,o,n){}_shouldPropertyChange(s,o,n){return n!==o&&(n===n||o===o)}attributeChangedCallback(s,o,n,r){o!==n&&this._attributeToProperty(s,n),super.attributeChangedCallback&&super.attributeChangedCallback(s,o,n,r)}_attributeToProperty(s,o,n){if(!this.__serializing){const r=this.__dataAttributes,a=r&&r[s]||s;this[a]=this._deserializeValue(o,n||this.constructor.typeForProperty(a))}}_propertyToAttribute(s,o,n){this.__serializing=!0,n=arguments.length<3?this[s]:n,this._valueToNodeAttribute(this,n,o||this.constructor.attributeNameForProperty(s)),this.__serializing=!1}_valueToNodeAttribute(s,o,n){const r=this._serializeValue(o);(n==="class"||n==="name"||n==="slot")&&(s=_(s)),r===void 0?s.removeAttribute(n):s.setAttribute(n,r===""&&window.trustedTypes?window.trustedTypes.emptyScript:r)}_serializeValue(s){switch(typeof s){case"boolean":return s?"":void 0;default:return s!=null?s.toString():void 0}}_deserializeValue(s,o){switch(o){case Boolean:return s!==null;case Number:return Number(s);default:return s}}}return e});/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const Ui={};let Ce=HTMLElement.prototype;for(;Ce;){let i=Object.getOwnPropertyNames(Ce);for(let e=0;e<i.length;e++)Ui[i[e]]=!0;Ce=Object.getPrototypeOf(Ce)}const Mo=(()=>window.trustedTypes?i=>trustedTypes.isHTML(i)||trustedTypes.isScript(i)||trustedTypes.isScriptURL(i):()=>!1)();function ko(i,e){if(!Ui[e]){let t=i[e];t!==void 0&&(i.__data?i._setPendingProperty(e,t):(i.__dataProto?i.hasOwnProperty(JSCompiler_renameProperty("__dataProto",i))||(i.__dataProto=Object.create(i.__dataProto)):i.__dataProto={},i.__dataProto[e]=t))}}const Lo=g(i=>{const e=Vi(i);class t extends e{static createPropertiesForAttributes(){let o=this.observedAttributes;for(let n=0;n<o.length;n++)this.prototype._createPropertyAccessor(Ri(o[n]))}static attributeNameForProperty(o){return He(o)}_initializeProperties(){this.__dataProto&&(this._initializeProtoProperties(this.__dataProto),this.__dataProto=null),super._initializeProperties()}_initializeProtoProperties(o){for(let n in o)this._setProperty(n,o[n])}_ensureAttribute(o,n){const r=this;r.hasAttribute(o)||this._valueToNodeAttribute(r,n,o)}_serializeValue(o){switch(typeof o){case"object":if(o instanceof Date)return o.toString();if(o){if(Mo(o))return o;try{return JSON.stringify(o)}catch{return""}}default:return super._serializeValue(o)}}_deserializeValue(o,n){let r;switch(n){case Object:try{r=JSON.parse(o)}catch{r=o}break;case Array:try{r=JSON.parse(o)}catch{r=null,console.warn(`Polymer::Attributes: couldn't decode Array as JSON: ${o}`)}break;case Date:r=isNaN(o)?String(o):Number(o),r=new Date(r);break;default:r=super._deserializeValue(o,n);break}return r}_definePropertyAccessor(o,n){ko(this,o),super._definePropertyAccessor(o,n)}_hasAccessor(o){return this.__dataHasAccessor&&this.__dataHasAccessor[o]}_isPropertyPending(o){return Boolean(this.__dataPending&&o in this.__dataPending)}}return t});/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const Do={"dom-if":!0,"dom-repeat":!0};let qt=!1,$t=!1;function Ho(){if(!qt){qt=!0;const i=document.createElement("textarea");i.placeholder="a",$t=i.placeholder===i.textContent}return $t}function Ro(i){Ho()&&i.localName==="textarea"&&i.placeholder&&i.placeholder===i.textContent&&(i.textContent=null)}const Fo=(()=>{const i=window.trustedTypes&&window.trustedTypes.createPolicy("polymer-template-event-attribute-policy",{createScript:e=>e});return(e,t,s)=>{const o=t.getAttribute(s);if(i&&s.startsWith("on-")){e.setAttribute(s,i.createScript(o,s));return}e.setAttribute(s,o)}})();function Bo(i){let e=i.getAttribute("is");if(e&&Do[e]){let t=i;for(t.removeAttribute("is"),i=t.ownerDocument.createElement(e),t.parentNode.replaceChild(i,t),i.appendChild(t);t.attributes.length;){const{name:s}=t.attributes[0];Fo(i,t,s),t.removeAttribute(s)}}return i}function ji(i,e){let t=e.parentInfo&&ji(i,e.parentInfo);if(t){for(let s=t.firstChild,o=0;s;s=s.nextSibling)if(e.parentIndex===o++)return s}else return i}function Vo(i,e,t,s){s.id&&(e[s.id]=t)}function Uo(i,e,t){if(t.events&&t.events.length)for(let s=0,o=t.events,n;s<o.length&&(n=o[s]);s++)i._addMethodEventListenerToNode(e,n.name,n.value,i)}function jo(i,e,t,s){t.templateInfo&&(e._templateInfo=t.templateInfo,e._parentTemplateInfo=s)}function qo(i,e,t){return i=i._methodHost||i,function(o){i[t]?i[t](o,o.detail):console.warn("listener method `"+t+"` not defined")}}const $o=g(i=>{class e extends i{static _parseTemplate(s,o){if(!s._templateInfo){let n=s._templateInfo={};n.nodeInfoList=[],n.nestedTemplate=Boolean(o),n.stripWhiteSpace=o&&o.stripWhiteSpace||s.hasAttribute&&s.hasAttribute("strip-whitespace"),this._parseTemplateContent(s,n,{parent:null})}return s._templateInfo}static _parseTemplateContent(s,o,n){return this._parseTemplateNode(s.content,o,n)}static _parseTemplateNode(s,o,n){let r=!1,a=s;return a.localName=="template"&&!a.hasAttribute("preserve-content")?r=this._parseTemplateNestedTemplate(a,o,n)||r:a.localName==="slot"&&(o.hasInsertionPoint=!0),Ro(a),a.firstChild&&this._parseTemplateChildNodes(a,o,n),a.hasAttributes&&a.hasAttributes()&&(r=this._parseTemplateNodeAttributes(a,o,n)||r),r||n.noted}static _parseTemplateChildNodes(s,o,n){if(!(s.localName==="script"||s.localName==="style"))for(let r=s.firstChild,a=0,l;r;r=l){if(r.localName=="template"&&(r=Bo(r)),l=r.nextSibling,r.nodeType===Node.TEXT_NODE){let c=l;for(;c&&c.nodeType===Node.TEXT_NODE;)r.textContent+=c.textContent,l=c.nextSibling,s.removeChild(c),c=l;if(o.stripWhiteSpace&&!r.textContent.trim()){s.removeChild(r);continue}}let d={parentIndex:a,parentInfo:n};this._parseTemplateNode(r,o,d)&&(d.infoIndex=o.nodeInfoList.push(d)-1),r.parentNode&&a++}}static _parseTemplateNestedTemplate(s,o,n){let r=s,a=this._parseTemplate(r,o);return(a.content=r.content.ownerDocument.createDocumentFragment()).appendChild(r.content),n.templateInfo=a,!0}static _parseTemplateNodeAttributes(s,o,n){let r=!1,a=Array.from(s.attributes);for(let l=a.length-1,d;d=a[l];l--)r=this._parseTemplateNodeAttribute(s,o,n,d.name,d.value)||r;return r}static _parseTemplateNodeAttribute(s,o,n,r,a){return r.slice(0,3)==="on-"?(s.removeAttribute(r),n.events=n.events||[],n.events.push({name:r.slice(3),value:a}),!0):r==="id"?(n.id=a,!0):!1}static _contentForTemplate(s){let o=s._templateInfo;return o&&o.content||s.content}_stampTemplate(s,o){s&&!s.content&&window.HTMLTemplateElement&&HTMLTemplateElement.decorate&&HTMLTemplateElement.decorate(s),o=o||this.constructor._parseTemplate(s);let n=o.nodeInfoList,r=o.content||s.content,a=document.importNode(r,!0);a.__noInsertionPoint=!o.hasInsertionPoint;let l=a.nodeList=new Array(n.length);a.$={};for(let d=0,c=n.length,h;d<c&&(h=n[d]);d++){let u=l[d]=ji(a,h);Vo(this,a.$,u,h),jo(this,u,h,o),Uo(this,u,h)}return a=a,a}_addMethodEventListenerToNode(s,o,n,r){r=r||s;let a=qo(r,o,n);return this._addEventListenerToNode(s,o,a),a}_addEventListenerToNode(s,o,n){s.addEventListener(o,n)}_removeEventListenerFromNode(s,o,n){s.removeEventListener(o,n)}}return e});/**
 * @fileoverview
 * @suppress {checkPrototypalTypes}
 * @license Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt The complete set of authors may be found
 * at http://polymer.github.io/AUTHORS.txt The complete set of contributors may
 * be found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by
 * Google as part of the polymer project is also subject to an additional IP
 * rights grant found at http://polymer.github.io/PATENTS.txt
 */let he=0;const ue=[],f={COMPUTE:"__computeEffects",REFLECT:"__reflectEffects",NOTIFY:"__notifyEffects",PROPAGATE:"__propagateEffects",OBSERVE:"__observeEffects",READ_ONLY:"__readOnly"},qi="__computeInfo",Yo=/[A-Z]/;function je(i,e,t){let s=i[e];if(!s)s=i[e]={};else if(!i.hasOwnProperty(e)&&(s=i[e]=Object.create(i[e]),t))for(let o in s){let n=s[o],r=s[o]=Array(n.length);for(let a=0;a<n.length;a++)r[a]=n[a]}return s}function ae(i,e,t,s,o,n){if(e){let r=!1;const a=he++;for(let l in t){let d=o?M(l):l,c=e[d];if(c)for(let h=0,u=c.length,p;h<u&&(p=c[h]);h++)(!p.info||p.info.lastRun!==a)&&(!o||At(l,p.trigger))&&(p.info&&(p.info.lastRun=a),p.fn(i,l,t,s,p.info,o,n),r=!0)}return r}return!1}function Wo(i,e,t,s,o,n,r,a){let l=!1,d=r?M(s):s,c=e[d];if(c)for(let h=0,u=c.length,p;h<u&&(p=c[h]);h++)(!p.info||p.info.lastRun!==t)&&(!r||At(s,p.trigger))&&(p.info&&(p.info.lastRun=t),p.fn(i,s,o,n,p.info,r,a),l=!0);return l}function At(i,e){if(e){let t=e.name;return t==i||!!(e.structured&&Di(t,i))||!!(e.wildcard&&ce(t,i))}else return!0}function Yt(i,e,t,s,o){let n=typeof o.method=="string"?i[o.method]:o.method,r=o.property;n?n.call(i,i.__data[r],s[r]):o.dynamicFn||console.warn("observer method `"+o.method+"` not defined")}function Go(i,e,t,s,o){let n=i[f.NOTIFY],r,a=he++;for(let d in e)e[d]&&(n&&Wo(i,n,a,d,t,s,o)||o&&Ko(i,d,t))&&(r=!0);let l;r&&(l=i.__dataHost)&&l._invalidateProperties&&l._invalidateProperties()}function Ko(i,e,t){let s=M(e);if(s!==e){let o=He(s)+"-changed";return $i(i,o,t[e],e),!0}return!1}function $i(i,e,t,s){let o={value:t,queueProperty:!0};s&&(o.path=s),_(i).dispatchEvent(new CustomEvent(e,{detail:o}))}function Jo(i,e,t,s,o,n){let a=(n?M(e):e)!=e?e:null,l=a?w(i,a):i.__data[e];a&&l===void 0&&(l=t[e]),$i(i,o.eventName,l,a)}function Xo(i,e,t,s,o){let n,r=i.detail,a=r&&r.path;a?(s=Se(t,s,a),n=r&&r.value):n=i.currentTarget[t],n=o?!n:n,(!e[f.READ_ONLY]||!e[f.READ_ONLY][s])&&e._setPendingPropertyOrPath(s,n,!0,Boolean(a))&&(!r||!r.queueProperty)&&e._invalidateProperties()}function Zo(i,e,t,s,o){let n=i.__data[e];Oe&&(n=Oe(n,o.attrName,"attribute",i)),i._propertyToAttribute(e,o.attrName,n)}function Qo(i,e,t,s){let o=i[f.COMPUTE];if(o)if(ho){he++;const n=tn(i),r=[];for(let l in e)Wt(l,o,r,n,s);let a;for(;a=r.shift();)Yi(i,"",e,t,a)&&Wt(a.methodInfo,o,r,n,s);Object.assign(t,i.__dataOld),Object.assign(e,i.__dataPending),i.__dataPending=null}else{let n=e;for(;ae(i,o,n,t,s);)Object.assign(t,i.__dataOld),Object.assign(e,i.__dataPending),n=i.__dataPending,i.__dataPending=null}}const en=(i,e,t)=>{let s=0,o=e.length-1,n=-1;for(;s<=o;){const r=s+o>>1,a=t.get(e[r].methodInfo)-t.get(i.methodInfo);if(a<0)s=r+1;else if(a>0)o=r-1;else{n=r;break}}n<0&&(n=o+1),e.splice(n,0,i)},Wt=(i,e,t,s,o)=>{const n=o?M(i):i,r=e[n];if(r)for(let a=0;a<r.length;a++){const l=r[a];l.info.lastRun!==he&&(!o||At(i,l.trigger))&&(l.info.lastRun=he,en(l.info,t,s))}};function tn(i){let e=i.constructor.__orderedComputedDeps;if(!e){e=new Map;const t=i[f.COMPUTE];let{counts:s,ready:o,total:n}=sn(i),r;for(;r=o.shift();){e.set(r,e.size);const a=t[r];a&&a.forEach(l=>{const d=l.info.methodInfo;--n,--s[d]===0&&o.push(d)})}n!==0&&console.warn(`Computed graph for ${i.localName} incomplete; circular?`),i.constructor.__orderedComputedDeps=e}return e}function sn(i){const e=i[qi],t={},s=i[f.COMPUTE],o=[];let n=0;for(let r in e){const a=e[r];n+=t[r]=a.args.filter(l=>!l.literal).length+(a.dynamicFn?1:0)}for(let r in s)e[r]||o.push(r);return{counts:t,ready:o,total:n}}function Yi(i,e,t,s,o){let n=st(i,e,t,s,o);if(n===ue)return!1;let r=o.methodInfo;return i.__dataHasAccessor&&i.__dataHasAccessor[r]?i._setPendingProperty(r,n,!0):(i[r]=n,!1)}function on(i,e,t){let s=i.__dataLinkedPaths;if(s){let o;for(let n in s){let r=s[n];ce(n,e)?(o=Se(n,r,e),i._setPendingPropertyOrPath(o,t,!0,!0)):ce(r,e)&&(o=Se(r,n,e),i._setPendingPropertyOrPath(o,t,!0,!0))}}}function qe(i,e,t,s,o,n,r){t.bindings=t.bindings||[];let a={kind:s,target:o,parts:n,literal:r,isCompound:n.length!==1};if(t.bindings.push(a),dn(a)){let{event:d,negate:c}=a.parts[0];a.listenerEvent=d||He(o)+"-changed",a.listenerNegate=c}let l=e.nodeInfoList.length;for(let d=0;d<a.parts.length;d++){let c=a.parts[d];c.compoundIndex=d,nn(i,e,a,c,l)}}function nn(i,e,t,s,o){if(!s.literal)if(t.kind==="attribute"&&t.target[0]==="-")console.warn("Cannot set attribute "+t.target+' because "-" is not a valid attribute starting character');else{let n=s.dependencies,r={index:o,binding:t,part:s,evaluator:i};for(let a=0;a<n.length;a++){let l=n[a];typeof l=="string"&&(l=Gi(l),l.wildcard=!0),i._addTemplatePropertyEffect(e,l.rootProperty,{fn:rn,info:r,trigger:l})}}}function rn(i,e,t,s,o,n,r){let a=r[o.index],l=o.binding,d=o.part;if(n&&d.source&&e.length>d.source.length&&l.kind=="property"&&!l.isCompound&&a.__isPropertyEffectsClient&&a.__dataHasAccessor&&a.__dataHasAccessor[l.target]){let c=t[e];e=Se(d.source,l.target,e),a._setPendingPropertyOrPath(e,c,!1,!0)&&i._enqueueClient(a)}else{let c=o.evaluator._evaluateBinding(i,d,e,t,s,n);c!==ue&&an(i,a,l,d,c)}}function an(i,e,t,s,o){if(o=ln(e,o,t,s),Oe&&(o=Oe(o,t.target,t.kind,e)),t.kind=="attribute")i._valueToNodeAttribute(e,o,t.target);else{let n=t.target;e.__isPropertyEffectsClient&&e.__dataHasAccessor&&e.__dataHasAccessor[n]?(!e[f.READ_ONLY]||!e[f.READ_ONLY][n])&&e._setPendingProperty(n,o)&&i._enqueueClient(e):i._setUnmanagedPropertyToNode(e,n,o)}}function ln(i,e,t,s){if(t.isCompound){let o=i.__dataCompoundStorage[t.target];o[s.compoundIndex]=e,e=o.join("")}return t.kind!=="attribute"&&(t.target==="textContent"||t.target==="value"&&(i.localName==="input"||i.localName==="textarea"))&&(e=e==null?"":e),e}function dn(i){return Boolean(i.target)&&i.kind!="attribute"&&i.kind!="text"&&!i.isCompound&&i.parts[0].mode==="{"}function cn(i,e){let{nodeList:t,nodeInfoList:s}=e;if(s.length)for(let o=0;o<s.length;o++){let n=s[o],r=t[o],a=n.bindings;if(a)for(let l=0;l<a.length;l++){let d=a[l];hn(r,d),un(r,i,d)}r.__dataHost=i}}function hn(i,e){if(e.isCompound){let t=i.__dataCompoundStorage||(i.__dataCompoundStorage={}),s=e.parts,o=new Array(s.length);for(let r=0;r<s.length;r++)o[r]=s[r].literal;let n=e.target;t[n]=o,e.literal&&e.kind=="property"&&(n==="className"&&(i=_(i)),i[n]=e.literal)}}function un(i,e,t){if(t.listenerEvent){let s=t.parts[0];i.addEventListener(t.listenerEvent,function(o){Xo(o,e,t.target,s.source,s.negate)})}}function Gt(i,e,t,s,o,n){n=e.static||n&&(typeof n!="object"||n[e.methodName]);let r={methodName:e.methodName,args:e.args,methodInfo:o,dynamicFn:n};for(let a=0,l;a<e.args.length&&(l=e.args[a]);a++)l.literal||i._addPropertyEffect(l.rootProperty,t,{fn:s,info:r,trigger:l});return n&&i._addPropertyEffect(e.methodName,t,{fn:s,info:r}),r}function st(i,e,t,s,o){let n=i._methodHost||i,r=n[o.methodName];if(r){let a=i._marshalArgs(o.args,e,t);return a===ue?ue:r.apply(n,a)}else o.dynamicFn||console.warn("method `"+o.methodName+"` not defined")}const pn=[],Wi="(?:[a-zA-Z_$][\\w.:$\\-*]*)",fn="(?:[-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?)",_n="(?:'(?:[^'\\\\]|\\\\.)*')",mn='(?:"(?:[^"\\\\]|\\\\.)*")',gn="(?:"+_n+"|"+mn+")",Kt="(?:("+Wi+"|"+fn+"|"+gn+")\\s*)",vn="(?:"+Kt+"(?:,\\s*"+Kt+")*)",yn="(?:\\(\\s*(?:"+vn+"?)\\)\\s*)",bn="("+Wi+"\\s*"+yn+"?)",wn="(\\[\\[|{{)\\s*",Cn="(?:]]|}})",xn="(?:(!)\\s*)?",An=wn+xn+bn+Cn,Jt=new RegExp(An,"g");function Xt(i){let e="";for(let t=0;t<i.length;t++){let s=i[t].literal;e+=s||""}return e}function $e(i){let e=i.match(/([^\s]+?)\(([\s\S]*)\)/);if(e){let s={methodName:e[1],static:!0,args:pn};if(e[2].trim()){let o=e[2].replace(/\\,/g,"&comma;").split(",");return En(o,s)}else return s}return null}function En(i,e){return e.args=i.map(function(t){let s=Gi(t);return s.literal||(e.static=!1),s},this),e}function Gi(i){let e=i.trim().replace(/&comma;/g,",").replace(/\\(.)/g,"$1"),t={name:e,value:"",literal:!1},s=e[0];switch(s==="-"&&(s=e[1]),s>="0"&&s<="9"&&(s="#"),s){case"'":case'"':t.value=e.slice(1,-1),t.literal=!0;break;case"#":t.value=Number(e),t.literal=!0;break}return t.literal||(t.rootProperty=M(e),t.structured=tt(e),t.structured&&(t.wildcard=e.slice(-2)==".*",t.wildcard&&(t.name=e.slice(0,-2)))),t}function Zt(i,e,t){let s=w(i,t);return s===void 0&&(s=e[t]),s}function Ki(i,e,t,s){const o={indexSplices:s};et&&!i._overrideLegacyUndefined&&(e.splices=o),i.notifyPath(t+".splices",o),i.notifyPath(t+".length",e.length),et&&!i._overrideLegacyUndefined&&(o.indexSplices=[])}function X(i,e,t,s,o,n){Ki(i,e,t,[{index:s,addedCount:o,removed:n,object:e,type:"splice"}])}function Pn(i){return i[0].toUpperCase()+i.substring(1)}const Et=g(i=>{const e=$o(Lo(i));class t extends e{constructor(){super(),this.__isPropertyEffectsClient=!0,this.__dataClientsReady,this.__dataPendingClients,this.__dataToNotify,this.__dataLinkedPaths,this.__dataHasPaths,this.__dataCompoundStorage,this.__dataHost,this.__dataTemp,this.__dataClientsInitialized,this.__data,this.__dataPending,this.__dataOld,this.__computeEffects,this.__computeInfo,this.__reflectEffects,this.__notifyEffects,this.__propagateEffects,this.__observeEffects,this.__readOnly,this.__templateInfo,this._overrideLegacyUndefined}get PROPERTY_EFFECT_TYPES(){return f}_initializeProperties(){super._initializeProperties(),this._registerHost(),this.__dataClientsReady=!1,this.__dataPendingClients=null,this.__dataToNotify=null,this.__dataLinkedPaths=null,this.__dataHasPaths=!1,this.__dataCompoundStorage=this.__dataCompoundStorage||null,this.__dataHost=this.__dataHost||null,this.__dataTemp={},this.__dataClientsInitialized=!1}_registerHost(){if(Z.length){let o=Z[Z.length-1];o._enqueueClient(this),this.__dataHost=o}}_initializeProtoProperties(o){this.__data=Object.create(o),this.__dataPending=Object.create(o),this.__dataOld={}}_initializeInstanceProperties(o){let n=this[f.READ_ONLY];for(let r in o)(!n||!n[r])&&(this.__dataPending=this.__dataPending||{},this.__dataOld=this.__dataOld||{},this.__data[r]=this.__dataPending[r]=o[r])}_addPropertyEffect(o,n,r){this._createPropertyAccessor(o,n==f.READ_ONLY);let a=je(this,n,!0)[o];a||(a=this[n][o]=[]),a.push(r)}_removePropertyEffect(o,n,r){let a=je(this,n,!0)[o],l=a.indexOf(r);l>=0&&a.splice(l,1)}_hasPropertyEffect(o,n){let r=this[n];return Boolean(r&&r[o])}_hasReadOnlyEffect(o){return this._hasPropertyEffect(o,f.READ_ONLY)}_hasNotifyEffect(o){return this._hasPropertyEffect(o,f.NOTIFY)}_hasReflectEffect(o){return this._hasPropertyEffect(o,f.REFLECT)}_hasComputedEffect(o){return this._hasPropertyEffect(o,f.COMPUTE)}_setPendingPropertyOrPath(o,n,r,a){if(a||M(Array.isArray(o)?o[0]:o)!==o){if(!a){let l=w(this,o);if(o=jt(this,o,n),!o||!super._shouldPropertyChange(o,n,l))return!1}if(this.__dataHasPaths=!0,this._setPendingProperty(o,n,r))return on(this,o,n),!0}else{if(this.__dataHasAccessor&&this.__dataHasAccessor[o])return this._setPendingProperty(o,n,r);this[o]=n}return!1}_setUnmanagedPropertyToNode(o,n,r){(r!==o[n]||typeof r=="object")&&(n==="className"&&(o=_(o)),o[n]=r)}_setPendingProperty(o,n,r){let a=this.__dataHasPaths&&tt(o),l=a?this.__dataTemp:this.__data;return this._shouldPropertyChange(o,n,l[o])?(this.__dataPending||(this.__dataPending={},this.__dataOld={}),o in this.__dataOld||(this.__dataOld[o]=this.__data[o]),a?this.__dataTemp[o]=n:this.__data[o]=n,this.__dataPending[o]=n,(a||this[f.NOTIFY]&&this[f.NOTIFY][o])&&(this.__dataToNotify=this.__dataToNotify||{},this.__dataToNotify[o]=r),!0):!1}_setProperty(o,n){this._setPendingProperty(o,n,!0)&&this._invalidateProperties()}_invalidateProperties(){this.__dataReady&&this._flushProperties()}_enqueueClient(o){this.__dataPendingClients=this.__dataPendingClients||[],o!==this&&this.__dataPendingClients.push(o)}_flushClients(){this.__dataClientsReady?this.__enableOrFlushClients():(this.__dataClientsReady=!0,this._readyClients(),this.__dataReady=!0)}__enableOrFlushClients(){let o=this.__dataPendingClients;if(o){this.__dataPendingClients=null;for(let n=0;n<o.length;n++){let r=o[n];r.__dataEnabled?r.__dataPending&&r._flushProperties():r._enableProperties()}}}_readyClients(){this.__enableOrFlushClients()}setProperties(o,n){for(let r in o)(n||!this[f.READ_ONLY]||!this[f.READ_ONLY][r])&&this._setPendingPropertyOrPath(r,o[r],!0);this._invalidateProperties()}ready(){this._flushProperties(),this.__dataClientsReady||this._flushClients(),this.__dataPending&&this._flushProperties()}_propertiesChanged(o,n,r){let a=this.__dataHasPaths;this.__dataHasPaths=!1;let l;Qo(this,n,r,a),l=this.__dataToNotify,this.__dataToNotify=null,this._propagatePropertyChanges(n,r,a),this._flushClients(),ae(this,this[f.REFLECT],n,r,a),ae(this,this[f.OBSERVE],n,r,a),l&&Go(this,l,n,r,a),this.__dataCounter==1&&(this.__dataTemp={})}_propagatePropertyChanges(o,n,r){this[f.PROPAGATE]&&ae(this,this[f.PROPAGATE],o,n,r),this.__templateInfo&&this._runEffectsForTemplate(this.__templateInfo,o,n,r)}_runEffectsForTemplate(o,n,r,a){const l=(d,c)=>{ae(this,o.propertyEffects,d,r,c,o.nodeList);for(let h=o.firstChild;h;h=h.nextSibling)this._runEffectsForTemplate(h,d,r,c)};o.runEffects?o.runEffects(l,n,a):l(n,a)}linkPaths(o,n){o=re(o),n=re(n),this.__dataLinkedPaths=this.__dataLinkedPaths||{},this.__dataLinkedPaths[o]=n}unlinkPaths(o){o=re(o),this.__dataLinkedPaths&&delete this.__dataLinkedPaths[o]}notifySplices(o,n){let r={path:""},a=w(this,o,r);Ki(this,a,r.path,n)}get(o,n){return w(n||this,o)}set(o,n,r){r?jt(r,o,n):(!this[f.READ_ONLY]||!this[f.READ_ONLY][o])&&this._setPendingPropertyOrPath(o,n,!0)&&this._invalidateProperties()}push(o,...n){let r={path:""},a=w(this,o,r),l=a.length,d=a.push(...n);return n.length&&X(this,a,r.path,l,n.length,[]),d}pop(o){let n={path:""},r=w(this,o,n),a=Boolean(r.length),l=r.pop();return a&&X(this,r,n.path,r.length,0,[l]),l}splice(o,n,r,...a){let l={path:""},d=w(this,o,l);n<0?n=d.length-Math.floor(-n):n&&(n=Math.floor(n));let c;return arguments.length===2?c=d.splice(n):c=d.splice(n,r,...a),(a.length||c.length)&&X(this,d,l.path,n,a.length,c),c}shift(o){let n={path:""},r=w(this,o,n),a=Boolean(r.length),l=r.shift();return a&&X(this,r,n.path,0,0,[l]),l}unshift(o,...n){let r={path:""},a=w(this,o,r),l=a.unshift(...n);return n.length&&X(this,a,r.path,0,n.length,[]),l}notifyPath(o,n){let r;if(arguments.length==1){let a={path:""};n=w(this,o,a),r=a.path}else Array.isArray(o)?r=re(o):r=o;this._setPendingPropertyOrPath(r,n,!0,!0)&&this._invalidateProperties()}_createReadOnlyProperty(o,n){this._addPropertyEffect(o,f.READ_ONLY),n&&(this["_set"+Pn(o)]=function(r){this._setProperty(o,r)})}_createPropertyObserver(o,n,r){let a={property:o,method:n,dynamicFn:Boolean(r)};this._addPropertyEffect(o,f.OBSERVE,{fn:Yt,info:a,trigger:{name:o}}),r&&this._addPropertyEffect(n,f.OBSERVE,{fn:Yt,info:a,trigger:{name:n}})}_createMethodObserver(o,n){let r=$e(o);if(!r)throw new Error("Malformed observer expression '"+o+"'");Gt(this,r,f.OBSERVE,st,null,n)}_createNotifyingProperty(o){this._addPropertyEffect(o,f.NOTIFY,{fn:Jo,info:{eventName:He(o)+"-changed",property:o}})}_createReflectedProperty(o){let n=this.constructor.attributeNameForProperty(o);n[0]==="-"?console.warn("Property "+o+" cannot be reflected to attribute "+n+' because "-" is not a valid starting attribute name. Use a lowercase first letter for the property instead.'):this._addPropertyEffect(o,f.REFLECT,{fn:Zo,info:{attrName:n}})}_createComputedProperty(o,n,r){let a=$e(n);if(!a)throw new Error("Malformed computed expression '"+n+"'");const l=Gt(this,a,f.COMPUTE,Yi,o,r);je(this,qi)[o]=l}_marshalArgs(o,n,r){const a=this.__data,l=[];for(let d=0,c=o.length;d<c;d++){let{name:h,structured:u,wildcard:p,value:m,literal:P}=o[d];if(!P)if(p){const T=ce(h,n),x=Zt(a,r,T?n:h);m={path:T?n:h,value:x,base:T?w(a,h):x}}else m=u?Zt(a,r,h):a[h];if(et&&!this._overrideLegacyUndefined&&m===void 0&&o.length>1)return ue;l[d]=m}return l}static addPropertyEffect(o,n,r){this.prototype._addPropertyEffect(o,n,r)}static createPropertyObserver(o,n,r){this.prototype._createPropertyObserver(o,n,r)}static createMethodObserver(o,n){this.prototype._createMethodObserver(o,n)}static createNotifyingProperty(o){this.prototype._createNotifyingProperty(o)}static createReadOnlyProperty(o,n){this.prototype._createReadOnlyProperty(o,n)}static createReflectedProperty(o){this.prototype._createReflectedProperty(o)}static createComputedProperty(o,n,r){this.prototype._createComputedProperty(o,n,r)}static bindTemplate(o){return this.prototype._bindTemplate(o)}_bindTemplate(o,n){let r=this.constructor._parseTemplate(o),a=this.__preBoundTemplateInfo==r;if(!a)for(let l in r.propertyEffects)this._createPropertyAccessor(l);if(n)if(r=Object.create(r),r.wasPreBound=a,!this.__templateInfo)this.__templateInfo=r;else{const l=o._parentTemplateInfo||this.__templateInfo,d=l.lastChild;r.parent=l,l.lastChild=r,r.previousSibling=d,d?d.nextSibling=r:l.firstChild=r}else this.__preBoundTemplateInfo=r;return r}static _addTemplatePropertyEffect(o,n,r){let a=o.hostProps=o.hostProps||{};a[n]=!0;let l=o.propertyEffects=o.propertyEffects||{};(l[n]=l[n]||[]).push(r)}_stampTemplate(o,n){n=n||this._bindTemplate(o,!0),Z.push(this);let r=super._stampTemplate(o,n);if(Z.pop(),n.nodeList=r.nodeList,!n.wasPreBound){let a=n.childNodes=[];for(let l=r.firstChild;l;l=l.nextSibling)a.push(l)}return r.templateInfo=n,cn(this,n),this.__dataClientsReady&&(this._runEffectsForTemplate(n,this.__data,null,!1),this._flushClients()),r}_removeBoundDom(o){const n=o.templateInfo,{previousSibling:r,nextSibling:a,parent:l}=n;r?r.nextSibling=a:l&&(l.firstChild=a),a?a.previousSibling=r:l&&(l.lastChild=r),n.nextSibling=n.previousSibling=null;let d=n.childNodes;for(let c=0;c<d.length;c++){let h=d[c];_(_(h).parentNode).removeChild(h)}}static _parseTemplateNode(o,n,r){let a=e._parseTemplateNode.call(this,o,n,r);if(o.nodeType===Node.TEXT_NODE){let l=this._parseBindings(o.textContent,n);l&&(o.textContent=Xt(l)||" ",qe(this,n,r,"text","textContent",l),a=!0)}return a}static _parseTemplateNodeAttribute(o,n,r,a,l){let d=this._parseBindings(l,n);if(d){let c=a,h="property";Yo.test(a)?h="attribute":a[a.length-1]=="$"&&(a=a.slice(0,-1),h="attribute");let u=Xt(d);return u&&h=="attribute"&&(a=="class"&&o.hasAttribute("class")&&(u+=" "+o.getAttribute(a)),o.setAttribute(a,u)),h=="attribute"&&c=="disable-upgrade$"&&o.setAttribute(a,""),o.localName==="input"&&c==="value"&&o.setAttribute(c,""),o.removeAttribute(c),h==="property"&&(a=Ri(a)),qe(this,n,r,h,a,d,u),!0}else return e._parseTemplateNodeAttribute.call(this,o,n,r,a,l)}static _parseTemplateNestedTemplate(o,n,r){let a=e._parseTemplateNestedTemplate.call(this,o,n,r);const l=o.parentNode,d=r.templateInfo,c=l.localName==="dom-if",h=l.localName==="dom-repeat";Rt&&(c||h)&&(l.removeChild(o),r=r.parentInfo,r.templateInfo=d,r.noted=!0,a=!1);let u=d.hostProps;if(ki&&c)u&&(n.hostProps=Object.assign(n.hostProps||{},u),Rt||(r.parentInfo.noted=!0));else{let p="{";for(let m in u){let P=[{mode:p,source:m,dependencies:[m],hostProp:!0}];qe(this,n,r,"property","_host_"+m,P)}}return a}static _parseBindings(o,n){let r=[],a=0,l;for(;(l=Jt.exec(o))!==null;){l.index>a&&r.push({literal:o.slice(a,l.index)});let d=l[1][0],c=Boolean(l[2]),h=l[3].trim(),u=!1,p="",m=-1;d=="{"&&(m=h.indexOf("::"))>0&&(p=h.substring(m+2),h=h.substring(0,m),u=!0);let P=$e(h),T=[];if(P){let{args:x,methodName:A}=P;for(let Ue=0;Ue<x.length;Ue++){let Ht=x[Ue];Ht.literal||T.push(Ht)}let B=n.dynamicFns;(B&&B[A]||P.static)&&(T.push(A),P.dynamicFn=!0)}else T.push(h);r.push({source:h,mode:d,negate:c,customEvent:u,signature:P,dependencies:T,event:p}),a=Jt.lastIndex}if(a&&a<o.length){let d=o.substring(a);d&&r.push({literal:d})}return r.length?r:null}static _evaluateBinding(o,n,r,a,l,d){let c;return n.signature?c=st(o,r,a,l,n.signature):r!=n.source?c=w(o,n.source):d&&tt(r)?c=w(o,r):c=o.__data[r],n.negate&&(c=!c),c}}return t}),Z=[];/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/function Ya(i){}/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/function Tn(i){const e={};for(let t in i){const s=i[t];e[t]=typeof s=="function"?{type:s}:s}return e}const On=g(i=>{const e=Vi(i);function t(n){const r=Object.getPrototypeOf(n);return r.prototype instanceof o?r:null}function s(n){if(!n.hasOwnProperty(JSCompiler_renameProperty("__ownProperties",n))){let r=null;if(n.hasOwnProperty(JSCompiler_renameProperty("properties",n))){const a=n.properties;a&&(r=Tn(a))}n.__ownProperties=r}return n.__ownProperties}class o extends e{static get observedAttributes(){if(!this.hasOwnProperty(JSCompiler_renameProperty("__observedAttributes",this))){this.prototype;const r=this._properties;this.__observedAttributes=r?Object.keys(r).map(a=>this.prototype._addPropertyToAttributeMap(a)):[]}return this.__observedAttributes}static finalize(){if(!this.hasOwnProperty(JSCompiler_renameProperty("__finalized",this))){const r=t(this);r&&r.finalize(),this.__finalized=!0,this._finalizeClass()}}static _finalizeClass(){const r=s(this);r&&this.createProperties(r)}static get _properties(){if(!this.hasOwnProperty(JSCompiler_renameProperty("__properties",this))){const r=t(this);this.__properties=Object.assign({},r&&r._properties,s(this))}return this.__properties}static typeForProperty(r){const a=this._properties[r];return a&&a.type}_initializeProperties(){this.constructor.finalize(),super._initializeProperties()}connectedCallback(){super.connectedCallback&&super.connectedCallback(),this._enableProperties()}disconnectedCallback(){super.disconnectedCallback&&super.disconnectedCallback()}}return o});/**
 * @fileoverview
 * @suppress {checkPrototypalTypes}
 * @license Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at
 * http://polymer.github.io/LICENSE.txt The complete set of authors may be found
 * at http://polymer.github.io/AUTHORS.txt The complete set of contributors may
 * be found at http://polymer.github.io/CONTRIBUTORS.txt Code distributed by
 * Google as part of the polymer project is also subject to an additional IP
 * rights grant found at http://polymer.github.io/PATENTS.txt
 */const Sn="3.5.1",Qt=window.ShadyCSS&&window.ShadyCSS.cssBuild,Nn=g(i=>{const e=On(Et(i));function t(l){if(!l.hasOwnProperty(JSCompiler_renameProperty("__propertyDefaults",l))){l.__propertyDefaults=null;let d=l._properties;for(let c in d){let h=d[c];"value"in h&&(l.__propertyDefaults=l.__propertyDefaults||{},l.__propertyDefaults[c]=h)}}return l.__propertyDefaults}function s(l){return l.hasOwnProperty(JSCompiler_renameProperty("__ownObservers",l))||(l.__ownObservers=l.hasOwnProperty(JSCompiler_renameProperty("observers",l))?l.observers:null),l.__ownObservers}function o(l,d,c,h){c.computed&&(c.readOnly=!0),c.computed&&(l._hasReadOnlyEffect(d)?console.warn(`Cannot redefine computed property '${d}'.`):l._createComputedProperty(d,c.computed,h)),c.readOnly&&!l._hasReadOnlyEffect(d)?l._createReadOnlyProperty(d,!c.computed):c.readOnly===!1&&l._hasReadOnlyEffect(d)&&console.warn(`Cannot make readOnly property '${d}' non-readOnly.`),c.reflectToAttribute&&!l._hasReflectEffect(d)?l._createReflectedProperty(d):c.reflectToAttribute===!1&&l._hasReflectEffect(d)&&console.warn(`Cannot make reflected property '${d}' non-reflected.`),c.notify&&!l._hasNotifyEffect(d)?l._createNotifyingProperty(d):c.notify===!1&&l._hasNotifyEffect(d)&&console.warn(`Cannot make notify property '${d}' non-notify.`),c.observer&&l._createPropertyObserver(d,c.observer,h[c.observer]),l._addPropertyToAttributeMap(d)}function n(l,d,c,h){if(!Qt){const u=d.content.querySelectorAll("style"),p=De(d),m=Co(c),P=d.content.firstElementChild;for(let x=0;x<m.length;x++){let A=m[x];A.textContent=l._processStyleText(A.textContent,h),d.content.insertBefore(A,P)}let T=0;for(let x=0;x<p.length;x++){let A=p[x],B=u[T];B!==A?(A=A.cloneNode(!0),B.parentNode.insertBefore(A,B)):T++,A.textContent=l._processStyleText(A.textContent,h)}}if(window.ShadyCSS&&window.ShadyCSS.prepareTemplate(d,c),_o&&Qt&&ro){const u=d.content.querySelectorAll("style");if(u){let p="";Array.from(u).forEach(m=>{p+=m.textContent,m.parentNode.removeChild(m)}),l._styleSheet=new CSSStyleSheet,l._styleSheet.replaceSync(p)}}}function r(l){let d=null;if(l&&(!de||lo)&&(d=K.import(l,"template"),de&&!d))throw new Error(`strictTemplatePolicy: expecting dom-module or null template for ${l}`);return d}class a extends e{static get polymerElementVersion(){return Sn}static _finalizeClass(){e._finalizeClass.call(this);const d=s(this);d&&this.createObservers(d,this._properties),this._prepareTemplate()}static _prepareTemplate(){let d=this.template;d&&(typeof d=="string"?(console.error("template getter must return HTMLTemplateElement"),d=null):zi||(d=d.cloneNode(!0))),this.prototype._template=d}static createProperties(d){for(let c in d)o(this.prototype,c,d[c],d)}static createObservers(d,c){const h=this.prototype;for(let u=0;u<d.length;u++)h._createMethodObserver(d[u],c)}static get template(){if(!this.hasOwnProperty(JSCompiler_renameProperty("_template",this))){let d=this.prototype.hasOwnProperty(JSCompiler_renameProperty("_template",this.prototype))?this.prototype._template:void 0;typeof d=="function"&&(d=d()),this._template=d!==void 0?d:this.hasOwnProperty(JSCompiler_renameProperty("is",this))&&r(this.is)||Object.getPrototypeOf(this.prototype).constructor.template}return this._template}static set template(d){this._template=d}static get importPath(){if(!this.hasOwnProperty(JSCompiler_renameProperty("_importPath",this))){const d=this.importMeta;if(d)this._importPath=yt(d.url);else{const c=K.import(this.is);this._importPath=c&&c.assetpath||Object.getPrototypeOf(this.prototype).constructor.importPath}}return this._importPath}constructor(){super(),this._template,this._importPath,this.rootPath,this.importPath,this.root,this.$}_initializeProperties(){this.constructor.finalize(),this.constructor._finalizeTemplate(this.localName),super._initializeProperties(),this.rootPath=ao,this.importPath=this.constructor.importPath;let d=t(this.constructor);if(!!d)for(let c in d){let h=d[c];if(this._canApplyPropertyDefault(c)){let u=typeof h.value=="function"?h.value.call(this):h.value;this._hasAccessor(c)?this._setPendingProperty(c,u,!0):this[c]=u}}}_canApplyPropertyDefault(d){return!this.hasOwnProperty(d)}static _processStyleText(d,c){return vt(d,c)}static _finalizeTemplate(d){const c=this.prototype._template;if(c&&!c.__polymerFinalized){c.__polymerFinalized=!0;const h=this.importPath,u=h?le(h):"";n(this,c,d,u),this.prototype._bindTemplate(c)}}connectedCallback(){window.ShadyCSS&&this._template&&window.ShadyCSS.styleElement(this),super.connectedCallback()}ready(){this._template&&(this.root=this._stampTemplate(this._template),this.$=this.root.$),super.ready()}_readyClients(){this._template&&(this.root=this._attachDom(this.root)),super._readyClients()}_attachDom(d){const c=_(this);if(c.attachShadow)return d?(c.shadowRoot||(c.attachShadow({mode:"open",shadyUpgradeFragment:d}),c.shadowRoot.appendChild(d),this.constructor._styleSheet&&(c.shadowRoot.adoptedStyleSheets=[this.constructor._styleSheet])),co&&window.ShadyDOM&&window.ShadyDOM.flushInitial(c.shadowRoot),c.shadowRoot):null;throw new Error("ShadowDOM not available. PolymerElement can create dom as children instead of in ShadowDOM by setting `this.root = this;` before `ready`.")}updateStyles(d){window.ShadyCSS&&window.ShadyCSS.styleSubtree(this,d)}resolveUrl(d,c){return!c&&this.importPath&&(c=le(this.importPath)),le(d,c)}static _parseTemplateContent(d,c,h){return c.dynamicFns=c.dynamicFns||this._properties,e._parseTemplateContent.call(this,d,c,h)}static _addTemplatePropertyEffect(d,c,h){return Mi&&!(c in this._properties)&&!(h.info.part.signature&&h.info.part.signature.static)&&!h.info.part.hostProp&&!d.nestedTemplate&&console.warn(`Property '${c}' used in template but not declared in 'properties'; attribute will not be observed.`),e._addTemplatePropertyEffect.call(this,d,c,h)}}return a});/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const ei=window.trustedTypes&&trustedTypes.createPolicy("polymer-html-literal",{createHTML:i=>i});class Ji{constructor(e,t){Zi(e,t);const s=t.reduce((o,n,r)=>o+Xi(n)+e[r+1],e[0]);this.value=s.toString()}toString(){return this.value}}function Xi(i){if(i instanceof Ji)return i.value;throw new Error(`non-literal value passed to Polymer's htmlLiteral function: ${i}`)}function In(i){if(i instanceof HTMLTemplateElement)return i.innerHTML;if(i instanceof Ji)return Xi(i);throw new Error(`non-template value passed to Polymer's html function: ${i}`)}const C=function(e,...t){Zi(e,t);const s=document.createElement("template");let o=t.reduce((n,r,a)=>n+In(r)+e[a+1],e[0]);return ei&&(o=ei.createHTML(o)),s.innerHTML=o,s},Zi=(i,e)=>{if(!Array.isArray(i)||!Array.isArray(i.raw)||e.length!==i.length-1)throw new TypeError("Invalid call to the html template tag")};/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const b=Nn(HTMLElement);/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Re=g(i=>class extends i{constructor(){super(),this.__controllers=new Set}connectedCallback(){super.connectedCallback(),this.__controllers.forEach(t=>{t.hostConnected&&t.hostConnected()})}disconnectedCallback(){super.disconnectedCallback(),this.__controllers.forEach(t=>{t.hostDisconnected&&t.hostDisconnected()})}addController(t){this.__controllers.add(t),this.$!==void 0&&this.isConnected&&t.hostConnected&&t.hostConnected()}removeController(t){this.__controllers.delete(t)}}),zn=/\/\*\*\s+vaadin-dev-mode:start([\s\S]*)vaadin-dev-mode:end\s+\*\*\//i,Te=window.Vaadin&&window.Vaadin.Flow&&window.Vaadin.Flow.clients;function Mn(){function i(){return!0}return Qi(i)}function kn(){try{return Ln()?!0:Dn()?Te?!Hn():!Mn():!1}catch{return!1}}function Ln(){return localStorage.getItem("vaadin.developmentmode.force")}function Dn(){return["localhost","127.0.0.1"].indexOf(window.location.hostname)>=0}function Hn(){return!!(Te&&Object.keys(Te).map(e=>Te[e]).filter(e=>e.productionMode).length>0)}function Qi(i,e){if(typeof i!="function")return;const t=zn.exec(i.toString());if(t)try{i=new Function(t[1])}catch(s){console.log("vaadin-development-mode-detector: uncommentAndRun() failed",s)}return i(e)}window.Vaadin=window.Vaadin||{};const ti=function(i,e){if(window.Vaadin.developmentMode)return Qi(i,e)};window.Vaadin.developmentMode===void 0&&(window.Vaadin.developmentMode=kn());function Rn(){}const Fn=function(){if(typeof ti=="function")return ti(Rn)};/**
 * @license
 * Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
 * This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
 * The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
 * The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
 * Code distributed by Google as part of the polymer project is also
 * subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
 */let ii=0,es=0;const $=[];let si=0,ot=!1;const ts=document.createTextNode("");new window.MutationObserver(Bn).observe(ts,{characterData:!0});function Bn(){ot=!1;const i=$.length;for(let e=0;e<i;e++){const t=$[e];if(t)try{t()}catch(s){setTimeout(()=>{throw s})}}$.splice(0,i),es+=i}const is={after(i){return{run(e){return window.setTimeout(e,i)},cancel(e){window.clearTimeout(e)}}},run(i,e){return window.setTimeout(i,e)},cancel(i){window.clearTimeout(i)}},Ga={run(i){return window.requestAnimationFrame(i)},cancel(i){window.cancelAnimationFrame(i)}},Vn={run(i){return window.requestIdleCallback?window.requestIdleCallback(i):window.setTimeout(i,16)},cancel(i){window.cancelIdleCallback?window.cancelIdleCallback(i):window.clearTimeout(i)}},Un={run(i){ot||(ot=!0,ts.textContent=si,si+=1),$.push(i);const e=ii;return ii+=1,e},cancel(i){const e=i-es;if(e>=0){if(!$[e])throw new Error(`invalid async handle: ${i}`);$[e]=null}}};/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/class J{static debounce(e,t,s){return e instanceof J?e._cancelAsync():e=new J,e.setConfig(t,s),e}constructor(){this._asyncModule=null,this._callback=null,this._timer=null}setConfig(e,t){this._asyncModule=e,this._callback=t,this._timer=this._asyncModule.run(()=>{this._timer=null,pe.delete(this),this._callback()})}cancel(){this.isActive()&&(this._cancelAsync(),pe.delete(this))}_cancelAsync(){this.isActive()&&(this._asyncModule.cancel(this._timer),this._timer=null)}flush(){this.isActive()&&(this.cancel(),this._callback())}isActive(){return this._timer!=null}}let pe=new Set;function jn(i){pe.add(i)}function qn(){const i=Boolean(pe.size);return pe.forEach(e=>{try{e.flush()}catch(t){setTimeout(()=>{throw t})}}),i}const Ka=()=>{let i;do i=qn();while(i)};/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class Ye{static detectScrollType(){const e=document.createElement("div");e.textContent="ABCD",e.dir="rtl",e.style.fontSize="14px",e.style.width="4px",e.style.height="1px",e.style.position="absolute",e.style.top="-1000px",e.style.overflow="scroll",document.body.appendChild(e);let t="reverse";return e.scrollLeft>0?t="default":(e.scrollLeft=2,e.scrollLeft<2&&(t="negative")),document.body.removeChild(e),t}static getNormalizedScrollLeft(e,t,s){const{scrollLeft:o}=s;if(t!=="rtl"||!e)return o;switch(e){case"negative":return s.scrollWidth-s.clientWidth+o;case"reverse":return s.scrollWidth-s.clientWidth-o;default:return o}}static setNormalizedScrollLeft(e,t,s,o){if(t!=="rtl"||!e){s.scrollLeft=o;return}switch(e){case"negative":s.scrollLeft=s.clientWidth-s.scrollWidth+o;break;case"reverse":s.scrollLeft=s.scrollWidth-s.clientWidth-o;break;default:s.scrollLeft=o;break}}}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const S=[];function $n(){const i=rt();S.forEach(e=>{nt(e,i)})}let xe;const Yn=new MutationObserver($n);Yn.observe(document.documentElement,{attributes:!0,attributeFilter:["dir"]});function nt(i,e,t=i.getAttribute("dir")){e?i.setAttribute("dir",e):t!=null&&i.removeAttribute("dir")}function rt(){return document.documentElement.getAttribute("dir")}const Pt=i=>class extends i{static get properties(){return{dir:{type:String,value:"",reflectToAttribute:!0,converter:{fromAttribute:t=>t||"",toAttribute:t=>t===""?null:t}}}}static finalize(){super.finalize(),xe||(xe=Ye.detectScrollType())}connectedCallback(){super.connectedCallback(),(!this.hasAttribute("dir")||this.__restoreSubscription)&&(this.__subscribe(),nt(this,rt(),null))}attributeChangedCallback(t,s,o){if(super.attributeChangedCallback(t,s,o),t!=="dir")return;const n=rt(),r=o===n&&S.indexOf(this)===-1,a=!o&&s&&S.indexOf(this)===-1;r||a?(this.__subscribe(),nt(this,n,o)):o!==n&&s===n&&this.__unsubscribe()}disconnectedCallback(){super.disconnectedCallback(),this.__restoreSubscription=S.includes(this),this.__unsubscribe()}_valueToNodeAttribute(t,s,o){o==="dir"&&s===""&&!t.hasAttribute("dir")||super._valueToNodeAttribute(t,s,o)}_attributeToProperty(t,s,o){t==="dir"&&!s?this.dir="":super._attributeToProperty(t,s,o)}__subscribe(){S.includes(this)||S.push(this)}__unsubscribe(){S.includes(this)&&S.splice(S.indexOf(this),1)}__getNormalizedScrollLeft(t){return Ye.getNormalizedScrollLeft(xe,this.getAttribute("dir")||"ltr",t)}__setNormalizedScrollLeft(t,s){return Ye.setNormalizedScrollLeft(xe,this.getAttribute("dir")||"ltr",t,s)}};/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */po(!1);window.Vaadin=window.Vaadin||{};window.Vaadin.registrations=window.Vaadin.registrations||[];window.Vaadin.developmentModeCallback=window.Vaadin.developmentModeCallback||{};window.Vaadin.developmentModeCallback["vaadin-usage-statistics"]=function(){Fn()};let We;const oi=new Set,k=i=>class extends Pt(i){static get version(){return"23.3.7"}static finalize(){super.finalize();const{is:t}=this;t&&!oi.has(t)&&(window.Vaadin.registrations.push(this),oi.add(t),window.Vaadin.developmentModeCallback&&(We=J.debounce(We,Vn,()=>{window.Vaadin.developmentModeCallback["vaadin-usage-statistics"]()}),jn(We)))}constructor(){super(),document.doctype===null&&console.warn('Vaadin components require the "standards mode" declaration. Please add <!DOCTYPE html> to the HTML document.')}};/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/function Q(i,e,t){return{index:i,removed:e,addedCount:t}}const ss=0,os=1,at=2,lt=3;function Wn(i,e,t,s,o,n){let r=n-o+1,a=t-e+1,l=new Array(r);for(let d=0;d<r;d++)l[d]=new Array(a),l[d][0]=d;for(let d=0;d<a;d++)l[0][d]=d;for(let d=1;d<r;d++)for(let c=1;c<a;c++)if(Tt(i[e+c-1],s[o+d-1]))l[d][c]=l[d-1][c-1];else{let h=l[d-1][c]+1,u=l[d][c-1]+1;l[d][c]=h<u?h:u}return l}function Gn(i){let e=i.length-1,t=i[0].length-1,s=i[e][t],o=[];for(;e>0||t>0;){if(e==0){o.push(at),t--;continue}if(t==0){o.push(lt),e--;continue}let n=i[e-1][t-1],r=i[e-1][t],a=i[e][t-1],l;r<a?l=r<n?r:n:l=a<n?a:n,l==n?(n==s?o.push(ss):(o.push(os),s=n),e--,t--):l==r?(o.push(lt),e--,s=r):(o.push(at),t--,s=a)}return o.reverse(),o}function Kn(i,e,t,s,o,n){let r=0,a=0,l,d=Math.min(t-e,n-o);if(e==0&&o==0&&(r=Jn(i,s,d)),t==i.length&&n==s.length&&(a=Xn(i,s,d-r)),e+=r,o+=r,t-=a,n-=a,t-e==0&&n-o==0)return[];if(e==t){for(l=Q(e,[],0);o<n;)l.removed.push(s[o++]);return[l]}else if(o==n)return[Q(e,[],t-e)];let c=Gn(Wn(i,e,t,s,o,n));l=void 0;let h=[],u=e,p=o;for(let m=0;m<c.length;m++)switch(c[m]){case ss:l&&(h.push(l),l=void 0),u++,p++;break;case os:l||(l=Q(u,[],0)),l.addedCount++,u++,l.removed.push(s[p]),p++;break;case at:l||(l=Q(u,[],0)),l.addedCount++,u++;break;case lt:l||(l=Q(u,[],0)),l.removed.push(s[p]),p++;break}return l&&h.push(l),h}function Jn(i,e,t){for(let s=0;s<t;s++)if(!Tt(i[s],e[s]))return s;return t}function Xn(i,e,t){let s=i.length,o=e.length,n=0;for(;n<t&&Tt(i[--s],e[--o]);)n++;return n}function Zn(i,e){return Kn(i,0,i.length,e,0,e.length)}function Tt(i,e){return i===e}/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/function V(i){return i.localName==="slot"}let Fe=class{static getFlattenedNodes(i){const e=_(i);return V(i)?(i=i,e.assignedNodes({flatten:!0})):Array.from(e.childNodes).map(t=>V(t)?(t=t,_(t).assignedNodes({flatten:!0})):[t]).reduce((t,s)=>t.concat(s),[])}constructor(i,e){this._shadyChildrenObserver=null,this._nativeChildrenObserver=null,this._connected=!1,this._target=i,this.callback=e,this._effectiveNodes=[],this._observer=null,this._scheduled=!1,this._boundSchedule=()=>{this._schedule()},this.connect(),this._schedule()}connect(){V(this._target)?this._listenSlots([this._target]):_(this._target).children&&(this._listenSlots(_(this._target).children),window.ShadyDOM?this._shadyChildrenObserver=window.ShadyDOM.observeChildren(this._target,i=>{this._processMutations(i)}):(this._nativeChildrenObserver=new MutationObserver(i=>{this._processMutations(i)}),this._nativeChildrenObserver.observe(this._target,{childList:!0}))),this._connected=!0}disconnect(){V(this._target)?this._unlistenSlots([this._target]):_(this._target).children&&(this._unlistenSlots(_(this._target).children),window.ShadyDOM&&this._shadyChildrenObserver?(window.ShadyDOM.unobserveChildren(this._shadyChildrenObserver),this._shadyChildrenObserver=null):this._nativeChildrenObserver&&(this._nativeChildrenObserver.disconnect(),this._nativeChildrenObserver=null)),this._connected=!1}_schedule(){this._scheduled||(this._scheduled=!0,xt.run(()=>this.flush()))}_processMutations(i){this._processSlotMutations(i),this.flush()}_processSlotMutations(i){if(i)for(let e=0;e<i.length;e++){let t=i[e];t.addedNodes&&this._listenSlots(t.addedNodes),t.removedNodes&&this._unlistenSlots(t.removedNodes)}}flush(){if(!this._connected)return!1;window.ShadyDOM&&ShadyDOM.flush(),this._nativeChildrenObserver?this._processSlotMutations(this._nativeChildrenObserver.takeRecords()):this._shadyChildrenObserver&&this._processSlotMutations(this._shadyChildrenObserver.takeRecords()),this._scheduled=!1;let i={target:this._target,addedNodes:[],removedNodes:[]},e=this.constructor.getFlattenedNodes(this._target),t=Zn(e,this._effectiveNodes);for(let o=0,n;o<t.length&&(n=t[o]);o++)for(let r=0,a;r<n.removed.length&&(a=n.removed[r]);r++)i.removedNodes.push(a);for(let o=0,n;o<t.length&&(n=t[o]);o++)for(let r=n.index;r<n.index+n.addedCount;r++)i.addedNodes.push(e[r]);this._effectiveNodes=e;let s=!1;return(i.addedNodes.length||i.removedNodes.length)&&(s=!0,this.callback.call(this._target,i)),s}_listenSlots(i){for(let e=0;e<i.length;e++){let t=i[e];V(t)&&t.addEventListener("slotchange",this._boundSchedule)}}_unlistenSlots(i){for(let e=0;e<i.length;e++){let t=i[e];V(t)&&t.removeEventListener("slotchange",this._boundSchedule)}}};/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */let Qn=0;function ns(){return Qn++}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class L extends EventTarget{static generateId(e,t){return`${e||"default"}-${t.localName}-${ns()}`}constructor(e,t,s,o,n){super(),this.host=e,this.slotName=t,this.slotFactory=s,this.slotInitializer=o,n&&(this.defaultId=L.generateId(t,e))}hostConnected(){if(!this.initialized){let e=this.getSlotChild();e?(this.node=e,this.initCustomNode(e)):e=this.attachDefaultNode(),this.initNode(e),this.observe(),this.initialized=!0}}attachDefaultNode(){const{host:e,slotName:t,slotFactory:s}=this;let o=this.defaultNode;return!o&&s&&(o=s(e),o instanceof Element&&(t!==""&&o.setAttribute("slot",t),this.node=o,this.defaultNode=o)),o&&e.appendChild(o),o}getSlotChild(){const{slotName:e}=this;return Array.from(this.host.childNodes).find(t=>t.nodeType===Node.ELEMENT_NODE&&t.slot===e||t.nodeType===Node.TEXT_NODE&&t.textContent.trim()&&e==="")}initNode(e){const{slotInitializer:t}=this;t&&t(this.host,e)}initCustomNode(e){}teardownNode(e){}observe(){const{slotName:e}=this,t=e===""?"slot:not([name])":`slot[name=${e}]`,s=this.host.shadowRoot.querySelector(t);this.__slotObserver=new Fe(s,o=>{const n=this.node,r=o.addedNodes.find(a=>a!==n);o.removedNodes.length&&o.removedNodes.forEach(a=>{this.teardownNode(a)}),r&&(n&&n.isConnected&&this.host.removeChild(n),this.node=r,r!==this.defaultNode&&(this.initCustomNode(r),this.initNode(r)))})}}/**
 * @license
 * Copyright (c) 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class Ot extends L{constructor(e){super(e,"tooltip"),this.setTarget(e)}initCustomNode(e){e.target=this.target,this.context!==void 0&&(e.context=this.context),this.manual!==void 0&&(e.manual=this.manual),this.opened!==void 0&&(e.opened=this.opened),this.position!==void 0&&(e._position=this.position),this.shouldShow!==void 0&&(e.shouldShow=this.shouldShow)}setContext(e){this.context=e;const t=this.node;t&&(t.context=e)}setManual(e){this.manual=e;const t=this.node;t&&(t.manual=e)}setOpened(e){this.opened=e;const t=this.node;t&&(t.opened=e)}setPosition(e){this.position=e;const t=this.node;t&&(t._position=e)}setShouldShow(e){this.shouldShow=e;const t=this.node;t&&(t.shouldShow=e)}setTarget(e){this.target=e;const t=this.node;t&&(t.target=e)}}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const rs=g(i=>class extends i{static get properties(){return{disabled:{type:Boolean,value:!1,observer:"_disabledChanged",reflectToAttribute:!0}}}_disabledChanged(t){this._setAriaDisabled(t)}_setAriaDisabled(t){t?this.setAttribute("aria-disabled","true"):this.removeAttribute("aria-disabled")}click(){this.disabled||super.click()}});/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const er=!1,tr=i=>i,St=typeof document.head.style.touchAction=="string",Ie="__polymerGestures",Ge="__polymerGesturesHandled",dt="__polymerGesturesTouchAction",ni=25,ri=5,ir=2,sr=["mousedown","mousemove","mouseup","click"],or=[0,1,4,2],nr=function(){try{return new MouseEvent("test",{buttons:1}).buttons===1}catch{return!1}}();function Nt(i){return sr.indexOf(i)>-1}let as=!1;(function(){try{const i=Object.defineProperty({},"passive",{get(){as=!0}});window.addEventListener("test",null,i),window.removeEventListener("test",null,i)}catch{}})();function ls(i){if(!(Nt(i)||i==="touchend")&&St&&as&&er)return{passive:!0}}const rr=navigator.userAgent.match(/iP(?:[oa]d|hone)|Android/),ar={button:!0,command:!0,fieldset:!0,input:!0,keygen:!0,optgroup:!0,option:!0,select:!0,textarea:!0};function F(i){const e=i.type;if(!Nt(e))return!1;if(e==="mousemove"){let s=i.buttons===void 0?1:i.buttons;return i instanceof window.MouseEvent&&!nr&&(s=or[i.which]||0),Boolean(s&1)}return(i.button===void 0?0:i.button)===0}function lr(i){if(i.type==="click"){if(i.detail===0)return!0;const e=D(i);if(!e.nodeType||e.nodeType!==Node.ELEMENT_NODE)return!0;const t=e.getBoundingClientRect(),s=i.pageX,o=i.pageY;return!(s>=t.left&&s<=t.right&&o>=t.top&&o<=t.bottom)}return!1}const N={mouse:{target:null,mouseIgnoreJob:null},touch:{x:0,y:0,id:-1,scrollDecided:!1}};function dr(i){let e="auto";const t=cs(i);for(let s=0,o;s<t.length;s++)if(o=t[s],o[dt]){e=o[dt];break}return e}function ds(i,e,t){i.movefn=e,i.upfn=t,document.addEventListener("mousemove",e),document.addEventListener("mouseup",t)}function Y(i){document.removeEventListener("mousemove",i.movefn),document.removeEventListener("mouseup",i.upfn),i.movefn=null,i.upfn=null}const cs=window.ShadyDOM&&window.ShadyDOM.noPatch?window.ShadyDOM.composedPath:i=>i.composedPath&&i.composedPath()||[],ve={},R=[];function cr(i,e){let t=document.elementFromPoint(i,e),s=t;for(;s&&s.shadowRoot&&!window.ShadyDOM;){const o=s;if(s=s.shadowRoot.elementFromPoint(i,e),o===s)break;s&&(t=s)}return t}function D(i){const e=cs(i);return e.length>0?e[0]:i.target}function hs(i){const e=i.type,s=i.currentTarget[Ie];if(!s)return;const o=s[e];if(!o)return;if(!i[Ge]&&(i[Ge]={},e.startsWith("touch"))){const r=i.changedTouches[0];if(e==="touchstart"&&i.touches.length===1&&(N.touch.id=r.identifier),N.touch.id!==r.identifier)return;St||(e==="touchstart"||e==="touchmove")&&hr(i)}const n=i[Ge];if(!n.skip){for(let r=0,a;r<R.length;r++)a=R[r],o[a.name]&&!n[a.name]&&a.flow&&a.flow.start.indexOf(i.type)>-1&&a.reset&&a.reset();for(let r=0,a;r<R.length;r++)a=R[r],o[a.name]&&!n[a.name]&&(n[a.name]=!0,a[e](i))}}function hr(i){const e=i.changedTouches[0],t=i.type;if(t==="touchstart")N.touch.x=e.clientX,N.touch.y=e.clientY,N.touch.scrollDecided=!1;else if(t==="touchmove"){if(N.touch.scrollDecided)return;N.touch.scrollDecided=!0;const s=dr(i);let o=!1;const n=Math.abs(N.touch.x-e.clientX),r=Math.abs(N.touch.y-e.clientY);i.cancelable&&(s==="none"?o=!0:s==="pan-x"?o=r>n:s==="pan-y"&&(o=n>r)),o?i.preventDefault():ze("track")}}function ai(i,e,t){return ve[e]?(ur(i,e,t),!0):!1}function Qa(i,e,t){return ve[e]?(pr(i,e,t),!0):!1}function ur(i,e,t){const s=ve[e],o=s.deps,n=s.name;let r=i[Ie];r||(i[Ie]=r={});for(let a=0,l,d;a<o.length;a++)l=o[a],!(rr&&Nt(l)&&l!=="click")&&(d=r[l],d||(r[l]=d={_count:0}),d._count===0&&i.addEventListener(l,hs,ls(l)),d[n]=(d[n]||0)+1,d._count=(d._count||0)+1);i.addEventListener(e,t),s.touchAction&&_r(i,s.touchAction)}function pr(i,e,t){const s=ve[e],o=s.deps,n=s.name,r=i[Ie];if(r)for(let a=0,l,d;a<o.length;a++)l=o[a],d=r[l],d&&d[n]&&(d[n]=(d[n]||1)-1,d._count=(d._count||1)-1,d._count===0&&i.removeEventListener(l,hs,ls(l)));i.removeEventListener(e,t)}function It(i){R.push(i);for(let e=0;e<i.emits.length;e++)ve[i.emits[e]]=i}function fr(i){for(let e=0,t;e<R.length;e++){t=R[e];for(let s=0,o;s<t.emits.length;s++)if(o=t.emits[s],o===i)return t}return null}function _r(i,e){St&&i instanceof HTMLElement&&Un.run(()=>{i.style.touchAction=e}),i[dt]=e}function zt(i,e,t){const s=new Event(e,{bubbles:!0,cancelable:!0,composed:!0});if(s.detail=t,tr(i).dispatchEvent(s),s.defaultPrevented){const o=t.preventer||t.sourceEvent;o&&o.preventDefault&&o.preventDefault()}}function ze(i){const e=fr(i);e.info&&(e.info.prevent=!0)}It({name:"downup",deps:["mousedown","touchstart","touchend"],flow:{start:["mousedown","touchstart"],end:["mouseup","touchend"]},emits:["down","up"],info:{movefn:null,upfn:null},reset(){Y(this.info)},mousedown(i){if(!F(i))return;const e=D(i),t=this,s=n=>{F(n)||(ee("up",e,n),Y(t.info))},o=n=>{F(n)&&ee("up",e,n),Y(t.info)};ds(this.info,s,o),ee("down",e,i)},touchstart(i){ee("down",D(i),i.changedTouches[0],i)},touchend(i){ee("up",D(i),i.changedTouches[0],i)}});function ee(i,e,t,s){!e||zt(e,i,{x:t.clientX,y:t.clientY,sourceEvent:t,preventer:s,prevent(o){return ze(o)}})}It({name:"track",touchAction:"none",deps:["mousedown","touchstart","touchmove","touchend"],flow:{start:["mousedown","touchstart"],end:["mouseup","touchend"]},emits:["track"],info:{x:0,y:0,state:"start",started:!1,moves:[],addMove(i){this.moves.length>ir&&this.moves.shift(),this.moves.push(i)},movefn:null,upfn:null,prevent:!1},reset(){this.info.state="start",this.info.started=!1,this.info.moves=[],this.info.x=0,this.info.y=0,this.info.prevent=!1,Y(this.info)},mousedown(i){if(!F(i))return;const e=D(i),t=this,s=n=>{const r=n.clientX,a=n.clientY;li(t.info,r,a)&&(t.info.state=t.info.started?n.type==="mouseup"?"end":"track":"start",t.info.state==="start"&&ze("tap"),t.info.addMove({x:r,y:a}),F(n)||(t.info.state="end",Y(t.info)),e&&Ke(t.info,e,n),t.info.started=!0)},o=n=>{t.info.started&&s(n),Y(t.info)};ds(this.info,s,o),this.info.x=i.clientX,this.info.y=i.clientY},touchstart(i){const e=i.changedTouches[0];this.info.x=e.clientX,this.info.y=e.clientY},touchmove(i){const e=D(i),t=i.changedTouches[0],s=t.clientX,o=t.clientY;li(this.info,s,o)&&(this.info.state==="start"&&ze("tap"),this.info.addMove({x:s,y:o}),Ke(this.info,e,t),this.info.state="track",this.info.started=!0)},touchend(i){const e=D(i),t=i.changedTouches[0];this.info.started&&(this.info.state="end",this.info.addMove({x:t.clientX,y:t.clientY}),Ke(this.info,e,t))}});function li(i,e,t){if(i.prevent)return!1;if(i.started)return!0;const s=Math.abs(i.x-e),o=Math.abs(i.y-t);return s>=ri||o>=ri}function Ke(i,e,t){if(!e)return;const s=i.moves[i.moves.length-2],o=i.moves[i.moves.length-1],n=o.x-i.x,r=o.y-i.y;let a,l=0;s&&(a=o.x-s.x,l=o.y-s.y),zt(e,"track",{state:i.state,x:t.clientX,y:t.clientY,dx:n,dy:r,ddx:a,ddy:l,sourceEvent:t,hover(){return cr(t.clientX,t.clientY)}})}It({name:"tap",deps:["mousedown","click","touchstart","touchend"],flow:{start:["mousedown","touchstart"],end:["click","touchend"]},emits:["tap"],info:{x:NaN,y:NaN,prevent:!1},reset(){this.info.x=NaN,this.info.y=NaN,this.info.prevent=!1},mousedown(i){F(i)&&(this.info.x=i.clientX,this.info.y=i.clientY)},click(i){F(i)&&di(this.info,i)},touchstart(i){const e=i.changedTouches[0];this.info.x=e.clientX,this.info.y=e.clientY},touchend(i){di(this.info,i.changedTouches[0],i)}});function di(i,e,t){const s=Math.abs(e.clientX-i.x),o=Math.abs(e.clientY-i.y),n=D(t||e);!n||ar[n.localName]&&n.hasAttribute("disabled")||(isNaN(s)||isNaN(o)||s<=ni&&o<=ni||lr(e))&&(i.prevent||zt(n,"tap",{x:e.clientX,y:e.clientY,sourceEvent:e,preventer:t}))}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const us=g(i=>class extends i{ready(){super.ready(),this.addEventListener("keydown",t=>{this._onKeyDown(t)}),this.addEventListener("keyup",t=>{this._onKeyUp(t)})}_onKeyDown(t){switch(t.key){case"Enter":this._onEnter(t);break;case"Escape":this._onEscape(t);break}}_onKeyUp(t){}_onEnter(t){}_onEscape(t){}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const mr=i=>class extends rs(us(i)){get _activeKeys(){return[" "]}ready(){super.ready(),ai(this,"down",t=>{this._shouldSetActive(t)&&this._setActive(!0)}),ai(this,"up",()=>{this._setActive(!1)})}disconnectedCallback(){super.disconnectedCallback(),this._setActive(!1)}_shouldSetActive(t){return!this.disabled}_onKeyDown(t){super._onKeyDown(t),this._shouldSetActive(t)&&this._activeKeys.includes(t.key)&&(this._setActive(!0),document.addEventListener("keyup",s=>{this._activeKeys.includes(s.key)&&this._setActive(!1)},{once:!0}))}_setActive(t){this.toggleAttribute("active",t)}};/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */let Mt=!1;window.addEventListener("keydown",()=>{Mt=!0},{capture:!0});window.addEventListener("mousedown",()=>{Mt=!1},{capture:!0});function ps(){return Mt}function fs(i){const e=i.style;if(e.visibility==="hidden"||e.display==="none")return!0;const t=window.getComputedStyle(i);return t.visibility==="hidden"||t.display==="none"}function gr(i){if(!br(i))return-1;const e=i.getAttribute("tabindex")||0;return Number(e)}function vr(i,e){const t=Math.max(i.tabIndex,0),s=Math.max(e.tabIndex,0);return t===0||s===0?s>t:t>s}function yr(i,e){const t=[];for(;i.length>0&&e.length>0;)vr(i[0],e[0])?t.push(e.shift()):t.push(i.shift());return t.concat(i,e)}function ct(i){const e=i.length;if(e<2)return i;const t=Math.ceil(e/2),s=ct(i.slice(0,t)),o=ct(i.slice(t));return yr(s,o)}function _s(i,e){if(i.nodeType!==Node.ELEMENT_NODE||fs(i))return!1;const t=i,s=gr(t);let o=s>0;s>=0&&e.push(t);let n=[];return t.localName==="slot"?n=t.assignedNodes({flatten:!0}):n=(t.shadowRoot||t).children,[...n].forEach(r=>{o=_s(r,e)||o}),o}function il(i){return i.offsetParent===null?!0:fs(i)}function br(i){return i.matches('[tabindex="-1"]')?!1:i.matches("input, select, textarea, button, object")?i.matches(":not([disabled])"):i.matches("a[href], area[href], iframe, [tabindex], [contentEditable]")}function wr(i){return i.getRootNode().activeElement===i}function Cr(i){const e=[];return _s(i,e)?ct(e):e}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const ms=g(i=>class extends i{get _keyboardActive(){return ps()}ready(){this.addEventListener("focusin",t=>{this._shouldSetFocus(t)&&this._setFocused(!0)}),this.addEventListener("focusout",t=>{this._shouldRemoveFocus(t)&&this._setFocused(!1)}),super.ready()}disconnectedCallback(){super.disconnectedCallback(),this.hasAttribute("focused")&&this._setFocused(!1)}_setFocused(t){this.toggleAttribute("focused",t),this.toggleAttribute("focus-ring",t&&this._keyboardActive)}_shouldSetFocus(t){return!0}_shouldRemoveFocus(t){return!0}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const gs=i=>class extends rs(i){static get properties(){return{tabindex:{type:Number,reflectToAttribute:!0,observer:"_tabindexChanged"},_lastTabIndex:{type:Number}}}_disabledChanged(t,s){super._disabledChanged(t,s),t?(this.tabindex!==void 0&&(this._lastTabIndex=this.tabindex),this.tabindex=-1):s&&(this.tabindex=this._lastTabIndex)}_tabindexChanged(t){this.disabled&&t!==-1&&(this._lastTabIndex=t,this.tabindex=-1)}};/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const xr=i=>class extends mr(gs(ms(i))){static get properties(){return{tabindex:{value:0}}}get _activeKeys(){return["Enter"," "]}ready(){super.ready(),this.hasAttribute("role")||this.setAttribute("role","button")}_onKeyDown(t){super._onKeyDown(t),this._activeKeys.includes(t.key)&&(t.preventDefault(),this.click())}};/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class ht extends xr(k(z(Re(b)))){static get is(){return"vaadin-button"}static get template(){return C`
      <style>
        :host {
          display: inline-block;
          position: relative;
          outline: none;
          white-space: nowrap;
          -webkit-user-select: none;
          -moz-user-select: none;
          user-select: none;
        }

        :host([hidden]) {
          display: none !important;
        }

        /* Aligns the button with form fields when placed on the same line.
          Note, to make it work, the form fields should have the same "::before" pseudo-element. */
        .vaadin-button-container::before {
          content: '\\2003';
          display: inline-block;
          width: 0;
          max-height: 100%;
        }

        .vaadin-button-container {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          text-align: center;
          width: 100%;
          height: 100%;
          min-height: inherit;
          text-shadow: inherit;
        }

        [part='prefix'],
        [part='suffix'] {
          flex: none;
        }

        [part='label'] {
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
      </style>
      <div class="vaadin-button-container">
        <span part="prefix" aria-hidden="true">
          <slot name="prefix"></slot>
        </span>
        <span part="label">
          <slot></slot>
        </span>
        <span part="suffix" aria-hidden="true">
          <slot name="suffix"></slot>
        </span>
      </div>
      <slot name="tooltip"></slot>
    `}ready(){super.ready(),this._tooltipController=new Ot(this),this.addController(this._tooltipController)}}customElements.define(ht.is,ht);const Ar=v`
  :host([theme~='margin']) {
    margin: var(--lumo-space-m);
  }

  :host([theme~='padding']) {
    padding: var(--lumo-space-m);
  }

  :host([theme~='spacing-xs']) {
    gap: var(--lumo-space-xs);
  }

  :host([theme~='spacing-s']) {
    gap: var(--lumo-space-s);
  }

  :host([theme~='spacing']) {
    gap: var(--lumo-space-m);
  }

  :host([theme~='spacing-l']) {
    gap: var(--lumo-space-l);
  }

  :host([theme~='spacing-xl']) {
    gap: var(--lumo-space-xl);
  }
`;y("vaadin-horizontal-layout",Ar,{moduleId:"lumo-horizontal-layout"});/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class ci extends k(z(b)){static get template(){return C`
      <style>
        :host {
          display: flex;
          box-sizing: border-box;
        }

        :host([hidden]) {
          display: none !important;
        }

        /* Theme variations */
        :host([theme~='margin']) {
          margin: 1em;
        }

        :host([theme~='padding']) {
          padding: 1em;
        }

        :host([theme~='spacing']) {
          gap: 1em;
        }
      </style>

      <slot></slot>
    `}static get is(){return"vaadin-horizontal-layout"}}customElements.define(ci.is,ci);y("vaadin-notification-card",v`
    :host {
      position: relative;
      margin: var(--lumo-space-s);
    }

    [part='overlay'] {
      background: var(--lumo-base-color) linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
      border-radius: var(--lumo-border-radius-l);
      box-shadow: 0 0 0 1px var(--lumo-contrast-10pct), var(--lumo-box-shadow-l);
      font-family: var(--lumo-font-family);
      font-size: var(--lumo-font-size-m);
      font-weight: 400;
      line-height: var(--lumo-line-height-s);
      letter-spacing: 0;
      text-transform: none;
      -webkit-text-size-adjust: 100%;
      -webkit-font-smoothing: antialiased;
      -moz-osx-font-smoothing: grayscale;
    }

    [part='content'] {
      padding: var(--lumo-space-wide-l);
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    [part='content'] ::slotted(vaadin-button) {
      flex: none;
      margin: 0 calc(var(--lumo-space-s) * -1) 0 var(--lumo-space-m);
    }

    :host([slot^='middle']) {
      max-width: 80vw;
      margin: var(--lumo-space-s) auto;
    }

    :host([slot\$='stretch']) {
      margin: 0;
    }

    :host([slot\$='stretch']) [part='overlay'] {
      border-radius: 0;
    }

    @media (min-width: 421px) {
      :host(:not([slot\$='stretch'])) {
        display: flex;
      }

      :host([slot\$='end']) {
        justify-content: flex-end;
      }

      :host([slot^='middle']),
      :host([slot\$='center']) {
        display: flex;
        justify-content: center;
      }
    }

    @keyframes lumo-notification-exit-fade-out {
      100% {
        opacity: 0;
      }
    }

    @keyframes lumo-notification-enter-fade-in {
      0% {
        opacity: 0;
      }
    }

    @keyframes lumo-notification-enter-slide-down {
      0% {
        transform: translateY(-200%);
        opacity: 0;
      }
    }

    @keyframes lumo-notification-exit-slide-up {
      100% {
        transform: translateY(-200%);
        opacity: 0;
      }
    }

    @keyframes lumo-notification-enter-slide-up {
      0% {
        transform: translateY(200%);
        opacity: 0;
      }
    }

    @keyframes lumo-notification-exit-slide-down {
      100% {
        transform: translateY(200%);
        opacity: 0;
      }
    }

    :host([slot='middle'][opening]) {
      animation: lumo-notification-enter-fade-in 300ms;
    }

    :host([slot='middle'][closing]) {
      animation: lumo-notification-exit-fade-out 300ms;
    }

    :host([slot^='top'][opening]) {
      animation: lumo-notification-enter-slide-down 300ms;
    }

    :host([slot^='top'][closing]) {
      animation: lumo-notification-exit-slide-up 300ms;
    }

    :host([slot^='bottom'][opening]) {
      animation: lumo-notification-enter-slide-up 300ms;
    }

    :host([slot^='bottom'][closing]) {
      animation: lumo-notification-exit-slide-down 300ms;
    }

    :host([theme~='primary']) [part='overlay'] {
      background: var(--lumo-primary-color);
      color: var(--lumo-primary-contrast-color);
      box-shadow: var(--lumo-box-shadow-l);
    }

    :host([theme~='primary']) {
      --_lumo-button-background-color: var(--lumo-shade-20pct);
      --_lumo-button-color: var(--lumo-primary-contrast-color);
      --_lumo-button-primary-background-color: var(--lumo-primary-contrast-color);
      --_lumo-button-primary-color: var(--lumo-primary-text-color);
    }

    :host([theme~='contrast']) [part='overlay'] {
      background: var(--lumo-contrast);
      color: var(--lumo-base-color);
      box-shadow: var(--lumo-box-shadow-l);
    }

    :host([theme~='contrast']) {
      --_lumo-button-background-color: var(--lumo-contrast-20pct);
      --_lumo-button-color: var(--lumo-base-color);
      --_lumo-button-primary-background-color: var(--lumo-base-color);
      --_lumo-button-primary-color: var(--lumo-contrast);
    }

    :host([theme~='success']) [part='overlay'] {
      background: var(--lumo-success-color);
      color: var(--lumo-success-contrast-color);
      box-shadow: var(--lumo-box-shadow-l);
    }

    :host([theme~='success']) {
      --_lumo-button-background-color: var(--lumo-shade-20pct);
      --_lumo-button-color: var(--lumo-success-contrast-color);
      --_lumo-button-primary-background-color: var(--lumo-success-contrast-color);
      --_lumo-button-primary-color: var(--lumo-success-text-color);
    }

    :host([theme~='error']) [part='overlay'] {
      background: var(--lumo-error-color);
      color: var(--lumo-error-contrast-color);
      box-shadow: var(--lumo-box-shadow-l);
    }

    :host([theme~='error']) {
      --_lumo-button-background-color: var(--lumo-shade-20pct);
      --_lumo-button-color: var(--lumo-error-contrast-color);
      --_lumo-button-primary-background-color: var(--lumo-error-contrast-color);
      --_lumo-button-primary-color: var(--lumo-error-text-color);
    }
  `,{moduleId:"lumo-notification-card"});/**
 * @license
 * Copyright 2020 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */const Er={HTML:1,SVG:2},vs=(i,e)=>e===void 0?(i==null?void 0:i._$litType$)!==void 0:(i==null?void 0:i._$litType$)===e;/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Be=i=>i.test(navigator.userAgent),ut=i=>i.test(navigator.platform),Pr=i=>i.test(navigator.vendor),rl=Be(/Android/),al=Be(/Chrome/)&&Pr(/Google Inc/),ll=Be(/Firefox/),Tr=ut(/^iPad/)||ut(/^Mac/)&&navigator.maxTouchPoints>1,Or=ut(/^iPhone/),ys=Or||Tr,dl=Be(/^((?!chrome|android).)*safari/i),cl=(()=>{try{return document.createEvent("TouchEvent"),!0}catch{return!1}})();/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */function Sr(i){if(window.Vaadin&&window.Vaadin.templateRendererCallback){window.Vaadin.templateRendererCallback(i);return}i.querySelector("template")&&console.warn(`WARNING: <template> inside <${i.localName}> is no longer supported. Import @vaadin/polymer-legacy-adapter/template-renderer.js to enable compatibility.`)}/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class hi extends z(k(b)){static get template(){return C`
      <style>
        :host {
          position: fixed;
          z-index: 1000;
          top: 0;
          left: 0;
          bottom: 0;
          right: 0;
          box-sizing: border-box;

          display: flex;
          flex-direction: column;
          align-items: stretch;
          pointer-events: none;
        }

        [region-group] {
          flex: 1 1 0%;
          display: flex;
        }

        [region-group='top'] {
          align-items: flex-start;
        }

        [region-group='bottom'] {
          align-items: flex-end;
        }

        [region-group] > [region] {
          flex: 1 1 0%;
        }

        @media (max-width: 420px) {
          [region-group] {
            flex-direction: column;
            align-items: stretch;
          }

          [region-group='top'] {
            justify-content: flex-start;
          }

          [region-group='bottom'] {
            justify-content: flex-end;
          }

          [region-group] > [region] {
            flex: initial;
          }
        }
      </style>

      <div region="top-stretch"><slot name="top-stretch"></slot></div>
      <div region-group="top">
        <div region="top-start"><slot name="top-start"></slot></div>
        <div region="top-center"><slot name="top-center"></slot></div>
        <div region="top-end"><slot name="top-end"></slot></div>
      </div>
      <div region="middle"><slot name="middle"></slot></div>
      <div region-group="bottom">
        <div region="bottom-start"><slot name="bottom-start"></slot></div>
        <div region="bottom-center"><slot name="bottom-center"></slot></div>
        <div region="bottom-end"><slot name="bottom-end"></slot></div>
      </div>
      <div region="bottom-stretch"><slot name="bottom-stretch"></slot></div>
    `}static get is(){return"vaadin-notification-container"}static get properties(){return{opened:{type:Boolean,value:!1,observer:"_openedChanged"}}}constructor(){super(),this._boundVaadinOverlayClose=this._onVaadinOverlayClose.bind(this),ys&&(this._boundIosResizeListener=()=>this._detectIosNavbar())}_openedChanged(e){e?(document.body.appendChild(this),document.addEventListener("vaadin-overlay-close",this._boundVaadinOverlayClose),this._boundIosResizeListener&&(this._detectIosNavbar(),window.addEventListener("resize",this._boundIosResizeListener))):(document.body.removeChild(this),document.removeEventListener("vaadin-overlay-close",this._boundVaadinOverlayClose),this._boundIosResizeListener&&window.removeEventListener("resize",this._boundIosResizeListener))}_detectIosNavbar(){const e=window.innerHeight,s=window.innerWidth>e,o=document.documentElement.clientHeight;s&&o>e?this.style.bottom=`${o-e}px`:this.style.bottom="0"}_onVaadinOverlayClose(e){const t=e.detail.sourceEvent;t&&t.composedPath().indexOf(this)>=0&&e.preventDefault()}}class ui extends z(b){static get template(){return C`
      <style>
        :host {
          display: block;
        }

        [part='overlay'] {
          pointer-events: auto;
        }
      </style>

      <div part="overlay">
        <div part="content">
          <slot></slot>
        </div>
      </div>
    `}static get is(){return"vaadin-notification-card"}ready(){super.ready(),this.setAttribute("role","alert"),this.setAttribute("aria-live","polite")}}class O extends Ti(k(b)){static get template(){return C`
      <style>
        :host {
          display: none !important;
        }
      </style>
      <vaadin-notification-card theme$="[[_theme]]"> </vaadin-notification-card>
    `}static get is(){return"vaadin-notification"}static get properties(){return{duration:{type:Number,value:5e3},opened:{type:Boolean,value:!1,notify:!0,observer:"_openedChanged"},position:{type:String,value:"bottom-start",observer:"_positionChanged"},renderer:Function}}static get observers(){return["_durationChanged(duration, opened)","_rendererChanged(renderer, opened, _card)"]}static show(e,t){return vs(e)?O._createAndShowNotification(s=>{Oi(e,s)},t):O._createAndShowNotification(s=>{s.innerText=e},t)}static _createAndShowNotification(e,t){const s=document.createElement(O.is);return t&&Number.isFinite(t.duration)&&(s.duration=t.duration),t&&t.position&&(s.position=t.position),t&&t.theme&&s.setAttribute("theme",t.theme),s.renderer=e,document.body.appendChild(s),s.opened=!0,s.addEventListener("opened-changed",o=>{o.detail.value||s.remove()}),s}ready(){super.ready(),this._card=this.shadowRoot.querySelector("vaadin-notification-card"),Sr(this)}disconnectedCallback(){super.disconnectedCallback(),this.opened=!1}requestContentUpdate(){!this.renderer||this.renderer(this._card,this)}_rendererChanged(e,t,s){if(!s)return;const o=this._oldRenderer!==e;this._oldRenderer=e,o&&(s.innerHTML="",delete s._$litPart$),t&&(this._didAnimateNotificationAppend||this._animatedAppendNotificationCard(),this.requestContentUpdate())}open(){this.opened=!0}close(){this.opened=!1}get _container(){return O._container||(O._container=document.createElement("vaadin-notification-container"),document.body.appendChild(O._container)),O._container}_openedChanged(e){e?(this._container.opened=!0,this._animatedAppendNotificationCard()):this._card&&this._closeNotificationCard()}_animatedAppendNotificationCard(){if(this._card){this._card.setAttribute("opening",""),this._appendNotificationCard();const e=()=>{this._card.removeEventListener("animationend",e),this._card.removeAttribute("opening")};this._card.addEventListener("animationend",e),this._didAnimateNotificationAppend=!0}else this._didAnimateNotificationAppend=!1}_appendNotificationCard(){if(!!this._card){if(!this._container.shadowRoot.querySelector(`slot[name="${this.position}"]`)){console.warn(`Invalid alignment parameter provided: position=${this.position}`);return}this._card.slot=this.position,this._container.firstElementChild&&/top/.test(this.position)?this._container.insertBefore(this._card,this._container.firstElementChild):this._container.appendChild(this._card)}}_removeNotificationCard(){this._card.parentNode&&this._card.parentNode.removeChild(this._card),this._card.removeAttribute("closing"),this._container.opened=Boolean(this._container.firstElementChild)}_closeNotificationCard(){this._durationTimeoutId&&clearTimeout(this._durationTimeoutId),this._animatedRemoveNotificationCard()}_animatedRemoveNotificationCard(){this._card.setAttribute("closing","");const e=getComputedStyle(this._card).getPropertyValue("animation-name");if(e&&e!=="none"){const t=()=>{this._removeNotificationCard(),this._card.removeEventListener("animationend",t)};this._card.addEventListener("animationend",t)}else this._removeNotificationCard()}_positionChanged(){this.opened&&this._animatedAppendNotificationCard()}_durationChanged(e,t){t&&(clearTimeout(this._durationTimeoutId),e>0&&(this._durationTimeoutId=setTimeout(()=>this.close(),e)))}}customElements.define(hi.is,hi);customElements.define(ui.is,ui);customElements.define(O.is,O);y("vaadin-input-container",v`
    :host {
      border-radius: var(--lumo-border-radius-m);
      background-color: var(--lumo-contrast-10pct);
      padding: 0 calc(0.375em + var(--lumo-border-radius-m) / 4 - 1px);
      font-weight: 500;
      line-height: 1;
      position: relative;
      cursor: text;
      box-sizing: border-box;
    }

    /* Used for hover and activation effects */
    :host::after {
      content: '';
      position: absolute;
      top: 0;
      right: 0;
      bottom: 0;
      left: 0;
      border-radius: inherit;
      pointer-events: none;
      background-color: var(--lumo-contrast-50pct);
      opacity: 0;
      transition: transform 0.15s, opacity 0.2s;
      transform-origin: 100% 0;
    }

    ::slotted(:not([slot$='fix'])) {
      cursor: inherit;
      min-height: var(--lumo-text-field-size, var(--lumo-size-m));
      padding: 0 0.25em;
      --_lumo-text-field-overflow-mask-image: linear-gradient(to left, transparent, #000 1.25em);
      -webkit-mask-image: var(--_lumo-text-field-overflow-mask-image);
      mask-image: var(--_lumo-text-field-overflow-mask-image);
    }

    /* Read-only */
    :host([readonly]) {
      color: var(--lumo-secondary-text-color);
      background-color: transparent;
      cursor: default;
    }

    :host([readonly])::after {
      background-color: transparent;
      opacity: 1;
      border: 1px dashed var(--lumo-contrast-30pct);
    }

    /* Disabled */
    :host([disabled]) {
      background-color: var(--lumo-contrast-5pct);
    }

    :host([disabled]) ::slotted(*) {
      color: var(--lumo-disabled-text-color);
      -webkit-text-fill-color: var(--lumo-disabled-text-color);
    }

    /* Invalid */
    :host([invalid]) {
      background-color: var(--lumo-error-color-10pct);
    }

    :host([invalid])::after {
      background-color: var(--lumo-error-color-50pct);
    }

    /* Slotted icons */
    ::slotted(iron-icon),
    ::slotted(vaadin-icon) {
      color: var(--lumo-contrast-60pct);
      width: var(--lumo-icon-size-m);
      height: var(--lumo-icon-size-m);
    }

    /* Vaadin icons are based on a 16x16 grid (unlike Lumo and Material icons with 24x24), so they look too big by default */
    ::slotted(iron-icon[icon^='vaadin:']),
    ::slotted(vaadin-icon[icon^='vaadin:']) {
      padding: 0.25em;
      box-sizing: border-box !important;
    }

    /* Text align */
    :host([dir='rtl']) ::slotted(:not([slot$='fix'])) {
      --_lumo-text-field-overflow-mask-image: linear-gradient(to right, transparent, #000 1.25em);
    }

    @-moz-document url-prefix() {
      :host([dir='rtl']) ::slotted(:not([slot$='fix'])) {
        mask-image: var(--_lumo-text-field-overflow-mask-image);
      }
    }

    :host([theme~='align-left']) ::slotted(:not([slot$='fix'])) {
      text-align: start;
      --_lumo-text-field-overflow-mask-image: none;
    }

    :host([theme~='align-center']) ::slotted(:not([slot$='fix'])) {
      text-align: center;
      --_lumo-text-field-overflow-mask-image: none;
    }

    :host([theme~='align-right']) ::slotted(:not([slot$='fix'])) {
      text-align: end;
      --_lumo-text-field-overflow-mask-image: none;
    }

    @-moz-document url-prefix() {
      /* Firefox is smart enough to align overflowing text to right */
      :host([theme~='align-right']) ::slotted(:not([slot$='fix'])) {
        --_lumo-text-field-overflow-mask-image: linear-gradient(to right, transparent 0.25em, #000 1.5em);
      }
    }

    @-moz-document url-prefix() {
      /* Firefox is smart enough to align overflowing text to right */
      :host([theme~='align-left']) ::slotted(:not([slot$='fix'])) {
        --_lumo-text-field-overflow-mask-image: linear-gradient(to left, transparent 0.25em, #000 1.5em);
      }
    }

    /* RTL specific styles */
    :host([dir='rtl'])::after {
      transform-origin: 0% 0;
    }

    :host([theme~='align-left'][dir='rtl']) ::slotted(:not([slot$='fix'])) {
      --_lumo-text-field-overflow-mask-image: none;
    }

    :host([theme~='align-center'][dir='rtl']) ::slotted(:not([slot$='fix'])) {
      --_lumo-text-field-overflow-mask-image: none;
    }

    :host([theme~='align-right'][dir='rtl']) ::slotted(:not([slot$='fix'])) {
      --_lumo-text-field-overflow-mask-image: none;
    }

    @-moz-document url-prefix() {
      /* Firefox is smart enough to align overflowing text to right */
      :host([theme~='align-right'][dir='rtl']) ::slotted(:not([slot$='fix'])) {
        --_lumo-text-field-overflow-mask-image: linear-gradient(to right, transparent 0.25em, #000 1.5em);
      }
    }

    @-moz-document url-prefix() {
      /* Firefox is smart enough to align overflowing text to right */
      :host([theme~='align-left'][dir='rtl']) ::slotted(:not([slot$='fix'])) {
        --_lumo-text-field-overflow-mask-image: linear-gradient(to left, transparent 0.25em, #000 1.5em);
      }
    }
  `,{moduleId:"lumo-input-container"});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class pi extends z(Pt(b)){static get is(){return"vaadin-input-container"}static get template(){return C`
      <style>
        :host {
          display: flex;
          align-items: center;
          flex: 0 1 auto;
        }

        :host([hidden]) {
          display: none !important;
        }

        /* Reset the native input styles */
        ::slotted(input) {
          -webkit-appearance: none;
          -moz-appearance: none;
          flex: auto;
          white-space: nowrap;
          overflow: hidden;
          width: 100%;
          height: 100%;
          outline: none;
          margin: 0;
          padding: 0;
          border: 0;
          border-radius: 0;
          min-width: 0;
          font: inherit;
          line-height: normal;
          color: inherit;
          background-color: transparent;
          /* Disable default invalid style in Firefox */
          box-shadow: none;
        }

        ::slotted(*) {
          flex: none;
        }

        ::slotted(:is(input, textarea))::placeholder {
          /* Use ::slotted(input:placeholder-shown) in themes to style the placeholder. */
          /* because ::slotted(...)::placeholder does not work in Safari. */
          font: inherit;
          color: inherit;
          /* Override default opacity in Firefox */
          opacity: 1;
        }
      </style>
      <slot name="prefix"></slot>
      <slot></slot>
      <slot name="suffix"></slot>
    `}static get properties(){return{disabled:{type:Boolean,reflectToAttribute:!0},readonly:{type:Boolean,reflectToAttribute:!0},invalid:{type:Boolean,reflectToAttribute:!0}}}ready(){super.ready(),this.addEventListener("pointerdown",e=>{e.target===this&&e.preventDefault()}),this.addEventListener("click",e=>{e.target===this&&this.shadowRoot.querySelector("slot:not([name])").assignedNodes({flatten:!0}).forEach(t=>t.focus&&t.focus())})}}customElements.define(pi.is,pi);/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const bs=document.createElement("template");bs.innerHTML=`
  <style>
    @font-face {
      font-family: 'lumo-icons';
      src: url(data:application/font-woff;charset=utf-8;base64,d09GRgABAAAAABEgAAsAAAAAIjQAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAABHU1VCAAABCAAAADsAAABUIIslek9TLzIAAAFEAAAAQwAAAFZAIUuKY21hcAAAAYgAAAD4AAADrsCU8d5nbHlmAAACgAAAC2cAABeAWri7U2hlYWQAAA3oAAAAMAAAADZa/6SsaGhlYQAADhgAAAAdAAAAJAbpA35obXR4AAAOOAAAABAAAACspBAAAGxvY2EAAA5IAAAAWAAAAFh57oA4bWF4cAAADqAAAAAfAAAAIAFKAXBuYW1lAAAOwAAAATEAAAIuUUJZCHBvc3QAAA/0AAABKwAAAelm8SzVeJxjYGRgYOBiMGCwY2BycfMJYeDLSSzJY5BiYGGAAJA8MpsxJzM9kYEDxgPKsYBpDiBmg4gCACY7BUgAeJxjYGS+yDiBgZWBgamKaQ8DA0MPhGZ8wGDIyAQUZWBlZsAKAtJcUxgcXjG+0mIO+p/FEMUcxDANKMwIkgMABn8MLQB4nO3SWW6DMABF0UtwCEnIPM/zhLK8LqhfXRybSP14XUYtHV9hGYQwQBNIo3cUIPkhQeM7rib1ekqnXg981XuC1qvy84lzojleh3puxL0hPjGjRU473teloEefAUNGjJkwZcacBUtWrNmwZceeA0dOnLlw5cadB09elPGhGf+j0NTI/65KfXerT6JhqKnpRKtgOpuqaTrtKjPUlqHmhto21I7pL6i6hlqY3q7qGWrfUAeGOjTUkaGODXViqFNDnRnq3FAXhro01JWhrg11Y6hbQ90Z6t5QD4Z6NNSToZ4N9WKoV0O9GerdUB+G+jTUl6GWRvkL24BkEXictVh9bFvVFb/nxvbz+7Rf/N6zHcd2bCfP+Wic1Z9N0jpNHCD9SNqqoVBgbQoMjY+pjA4hNnWa2pV1rHSIif0DGkyT2k10Kmu1Cag6huj4ZpqYBHSqJsTEJgZCG3TaVBFv595nO3ZIv4RIrPPuvefe884599zzO/cRF8G/tgn6CFFImNgkR0ggX8wlspbhSSWSdrC5ozd30s2dw5afzvgtyz9/zG9t1hV4RtF1pXolowvtzc2z6L2aYUQM45jKH9WDTvd1LRDoDASYWhfTzTyvboXz6uZX4ARX5wrF39y+HM2+CJ8d0pkyqBIqoze3D12ez4DrFoYzxI8dWwMrDlZ2DMqQAR9AROsJU+2smlTPaTTco52BVxXa2a2+I8vvqd2dVHm1LoPeTn/AZPRYGthDYOeZjBjKoFsVGulR3lGU95SeCK44oHU7MhWUGUKZDT3oSUcG2GWuh+EDDfUYA/jhIhl0TOsJNYSEu7mQmi3UzfXwZKA4BsVsHLXQYGgJW95qEtpJ1VcW9HiTriZBlFEqxsDjA09yCNUoQxxwd7KWSTt2y3GTKifkqHRCoWZc3m11Wa/dKdFgXD4kSYfkeJBKd8KMz7J8dZn/cGRCcLGDnA2Ge3bKzcvlnTDNthFWLH7Xt80ua5FMjA4WKelWv5Xo16vHuYzpRbJhhdVlftuRK0VlR27D9lu5TF0DPBi60OrHNO0AfP/uRWvhn/U3LXICE+nh+3IHPUJ8JE6GyBjZQLbjGchlrSgYngF8zyrIF4NJD3atUcgWsWunGN/UHX5B5/yg7uF87Nqp4Gf52F3gH73DjEZNRoqCKAr9giQJp5rGJABpiVE2htNhW9R8nw0jqYjCYcY4LIjwYNScf4WN06IZnZCEqsI4cFaQbo4Z1TsZBx40YhXkHOecaYE5oY37IIQ+iJJ+UsDYSun5MuRSBRZRUUhlY2DqOGajOR6zrSU/5My6l2DnusH1GQgnw5BZP7iuYM/ahcfQ7Z8y51ddfutvuwNqWQ0cBYr8fj0U0vsHpwerVaB2sWhXT2NExi2r1KUE2tUuVMnkepVQrxTmpQrZTG4iu8he8iPyM3KcPE/+RP5KPoE2CEAKclCBzXATxkYOtUY/o961PWRqsj0chRrHFBbtrjP9/P0ven5pcbRdpL94vfsy33e5+izuwz3nFLFPVNayPZx/jdG1fOChflFRvYzsW6L18efgLrSWIgvcqnGJYi4skO4xREURjbDuxKke5v0T3Mrzkt2fi31uyZlLLrqIpEuXXsMlgw442Jb0GAxjS1DM20kBoCzHLXm/jEm0IltdcvU0fEW24jgiwwRjVd9u4NJHcIyoHJcwvyVqgqj5hqBJ1ZWSJryh9p56UWhX1XbhRbW2ZopuZWsQd5y8mEQ8M+C6xjRYxZbDKWf5AgY+Qq/l6wSPk16zDFjowYuu+wjx13mfkxbyDDxadYT/LijZyI0THB+6yfLaWsRcO82zo9mWTNtpO18qlorZoIVMwSN40tky5DOQ1MCIAe24mvlsuwIIxPb10+uXDQ4uWz/9m3rj+ql7p6bufZARuPVq5tXtsn6KwfP8Jy0TeWOyNhUJN6mhX5rkUTtUppQWEMNTqEdaCGKFYKJaQrCE4JtDLYOlNEKmO5kBTPGY2A0N2sY3+dVlo1N9ycBsIGtOjQ2p/tlZvzo0ur4v6cOh8NTospB7U/X40KahoU3bGIH97dnwmtHlYffVG3R1YOwKM2vNhrPhCT5zk64sG53oS4b31aYjqe/B7+kQiXBN+b6h21hNUPMq29B8CU4elINdygMPKF1B+WBTG7Z9ZshpN/xwEuuDQZR+nuoo4CDaAiiwXmLpmukMQyPf/JMclqgL1ixZQ/nnP2VbdUODFGt2fgBvL123rlLYu/6A9ckb7F3K0/CyBMEu6aQoPscroCcacVehvyQyCZAsizsWWBkoLC+WAiWnOksLKaeuQDzGuqSk42aiYTiJ4zf9afl17SrqaTO1f+XlZAfIuYcq7/IqYMaMrksOJ6vHkOCPDq943xcCnHqVD9pHFRpMqSPXrIua1WNs+tOz1U+ciTCDpPk+c4QYJIHnYhxP/kVPAq+ahFpVhPcHp8qyarhiF+HsBU9Hrl+UZa876fbKipL0KqB6OdUveErgtOI97fZ63ae9SvWU6k2w1JfwqnUbHsYcFCJFrC/W12zIMMirWYEHxMPs6LGYSdkSZ5TsNP9PCpwnWC3HKZ1lydNjWHC2Mn3l6vL0dHn1ldP3LTSrX+vKrBqv7KmMr8p0SR6P1NqF63or6XRlIyO90f7+kf7+myOhvt4tq7f09oUiTc2/dycGgqFQcCDRLYmi1NL7fk0CknVMxEg/cdfs/TnpJMNkgqwj17B8beVazSrVbU4lG67IZYOCnWrYy3yBR9cyWcChywos3LJBEdhhFoAdYjiw0rLGm0xU5OzoGm5/ZfmHjVZpNNg6SznzGKDdwv2cCtVn6Eaxo12cfxLprpVtTcZ6hVx6dow7Yq7e8LXO8PY9Jgjoze9yCtU5FNbegcKkQMdCbt9au/te4Ebe0jkc0ukUL32eYnTpNs20h0KpUOhZPYwVcfhZnfdqeCvDfXiuCbAoYWcXERPc/mDQD3/hdF+wK4i/xv3kYfprIpAuMkk2kW3kdtS0kBIKpZwp8KxmsCyfM1MFzAss9LBkDxRyThiaqTLwKYKJVTwmWTudMyz+yks09346MDh4m72yOxCKrt1XMlQ1qPVlTEVVQ1ofdK/sCWjtZu9qGwZ8YZ9PPWlo1IV3eW3+U0aXblP39zrt+JPf6UhEQ1rUjNBULN+utyuaDNW34kpAVuSOeMTyWbSNWnooFu+QFNWQ4d/Ox4IPWx41fP/fB/Rjeoz08ezPA9TysMtmnOXfGN7Ui3xIYLDALrlDLOP09qtJuY2OeL0+QZXdRnR1nxRVBF/SOyKKPpcrn9mWzH4rH9IidE+PTNU2182+hOgSItrE1slByS24vaLvJpxOqe4Pduf3HJkZ+jLqUz9rRzB7p8gKcgWZwV1L8JtUS5Z2JxZSOCuBoMTQihMzLbCPA0KqGMAljRQjONklW/wjnXKy8vxT/Elvm3/KiMUMOoV0/vnDYlhec0SMKtt3/kKMyOt33tj2bqxQLsTjSGLl+EAsNhCnTyRGktW55EgCn/A4PlnWn+Mg8bgZrWqHxTbPwMuyy1u5YeZF2SUM7JRhddwRgiRuxpmgJmxn9ZW7XpcF3ViX/ar6ptRpGJ0S9Adg4qhb9sI3vbL7qNJV/y4i07t5TZBiho1imFoMz3gED+CtjYUxvP4SOxov4bFoNPg5aR1e+G4UgDPoedJTpogyCJ7oYvRqoVS0MQAy+CoNEdTDUjok5ZHZL/WtjV7rFj3PKQE3iKp7ou+rIxN3b9LB1dGjeT4cvKo3FrnWpYpuaFd/h3dtV8UeKN1Y9hpR3dt4p0H/zKuPQq0kZQUIIpuDfoiETsnIk+gCWMJZUXHtE8V9LkUc2TE8vOMbO4ax/MACabzyaGXc7u3FBr11ThBdB8SIeMAlCntG2KThHSPsaj2Dc9KNyY2a0KZ7ODaTHoRiFkeYz+shZBpCS4X6471KKKnuHd84edfk5F37d1XO5bbkcltu2ZLNbvnPXiUVAnVvprJrP+NObryjxrllS65md6Tm6wzFHRR4dY3QUUjb7MgxaIixU8hspi98fl/Xc+IB4iU66eCVL9YfAfahiSUt4TONS8x0D8W7u8vd3fGWx6OXlM/U1IoU/s61PGhpyXRFa3eReq2qG56lvmYtXavCC1iN7lbiBpWxXHU+cSlztVLVz0tVN600fVsLxaVDknhYioeoXP3t4lqV1r79MAw0GCI1FTL1YIGzPL1MMlJ9ZsN9P7lvA2yr9ZFUzwzPrVgxN/x/SS+chwB4nGNgZGBgAOLPrYdY4vltvjJwM78AijDUqG5oRND/XzNPZboF5HIwMIFEAU/lC+J4nGNgZGBgDvqfBSRfMAAB81QGRgZUoA0AVvYDbwAAAHicY2BgYGB+MTQwAM8EJo8AAAAAAE4AmgDoAQoBLAFOAXABmgHEAe4CGgKcAugEmgS8BNYE8gUOBSoFegXQBf4GRAZmBrYHGAeQCBgIUghqCP4JRgm+CdoKBAo+CoQKugr0C1QLmgvAeJxjYGRgYNBmTGEQZQABJiDmAkIGhv9gPgMAGJQBvAB4nG2RPU7DMBiG3/QP0UoIBGJh8QILavozdmRo9w7d09RpUzlx5LgVvQMn4BAcgoEzcAgOwVvzSZVQbcnf48fvFysJgGt8IcJxROiG9TgauODuj5ukG+EW+UG4jR4ehTv0Q+EunjER7uEWmk+IWpc0d3gVbuAKb8JN+nfhFvlDuI17fAp36L+Fu1jgR7iHp+jF7Arbz1Nb1nO93pnEncSJFtrVuS3VKB6e5EyX2iVer9TyoOr9eux9pjJnCzW1pdfGWFU5u9WpjzfeV5PBIBMfp7aAwQ4FLPrIkbKWqDHn+67pDRK4s4lzbsEux5qHvcIIMb/nueSMyTKkE3jWFdNLHLjW2PPmMa1Hxn3GjGW/wjT0HtOG09JU4WxLk9LH2ISuiv9twJn9y8fh9uIXI+BknAAAAHicbY7ZboMwEEW5CVBCSLrv+76kfJRjTwHFsdGAG+Xvy5JUfehIHp0rnxmNN/D6ir3/a4YBhvARIMQOIowQY4wEE0yxiz3s4wCHOMIxTnCKM5zjApe4wjVucIs73OMBj3jCM17wije84wMzfHqJ0EVmUkmmJo77oOmrHvfIRZbXsTCZplTZldlgb3TYGVHProwFs11t1A57tcON2rErR3PBqcwF1/6ctI6k0GSU4JHMSS6WghdJQ99sTbfuN7QLJ9vQ37dNrgyktnIxlDYLJNuqitpRbYWKFNuyDT6pog6oOYKHtKakeakqKjHXpPwlGRcsC+OqxLIiJpXqoqqDMreG2l5bv9Ri3TRX+c23DZna9WFFgmXuO6Ps1Jm/w6ErW8N3FbHn/QC444j0AA==) format('woff');
      font-weight: normal;
      font-style: normal;
    }

    html {
      --lumo-icons-align-center: "\\ea01";
      --lumo-icons-align-left: "\\ea02";
      --lumo-icons-align-right: "\\ea03";
      --lumo-icons-angle-down: "\\ea04";
      --lumo-icons-angle-left: "\\ea05";
      --lumo-icons-angle-right: "\\ea06";
      --lumo-icons-angle-up: "\\ea07";
      --lumo-icons-arrow-down: "\\ea08";
      --lumo-icons-arrow-left: "\\ea09";
      --lumo-icons-arrow-right: "\\ea0a";
      --lumo-icons-arrow-up: "\\ea0b";
      --lumo-icons-bar-chart: "\\ea0c";
      --lumo-icons-bell: "\\ea0d";
      --lumo-icons-calendar: "\\ea0e";
      --lumo-icons-checkmark: "\\ea0f";
      --lumo-icons-chevron-down: "\\ea10";
      --lumo-icons-chevron-left: "\\ea11";
      --lumo-icons-chevron-right: "\\ea12";
      --lumo-icons-chevron-up: "\\ea13";
      --lumo-icons-clock: "\\ea14";
      --lumo-icons-cog: "\\ea15";
      --lumo-icons-cross: "\\ea16";
      --lumo-icons-download: "\\ea17";
      --lumo-icons-dropdown: "\\ea18";
      --lumo-icons-edit: "\\ea19";
      --lumo-icons-error: "\\ea1a";
      --lumo-icons-eye: "\\ea1b";
      --lumo-icons-eye-disabled: "\\ea1c";
      --lumo-icons-menu: "\\ea1d";
      --lumo-icons-minus: "\\ea1e";
      --lumo-icons-ordered-list: "\\ea1f";
      --lumo-icons-phone: "\\ea20";
      --lumo-icons-photo: "\\ea21";
      --lumo-icons-play: "\\ea22";
      --lumo-icons-plus: "\\ea23";
      --lumo-icons-redo: "\\ea24";
      --lumo-icons-reload: "\\ea25";
      --lumo-icons-search: "\\ea26";
      --lumo-icons-undo: "\\ea27";
      --lumo-icons-unordered-list: "\\ea28";
      --lumo-icons-upload: "\\ea29";
      --lumo-icons-user: "\\ea2a";
    }
  </style>
`;document.head.appendChild(bs.content);/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const ws=v`
  [part$='button'] {
    flex: none;
    width: 1em;
    height: 1em;
    line-height: 1;
    font-size: var(--lumo-icon-size-m);
    text-align: center;
    color: var(--lumo-contrast-60pct);
    transition: 0.2s color;
    cursor: var(--lumo-clickable-cursor);
  }

  [part$='button']:hover {
    color: var(--lumo-contrast-90pct);
  }

  :host([disabled]) [part$='button'],
  :host([readonly]) [part$='button'] {
    color: var(--lumo-contrast-20pct);
    cursor: default;
  }

  [part$='button']::before {
    font-family: 'lumo-icons';
    display: block;
  }
`;y("",ws,{moduleId:"lumo-field-button"});/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Nr=v`
  :host([has-helper]) [part='helper-text']::before {
    content: '';
    display: block;
    height: 0.4em;
  }

  [part='helper-text'] {
    display: block;
    color: var(--lumo-secondary-text-color);
    font-size: var(--lumo-font-size-xs);
    line-height: var(--lumo-line-height-xs);
    margin-left: calc(var(--lumo-border-radius-m) / 4);
    transition: color 0.2s;
  }

  :host(:hover:not([readonly])) [part='helper-text'] {
    color: var(--lumo-body-text-color);
  }

  :host([disabled]) [part='helper-text'] {
    color: var(--lumo-disabled-text-color);
    -webkit-text-fill-color: var(--lumo-disabled-text-color);
  }

  :host([has-helper][theme~='helper-above-field']) [part='helper-text']::before {
    display: none;
  }

  :host([has-helper][theme~='helper-above-field']) [part='helper-text']::after {
    content: '';
    display: block;
    height: 0.4em;
  }

  :host([has-helper][theme~='helper-above-field']) [part='label'] {
    order: 0;
    padding-bottom: 0.4em;
  }

  :host([has-helper][theme~='helper-above-field']) [part='helper-text'] {
    order: 1;
  }

  :host([has-helper][theme~='helper-above-field']) [part='label'] + * {
    order: 2;
  }

  :host([has-helper][theme~='helper-above-field']) [part='error-message'] {
    order: 3;
  }
`;/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Cs=v`
  [part='label'] {
    align-self: flex-start;
    color: var(--lumo-secondary-text-color);
    font-weight: 500;
    font-size: var(--lumo-font-size-s);
    margin-left: calc(var(--lumo-border-radius-m) / 4);
    transition: color 0.2s;
    line-height: 1;
    padding-right: 1em;
    padding-bottom: 0.5em;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    position: relative;
    max-width: 100%;
    box-sizing: border-box;
  }

  :host([has-label])::before {
    margin-top: calc(var(--lumo-font-size-s) * 1.5);
  }

  :host([has-label][theme~='small'])::before {
    margin-top: calc(var(--lumo-font-size-xs) * 1.5);
  }

  :host([has-label]) {
    padding-top: var(--lumo-space-m);
  }

  :host([has-label]) ::slotted([slot='tooltip']) {
    --vaadin-tooltip-offset-bottom: calc((var(--lumo-space-m) - var(--lumo-space-xs)) * -1);
  }

  :host([required]) [part='required-indicator']::after {
    content: var(--lumo-required-field-indicator, '•');
    transition: opacity 0.2s;
    color: var(--lumo-required-field-indicator-color, var(--lumo-primary-text-color));
    position: absolute;
    right: 0;
    width: 1em;
    text-align: center;
  }

  :host([invalid]) [part='required-indicator']::after {
    color: var(--lumo-required-field-indicator-color, var(--lumo-error-text-color));
  }

  [part='error-message'] {
    margin-left: calc(var(--lumo-border-radius-m) / 4);
    font-size: var(--lumo-font-size-xs);
    line-height: var(--lumo-line-height-xs);
    color: var(--lumo-error-text-color);
    will-change: max-height;
    transition: 0.4s max-height;
    max-height: 5em;
  }

  :host([has-error-message]) [part='error-message']::before,
  :host([has-error-message]) [part='error-message']::after {
    content: '';
    display: block;
    height: 0.4em;
  }

  :host(:not([invalid])) [part='error-message'] {
    max-height: 0;
    overflow: hidden;
  }

  /* RTL specific styles */

  :host([dir='rtl']) [part='label'] {
    margin-left: 0;
    margin-right: calc(var(--lumo-border-radius-m) / 4);
  }

  :host([dir='rtl']) [part='label'] {
    padding-left: 1em;
    padding-right: 0;
  }

  :host([dir='rtl']) [part='required-indicator']::after {
    right: auto;
    left: 0;
  }

  :host([dir='rtl']) [part='error-message'] {
    margin-left: 0;
    margin-right: calc(var(--lumo-border-radius-m) / 4);
  }
`;y("",Cs,{moduleId:"lumo-required-field"});/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Ir=v`
  :host {
    --lumo-text-field-size: var(--lumo-size-m);
    color: var(--lumo-body-text-color);
    font-size: var(--lumo-font-size-m);
    font-family: var(--lumo-font-family);
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    -webkit-tap-highlight-color: transparent;
    padding: var(--lumo-space-xs) 0;
  }

  :host::before {
    height: var(--lumo-text-field-size);
    box-sizing: border-box;
    display: inline-flex;
    align-items: center;
  }

  :host([focused]:not([readonly])) [part='label'] {
    color: var(--lumo-primary-text-color);
  }

  :host([focused]) [part='input-field'] ::slotted(:is(input, textarea)) {
    -webkit-mask-image: none;
    mask-image: none;
  }

  ::slotted(:is(input, textarea):placeholder-shown) {
    color: var(--lumo-secondary-text-color);
  }

  /* Hover */
  :host(:hover:not([readonly]):not([focused])) [part='label'] {
    color: var(--lumo-body-text-color);
  }

  :host(:hover:not([readonly]):not([focused])) [part='input-field']::after {
    opacity: 0.1;
  }

  /* Touch device adjustment */
  @media (pointer: coarse) {
    :host(:hover:not([readonly]):not([focused])) [part='label'] {
      color: var(--lumo-secondary-text-color);
    }

    :host(:hover:not([readonly]):not([focused])) [part='input-field']::after {
      opacity: 0;
    }

    :host(:active:not([readonly]):not([focused])) [part='input-field']::after {
      opacity: 0.2;
    }
  }

  /* Trigger when not focusing using the keyboard */
  :host([focused]:not([focus-ring]):not([readonly])) [part='input-field']::after {
    transform: scaleX(0);
    transition-duration: 0.15s, 1s;
  }

  /* Focus-ring */
  :host([focus-ring]) [part='input-field'] {
    box-shadow: 0 0 0 2px var(--lumo-primary-color-50pct);
  }

  /* Read-only and disabled */
  :host(:is([readonly], [disabled])) ::slotted(:is(input, textarea):placeholder-shown) {
    opacity: 0;
  }

  /* Disabled style */
  :host([disabled]) {
    pointer-events: none;
  }

  :host([disabled]) [part='label'],
  :host([disabled]) [part='input-field'] ::slotted(*) {
    color: var(--lumo-disabled-text-color);
    -webkit-text-fill-color: var(--lumo-disabled-text-color);
  }

  /* Invalid style */
  :host([invalid][focus-ring]) [part='input-field'] {
    box-shadow: 0 0 0 2px var(--lumo-error-color-50pct);
  }

  :host([input-prevented]) [part='input-field'] {
    animation: shake 0.15s infinite;
  }

  @keyframes shake {
    25% {
      transform: translateX(4px);
    }
    75% {
      transform: translateX(-4px);
    }
  }

  /* Small theme */
  :host([theme~='small']) {
    font-size: var(--lumo-font-size-s);
    --lumo-text-field-size: var(--lumo-size-s);
  }

  :host([theme~='small']) [part='label'] {
    font-size: var(--lumo-font-size-xs);
  }

  :host([theme~='small']) [part='error-message'] {
    font-size: var(--lumo-font-size-xxs);
  }

  /* Slotted content */
  [part='input-field'] ::slotted(:not(iron-icon):not(vaadin-icon):not(input):not(textarea)) {
    color: var(--lumo-secondary-text-color);
    font-weight: 400;
  }

  [part='clear-button']::before {
    content: var(--lumo-icons-cross);
  }
`,Ve=[Cs,ws,Nr,Ir];y("",Ve,{moduleId:"lumo-input-field-shared-styles"});/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */y("vaadin-text-field",Ve,{moduleId:"lumo-text-field-styles"});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class zr extends L{constructor(e,t){super(e,"input",()=>document.createElement("input"),(s,o)=>{s.value&&o.setAttribute("value",s.value),s.type&&o.setAttribute("type",s.type),o.id=this.defaultId,typeof t=="function"&&t(o)},!0)}}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Mr=g(i=>class extends ms(gs(i)){static get properties(){return{autofocus:{type:Boolean},focusElement:{type:Object,readOnly:!0,observer:"_focusElementChanged"},_lastTabIndex:{value:0}}}constructor(){super(),this._boundOnBlur=this._onBlur.bind(this),this._boundOnFocus=this._onFocus.bind(this)}ready(){super.ready(),this.autofocus&&!this.disabled&&requestAnimationFrame(()=>{this.focus(),this.setAttribute("focus-ring","")})}focus(){!this.focusElement||this.disabled||(this.focusElement.focus(),this._setFocused(!0))}blur(){!this.focusElement||(this.focusElement.blur(),this._setFocused(!1))}click(){this.focusElement&&!this.disabled&&this.focusElement.click()}_focusElementChanged(t,s){t?(t.disabled=this.disabled,this._addFocusListeners(t),this.__forwardTabIndex(this.tabindex)):s&&this._removeFocusListeners(s)}_addFocusListeners(t){t.addEventListener("blur",this._boundOnBlur),t.addEventListener("focus",this._boundOnFocus)}_removeFocusListeners(t){t.removeEventListener("blur",this._boundOnBlur),t.removeEventListener("focus",this._boundOnFocus)}_onFocus(t){t.stopPropagation(),this.dispatchEvent(new Event("focus"))}_onBlur(t){t.stopPropagation(),this.dispatchEvent(new Event("blur"))}_shouldSetFocus(t){return t.target===this.focusElement}_disabledChanged(t,s){super._disabledChanged(t,s),this.focusElement&&(this.focusElement.disabled=t),t&&this.blur()}_tabindexChanged(t){this.__forwardTabIndex(t)}__forwardTabIndex(t){t!==void 0&&this.focusElement&&(this.focusElement.tabIndex=t,t!==-1&&(this.tabindex=void 0)),this.disabled&&t&&(t!==-1&&(this._lastTabIndex=t),this.tabindex=void 0)}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class kr extends L{constructor(e){super(e,"error-message",()=>document.createElement("div"),(t,s)=>{this.__updateErrorId(s),this.__updateHasError()},!0)}get errorId(){return this.node&&this.node.id}setErrorMessage(e){this.errorMessage=e,this.__updateHasError()}setInvalid(e){this.invalid=e,this.__updateHasError()}initCustomNode(e){this.__updateErrorId(e),e.textContent&&!this.errorMessage&&(this.errorMessage=e.textContent.trim()),this.__updateHasError()}teardownNode(e){let t=this.getSlotChild();!t&&e!==this.defaultNode&&(t=this.attachDefaultNode(),this.initNode(t)),this.__updateHasError()}__isNotEmpty(e){return Boolean(e&&e.trim()!=="")}__updateHasError(){const e=this.node,t=Boolean(this.invalid&&this.__isNotEmpty(this.errorMessage));e&&(e.textContent=t?this.errorMessage:"",e.hidden=!t,t?e.setAttribute("role","alert"):e.removeAttribute("role")),this.host.toggleAttribute("has-error-message",t)}__updateErrorId(e){e.id||(e.id=this.defaultId)}}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */function Lr(i){const e=[];for(;i;){if(i.nodeType===Node.DOCUMENT_NODE){e.push(i);break}if(i.nodeType===Node.DOCUMENT_FRAGMENT_NODE){e.push(i),i=i.host;continue}if(i.assignedSlot){i=i.assignedSlot;continue}i=i.parentNode}return e}function xs(i){return i?new Set(i.split(" ")):new Set}function As(i){return[...i].join(" ")}function Es(i,e,t){const s=xs(i.getAttribute(e));s.add(t),i.setAttribute(e,As(s))}function Ps(i,e,t){const s=xs(i.getAttribute(e));if(s.delete(t),s.size===0){i.removeAttribute(e);return}i.setAttribute(e,As(s))}function ul(i){return i.nodeType===Node.TEXT_NODE&&i.textContent.trim()===""}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class Dr{constructor(e){this.host=e,this.__required=!1}setTarget(e){this.__target=e,this.__setAriaRequiredAttribute(this.__required),this.__setLabelIdToAriaAttribute(this.__labelId),this.__setErrorIdToAriaAttribute(this.__errorId),this.__setHelperIdToAriaAttribute(this.__helperId)}setRequired(e){this.__setAriaRequiredAttribute(e),this.__required=e}setLabelId(e){this.__setLabelIdToAriaAttribute(e,this.__labelId),this.__labelId=e}setErrorId(e){this.__setErrorIdToAriaAttribute(e,this.__errorId),this.__errorId=e}setHelperId(e){this.__setHelperIdToAriaAttribute(e,this.__helperId),this.__helperId=e}get __isGroupField(){return this.__target===this.host}__setLabelIdToAriaAttribute(e,t){this.__setAriaAttributeId("aria-labelledby",e,t)}__setErrorIdToAriaAttribute(e,t){this.__isGroupField?this.__setAriaAttributeId("aria-labelledby",e,t):this.__setAriaAttributeId("aria-describedby",e,t)}__setHelperIdToAriaAttribute(e,t){this.__isGroupField?this.__setAriaAttributeId("aria-labelledby",e,t):this.__setAriaAttributeId("aria-describedby",e,t)}__setAriaRequiredAttribute(e){!this.__target||["input","textarea"].includes(this.__target.localName)||(e?this.__target.setAttribute("aria-required","true"):this.__target.removeAttribute("aria-required"))}__setAriaAttributeId(e,t,s){!this.__target||(s&&Ps(this.__target,e,s),t&&Es(this.__target,e,t))}}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class Hr extends L{constructor(e){super(e,"helper",null,null,!0)}get helperId(){return this.node&&this.node.id}initCustomNode(e){this.__updateHelperId(e),this.__observeHelper(e);const t=this.__hasHelper(e);this.__toggleHasHelper(t)}teardownNode(e){this.__helperIdObserver&&this.__helperIdObserver.disconnect();const t=this.getSlotChild();if(t&&t!==this.defaultNode){const s=this.__hasHelper(t);this.__toggleHasHelper(s)}else this.__applyDefaultHelper(this.helperText,t)}setHelperText(e){this.helperText=e;const t=this.getSlotChild();(!t||t===this.defaultNode)&&this.__applyDefaultHelper(e,t)}__hasHelper(e){return e?e.children.length>0||e.nodeType===Node.ELEMENT_NODE&&customElements.get(e.localName)||this.__isNotEmpty(e.textContent):!1}__isNotEmpty(e){return e&&e.trim()!==""}__applyDefaultHelper(e,t){const s=this.__isNotEmpty(e);s&&!t&&(this.slotFactory=()=>document.createElement("div"),t=this.attachDefaultNode(),this.__updateHelperId(t),this.__observeHelper(t)),t&&(t.textContent=e),this.__toggleHasHelper(s)}__observeHelper(e){this.__helperObserver=new MutationObserver(t=>{t.forEach(s=>{const o=s.target,n=o===this.node;if(s.type==="attributes")n&&o.id!==this.defaultId&&this.__updateHelperId(o);else if(n||o.parentElement===this.node){const r=this.__hasHelper(this.node);this.__toggleHasHelper(r)}})}),this.__helperObserver.observe(e,{attributes:!0,attributeFilter:["id"],childList:!0,subtree:!0,characterData:!0})}__toggleHasHelper(e){this.host.toggleAttribute("has-helper",e),this.dispatchEvent(new CustomEvent("helper-changed",{detail:{hasHelper:e,node:this.node}}))}__updateHelperId(e){e.id||(e.id=this.defaultId)}}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class Rr extends L{constructor(e){super(e,"label",()=>document.createElement("label"),(t,s)=>{this.__updateLabelId(s),this.__updateDefaultLabel(this.label),this.__observeLabel(s)},!0)}get labelId(){return this.node.id}initCustomNode(e){this.__updateLabelId(e);const t=this.__hasLabel(e);this.__toggleHasLabel(t)}teardownNode(e){this.__labelObserver&&this.__labelObserver.disconnect();let t=this.getSlotChild();!t&&e!==this.defaultNode&&(t=this.attachDefaultNode(),this.initNode(t));const s=this.__hasLabel(t);this.__toggleHasLabel(s)}setLabel(e){this.label=e,this.__updateDefaultLabel(e)}__hasLabel(e){return e?e.children.length>0||this.__isNotEmpty(e.textContent):!1}__isNotEmpty(e){return Boolean(e&&e.trim()!=="")}__observeLabel(e){this.__labelObserver=new MutationObserver(t=>{t.forEach(s=>{const o=s.target,n=o===this.node;if(s.type==="attributes")n&&o.id!==this.defaultId&&this.__updateLabelId(o);else if(n||o.parentElement===this.node){const r=this.__hasLabel(this.node);this.__toggleHasLabel(r)}})}),this.__labelObserver.observe(e,{attributes:!0,attributeFilter:["id"],childList:!0,subtree:!0,characterData:!0})}__toggleHasLabel(e){this.host.toggleAttribute("has-label",e),this.dispatchEvent(new CustomEvent("label-changed",{detail:{hasLabel:e,node:this.node}}))}__updateDefaultLabel(e){if(this.defaultNode&&(this.defaultNode.textContent=e,this.defaultNode===this.node)){const t=this.__isNotEmpty(e);this.__toggleHasLabel(t)}}__updateLabelId(e){e.id||(e.id=this.defaultId)}}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Fr=g(i=>class extends Re(i){static get properties(){return{label:{type:String,observer:"_labelChanged"}}}get _labelId(){return this._labelController.labelId}get _labelNode(){return this._labelController.node}constructor(){super(),this._labelController=new Rr(this)}ready(){super.ready(),this.addController(this._labelController)}_labelChanged(t){this._labelController.setLabel(t)}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Ts=g(i=>class extends i{static get properties(){return{invalid:{type:Boolean,reflectToAttribute:!0,notify:!0,value:!1},required:{type:Boolean,reflectToAttribute:!0}}}validate(){const t=this.checkValidity();return this._setInvalid(!t),this.dispatchEvent(new CustomEvent("validated",{detail:{valid:t}})),t}checkValidity(){return!this.required||!!this.value}_setInvalid(t){this._shouldSetInvalid(t)&&(this.invalid=t)}_shouldSetInvalid(t){return!0}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Br=i=>class extends Ts(Fr(Re(i))){static get properties(){return{ariaTarget:{type:Object,observer:"_ariaTargetChanged"},errorMessage:{type:String,observer:"_errorMessageChanged"},helperText:{type:String,observer:"_helperTextChanged"}}}static get observers(){return["_invalidChanged(invalid)","_requiredChanged(required)"]}get _errorId(){return this._errorController.errorId}get _errorNode(){return this._errorController.node}get _helperId(){return this._helperController.helperId}get _helperNode(){return this._helperController.node}constructor(){super(),this._fieldAriaController=new Dr(this),this._helperController=new Hr(this),this._errorController=new kr(this),this._labelController.addEventListener("label-changed",t=>{const{hasLabel:s,node:o}=t.detail;this.__labelChanged(s,o)}),this._helperController.addEventListener("helper-changed",t=>{const{hasHelper:s,node:o}=t.detail;this.__helperChanged(s,o)})}ready(){super.ready(),this.addController(this._fieldAriaController),this.addController(this._helperController),this.addController(this._errorController)}__helperChanged(t,s){t?this._fieldAriaController.setHelperId(s.id):this._fieldAriaController.setHelperId(null)}__labelChanged(t,s){t?this._fieldAriaController.setLabelId(s.id):this._fieldAriaController.setLabelId(null)}_errorMessageChanged(t){this._errorController.setErrorMessage(t)}_helperTextChanged(t){this._helperController.setHelperText(t)}_ariaTargetChanged(t){t&&this._fieldAriaController.setTarget(t)}_requiredChanged(t){this._fieldAriaController.setRequired(t)}_invalidChanged(t){this._errorController.setInvalid(t),setTimeout(()=>{t?this._fieldAriaController.setErrorId(this._errorController.errorId):this._fieldAriaController.setErrorId(null)})}};/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Vr=g(i=>class extends i{static get properties(){return{stateTarget:{type:Object,observer:"_stateTargetChanged"}}}static get delegateAttrs(){return[]}static get delegateProps(){return[]}ready(){super.ready(),this._createDelegateAttrsObserver(),this._createDelegatePropsObserver()}_stateTargetChanged(t){t&&(this._ensureAttrsDelegated(),this._ensurePropsDelegated())}_createDelegateAttrsObserver(){this._createMethodObserver(`_delegateAttrsChanged(${this.constructor.delegateAttrs.join(", ")})`)}_createDelegatePropsObserver(){this._createMethodObserver(`_delegatePropsChanged(${this.constructor.delegateProps.join(", ")})`)}_ensureAttrsDelegated(){this.constructor.delegateAttrs.forEach(t=>{this._delegateAttribute(t,this[t])})}_ensurePropsDelegated(){this.constructor.delegateProps.forEach(t=>{this._delegateProperty(t,this[t])})}_delegateAttrsChanged(...t){this.constructor.delegateAttrs.forEach((s,o)=>{this._delegateAttribute(s,t[o])})}_delegatePropsChanged(...t){this.constructor.delegateProps.forEach((s,o)=>{this._delegateProperty(s,t[o])})}_delegateAttribute(t,s){!this.stateTarget||(t==="invalid"&&this._delegateAttribute("aria-invalid",s?"true":!1),typeof s=="boolean"?this.stateTarget.toggleAttribute(t,s):s?this.stateTarget.setAttribute(t,s):this.stateTarget.removeAttribute(t))}_delegateProperty(t,s){!this.stateTarget||(this.stateTarget[t]=s)}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Ur=g(i=>class extends i{static get properties(){return{inputElement:{type:Object,readOnly:!0,observer:"_inputElementChanged"},type:{type:String,readOnly:!0},value:{type:String,value:"",observer:"_valueChanged",notify:!0},_hasInputValue:{type:Boolean,value:!1,observer:"_hasInputValueChanged"}}}constructor(){super(),this._boundOnInput=this.__onInput.bind(this),this._boundOnChange=this._onChange.bind(this)}clear(){this.value=""}_addInputListeners(t){t.addEventListener("input",this._boundOnInput),t.addEventListener("change",this._boundOnChange)}_removeInputListeners(t){t.removeEventListener("input",this._boundOnInput),t.removeEventListener("change",this._boundOnChange)}_forwardInputValue(t){!this.inputElement||(t!=null?this.inputElement.value=t:this.inputElement.value="")}_inputElementChanged(t,s){t?this._addInputListeners(t):s&&this._removeInputListeners(s)}_hasInputValueChanged(t,s){(t||s)&&this.dispatchEvent(new CustomEvent("has-input-value-changed"))}__onInput(t){this._setHasInputValue(t),this._onInput(t)}_onInput(t){const s=t.composedPath()[0];this.__userInput=t.isTrusted,this.value=s.value,this.__userInput=!1}_onChange(t){}_toggleHasValue(t){this.toggleAttribute("has-value",t)}_valueChanged(t,s){this._toggleHasValue(this._hasValue),!(t===""&&s===void 0)&&(this.__userInput||this._forwardInputValue(t))}get _hasValue(){return this.value!=null&&this.value!==""}_setHasInputValue(t){const s=t.composedPath()[0];this._hasInputValue=s.value.length>0}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Os=g(i=>class extends Vr(Ts(Ur(i))){static get constraints(){return["required"]}static get delegateAttrs(){return[...super.delegateAttrs,"required"]}ready(){super.ready(),this._createConstraintsObserver()}checkValidity(){return this.inputElement&&this._hasValidConstraints(this.constructor.constraints.map(t=>this[t]))?this.inputElement.checkValidity():!this.invalid}_hasValidConstraints(t){return t.some(s=>this.__isValidConstraint(s))}_createConstraintsObserver(){this._createMethodObserver(`_constraintsChanged(stateTarget, ${this.constructor.constraints.join(", ")})`)}_constraintsChanged(t,...s){if(!t)return;const o=this._hasValidConstraints(s),n=this.__previousHasConstraints&&!o;(this._hasValue||this.invalid)&&o?this.validate():n&&this._setInvalid(!1),this.__previousHasConstraints=o}_onChange(t){t.stopPropagation(),this.validate(),this.dispatchEvent(new CustomEvent("change",{detail:{sourceEvent:t},bubbles:t.bubbles,cancelable:t.cancelable}))}__isValidConstraint(t){return Boolean(t)||t===0}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Je=new WeakMap;function jr(i){return Je.has(i)||Je.set(i,new Set),Je.get(i)}function qr(i,e){const t=document.createElement("style");t.textContent=i,e===document?document.head.appendChild(t):e.insertBefore(t,e.firstChild)}const $r=g(i=>class extends i{get slotStyles(){return{}}connectedCallback(){super.connectedCallback(),this.__applySlotStyles()}__applySlotStyles(){const t=this.getRootNode(),s=jr(t);this.slotStyles.forEach(o=>{s.has(o)||(qr(o,t),s.add(o))})}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Yr=i=>class extends $r(Mr(Os(Br(us(i))))){static get properties(){return{allowedCharPattern:{type:String,observer:"_allowedCharPatternChanged"},autoselect:{type:Boolean,value:!1},clearButtonVisible:{type:Boolean,reflectToAttribute:!0,value:!1},name:{type:String,reflectToAttribute:!0},placeholder:{type:String,reflectToAttribute:!0},readonly:{type:Boolean,value:!1,reflectToAttribute:!0},title:{type:String,reflectToAttribute:!0}}}static get delegateAttrs(){return[...super.delegateAttrs,"name","type","placeholder","readonly","invalid","title"]}constructor(){super(),this._boundOnPaste=this._onPaste.bind(this),this._boundOnDrop=this._onDrop.bind(this),this._boundOnBeforeInput=this._onBeforeInput.bind(this)}get clearElement(){return console.warn(`Please implement the 'clearElement' property in <${this.localName}>`),null}get slotStyles(){return[`
          :is(input[slot='input'], textarea[slot='textarea'])::placeholder {
            font: inherit;
            color: inherit;
          }
        `]}ready(){super.ready(),this.clearElement&&this.clearElement.addEventListener("click",t=>this._onClearButtonClick(t))}_onClearButtonClick(t){t.preventDefault(),this.inputElement.focus(),this.__clear()}_onFocus(t){super._onFocus(t),this.autoselect&&this.inputElement&&this.inputElement.select()}_onEscape(t){super._onEscape(t),this.clearButtonVisible&&!!this.value&&(t.stopPropagation(),this.__clear())}_onChange(t){t.stopPropagation(),this.validate(),this.dispatchEvent(new CustomEvent("change",{detail:{sourceEvent:t},bubbles:t.bubbles,cancelable:t.cancelable}))}__clear(){this.clear(),this.inputElement.dispatchEvent(new Event("input",{bubbles:!0,composed:!0})),this.inputElement.dispatchEvent(new Event("change",{bubbles:!0}))}_addInputListeners(t){super._addInputListeners(t),t.addEventListener("paste",this._boundOnPaste),t.addEventListener("drop",this._boundOnDrop),t.addEventListener("beforeinput",this._boundOnBeforeInput)}_removeInputListeners(t){super._removeInputListeners(t),t.removeEventListener("paste",this._boundOnPaste),t.removeEventListener("drop",this._boundOnDrop),t.removeEventListener("beforeinput",this._boundOnBeforeInput)}_onKeyDown(t){super._onKeyDown(t),this.allowedCharPattern&&!this.__shouldAcceptKey(t)&&(t.preventDefault(),this._markInputPrevented())}_markInputPrevented(){this.setAttribute("input-prevented",""),this._preventInputDebouncer=J.debounce(this._preventInputDebouncer,is.after(200),()=>{this.removeAttribute("input-prevented")})}__shouldAcceptKey(t){return t.metaKey||t.ctrlKey||!t.key||t.key.length!==1||this.__allowedCharRegExp.test(t.key)}_onPaste(t){if(this.allowedCharPattern){const s=t.clipboardData.getData("text");this.__allowedTextRegExp.test(s)||(t.preventDefault(),this._markInputPrevented())}}_onDrop(t){if(this.allowedCharPattern){const s=t.dataTransfer.getData("text");this.__allowedTextRegExp.test(s)||(t.preventDefault(),this._markInputPrevented())}}_onBeforeInput(t){this.allowedCharPattern&&t.data&&!this.__allowedTextRegExp.test(t.data)&&(t.preventDefault(),this._markInputPrevented())}_allowedCharPatternChanged(t){if(t)try{this.__allowedCharRegExp=new RegExp(`^${t}$`),this.__allowedTextRegExp=new RegExp(`^${t}*$`)}catch(s){console.error(s)}}};/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Ss=i=>class extends Yr(i){static get properties(){return{autocomplete:{type:String},autocorrect:{type:String},autocapitalize:{type:String,reflectToAttribute:!0}}}static get delegateAttrs(){return[...super.delegateAttrs,"autocapitalize","autocomplete","autocorrect"]}_inputElementChanged(t){super._inputElementChanged(t),t&&(t.value&&t.value!==this.value&&(console.warn(`Please define value on the <${this.localName}> component!`),t.value=""),this.value&&(t.value=this.value))}get __data(){return this.__dataValue||{}}set __data(t){this.__dataValue=t}_setFocused(t){super._setFocused(t),t||this.validate()}_onInput(t){super._onInput(t),this.invalid&&this.validate()}_valueChanged(t,s){super._valueChanged(t,s),s!==void 0&&this.invalid&&this.validate()}};/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class Ns{constructor(e,t){this.input=e,this.__preventDuplicateLabelClick=this.__preventDuplicateLabelClick.bind(this),t.addEventListener("label-changed",s=>{this.__initLabel(s.detail.node)}),this.__initLabel(t.node)}__initLabel(e){e&&(e.addEventListener("click",this.__preventDuplicateLabelClick),this.input&&e.setAttribute("for",this.input.id))}__preventDuplicateLabelClick(){const e=t=>{t.stopImmediatePropagation(),this.input.removeEventListener("click",e)};this.input.addEventListener("click",e)}}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Is=i=>class extends Os(i){static get properties(){return{pattern:{type:String},preventInvalidInput:{type:Boolean,observer:"_preventInvalidInputChanged"}}}static get delegateAttrs(){return[...super.delegateAttrs,"pattern"]}static get constraints(){return[...super.constraints,"pattern"]}_checkInputValue(){if(this.preventInvalidInput){const t=this.inputElement;t&&t.value.length>0&&!this.checkValidity()&&(t.value=this.value||"",this.setAttribute("input-prevented",""),this._inputDebouncer=J.debounce(this._inputDebouncer,is.after(200),()=>{this.removeAttribute("input-prevented")}))}}_onInput(t){this._checkInputValue(),super._onInput(t)}_preventInvalidInputChanged(t){t&&console.warn('WARNING: Since Vaadin 23.2, "preventInvalidInput" is deprecated. Please use "allowedCharPattern" instead.')}};/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd..
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Wr=v`
  [part='clear-button'] {
    display: none;
    cursor: default;
  }

  [part='clear-button']::before {
    content: '✕';
  }

  :host([clear-button-visible][has-value]:not([disabled]):not([readonly])) [part='clear-button'] {
    display: block;
  }
`;/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd..
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Gr=v`
  :host {
    display: inline-flex;
    outline: none;
  }

  :host::before {
    content: '\\2003';
    width: 0;
    display: inline-block;
    /* Size and position this element on the same vertical position as the input-field element
          to make vertical align for the host element work as expected */
  }

  :host([hidden]) {
    display: none !important;
  }

  :host(:not([has-label])) [part='label'] {
    display: none;
  }
`;/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd..
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Kr=v`
  [class$='container'] {
    display: flex;
    flex-direction: column;
    min-width: 100%;
    max-width: 100%;
    width: var(--vaadin-field-default-width, 12em);
  }
`;/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd..
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const zs=[Gr,Kr,Wr];/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */y("vaadin-text-field",zs,{moduleId:"vaadin-text-field-styles"});class pt extends Is(Ss(z(k(b)))){static get is(){return"vaadin-text-field"}static get template(){return C`
      <style>
        [part='input-field'] {
          flex-grow: 0;
        }
      </style>

      <div class="vaadin-field-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true" on-click="focus"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          readonly="[[readonly]]"
          disabled="[[disabled]]"
          invalid="[[invalid]]"
          theme$="[[_theme]]"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="input"></slot>
          <slot name="suffix" slot="suffix"></slot>
          <div id="clearButton" part="clear-button" slot="suffix" aria-hidden="true"></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
      </div>
      <slot name="tooltip"></slot>
    `}static get properties(){return{maxlength:{type:Number},minlength:{type:Number}}}static get delegateAttrs(){return[...super.delegateAttrs,"maxlength","minlength"]}static get constraints(){return[...super.constraints,"maxlength","minlength"]}constructor(){super(),this._setType("text")}get clearElement(){return this.$.clearButton}ready(){super.ready(),this.addController(new zr(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e})),this.addController(new Ns(this.inputElement,this._labelController)),this._tooltipController=new Ot(this),this._tooltipController.setPosition("top"),this.addController(this._tooltipController)}}customElements.define(pt.is,pt);/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Jr=v`
  :host {
    position: absolute;
    right: 0;
    top: 0;
    margin: 0;
    padding: 0;
    width: 100%;
    height: 100%;
    min-width: auto;
    background: transparent;
    outline: none;
  }
`;y("vaadin-password-field-button",[Ni,Jr],{moduleId:"lumo-password-field-button"});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Xr=v`
  [part='reveal-button']::before {
    content: var(--lumo-icons-eye);
  }

  :host([password-visible]) [part='reveal-button']::before {
    content: var(--lumo-icons-eye-disabled);
  }

  /* Make it easy to hide the button across the whole app */
  [part='reveal-button'] {
    position: relative;
    display: var(--lumo-password-field-reveal-button-display, block);
  }

  [part='reveal-button'][hidden] {
    display: none !important;
  }
`;y("vaadin-password-field",[Ve,Xr],{moduleId:"lumo-password-field"});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class fi extends ht{static get is(){return"vaadin-password-field-button"}static get template(){return C`
      <style>
        :host {
          display: block;
        }

        :host([hidden]) {
          display: none !important;
        }
      </style>
      <slot name="tooltip"></slot>
    `}}customElements.define(fi.is,fi);/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Zr=C`
  <div part="reveal-button" slot="suffix">
    <slot name="reveal"></slot>
  </div>
`;let Ae;class _i extends pt{static get is(){return"vaadin-password-field"}static get template(){if(!Ae){Ae=super.template.cloneNode(!0);const e=Zr.content.querySelector('[part="reveal-button"]');Ae.content.querySelector('[part="input-field"]').appendChild(e)}return Ae}static get properties(){return{revealButtonHidden:{type:Boolean,observer:"_revealButtonHiddenChanged",value:!1},passwordVisible:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_passwordVisibleChanged",readOnly:!0},i18n:{type:Object,value:()=>({reveal:"Show password"})}}}static get observers(){return["__i18nChanged(i18n.*)"]}get slotStyles(){const e=this.localName;return[...super.slotStyles,`
        ${e} [slot="input"]::-ms-reveal {
          display: none;
        }
      `]}get _revealNode(){return this._revealButtonController&&this._revealButtonController.node}constructor(){super(),this._setType("password"),this.__boundRevealButtonClick=this._onRevealButtonClick.bind(this),this.__boundRevealButtonTouchend=this._onRevealButtonTouchend.bind(this)}ready(){super.ready(),this._revealPart=this.shadowRoot.querySelector('[part="reveal-button"]'),this._revealButtonController=new L(this,"reveal",()=>document.createElement("vaadin-password-field-button"),(e,t)=>{t.disabled=e.disabled,t.addEventListener("click",e.__boundRevealButtonClick),t.addEventListener("touchend",e.__boundRevealButtonTouchend)}),this.addController(this._revealButtonController),this.__updateAriaLabel(this.i18n),this._updateToggleState(!1),this._toggleRevealHidden(this.revealButtonHidden),this.inputElement&&(this.inputElement.autocapitalize="off")}_shouldSetFocus(e){return e.target===this.inputElement||e.target===this._revealNode}_shouldRemoveFocus(e){return!(e.relatedTarget===this._revealNode||e.relatedTarget===this.inputElement&&e.target===this._revealNode)}_setFocused(e){if(super._setFocused(e),!e)this._setPasswordVisible(!1);else{const t=this.getRootNode().activeElement===this._revealNode;this.toggleAttribute("focus-ring",this._keyboardActive&&!t)}}__updateAriaLabel(e){e.reveal&&this._revealNode&&this._revealNode.setAttribute("aria-label",e.reveal)}__i18nChanged(e){this.__updateAriaLabel(e.base)}_revealButtonHiddenChanged(e){this._toggleRevealHidden(e)}_togglePasswordVisibility(){this._setPasswordVisible(!this.passwordVisible)}_onRevealButtonClick(){this._togglePasswordVisibility()}_onRevealButtonTouchend(e){e.preventDefault(),this._togglePasswordVisibility(),this.inputElement.focus()}_toggleRevealHidden(e){this._revealNode&&(e?(this._revealPart.setAttribute("hidden",""),this._revealNode.setAttribute("tabindex","-1"),this._revealNode.setAttribute("aria-hidden","true")):(this._revealPart.removeAttribute("hidden"),this._revealNode.setAttribute("tabindex","0"),this._revealNode.removeAttribute("aria-hidden")))}_updateToggleState(e){this._revealNode&&this._revealNode.setAttribute("aria-pressed",e?"true":"false")}_passwordVisibleChanged(e){this._setType(e?"text":"password"),this._updateToggleState(e)}_disabledChanged(e,t){super._disabledChanged(e,t),this._revealNode&&(this._revealNode.disabled=e)}}customElements.define(_i.is,_i);/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */let mi=0;function Ms(i,e=[],t={}){const s=t.moduleId||`custom-style-module-${mi}`;mi+=1;const o=document.createElement("dom-module");i&&o.setAttribute("theme-for",i);const n=!!(e.length&&t.moduleId),r=[].concat(t.include||[]);r.length===0?o.__allStyles=e:n||(o.__partialStyles=e),o.innerHTML=`
    <template>
      ${r.map(a=>`<style include=${a}></style>`)}
      ${n?`<style>${e.map(a=>a.cssText).join(`
`)}</style>`:""}
    </template>
  `,o.register(s)}function Qr(i){return De(i.querySelector("template")).map(e=>Gs(e.textContent))}function ea(){const e=K.prototype.modules;return Object.keys(e).map(t=>{const s=e[t],o=s.getAttribute("theme-for");return s.__allStyles=s.__allStyles||Qr(s).concat(s.__partialStyles||[]),{themeFor:o,moduleId:t,styles:s.__allStyles}})}window.Vaadin=window.Vaadin||{};window.Vaadin.styleModules={getAllThemes:ea,registerStyles:Ms};be&&be.length>0&&(be.forEach(i=>{Ms(i.themeFor,i.styles,{moduleId:i.moduleId,include:i.include})}),be.length=0);/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/function kt(i,e,t,s,o){let n;o&&(n=typeof t=="object"&&t!==null,n&&(s=i.__dataTemp[e]));let r=s!==t&&(s===s||t===t);return n&&r&&(i.__dataTemp[e]=t),r}const Lt=g(i=>{class e extends i{_shouldPropertyChange(s,o,n){return kt(this,s,o,n,!0)}}return e}),xl=g(i=>{class e extends i{static get properties(){return{mutableData:Boolean}}_shouldPropertyChange(s,o,n){return kt(this,s,o,n,this.mutableData)}}return e});Lt._mutablePropertyChange=kt;/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/let ft=null;function _t(){return ft}_t.prototype=Object.create(HTMLTemplateElement.prototype,{constructor:{value:_t,writable:!0}});const ks=Et(_t),ta=Lt(ks);function ia(i,e){ft=i,Object.setPrototypeOf(i,e.prototype),new e,ft=null}const sa=Et(class{});function Ls(i,e){for(let t=0;t<e.length;t++){let s=e[t];if(Boolean(i)!=Boolean(s.__hideTemplateChildren__))if(s.nodeType===Node.TEXT_NODE)i?(s.__polymerTextContent__=s.textContent,s.textContent=""):s.textContent=s.__polymerTextContent__;else if(s.localName==="slot")if(i)s.__polymerReplaced__=document.createComment("hidden-slot"),_(_(s).parentNode).replaceChild(s.__polymerReplaced__,s);else{const o=s.__polymerReplaced__;o&&_(_(o).parentNode).replaceChild(s,o)}else s.style&&(i?(s.__polymerDisplay__=s.style.display,s.style.display="none"):s.style.display=s.__polymerDisplay__);s.__hideTemplateChildren__=i,s._showHideChildren&&s._showHideChildren(i)}}class H extends sa{constructor(e){super(),this._configureProperties(e),this.root=this._stampTemplate(this.__dataHost);let t=[];this.children=t;for(let o=this.root.firstChild;o;o=o.nextSibling)t.push(o),o.__templatizeInstance=this;this.__templatizeOwner&&this.__templatizeOwner.__hideTemplateChildren__&&this._showHideChildren(!0);let s=this.__templatizeOptions;(e&&s.instanceProps||!s.instanceProps)&&this._enableProperties()}_configureProperties(e){if(this.__templatizeOptions.forwardHostProp)for(let s in this.__hostProps)this._setPendingProperty(s,this.__dataHost["_host_"+s]);for(let s in e)this._setPendingProperty(s,e[s])}forwardHostProp(e,t){this._setPendingPropertyOrPath(e,t,!1,!0)&&this.__dataHost._enqueueClient(this)}_addEventListenerToNode(e,t,s){if(this._methodHost&&this.__templatizeOptions.parentModel)this._methodHost._addEventListenerToNode(e,t,o=>{o.model=this,s(o)});else{let o=this.__dataHost.__dataHost;o&&o._addEventListenerToNode(e,t,s)}}_showHideChildren(e){Ls(e,this.children)}_setUnmanagedPropertyToNode(e,t,s){e.__hideTemplateChildren__&&e.nodeType==Node.TEXT_NODE&&t=="textContent"?e.__polymerTextContent__=s:super._setUnmanagedPropertyToNode(e,t,s)}get parentModel(){let e=this.__parentModel;if(!e){let t;e=this;do e=e.__dataHost.__dataHost;while((t=e.__templatizeOptions)&&!t.parentModel);this.__parentModel=e}return e}dispatchEvent(e){return!0}}H.prototype.__dataHost;H.prototype.__templatizeOptions;H.prototype._methodHost;H.prototype.__templatizeOwner;H.prototype.__hostProps;const oa=Lt(H);function gi(i){let e=i.__dataHost;return e&&e._methodHost||e}function na(i,e,t){let s=t.mutableData?oa:H;fe.mixin&&(s=fe.mixin(s));let o=class extends s{};return o.prototype.__templatizeOptions=t,o.prototype._bindTemplate(i),la(o,i,e,t),o}function ra(i,e,t,s){let o=t.forwardHostProp;if(o&&e.hasHostProps){const n=i.localName=="template";let r=e.templatizeTemplateClass;if(!r){if(n){let l=t.mutableData?ta:ks;class d extends l{}r=e.templatizeTemplateClass=d}else{const l=i.constructor;class d extends l{}r=e.templatizeTemplateClass=d}let a=e.hostProps;for(let l in a)r.prototype._addPropertyEffect("_host_"+l,r.prototype.PROPERTY_EFFECT_TYPES.PROPAGATE,{fn:aa(l,o)}),r.prototype._createNotifyingProperty("_host_"+l);Mi&&s&&ha(e,t,s)}if(i.__dataProto&&Object.assign(i.__data,i.__dataProto),n)ia(i,r),i.__dataTemp={},i.__dataPending=null,i.__dataOld=null,i._enableProperties();else{Object.setPrototypeOf(i,r.prototype);const a=e.hostProps;for(let l in a)if(l="_host_"+l,l in i){const d=i[l];delete i[l],i.__data[l]=d}}}}function aa(i,e){return function(s,o,n){e.call(s.__templatizeOwner,o.substring(6),n[o])}}function la(i,e,t,s){let o=t.hostProps||{};for(let n in s.instanceProps){delete o[n];let r=s.notifyInstanceProp;r&&i.prototype._addPropertyEffect(n,i.prototype.PROPERTY_EFFECT_TYPES.NOTIFY,{fn:da(n,r)})}if(s.forwardHostProp&&e.__dataHost)for(let n in o)t.hasHostProps||(t.hasHostProps=!0),i.prototype._addPropertyEffect(n,i.prototype.PROPERTY_EFFECT_TYPES.NOTIFY,{fn:ca()})}function da(i,e){return function(s,o,n){e.call(s.__templatizeOwner,s,o,n[o])}}function ca(){return function(e,t,s){e.__dataHost._setPendingPropertyOrPath("_host_"+t,s[t],!0,!0)}}function fe(i,e,t){if(de&&!gi(i))throw new Error("strictTemplatePolicy: template owner not trusted");if(t=t||{},i.__templatizeOwner)throw new Error("A <template> can only be templatized once");i.__templatizeOwner=e;let o=(e?e.constructor:H)._parseTemplate(i),n=o.templatizeInstanceClass;n||(n=na(i,o,t),o.templatizeInstanceClass=n);const r=gi(i);ra(i,o,t,r);let a=class extends n{};return a.prototype._methodHost=r,a.prototype.__dataHost=i,a.prototype.__templatizeOwner=e,a.prototype.__hostProps=o.hostProps,a=a,a}function ha(i,e,t){const s=t.constructor._properties,{propertyEffects:o}=i,{instanceProps:n}=e;for(let r in o)if(!s[r]&&!(n&&n[r])){const a=o[r];for(let l=0;l<a.length;l++){const{part:d}=a[l].info;if(!(d.signature&&d.signature.static)){console.warn(`Property '${r}' used in template but not declared in 'properties'; attribute will not be observed.`);break}}}}function El(i,e){let t;for(;e;)if(t=e.__dataHost?e:e.__templatizeInstance)if(t.__dataHost!=i)e=t.__dataHost;else return t;else e=_(e).parentNode;return null}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class Me extends b{static create(e,t){const s=new this;return s.__template=t,s.__component=e,s}static get is(){return"vaadin-template-renderer-templatizer"}constructor(){super(),this.__template=null,this.__component=null,this.__TemplateClass=null,this.__templateInstances=new Set}render(e,t={}){let s=e.__templateInstance;if(this.__hasTemplateInstance(s)&&this.__isTemplateInstanceAttachedToDOM(s)){this.__updateProperties(s,t);return}this.__hasTemplateInstance(s)&&this.__disposeOfTemplateInstance(s),s=this.__createTemplateInstance(t),e.__templateInstance=s,e.innerHTML="",e.appendChild(s.root)}__updateProperties(e,t){e.item===t.item&&e._setPendingProperty("item"),e.__properties=t,e.setProperties(t)}__createTemplateInstance(e){this.__createTemplateClass(e);const t=new this.__TemplateClass(e);return t.__properties=e,this.__templateInstances.add(t),t}__disposeOfTemplateInstance(e){this.__templateInstances.delete(e)}__hasTemplateInstance(e){return this.__templateInstances.has(e)}__isTemplateInstanceAttachedToDOM(e){return e.children.length===0?!1:!!e.children[0].parentElement}__createTemplateClass(e){if(this.__TemplateClass)return;const t=Object.keys(e).reduce((s,o)=>({...s,[o]:!0}),{});this.__TemplateClass=fe(this.__template,this,{parentModel:!0,instanceProps:t,forwardHostProp(s,o){this.__templateInstances.forEach(n=>{n.forwardHostProp(s,o)})},notifyInstanceProp(s,o,n){let r;r=o.split(".")[0],r=r[0].toUpperCase()+r.slice(1);const a=`_on${r}PropertyChanged`;this[a]&&this[a](s,o,n)}})}}customElements.define(Me.is,Me);/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class _e extends Me{static get is(){return"vaadin-template-renderer-grid-templatizer"}_onItemPropertyChanged(e,t,s){if(t==="item"||!Array.isArray(this.__grid.items))return;const o=this.__grid.items.indexOf(e.item);t=t.replace(/^item\./,""),t=`items.${o}.${t}`,this.__grid.notifyPath(t,s)}_onExpandedPropertyChanged(e,t,s){e.__properties.expanded!==s&&(s?this.__grid.expandItem(e.item):this.__grid.collapseItem(e.item))}_onSelectedPropertyChanged(e,t,s){e.__properties.selected!==s&&(s?this.__grid.selectItem(e.item):this.__grid.deselectItem(e.item))}_onDetailsOpenedPropertyChanged(e,t,s){e.__properties.detailsOpened!==s&&(s?this.__grid.openItemDetails(e.item):this.__grid.closeItemDetails(e.item))}get __grid(){return this.__component.__gridElement?this.__component:this.__component._grid}}customElements.define(_e.is,_e);/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */function U(i,e,t=Me){const s=t.create(i,e),o=(n,r,a)=>{s.render(n,a)};return e.__templatizer=s,o.__templatized=!0,o}function j(i,e,t){const s=i[e];if(s&&!s.__templatized){const o=i.localName;throw new Error(`Cannot use both a template and a renderer for <${o} />.`)}i[e]=t}function ua(i){i.__suppressTemplateWarning||i.hasAttribute("suppress-template-warning")||(console.warn(`WARNING: <template> inside <${i.localName}> is deprecated. Use a renderer function instead (see https://vaad.in/template-renderer)`),i.__suppressTemplateWarning=!0)}function pa(i,e){if(e.matches(".row-details")){const t=U(i,e,_e);j(i,"rowDetailsRenderer",t)}}function fa(i,e){if(e.matches(".header")){const s=U(i,e);j(i,"headerRenderer",s);return}if(e.matches(".footer")){const s=U(i,e);j(i,"footerRenderer",s);return}if(e.matches(".editor")){const s=U(i,e,_e);j(i,"editModeRenderer",s);return}const t=U(i,e,_e);j(i,"renderer",t)}function _a(i,e){if(ua(i),i.__gridElement){pa(i,e);return}if(i.__gridColumnElement){fa(i,e);return}const t=U(i,e);j(i,"renderer",t)}function Ds(i){Fe.getFlattenedNodes(i).filter(e=>e instanceof HTMLTemplateElement).forEach(e=>{e.__templatizer||_a(i,e)})}function ma(i){i.__templateObserver||(i.__templateObserver=new Fe(i,()=>{Ds(i)}))}window.Vaadin=window.Vaadin||{};window.Vaadin.templateRendererCallback=i=>{Ds(i),ma(i)};/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const ga=v`
  [part='input-field'],
  [part='input-field'] ::slotted(textarea) {
    height: auto;
    box-sizing: border-box;
  }

  [part='input-field'] {
    /* Equal to the implicit padding in vaadin-text-field */
    padding-top: calc((var(--lumo-text-field-size) - 1em * var(--lumo-line-height-s)) / 2);
    padding-bottom: calc((var(--lumo-text-field-size) - 1em * var(--lumo-line-height-s)) / 2);
    transition: background-color 0.1s;
    line-height: var(--lumo-line-height-s);
  }

  :host(:not([readonly])) [part='input-field']::after {
    display: none;
  }

  :host([readonly]) [part='input-field'] {
    border: 1px dashed var(--lumo-contrast-30pct);
  }

  :host([readonly]) [part='input-field']::after {
    border: none;
  }

  :host(:hover:not([readonly]):not([focused]):not([invalid])) [part='input-field'] {
    background-color: var(--lumo-contrast-20pct);
  }

  @media (pointer: coarse) {
    :host(:hover:not([readonly]):not([focused]):not([invalid])) [part='input-field'] {
      background-color: var(--lumo-contrast-10pct);
    }

    :host(:active:not([readonly]):not([focused])) [part='input-field'] {
      background-color: var(--lumo-contrast-20pct);
    }
  }

  [part='input-field'] ::slotted(textarea) {
    line-height: inherit;
    --_lumo-text-field-overflow-mask-image: none;
  }

  /* Vertically align icon prefix/suffix with the first line of text */
  [part='input-field'] ::slotted(iron-icon),
  [part='input-field'] ::slotted(vaadin-icon) {
    margin-top: calc((var(--lumo-icon-size-m) - 1em * var(--lumo-line-height-s)) / -2);
  }
`;y("vaadin-text-area",[Ve,ga],{moduleId:"lumo-text-area"});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Ee=new ResizeObserver(i=>{setTimeout(()=>{i.forEach(e=>{e.target.resizables?e.target.resizables.forEach(t=>{t._onResize(e.contentRect)}):e.target._onResize(e.contentRect)})})}),va=g(i=>class extends i{connectedCallback(){if(super.connectedCallback(),Ee.observe(this),this._observeParent){const t=this.parentNode instanceof ShadowRoot?this.parentNode.host:this.parentNode;t.resizables||(t.resizables=new Set,Ee.observe(t)),t.resizables.add(this),this.__parent=t}}disconnectedCallback(){super.disconnectedCallback(),Ee.unobserve(this);const t=this.__parent;if(this._observeParent&&t){const s=t.resizables;s&&(s.delete(this),s.size===0&&Ee.unobserve(t)),this.__parent=null}}get _observeParent(){return!1}_onResize(t){}notifyResize(){console.warn("WARNING: Since Vaadin 23, notifyResize() is deprecated. The component uses a ResizeObserver internally and doesn't need to be explicitly notified of resizes.")}});/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class ya extends L{constructor(e,t){super(e,"textarea",()=>document.createElement("textarea"),(s,o)=>{const n=s.getAttribute("value");n&&(o.value=n);const r=s.getAttribute("name");r&&o.setAttribute("name",r),o.id=this.defaultId,typeof t=="function"&&t(o)},!0)}}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */y("vaadin-text-area",zs,{moduleId:"vaadin-text-area-styles"});class vi extends va(Is(Ss(z(k(b))))){static get is(){return"vaadin-text-area"}static get template(){return C`
      <style>
        :host {
          animation: 1ms vaadin-text-area-appear;
        }

        .vaadin-text-area-container {
          flex: auto;
        }

        /* The label, helper text and the error message should neither grow nor shrink. */
        [part='label'],
        [part='helper-text'],
        [part='error-message'] {
          flex: none;
        }

        [part='input-field'] {
          flex: auto;
          overflow: auto;
          -webkit-overflow-scrolling: touch;
        }

        ::slotted(textarea) {
          -webkit-appearance: none;
          -moz-appearance: none;
          flex: auto;
          overflow: hidden;
          width: 100%;
          height: 100%;
          outline: none;
          resize: none;
          margin: 0;
          padding: 0 0.25em;
          border: 0;
          border-radius: 0;
          min-width: 0;
          font: inherit;
          font-size: 1em;
          line-height: normal;
          color: inherit;
          background-color: transparent;
          /* Disable default invalid style in Firefox */
          box-shadow: none;
        }

        /* Override styles from <vaadin-input-container> */
        [part='input-field'] ::slotted(textarea) {
          align-self: stretch;
          white-space: pre-wrap;
        }

        [part='input-field'] ::slotted(:not(textarea)) {
          align-self: flex-start;
        }

        /* Workaround https://bugzilla.mozilla.org/show_bug.cgi?id=1739079 */
        :host([disabled]) ::slotted(textarea) {
          user-select: none;
        }

        @keyframes vaadin-text-area-appear {
          to {
            opacity: 1;
          }
        }
      </style>

      <div class="vaadin-text-area-container">
        <div part="label">
          <slot name="label"></slot>
          <span part="required-indicator" aria-hidden="true"></span>
        </div>

        <vaadin-input-container
          part="input-field"
          readonly="[[readonly]]"
          disabled="[[disabled]]"
          invalid="[[invalid]]"
          theme$="[[_theme]]"
          on-scroll="__scrollPositionUpdated"
        >
          <slot name="prefix" slot="prefix"></slot>
          <slot name="textarea"></slot>
          <slot name="suffix" slot="suffix"></slot>
          <div id="clearButton" part="clear-button" slot="suffix" aria-hidden="true"></div>
        </vaadin-input-container>

        <div part="helper-text">
          <slot name="helper"></slot>
        </div>

        <div part="error-message">
          <slot name="error-message"></slot>
        </div>
      </div>

      <slot name="tooltip"></slot>
    `}static get properties(){return{maxlength:{type:Number},minlength:{type:Number}}}static get delegateAttrs(){return[...super.delegateAttrs,"maxlength","minlength"]}static get constraints(){return[...super.constraints,"maxlength","minlength"]}get clearElement(){return this.$.clearButton}_onResize(){this.__scrollPositionUpdated()}ready(){super.ready(),this.addController(new ya(this,e=>{this._setInputElement(e),this._setFocusElement(e),this.stateTarget=e,this.ariaTarget=e})),this.addController(new Ns(this.inputElement,this._labelController)),this._tooltipController=new Ot(this),this._tooltipController.setPosition("top"),this.addController(this._tooltipController),this.addEventListener("animationend",this._onAnimationEnd),this._inputField=this.shadowRoot.querySelector("[part=input-field]"),this._inputField.addEventListener("wheel",e=>{const t=this._inputField.scrollTop;this._inputField.scrollTop+=e.deltaY,t!==this._inputField.scrollTop&&(e.preventDefault(),this.__scrollPositionUpdated())}),this._updateHeight(),this.__scrollPositionUpdated()}__scrollPositionUpdated(){this._inputField.style.setProperty("--_text-area-vertical-scroll-position","0px"),this._inputField.style.setProperty("--_text-area-vertical-scroll-position",`${this._inputField.scrollTop}px`)}_onAnimationEnd(e){e.animationName.indexOf("vaadin-text-area-appear")===0&&this._updateHeight()}_valueChanged(e,t){super._valueChanged(e,t),this._updateHeight()}_updateHeight(){const e=this.inputElement,t=this._inputField;if(!e||!t)return;const s=t.scrollTop,o=this.value?this.value.length:0;if(this._oldValueLength>=o){const r=getComputedStyle(t).height,a=getComputedStyle(e).width;t.style.display="block",t.style.height=r,e.style.maxWidth=a,e.style.height="auto"}this._oldValueLength=o;const n=e.scrollHeight;n>e.clientHeight&&(e.style.height=`${n}px`),e.style.removeProperty("max-width"),t.style.removeProperty("display"),t.style.removeProperty("height"),t.scrollTop=s}checkValidity(){if(!super.checkValidity())return!1;if(!this.pattern||!this.inputElement.value)return!0;try{const e=this.inputElement.value.match(this.pattern);return e?e[0]===e.input:!1}catch{return!0}}}customElements.define(vi.is,vi);/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Dt=v`
  :host {
    top: var(--lumo-space-m);
    right: var(--lumo-space-m);
    bottom: var(--lumo-space-m);
    left: var(--lumo-space-m);
    /* Workaround for Edge issue (only on Surface), where an overflowing vaadin-list-box inside vaadin-select-overlay makes the overlay transparent */
    /* stylelint-disable-next-line */
    outline: 0px solid transparent;
  }

  [part='overlay'] {
    background-color: var(--lumo-base-color);
    background-image: linear-gradient(var(--lumo-tint-5pct), var(--lumo-tint-5pct));
    border-radius: var(--lumo-border-radius-m);
    box-shadow: 0 0 0 1px var(--lumo-shade-5pct), var(--lumo-box-shadow-m);
    color: var(--lumo-body-text-color);
    font-family: var(--lumo-font-family);
    font-size: var(--lumo-font-size-m);
    font-weight: 400;
    line-height: var(--lumo-line-height-m);
    letter-spacing: 0;
    text-transform: none;
    -webkit-text-size-adjust: 100%;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }

  [part='content'] {
    padding: var(--lumo-space-xs);
  }

  [part='backdrop'] {
    background-color: var(--lumo-shade-20pct);
    animation: 0.2s lumo-overlay-backdrop-enter both;
    will-change: opacity;
  }

  @keyframes lumo-overlay-backdrop-enter {
    0% {
      opacity: 0;
    }
  }

  :host([closing]) [part='backdrop'] {
    animation: 0.2s lumo-overlay-backdrop-exit both;
  }

  @keyframes lumo-overlay-backdrop-exit {
    100% {
      opacity: 0;
    }
  }

  @keyframes lumo-overlay-dummy-animation {
    0% {
      opacity: 1;
    }

    100% {
      opacity: 1;
    }
  }
`;y("",Dt,{moduleId:"lumo-overlay"});y("vaadin-overlay",Dt,{moduleId:"lumo-vaadin-overlay"});/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/let ke=!1,Hs=[],Rs=[];function Fs(){ke=!0,requestAnimationFrame(function(){ke=!1,ba(Hs),setTimeout(function(){wa(Rs)})})}function ba(i){for(;i.length;)Bs(i.shift())}function wa(i){for(let e=0,t=i.length;e<t;e++)Bs(i.shift())}function Bs(i){const e=i[0],t=i[1],s=i[2];try{t.apply(e,s)}catch(o){setTimeout(()=>{throw o})}}function Tl(i,e,t){ke||Fs(),Hs.push([i,e,t])}function Ca(i,e,t){ke||Fs(),Rs.push([i,e,t])}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Xe=[];class xa{constructor(e){this.host=e,this.__trapNode=null,this.__onKeyDown=this.__onKeyDown.bind(this)}hostConnected(){document.addEventListener("keydown",this.__onKeyDown)}hostDisconnected(){document.removeEventListener("keydown",this.__onKeyDown)}trapFocus(e){if(this.__trapNode=e,this.__focusableElements.length===0)throw this.__trapNode=null,new Error("The trap node should have at least one focusable descendant or be focusable itself.");Xe.push(this),this.__focusedElementIndex===-1&&this.__focusableElements[0].focus()}releaseFocus(){this.__trapNode=null,Xe.pop()}__onKeyDown(e){if(!!this.__trapNode&&this===Array.from(Xe).pop()&&e.key==="Tab"){e.preventDefault();const t=e.shiftKey;this.__focusNextElement(t)}}__focusNextElement(e=!1){const t=this.__focusableElements,s=e?-1:1,o=this.__focusedElementIndex,n=(t.length+o+s)%t.length,r=t[n];r.focus(),r.localName==="input"&&r.select()}get __focusableElements(){return Cr(this.__trapNode)}get __focusedElementIndex(){const e=this.__focusableElements;return e.indexOf(e.filter(wr).pop())}}/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class I extends z(Pt(Re(b))){static get template(){return C`
      <style>
        :host {
          z-index: 200;
          position: fixed;

          /* Despite of what the names say, <vaadin-overlay> is just a container
          for position/sizing/alignment. The actual overlay is the overlay part. */

          /* Default position constraints: the entire viewport. Note: themes can
          override this to introduce gaps between the overlay and the viewport. */
          top: 0;
          right: 0;
          bottom: var(--vaadin-overlay-viewport-bottom);
          left: 0;

          /* Use flexbox alignment for the overlay part. */
          display: flex;
          flex-direction: column; /* makes dropdowns sizing easier */
          /* Align to center by default. */
          align-items: center;
          justify-content: center;

          /* Allow centering when max-width/max-height applies. */
          margin: auto;

          /* The host is not clickable, only the overlay part is. */
          pointer-events: none;

          /* Remove tap highlight on touch devices. */
          -webkit-tap-highlight-color: transparent;

          /* CSS API for host */
          --vaadin-overlay-viewport-bottom: 0;
        }

        :host([hidden]),
        :host(:not([opened]):not([closing])) {
          display: none !important;
        }

        [part='overlay'] {
          -webkit-overflow-scrolling: touch;
          overflow: auto;
          pointer-events: auto;

          /* Prevent overflowing the host in MSIE 11 */
          max-width: 100%;
          box-sizing: border-box;

          -webkit-tap-highlight-color: initial; /* reenable tap highlight inside */
        }

        [part='backdrop'] {
          z-index: -1;
          content: '';
          background: rgba(0, 0, 0, 0.5);
          position: fixed;
          top: 0;
          left: 0;
          bottom: 0;
          right: 0;
          pointer-events: auto;
        }
      </style>

      <div id="backdrop" part="backdrop" hidden$="[[!withBackdrop]]"></div>
      <div part="overlay" id="overlay" tabindex="0">
        <div part="content" id="content">
          <slot></slot>
        </div>
      </div>
    `}static get is(){return"vaadin-overlay"}static get properties(){return{opened:{type:Boolean,notify:!0,observer:"_openedChanged",reflectToAttribute:!0},owner:Element,renderer:Function,template:{type:Object,notify:!0},content:{type:Object,notify:!0},withBackdrop:{type:Boolean,value:!1,reflectToAttribute:!0},model:Object,modeless:{type:Boolean,value:!1,reflectToAttribute:!0,observer:"_modelessChanged"},hidden:{type:Boolean,reflectToAttribute:!0,observer:"_hiddenChanged"},focusTrap:{type:Boolean,value:!1},restoreFocusOnClose:{type:Boolean,value:!1},restoreFocusNode:{type:HTMLElement},_mouseDownInside:{type:Boolean},_mouseUpInside:{type:Boolean},_instance:{type:Object},_originalContentPart:Object,_contentNodes:Array,_oldOwner:Element,_oldModel:Object,_oldTemplate:Object,_oldRenderer:Object,_oldOpened:Boolean}}static get observers(){return["_templateOrRendererChanged(template, renderer, owner, model, opened)"]}constructor(){super(),this._boundMouseDownListener=this._mouseDownListener.bind(this),this._boundMouseUpListener=this._mouseUpListener.bind(this),this._boundOutsideClickListener=this._outsideClickListener.bind(this),this._boundKeydownListener=this._keydownListener.bind(this),this._observer=new Fe(this,e=>{this._setTemplateFromNodes(e.addedNodes)}),this._boundIronOverlayCanceledListener=this._ironOverlayCanceled.bind(this),ys&&(this._boundIosResizeListener=()=>this._detectIosNavbar()),this.__focusTrapController=new xa(this)}ready(){super.ready(),this._observer.flush(),this.addEventListener("click",()=>{}),this.$.backdrop.addEventListener("click",()=>{}),this.addController(this.__focusTrapController)}_detectIosNavbar(){if(!this.opened)return;const e=window.innerHeight,s=window.innerWidth>e,o=document.documentElement.clientHeight;s&&o>e?this.style.setProperty("--vaadin-overlay-viewport-bottom",`${o-e}px`):this.style.setProperty("--vaadin-overlay-viewport-bottom","0")}_setTemplateFromNodes(e){this.template=e.find(t=>t.localName&&t.localName==="template")||this.template}close(e){const t=new CustomEvent("vaadin-overlay-close",{bubbles:!0,cancelable:!0,detail:{sourceEvent:e}});this.dispatchEvent(t),t.defaultPrevented||(this.opened=!1)}connectedCallback(){super.connectedCallback(),this._boundIosResizeListener&&(this._detectIosNavbar(),window.addEventListener("resize",this._boundIosResizeListener))}disconnectedCallback(){super.disconnectedCallback(),this._boundIosResizeListener&&window.removeEventListener("resize",this._boundIosResizeListener)}requestContentUpdate(){this.renderer&&this.renderer.call(this.owner,this.content,this.owner,this.model)}_ironOverlayCanceled(e){e.preventDefault()}_mouseDownListener(e){this._mouseDownInside=e.composedPath().indexOf(this.$.overlay)>=0}_mouseUpListener(e){this._mouseUpInside=e.composedPath().indexOf(this.$.overlay)>=0}_outsideClickListener(e){if(e.composedPath().includes(this.$.overlay)||this._mouseDownInside||this._mouseUpInside){this._mouseDownInside=!1,this._mouseUpInside=!1;return}if(!this._last)return;const t=new CustomEvent("vaadin-overlay-outside-click",{bubbles:!0,cancelable:!0,detail:{sourceEvent:e}});this.dispatchEvent(t),this.opened&&!t.defaultPrevented&&this.close(e)}_keydownListener(e){if(!!this._last&&!(this.modeless&&!e.composedPath().includes(this.$.overlay))&&e.key==="Escape"){const t=new CustomEvent("vaadin-overlay-escape-press",{bubbles:!0,cancelable:!0,detail:{sourceEvent:e}});this.dispatchEvent(t),this.opened&&!t.defaultPrevented&&this.close(e)}}_ensureTemplatized(){this._setTemplateFromNodes(Array.from(this.children))}_openedChanged(e,t){this._instance||this._ensureTemplatized(),e?(this.__restoreFocusNode=this._getActiveElement(),this._animatedOpening(),Ca(this,()=>{this.focusTrap&&this.__focusTrapController.trapFocus(this.$.overlay);const s=new CustomEvent("vaadin-overlay-open",{bubbles:!0});this.dispatchEvent(s)}),document.addEventListener("keydown",this._boundKeydownListener),this.modeless||this._addGlobalListeners()):t&&(this.focusTrap&&this.__focusTrapController.releaseFocus(),this._animatedClosing(),document.removeEventListener("keydown",this._boundKeydownListener),this.modeless||this._removeGlobalListeners())}_hiddenChanged(e){e&&this.hasAttribute("closing")&&this._flushAnimation("closing")}_shouldAnimate(){const e=getComputedStyle(this).getPropertyValue("animation-name");return!(getComputedStyle(this).getPropertyValue("display")==="none")&&e&&e!=="none"}_enqueueAnimation(e,t){const s=`__${e}Handler`,o=n=>{n&&n.target!==this||(t(),this.removeEventListener("animationend",o),delete this[s])};this[s]=o,this.addEventListener("animationend",o)}_flushAnimation(e){const t=`__${e}Handler`;typeof this[t]=="function"&&this[t]()}_animatedOpening(){this.parentNode===document.body&&this.hasAttribute("closing")&&this._flushAnimation("closing"),this._attachOverlay(),this.modeless||this._enterModalState(),this.setAttribute("opening",""),this._shouldAnimate()?this._enqueueAnimation("opening",()=>{this._finishOpening()}):this._finishOpening()}_attachOverlay(){this._placeholder=document.createComment("vaadin-overlay-placeholder"),this.parentNode.insertBefore(this._placeholder,this),document.body.appendChild(this),this.bringToFront()}_finishOpening(){document.addEventListener("iron-overlay-canceled",this._boundIronOverlayCanceledListener),this.removeAttribute("opening")}_finishClosing(){document.removeEventListener("iron-overlay-canceled",this._boundIronOverlayCanceledListener),this._detachOverlay(),this.$.overlay.style.removeProperty("pointer-events"),this.removeAttribute("closing")}_animatedClosing(){if(this.hasAttribute("opening")&&this._flushAnimation("opening"),this._placeholder){this._exitModalState();const e=this.restoreFocusNode||this.__restoreFocusNode;if(this.restoreFocusOnClose&&e){const t=this._getActiveElement();(t===document.body||this._deepContains(t))&&setTimeout(()=>e.focus()),this.__restoreFocusNode=null}this.setAttribute("closing",""),this.dispatchEvent(new CustomEvent("vaadin-overlay-closing")),this._shouldAnimate()?this._enqueueAnimation("closing",()=>{this._finishClosing()}):this._finishClosing()}}_detachOverlay(){this._placeholder.parentNode.insertBefore(this,this._placeholder),this._placeholder.parentNode.removeChild(this._placeholder)}static get __attachedInstances(){return Array.from(document.body.children).filter(e=>e instanceof I&&!e.hasAttribute("closing")).sort((e,t)=>e.__zIndex-t.__zIndex||0)}get _last(){return this===I.__attachedInstances.pop()}_modelessChanged(e){e?(this._removeGlobalListeners(),this._exitModalState()):this.opened&&(this._addGlobalListeners(),this._enterModalState())}_addGlobalListeners(){document.addEventListener("mousedown",this._boundMouseDownListener),document.addEventListener("mouseup",this._boundMouseUpListener),document.documentElement.addEventListener("click",this._boundOutsideClickListener,!0)}_enterModalState(){document.body.style.pointerEvents!=="none"&&(this._previousDocumentPointerEvents=document.body.style.pointerEvents,document.body.style.pointerEvents="none"),I.__attachedInstances.forEach(e=>{e!==this&&(e.shadowRoot.querySelector('[part="overlay"]').style.pointerEvents="none")})}_removeGlobalListeners(){document.removeEventListener("mousedown",this._boundMouseDownListener),document.removeEventListener("mouseup",this._boundMouseUpListener),document.documentElement.removeEventListener("click",this._boundOutsideClickListener,!0)}_exitModalState(){this._previousDocumentPointerEvents!==void 0&&(document.body.style.pointerEvents=this._previousDocumentPointerEvents,delete this._previousDocumentPointerEvents);const e=I.__attachedInstances;let t;for(;(t=e.pop())&&!(t!==this&&(t.shadowRoot.querySelector('[part="overlay"]').style.removeProperty("pointer-events"),!t.modeless)););}_removeOldContent(){!this.content||!this._contentNodes||(this._observer.disconnect(),this._contentNodes.forEach(e=>{e.parentNode===this.content&&this.content.removeChild(e)}),this._originalContentPart&&(this.$.content.parentNode.replaceChild(this._originalContentPart,this.$.content),this.$.content=this._originalContentPart,this._originalContentPart=void 0),this._observer.connect(),this._contentNodes=void 0,this.content=void 0)}_stampOverlayTemplate(e){this._removeOldContent(),e._Templatizer||(e._Templatizer=fe(e,this,{forwardHostProp(s,o){this._instance&&this._instance.forwardHostProp(s,o)}})),this._instance=new e._Templatizer({}),this._contentNodes=Array.from(this._instance.root.childNodes);const t=e._templateRoot||(e._templateRoot=e.getRootNode());if(t!==document){this.$.content.shadowRoot||this.$.content.attachShadow({mode:"open"});let s=Array.from(t.querySelectorAll("style")).reduce((o,n)=>o+n.textContent,"");if(s=s.replace(/:host/g,":host-nomatch"),s){const o=document.createElement("style");o.textContent=s,this.$.content.shadowRoot.appendChild(o),this._contentNodes.unshift(o)}this.$.content.shadowRoot.appendChild(this._instance.root),this.content=this.$.content.shadowRoot}else this.appendChild(this._instance.root),this.content=this}_removeNewRendererOrTemplate(e,t,s,o){e!==t?this.template=void 0:s!==o&&(this.renderer=void 0)}_templateOrRendererChanged(e,t,s,o,n){if(e&&t)throw this._removeNewRendererOrTemplate(e,this._oldTemplate,t,this._oldRenderer),new Error("You should only use either a renderer or a template for overlay content");const r=this._oldOwner!==s||this._oldModel!==o;this._oldModel=o,this._oldOwner=s;const a=this._oldTemplate!==e;this._oldTemplate=e;const l=this._oldRenderer!==t;this._oldRenderer=t;const d=this._oldOpened!==n;this._oldOpened=n,l&&(this.content=this,this.content.innerHTML="",delete this.content._$litPart$),e&&a?this._stampOverlayTemplate(e):t&&(l||d||r)&&n&&this.requestContentUpdate()}_getActiveElement(){let e=document.activeElement||document.body;for(;e.shadowRoot&&e.shadowRoot.activeElement;)e=e.shadowRoot.activeElement;return e}_deepContains(e){if(this.contains(e))return!0;let t=e;const s=e.ownerDocument;for(;t&&t!==s&&t!==this;)t=t.parentNode||t.host;return t===this}bringToFront(){let e="";const t=I.__attachedInstances.filter(s=>s!==this).pop();t&&(e=t.__zIndex+1),this.style.zIndex=e,this.__zIndex=e||parseFloat(getComputedStyle(this).zIndex)}}customElements.define(I.is,I);const Aa=v`
  :host {
    --vaadin-tooltip-offset-top: var(--lumo-space-xs);
    --vaadin-tooltip-offset-bottom: var(--lumo-space-xs);
    --vaadin-tooltip-offset-start: var(--lumo-space-xs);
    --vaadin-tooltip-offset-end: var(--lumo-space-xs);
  }

  [part='overlay'] {
    background: var(--lumo-base-color) linear-gradient(var(--lumo-contrast-5pct), var(--lumo-contrast-5pct));
    color: var(--lumo-body-text-color);
    font-size: var(--lumo-font-size-xs);
    line-height: var(--lumo-line-height-s);
  }

  [part='content'] {
    padding: var(--lumo-space-xs) var(--lumo-space-s);
  }
`;y("vaadin-tooltip-overlay",[Dt,Aa],{moduleId:"lumo-tooltip-overlay"});/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const Ze={start:"top",end:"bottom"},Qe={start:"left",end:"right"},yi=new ResizeObserver(i=>{setTimeout(()=>{i.forEach(e=>{e.target.__overlay&&e.target.__overlay._updatePosition()})})}),Ea=i=>class extends i{static get properties(){return{positionTarget:{type:Object,value:null},horizontalAlign:{type:String,value:"start"},verticalAlign:{type:String,value:"top"},noHorizontalOverlap:{type:Boolean,value:!1},noVerticalOverlap:{type:Boolean,value:!1},requiredVerticalSpace:{type:Number,value:0}}}static get observers(){return["__positionSettingsChanged(horizontalAlign, verticalAlign, noHorizontalOverlap, noVerticalOverlap, requiredVerticalSpace)","__overlayOpenedChanged(opened, positionTarget)"]}constructor(){super(),this.__onScroll=this.__onScroll.bind(this),this._updatePosition=this._updatePosition.bind(this)}connectedCallback(){super.connectedCallback(),this.opened&&this.__addUpdatePositionEventListeners()}disconnectedCallback(){super.disconnectedCallback(),this.__removeUpdatePositionEventListeners()}__addUpdatePositionEventListeners(){window.addEventListener("resize",this._updatePosition),this.__positionTargetAncestorRootNodes=Lr(this.positionTarget),this.__positionTargetAncestorRootNodes.forEach(t=>{t.addEventListener("scroll",this.__onScroll,!0)})}__removeUpdatePositionEventListeners(){window.removeEventListener("resize",this._updatePosition),this.__positionTargetAncestorRootNodes&&(this.__positionTargetAncestorRootNodes.forEach(t=>{t.removeEventListener("scroll",this.__onScroll,!0)}),this.__positionTargetAncestorRootNodes=null)}__overlayOpenedChanged(t,s){if(this.__removeUpdatePositionEventListeners(),s&&(s.__overlay=null,yi.unobserve(s),t&&(this.__addUpdatePositionEventListeners(),s.__overlay=this,yi.observe(s))),t){const o=getComputedStyle(this);this.__margins||(this.__margins={},["top","bottom","left","right"].forEach(n=>{this.__margins[n]=parseInt(o[n],10)})),this.setAttribute("dir",o.direction),this._updatePosition(),requestAnimationFrame(()=>this._updatePosition())}}get __isRTL(){return this.getAttribute("dir")==="rtl"}__positionSettingsChanged(){this._updatePosition()}__onScroll(t){this.contains(t.target)||this._updatePosition()}_updatePosition(){if(!this.positionTarget||!this.opened)return;const t=this.positionTarget.getBoundingClientRect(),s=this.__shouldAlignStartVertically(t);this.style.justifyContent=s?"flex-start":"flex-end";const o=this.__shouldAlignStartHorizontally(t,this.__isRTL),n=!this.__isRTL&&o||this.__isRTL&&!o;this.style.alignItems=n?"flex-start":"flex-end";const r=this.getBoundingClientRect(),a=this.__calculatePositionInOneDimension(t,r,this.noVerticalOverlap,Ze,this,s),l=this.__calculatePositionInOneDimension(t,r,this.noHorizontalOverlap,Qe,this,o);Object.assign(this.style,a,l),this.toggleAttribute("bottom-aligned",!s),this.toggleAttribute("top-aligned",s),this.toggleAttribute("end-aligned",!n),this.toggleAttribute("start-aligned",n)}__shouldAlignStartHorizontally(t,s){const o=Math.max(this.__oldContentWidth||0,this.$.overlay.offsetWidth);this.__oldContentWidth=this.$.overlay.offsetWidth;const n=Math.min(window.innerWidth,document.documentElement.clientWidth),r=!s&&this.horizontalAlign==="start"||s&&this.horizontalAlign==="end";return this.__shouldAlignStart(t,o,n,this.__margins,r,this.noHorizontalOverlap,Qe)}__shouldAlignStartVertically(t){const s=this.requiredVerticalSpace||Math.max(this.__oldContentHeight||0,this.$.overlay.offsetHeight);this.__oldContentHeight=this.$.overlay.offsetHeight;const o=Math.min(window.innerHeight,document.documentElement.clientHeight),n=this.verticalAlign==="top";return this.__shouldAlignStart(t,s,o,this.__margins,n,this.noVerticalOverlap,Ze)}__shouldAlignStart(t,s,o,n,r,a,l){const d=o-t[a?l.end:l.start]-n[l.end],c=t[a?l.start:l.end]-n[l.start],h=r?d:c,p=h>(r?c:d)||h>s;return r===p}__adjustBottomProperty(t,s,o){let n;if(t===s.end){if(s.end===Ze.end){const r=Math.min(window.innerHeight,document.documentElement.clientHeight);if(o>r&&this.__oldViewportHeight){const a=this.__oldViewportHeight-r;n=o-a}this.__oldViewportHeight=r}if(s.end===Qe.end){const r=Math.min(window.innerWidth,document.documentElement.clientWidth);if(o>r&&this.__oldViewportWidth){const a=this.__oldViewportWidth-r;n=o-a}this.__oldViewportWidth=r}}return n}__calculatePositionInOneDimension(t,s,o,n,r,a){const l=a?n.start:n.end,d=a?n.end:n.start,c=parseFloat(r.style[l]||getComputedStyle(r)[l]),h=this.__adjustBottomProperty(l,n,c),u=s[a?n.start:n.end]-t[o===a?n.end:n.start],p=h?`${h}px`:`${c+u*(a?-1:1)}px`;return{[l]:p,[d]:""}}};/**
 * @license
 * Copyright (c) 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */y("vaadin-tooltip-overlay",v`
    [part='overlay'] {
      max-width: 40ch;
    }

    :host([position^='top'][top-aligned]) [part='overlay'],
    :host([position^='bottom'][top-aligned]) [part='overlay'] {
      margin-top: var(--vaadin-tooltip-offset-top, 0);
    }

    :host([position^='top'][bottom-aligned]) [part='overlay'],
    :host([position^='bottom'][bottom-aligned]) [part='overlay'] {
      margin-bottom: var(--vaadin-tooltip-offset-bottom, 0);
    }

    :host([position^='start'][start-aligned]) [part='overlay'],
    :host([position^='end'][start-aligned]) [part='overlay'] {
      margin-inline-start: var(--vaadin-tooltip-offset-start, 0);
    }

    :host([position^='start'][end-aligned]) [part='overlay'],
    :host([position^='end'][end-aligned]) [part='overlay'] {
      margin-inline-end: var(--vaadin-tooltip-offset-end, 0);
    }
  `,{moduleId:"vaadin-tooltip-overlay-styles"});let te;class bi extends Ea(I){static get is(){return"vaadin-tooltip-overlay"}static get template(){return te||(te=super.template.cloneNode(!0),te.content.querySelector('[part~="overlay"]').removeAttribute("tabindex"),te.content.querySelector('[part~="content"]').innerHTML="<slot></slot>"),te}static get properties(){return{position:{type:String,reflectToAttribute:!0}}}ready(){super.ready(),this.owner=this.__dataHost,this.owner._overlayElement=this}requestContentUpdate(){if(super.requestContentUpdate(),this.toggleAttribute("hidden",this.textContent.trim()===""),this.positionTarget&&this.owner){const e=getComputedStyle(this.owner);["top","bottom","start","end"].forEach(t=>{this.style.setProperty(`--vaadin-tooltip-offset-${t}`,e.getPropertyValue(`--vaadin-tooltip-offset-${t}`))})}}_updatePosition(){if(super._updatePosition(),!!this.positionTarget){if(this.position==="bottom"||this.position==="top"){const e=this.positionTarget.getBoundingClientRect(),t=this.$.overlay.getBoundingClientRect(),s=e.width/2-t.width/2;if(this.style.left){const o=t.left+s;o>0&&(this.style.left=`${o}px`)}if(this.style.right){const o=parseFloat(this.style.right)+s;o>0&&(this.style.right=`${o}px`)}}if(this.position==="start"||this.position==="end"){const e=this.positionTarget.getBoundingClientRect(),t=this.$.overlay.getBoundingClientRect(),s=e.height/2-t.height/2;this.style.top=`${t.top+s}px`}}}}customElements.define(bi.is,bi);/**
 * @license
 * Copyright (c) 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const W=500;let Vs=W,Us=W,js=W;const Pe=new Set;let ie=!1,se=null,oe=null;class Pa{constructor(e){this.host=e}open(e={immediate:!1}){const{immediate:t,hover:s,focus:o}=e,n=s&&this.hoverDelay>0,r=o&&this.focusDelay>0;!t&&(n||r)&&!this.__closeTimeout?this.__warmupTooltip(r):this.__showTooltip()}close(e){!e&&this.hideDelay>0?this.__scheduleClose():(this.__abortClose(),this._setOpened(!1)),this.__abortWarmUp(),ie&&(this.__abortCooldown(),this.__scheduleCooldown())}get openedProp(){return this.host.manual?"opened":"_autoOpened"}get focusDelay(){const e=this.host;return e.focusDelay!=null&&e.focusDelay>0?e.focusDelay:Vs}get hoverDelay(){const e=this.host;return e.hoverDelay!=null&&e.hoverDelay>0?e.hoverDelay:Us}get hideDelay(){const e=this.host;return e.hideDelay!=null&&e.hideDelay>0?e.hideDelay:js}_isOpened(){return this.host[this.openedProp]}_setOpened(e){this.host[this.openedProp]=e}__flushClosingTooltips(){Pe.forEach(e=>{e._stateController.close(!0),Pe.delete(e)})}__showTooltip(){this.__abortClose(),this.__flushClosingTooltips(),this._setOpened(!0),ie=!0,this.__abortWarmUp(),this.__abortCooldown()}__warmupTooltip(e){this._isOpened()||(ie?this.__showTooltip():this.__scheduleWarmUp(e))}__abortClose(){this.__closeTimeout&&(clearTimeout(this.__closeTimeout),this.__closeTimeout=null)}__abortCooldown(){oe&&(clearTimeout(oe),oe=null)}__abortWarmUp(){se&&(clearTimeout(se),se=null)}__scheduleClose(){this._isOpened()&&(Pe.add(this.host),this.__closeTimeout=setTimeout(()=>{Pe.delete(this.host),this.__closeTimeout=null,this._setOpened(!1)},this.hideDelay))}__scheduleCooldown(){oe=setTimeout(()=>{oe=null,ie=!1},this.hideDelay)}__scheduleWarmUp(e){const t=e?this.focusDelay:this.hoverDelay;se=setTimeout(()=>{se=null,ie=!0,this.__showTooltip()},t)}}class wi extends Ti(k(b)){static get is(){return"vaadin-tooltip"}static get template(){return C`
      <style>
        :host {
          display: none;
        }
      </style>
      <vaadin-tooltip-overlay
        id="[[_uniqueId]]"
        role="tooltip"
        renderer="[[_renderer]]"
        theme$="[[_theme]]"
        opened="[[__computeOpened(manual, opened, _autoOpened, _isConnected)]]"
        position-target="[[target]]"
        position="[[__effectivePosition]]"
        no-horizontal-overlap$="[[__computeNoHorizontalOverlap(__effectivePosition)]]"
        no-vertical-overlap$="[[__computeNoVerticalOverlap(__effectivePosition)]]"
        horizontal-align="[[__computeHorizontalAlign(__effectivePosition)]]"
        vertical-align="[[__computeVerticalAlign(__effectivePosition)]]"
        on-mouseleave="__onOverlayMouseLeave"
        modeless
      ></vaadin-tooltip-overlay>
    `}static get properties(){return{context:{type:Object,value:()=>({})},focusDelay:{type:Number},for:{type:String,observer:"__forChanged"},hideDelay:{type:Number},hoverDelay:{type:Number},manual:{type:Boolean,value:!1},opened:{type:Boolean,value:!1},position:{type:String},shouldShow:{type:Object,value:()=>(e,t)=>!0},target:{type:Object,observer:"__targetChanged"},text:{type:String,observer:"__textChanged"},generator:{type:Object},_autoOpened:{type:Boolean,observer:"__autoOpenedChanged"},_position:{type:String,value:"bottom"},__effectivePosition:{type:String,computed:"__computePosition(position, _position)"},_overlayElement:Object,__isTargetHidden:{type:Boolean,value:!1},_isConnected:{type:Boolean}}}static get observers(){return["__generatorChanged(_overlayElement, generator, context)"]}static setDefaultFocusDelay(e){Vs=e!=null&&e>=0?e:W}static setDefaultHideDelay(e){js=e!=null&&e>=0?e:W}static setDefaultHoverDelay(e){Us=e!=null&&e>=0?e:W}constructor(){super(),this._uniqueId=`vaadin-tooltip-${ns()}`,this._renderer=this.__tooltipRenderer.bind(this),this.__onFocusin=this.__onFocusin.bind(this),this.__onFocusout=this.__onFocusout.bind(this),this.__onMouseDown=this.__onMouseDown.bind(this),this.__onMouseEnter=this.__onMouseEnter.bind(this),this.__onMouseLeave=this.__onMouseLeave.bind(this),this.__onKeyDown=this.__onKeyDown.bind(this),this.__onOverlayOpen=this.__onOverlayOpen.bind(this),this.__targetVisibilityObserver=new IntersectionObserver(([e])=>{this.__onTargetVisibilityChange(e.isIntersecting)},{threshold:1}),this._stateController=new Pa(this)}connectedCallback(){super.connectedCallback(),this._isConnected=!0,document.body.addEventListener("vaadin-overlay-open",this.__onOverlayOpen)}disconnectedCallback(){super.disconnectedCallback(),this._autoOpened&&this._stateController.close(!0),this._isConnected=!1,document.body.removeEventListener("vaadin-overlay-open",this.__onOverlayOpen)}__computeHorizontalAlign(e){return["top-end","bottom-end","start-top","start","start-bottom"].includes(e)?"end":"start"}__computeNoHorizontalOverlap(e){return["start-top","start","start-bottom","end-top","end","end-bottom"].includes(e)}__computeNoVerticalOverlap(e){return["top-start","top-end","top","bottom-start","bottom","bottom-end"].includes(e)}__computeVerticalAlign(e){return["top-start","top-end","top","start-bottom","end-bottom"].includes(e)?"bottom":"top"}__computeOpened(e,t,s,o){return o&&(e?t:s)}__computePosition(e,t){return e||t}__tooltipRenderer(e){e.textContent=typeof this.generator=="function"?this.generator(this.context):this.text}__autoOpenedChanged(e,t){e?document.addEventListener("keydown",this.__onKeyDown,!0):t&&document.removeEventListener("keydown",this.__onKeyDown,!0)}__forChanged(e){if(e){const t=this.getRootNode().getElementById(e);t?this.target=t:console.warn(`No element with id="${e}" found to show tooltip.`)}}__targetChanged(e,t){t&&(t.removeEventListener("mouseenter",this.__onMouseEnter),t.removeEventListener("mouseleave",this.__onMouseLeave),t.removeEventListener("focusin",this.__onFocusin),t.removeEventListener("focusout",this.__onFocusout),t.removeEventListener("mousedown",this.__onMouseDown),this.__targetVisibilityObserver.unobserve(t),Ps(t,"aria-describedby",this._uniqueId)),e&&(e.addEventListener("mouseenter",this.__onMouseEnter),e.addEventListener("mouseleave",this.__onMouseLeave),e.addEventListener("focusin",this.__onFocusin),e.addEventListener("focusout",this.__onFocusout),e.addEventListener("mousedown",this.__onMouseDown),requestAnimationFrame(()=>{this.__targetVisibilityObserver.observe(e)}),Es(e,"aria-describedby",this._uniqueId))}__onFocusin(e){this.manual||!ps()||this.target.contains(e.relatedTarget)||!this.__isShouldShow()||(this.__focusInside=!0,!this.__isTargetHidden&&(!this.__hoverInside||!this._autoOpened)&&this._stateController.open({focus:!0}))}__onFocusout(e){this.manual||this.target.contains(e.relatedTarget)||(this.__focusInside=!1,this.__hoverInside||this._stateController.close(!0))}__onKeyDown(e){e.key==="Escape"&&(e.stopPropagation(),this._stateController.close(!0))}__onMouseDown(){this._stateController.close(!0)}__onMouseEnter(){this.manual||!this.__isShouldShow()||this.__hoverInside||(this.__hoverInside=!0,!this.__isTargetHidden&&(!this.__focusInside||!this._autoOpened)&&this._stateController.open({hover:!0}))}__onMouseLeave(e){e.relatedTarget!==this._overlayElement&&this.__handleMouseLeave()}__onOverlayMouseLeave(e){e.relatedTarget!==this.target&&this.__handleMouseLeave()}__handleMouseLeave(){this.manual||(this.__hoverInside=!1,this.__focusInside||this._stateController.close())}__onOverlayOpen(){this.manual||this._overlayElement.opened&&!this._overlayElement._last&&this._stateController.close(!0)}__onTargetVisibilityChange(e){const t=this.__isTargetHidden;if(this.__isTargetHidden=!e,t&&e&&(this.__focusInside||this.__hoverInside)){this._stateController.open({immediate:!0});return}!e&&this._autoOpened&&this._stateController.close(!0)}__isShouldShow(){return!(typeof this.shouldShow=="function"&&this.shouldShow(this.target,this.context)!==!0)}__textChanged(e,t){this._overlayElement&&(e||t)&&this._overlayElement.requestContentUpdate()}__generatorChanged(e,t,s){e&&((t!==this.__oldTextGenerator||s!==this.__oldContext)&&e.requestContentUpdate(),this.__oldTextGenerator=t,this.__oldContext=s)}}customElements.define(wi.is,wi);/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */class mt extends Ks{constructor(e){if(super(e),this.it=G,e.type!==Js.CHILD)throw Error(this.constructor.directiveName+"() can only be used in child bindings")}render(e){if(e===G||e==null)return this._t=void 0,this.it=e;if(e===Xs)return e;if(typeof e!="string")throw Error(this.constructor.directiveName+"() called with a non-string value");if(e===this.it)return this._t;this.it=e;const t=[e];return t.raw=t,this._t={_$litType$:this.constructor.resultType,strings:t,values:[]}}}mt.directiveName="unsafeHTML",mt.resultType=1;/**
 * @license
 * Copyright 2017 Google LLC
 * SPDX-License-Identifier: BSD-3-Clause
 */class gt extends mt{}gt.directiveName="unsafeSVG",gt.resultType=2;const Ta=Zs(gt);/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */function Oa(i){let e=G;if(i){const t=i.cloneNode(!0);t.removeAttribute("id"),e=Qs`${Ta(t.outerHTML)}`}return e}function Sa(i){return vs(i,Er.SVG)||i===G}function Na(i){let e=i==null||i===""?G:i;return Sa(e)||(console.error("[vaadin-icon] Invalid svg passed, please use Lit svg literal."),e=G),e}function Sl(i,e){const t=Na(i);Oi(t,e)}/**
 * @license
 * Copyright (c) 2021 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const ne={};class Le extends k(b){static get template(){return null}static get is(){return"vaadin-iconset"}static get properties(){return{name:{type:String,observer:"__nameChanged"},size:{type:Number,value:24}}}static getIconset(e){let t=ne[e];return t||(t=document.createElement("vaadin-iconset"),t.name=e,ne[e]=t),t}connectedCallback(){super.connectedCallback(),this.style.display="none"}applyIcon(e){this._icons=this._icons||this.__createIconMap();const t=this._icons[this.__getIconId(e)];return{svg:Oa(t),size:this.size,viewBox:t?t.getAttribute("viewBox"):null}}__createIconMap(){const e={};return this.querySelectorAll("[id]").forEach(t=>{e[this.__getIconId(t.id)]=t}),e}__getIconId(e){return(e||"").replace(`${this.name}:`,"")}__nameChanged(e,t){t&&(ne[e]=Le.getIconset(t),delete ne[t]),e&&(ne[e]=this,document.dispatchEvent(new CustomEvent("vaadin-iconset-registered",{detail:e})))}}customElements.define(Le.is,Le);/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */const qs=document.createElement("template");qs.innerHTML=`<vaadin-iconset name="lumo" size="1000">
<svg xmlns="http://www.w3.org/2000/svg">
<defs>
<g id="lumo:align-center"><path d="M167 217c0-18 17-33 38-34H795c21 0 38 15 38 34 0 18-17 33-38 33H205C184 250 167 235 167 217z m83 191c0-18 13-33 29-33H721c16 0 29 15 29 33 0 18-13 33-29 34H279C263 442 250 427 250 408zM250 792c0-18 13-33 29-34H721c16 0 29 15 29 34s-13 33-29 33H279C263 825 250 810 250 792z m-83-192c0-18 17-33 38-33H795c21 0 38 15 38 33s-17 33-38 33H205C184 633 167 618 167 600z" fill-rule="evenodd" clip-rule="evenodd"></path></g>
<g id="lumo:align-left"><path d="M167 217c0-18 17-33 38-34H795c21 0 38 15 38 34 0 18-17 33-38 33H205C184 250 167 235 167 217z m0 191c0-18 13-33 28-33H638c16 0 29 15 29 33 0 18-13 33-29 34H195C179 442 167 427 167 408zM167 792c0-18 13-33 28-34H638c16 0 29 15 29 34s-13 33-29 33H195C179 825 167 810 167 792z m0-192c0-18 17-33 38-33H795c21 0 38 15 38 33s-17 33-38 33H205C184 633 167 618 167 600z" fill-rule="evenodd" clip-rule="evenodd"></path></g>
<g id="lumo:align-right"><path d="M167 217c0-18 17-33 38-34H795c21 0 38 15 38 34 0 18-17 33-38 33H205C184 250 167 235 167 217z m166 191c0-18 13-33 29-33H805c16 0 29 15 28 33 0 18-13 33-28 34H362C346 442 333 427 333 408zM333 792c0-18 13-33 29-34H805c16 0 29 15 28 34s-13 33-28 33H362C346 825 333 810 333 792z m-166-192c0-18 17-33 38-33H795c21 0 38 15 38 33s-17 33-38 33H205C184 633 167 618 167 600z" fill-rule="evenodd" clip-rule="evenodd"></path></g>
<g id="lumo:angle-down"><path d="M283 391c-18-16-46-15-63 4-16 18-15 46 3 63l244 224c17 15 43 15 60 0l250-229c18-16 20-45 3-63-16-18-45-20-63-4l-220 203-214-198z"></path></g>
<g id="lumo:angle-left"><path d="M601 710c16 18 15 46-3 63-18 16-46 15-63-4l-224-244c-15-17-15-43 0-59l229-250c16-18 45-20 63-4 18 16 20 45 3 63l-203 220 198 215z"></path></g>
<g id="lumo:angle-right"><path d="M399 275c-16-18-15-46 3-63 18-16 46-15 63 4l224 244c15 17 15 43 0 59l-229 250c-16 18-45 20-63 4-18-16-20-45-3-63l203-220-198-215z"></path></g>
<g id="lumo:angle-up"><path d="M283 635c-18 16-46 15-63-3-16-18-15-46 3-63l244-224c17-15 43-15 60 0l250 229c18 16 20 45 3 63-16 18-45 20-63 3l-220-202L283 635z"></path></g>
<g id="lumo:arrow-down"><path d="M538 646l125-112c15-14 39-12 53 4 14 15 12 39-4 53l-187 166c0 0 0 0 0 0-14 13-36 12-50 0l-187-166c-15-14-17-37-4-53 14-15 37-17 53-4L462 646V312c0-21 17-38 38-37s38 17 37 37v334z"></path></g>
<g id="lumo:arrow-left"><path d="M375 538l111 125c14 15 12 39-3 53-15 14-39 12-53-4l-166-187c0 0 0 0 0 0-13-14-12-36 0-50l166-187c14-15 37-17 53-4 15 14 17 37 3 53L375 463h333c21 0 38 17 38 37 0 21-17 38-38 38h-333z"></path></g>
<g id="lumo:arrow-right"><path d="M625 538h-333c-21 0-38-17-38-38 0-21 17-38 38-37h333l-111-126c-14-15-12-39 3-53 15-14 39-12 53 4l166 187c13 14 13 36 0 50 0 0 0 0 0 0l-166 187c-14 15-37 17-53 4-15-14-17-37-3-53l111-125z"></path></g>
<g id="lumo:arrow-up"><path d="M538 354V688c0 21-17 38-38 37s-38-17-38-38V354l-125 112c-15 14-39 12-53-4-14-15-12-39 4-53l187-166c14-13 36-13 50 0 0 0 0 0 0 0l187 166c15 14 17 37 4 53-14 15-37 17-53 4L538 354z"></path></g>
<g id="lumo:bar-chart"><path d="M175 500h108c28 0 50 22 50 50v233c0 28-22 50-50 50H175c-28 0-50-22-50-50v-233c0-28 22-50 50-50z m33 67c-9 0-17 7-16 16v167c0 9 7 17 16 17h42c9 0 17-7 17-17v-167c0-9-7-17-17-16H208zM446 167h108c28 0 50 22 50 50v566c0 28-22 50-50 50h-108c-28 0-50-22-50-50V217c0-28 22-50 50-50z m33 66c-9 0-17 7-17 17v500c0 9 7 17 17 17h42c9 0 17-7 16-17V250c0-9-7-17-16-17h-42zM717 333h108c28 0 50 22 50 50v400c0 28-22 50-50 50h-108c-28 0-50-22-50-50V383c0-28 22-50 50-50z m33 67c-9 0-17 7-17 17v333c0 9 7 17 17 17h42c9 0 17-7 16-17v-333c0-9-7-17-16-17h-42z"></path></g>
<g id="lumo:bell"><path d="M367 675H292v-258C292 325 366 250 459 250H458V208c0-23 18-42 42-41 23 0 42 18 42 41v42h-1C634 250 708 325 708 417V675h-75v-258c0-51-41-92-91-92h-84C408 325 367 366 367 417V675z m-159 37c0-21 17-38 38-37h508c21 0 37 17 38 37 0 21-17 38-38 38H246C225 750 208 733 208 713z m230 71h125v32c0 17-14 31-32 31h-62c-17 0-32-14-31-31v-32z"></path></g>
<g id="lumo:calendar"><path d="M375 208h250v-20C625 176 634 167 646 167h41C699 167 708 176 708 188V208h74c23 0 41 19 41 42v42C823 315 804 333 782 333H218C196 333 177 315 177 292V250C177 227 196 208 218 208H292v-20C292 176 301 167 313 167h41C366 167 375 176 375 188V208zM229 375h42C283 375 292 384 292 396v41C292 449 282 458 271 458h-42C217 458 208 449 208 437v-41C208 384 218 375 229 375z m125 0h42C408 375 417 384 417 396v41C417 449 407 458 396 458h-42C342 458 333 449 333 437v-41C333 384 343 375 354 375z m125 0h42C533 375 542 384 542 396v41C542 449 532 458 521 458h-42C467 458 458 449 458 437v-41C458 384 468 375 479 375z m-250 125h42C283 500 292 509 292 521v41C292 574 282 583 271 583h-42C217 583 208 574 208 562v-41C208 509 218 500 229 500z m125 0h42C408 500 417 509 417 521v41C417 574 407 583 396 583h-42C342 583 333 574 333 562v-41C333 509 343 500 354 500z m125 0h42c12 0 21 9 21 21v41C542 574 532 583 521 583h-42C467 583 458 574 458 562v-41C458 509 468 500 479 500z m-250 125h42C283 625 292 634 292 646v41C292 699 282 708 271 708h-42C217 708 208 699 208 687v-41C208 634 218 625 229 625z m125 0h42C408 625 417 634 417 646v41C417 699 407 708 396 708h-42C342 708 333 699 333 687v-41C333 634 343 625 354 625z m125 0h42c12 0 21 9 21 21v41C542 699 532 708 521 708h-42C467 708 458 699 458 687v-41C458 634 468 625 479 625z m125-250h42C658 375 667 384 667 396v41C667 449 657 458 646 458h-42C592 458 583 449 583 437v-41C583 384 593 375 604 375z m0 125h42c12 0 21 9 21 21v41C667 574 657 583 646 583h-42C592 583 583 574 583 562v-41C583 509 593 500 604 500z m0 125h42c12 0 21 9 21 21v41C667 699 657 708 646 708h-42C592 708 583 699 583 687v-41C583 634 593 625 604 625z m125 0h42c12 0 21 9 21 21v41C792 699 782 708 771 708h-42C717 708 708 699 708 687v-41C708 634 718 625 729 625z m-500 125h42C283 750 292 759 292 771v41C292 824 282 833 271 833h-42C217 833 208 824 208 812v-41C208 759 218 750 229 750z m125 0h42C408 750 417 759 417 771v41C417 824 407 833 396 833h-42C342 833 333 824 333 812v-41C333 759 343 750 354 750z m125 0h42c12 0 21 9 21 21v41C542 824 532 833 521 833h-42C467 833 458 824 458 812v-41C458 759 468 750 479 750z m125 0h42c12 0 21 9 21 21v41C667 824 657 833 646 833h-42C592 833 583 824 583 812v-41C583 759 593 750 604 750z m125 0h42c12 0 21 9 21 21v41C792 824 782 833 771 833h-42C717 833 708 824 708 812v-41C708 759 718 750 729 750z m0-250h42c12 0 21 9 21 21v41C792 574 782 583 771 583h-42C717 583 708 574 708 562v-41C708 509 718 500 729 500z m0-125h42C783 375 792 384 792 396v41C792 449 782 458 771 458h-42C717 458 708 449 708 437v-41C708 384 718 375 729 375z"></path></g>
<g id="lumo:checkmark"><path d="M318 493c-15-15-38-15-53 0-15 15-15 38 0 53l136 136c15 15 38 15 53 0l323-322c15-15 15-38 0-53-15-15-38-15-54 0l-295 296-110-110z"></path></g>
<g id="lumo:chevron-down"><path d="M533 654l210-199c9-9 9-23 0-32C739 419 733 417 726 417H274C261 417 250 427 250 439c0 6 2 12 7 16l210 199c18 17 48 17 66 0z"></path></g>
<g id="lumo:chevron-left"><path d="M346 533l199 210c9 9 23 9 32 0 4-4 7-10 6-17V274C583 261 573 250 561 250c-6 0-12 2-16 7l-199 210c-17 18-17 48 0 66z"></path></g>
<g id="lumo:chevron-right"><path d="M654 533L455 743c-9 9-23 9-32 0C419 739 417 733 417 726V274C417 261 427 250 439 250c6 0 12 2 16 7l199 210c17 18 17 48 0 66z"></path></g>
<g id="lumo:chevron-up"><path d="M533 346l210 199c9 9 9 23 0 32-4 4-10 7-17 6H274C261 583 250 573 250 561c0-6 2-12 7-16l210-199c18-17 48-17 66 0z"></path></g>
<g id="lumo:clock"><path d="M538 489l85 85c15 15 15 38 0 53-15 15-38 15-53 0l-93-93a38 38 0 0 1-2-2C467 525 462 515 462 504V308c0-21 17-38 38-37 21 0 38 17 37 37v181zM500 833c-184 0-333-149-333-333s149-333 333-333 333 149 333 333-149 333-333 333z m0-68c146 0 265-118 265-265 0-146-118-265-265-265-146 0-265 118-265 265 0 146 118 265 265 265z"></path></g>
<g id="lumo:cog"><path d="M833 458l-81-18c-8-25-17-50-29-75L767 292 708 233l-72 49c-21-12-46-25-75-30L542 167h-84l-19 79c-25 8-50 17-71 30L296 233 233 296l47 69c-12 21-21 46-29 71L167 458v84l84 25c8 25 17 50 29 75L233 708 292 767l76-44c21 12 46 25 75 29L458 833h84l19-81c25-8 50-17 75-29L708 767l59-59-44-66c12-21 25-46 29-75L833 542v-84z m-333 217c-96 0-175-79-175-175 0-96 79-175 175-175 96 0 175 79 175 175 0 96-79 175-175 175z"></path></g>
<g id="lumo:cross"><path d="M445 500l-142-141c-15-15-15-40 0-56 15-15 40-15 56 0L500 445l141-142c15-15 40-15 56 0 15 15 15 40 0 56L555 500l142 141c15 15 15 40 0 56-15 15-40 15-56 0L500 555l-141 142c-15 15-40 15-56 0-15-15-15-40 0-56L445 500z"></path></g>
<g id="lumo:download"><path d="M538 521l125-112c15-14 39-12 53 4 14 15 12 39-4 53l-187 166a38 38 0 0 1 0 0c-14 13-36 12-50 0l-187-166c-15-14-17-37-4-53 14-15 37-17 53-4L462 521V188c0-21 17-38 38-38s38 17 37 38v333zM758 704c0-21 17-38 38-37 21 0 38 17 37 37v92c0 21-17 38-37 37H204c-21 0-38-17-37-37v-92c0-21 17-38 37-37s38 17 38 37v54h516v-54z"></path></g>
<g id="lumo:dropdown"><path d="M317 393c-15-14-39-13-53 3-14 15-13 39 3 53l206 189c14 13 36 13 50 0l210-193c15-14 17-38 3-53-14-15-38-17-53-3l-185 171L317 393z"></path></g>
<g id="lumo:edit"><path d="M673 281l62 56-205 233c-9 10-38 24-85 39a8 8 0 0 1-5 0c-4-1-7-6-6-10l0 0c14-47 25-76 35-86l204-232z m37-42l52-59c15-17 41-18 58-2 17 16 18 42 3 59L772 295l-62-56zM626 208l-67 75h-226C305 283 283 306 283 333v334C283 695 306 717 333 717h334c28 0 50-22 50-50v-185L792 398v269C792 736 736 792 667 792H333C264 792 208 736 208 667V333C208 264 264 208 333 208h293z"></path></g>
<g id="lumo:error"><path d="M500 833c-184 0-333-149-333-333s149-333 333-333 333 149 333 333-149 333-333 333z m0-68c146 0 265-118 265-265 0-146-118-265-265-265-146 0-265 118-265 265 0 146 118 265 265 265zM479 292h42c12 0 21 9 20 20l-11 217c0 8-6 13-13 13h-34c-7 0-13-6-13-13l-11-217C459 301 468 292 479 292zM483 608h34c12 0 21 9 20 21v33c0 12-9 21-20 21h-34c-12 0-21-9-21-21v-33c0-12 9-21 21-21z"></path></g>
<g id="lumo:eye"><path d="M500 750c-187 0-417-163-417-250s230-250 417-250 417 163 417 250-230 250-417 250z m-336-231c20 22 47 46 78 69C322 644 411 678 500 678s178-34 258-90c31-22 59-46 78-69 6-7 12-14 16-19-4-6-9-12-16-19-20-22-47-46-78-69C678 356 589 322 500 322s-178 34-258 90c-31 22-59 46-78 69-6 7-12 14-16 19 4 6 9 12 16 19zM500 646c-81 0-146-65-146-146s65-146 146-146 146 65 146 146-65 146-146 146z m0-75c39 0 71-32 71-71 0-39-32-71-71-71-39 0-71 32-71 71 0 39 32 71 71 71z"></path></g>
<g id="lumo:eye-disabled"><path d="M396 735l60-60c15 2 30 3 44 3 89 0 178-34 258-90 31-22 59-46 78-69 6-7 12-14 16-19-4-6-9-12-16-19-20-22-47-46-78-69-8-5-15-11-23-15l50-51C862 397 917 458 917 500c0 87-230 250-417 250-34 0-69-5-104-15zM215 654C138 603 83 542 83 500c0-87 230-250 417-250 34 0 69 5 104 15l-59 60c-15-2-30-3-45-3-89 0-178 34-258 90-31 22-59 46-78 69-6 7-12 14-16 19 4 6 9 12 16 19 20 22 47 46 78 69 8 5 16 11 24 16L215 654z m271-9l159-159c0 5 1 9 1 14 0 81-65 146-146 146-5 0-9 0-14-1z m-131-131C354 510 354 505 354 500c0-81 65-146 146-146 5 0 10 0 14 1l-159 159z m-167 257L780 179c12-12 32-12 44 0 12 12 12 32 0 44L232 815c-12 12-32 12-44 0s-12-32 0-44z"></path></g>
<g id="lumo:menu"><path d="M167 292c0-23 19-42 41-42h584C815 250 833 268 833 292c0 23-19 42-41 41H208C185 333 167 315 167 292z m0 208c0-23 19-42 41-42h584C815 458 833 477 833 500c0 23-19 42-41 42H208C185 542 167 523 167 500z m0 208c0-23 19-42 41-41h584C815 667 833 685 833 708c0 23-19 42-41 42H208C185 750 167 732 167 708z"></path></g>
<g id="lumo:minus"><path d="M261 461c-22 0-39 18-39 39 0 22 18 39 39 39h478c22 0 39-18 39-39 0-22-18-39-39-39H261z"></path></g>
<g id="lumo:ordered-list"><path d="M138 333V198H136l-43 28v-38l45-31h45V333H138z m-61 128c0-35 27-59 68-59 39 0 66 21 66 53 0 20-11 37-43 64l-29 27v2h74V583H80v-30l55-52c26-24 32-33 33-43 0-13-10-22-24-22-15 0-26 10-26 25v1h-41v-1zM123 759v-31h21c15 0 25-8 25-21 0-13-10-21-25-21-15 0-26 9-26 23h-41c1-34 27-56 68-57 39 0 66 20 66 49 0 20-14 36-33 39v3c24 3 40 19 39 41 0 32-30 54-73 54-41 0-69-22-70-57h43c1 13 11 22 28 22 16 0 27-9 27-22 0-14-10-22-28-22h-21zM333 258c0-18 15-33 34-33h516c18 0 33 15 34 33 0 18-15 33-34 34H367c-18 0-33-15-34-34z m0 250c0-18 15-33 34-33h516c18 0 33 15 34 33s-15 33-34 34H367c-18 0-33-15-34-34z m0 250c0-18 15-33 34-33h516c18 0 33 15 34 33s-15 33-34 34H367c-18 0-33-15-34-34z"></path></g>
<g id="lumo:phone"><path d="M296 208l42-37c17-15 44-13 58 4a42 42 0 0 1 5 7L459 282c12 20 5 45-15 57l-7 4c-17 10-25 30-19 48l20 66a420 420 0 0 0 93 157l41 45c13 14 35 17 51 8l7-5c20-12 45-5 57 16L745 777c12 20 5 45-15 57a42 42 0 0 1-8 4l-52 17c-61 21-129 4-174-43l-50-52c-81-85-141-189-175-302l-24-78c-19-62 0-129 49-172z"></path></g>
<g id="lumo:photo"><path d="M208 167h584c69 0 125 56 125 125v416c0 69-56 125-125 125H208c-69 0-125-56-125-125V292c0-69 56-125 125-125z m584 75H208c-28 0-50 22-50 50v416c0 28 22 50 50 50h584c28 0 50-22 50-50V292c0-28-22-50-50-50zM239 740l167-167c12-12 31-14 45-6l73 43 172-201c13-15 34-18 50-7l95 67v92l-111-78-169 199c-12 14-32 17-47 8l-76-43-111 111H229c2-7 5-13 10-18zM458 427C458 490 407 542 344 542S229 490 229 427c0-63 51-115 115-115S458 364 458 427z m-62 0C396 398 373 375 344 375S292 398 292 427c0 29 23 52 52 52s52-23 52-52z"></path></g>
<g id="lumo:play"><path d="M689 528l-298 175c-13 8-34 8-48 0-6-4-10-9-10-14V311C333 300 348 292 367 292c9 0 17 2 24 5l298 175c26 15 26 40 0 56z"></path></g>
<g id="lumo:plus"><path d="M461 461H261c-22 0-39 18-39 39 0 22 18 39 39 39h200v200c0 22 18 39 39 39 22 0 39-18 39-39v-200h200c22 0 39-18 39-39 0-22-18-39-39-39h-200V261c0-22-18-39-39-39-22 0-39 18-39 39v200z"></path></g>
<g id="lumo:redo"><path d="M290 614C312 523 393 458 491 458c55 0 106 22 144 57l-88 88c-3 3-5 7-5 11 0 8 6 15 15 15l193-5c17 0 31-15 31-32l5-192c0-4-1-8-4-11-6-6-16-6-22 0l-66 67C641 406 570 375 491 375c-136 0-248 90-281 215-1 2-1 5-1 8-8 44 45 68 73 32 4-5 7-11 8-16z"></path></g>
<g id="lumo:reload"><path d="M500 233V137c0-9 7-16 15-16 4 0 8 2 10 4l133 140c12 12 12 32 0 45l-133 140c-6 6-15 6-21 0C502 447 500 443 500 438V308c-117 0-212 95-212 213 0 117 95 212 212 212 117 0 212-95 212-212 0-21 17-38 38-38s38 17 37 38c0 159-129 288-287 287-159 0-288-129-288-287 0-159 129-288 288-288z"></path></g>
<g id="lumo:search"><path d="M662 603l131 131c16 16 16 42 0 59-16 16-43 16-59 0l-131-131C562 691 512 708 458 708c-138 0-250-112-250-250 0-138 112-250 250-250 138 0 250 112 250 250 0 54-17 104-46 145zM458 646c104 0 188-84 188-188S562 271 458 271 271 355 271 458s84 188 187 188z"></path></g>
<g id="lumo:undo"><path d="M710 614C688 523 607 458 509 458c-55 0-106 22-144 57l88 88c3 3 5 7 5 11 0 8-6 15-15 15l-193-5c-17 0-31-15-31-32L214 400c0-4 1-8 4-11 6-6 16-6 22 0l66 67C359 406 430 375 509 375c136 0 248 90 281 215 1 2 1 5 1 8 8 44-45 68-73 32-4-5-7-11-8-16z"></path></g>
<g id="lumo:unordered-list"><path d="M146 325c-42 0-67-26-67-63 0-37 25-63 67-63 42 0 67 26 67 63 0 37-25 63-67 63z m0 250c-42 0-67-26-67-63 0-37 25-63 67-63 42 0 67 26 67 63 0 37-25 63-67 63z m0 250c-42 0-67-26-67-63 0-37 25-63 67-63 42 0 67 26 67 63 0 37-25 63-67 63zM333 258c0-18 15-33 34-33h516c18 0 33 15 34 33 0 18-15 33-34 34H367c-18 0-33-15-34-34z m0 250c0-18 15-33 34-33h516c18 0 33 15 34 33s-15 33-34 34H367c-18 0-33-15-34-34z m0 250c0-18 15-33 34-33h516c18 0 33 15 34 33s-15 33-34 34H367c-18 0-33-15-34-34z"></path></g>
<g id="lumo:upload"><path d="M454 271V604c0 21-17 38-37 38s-38-17-38-38V271L254 382c-15 14-39 12-53-3-14-15-12-39 3-53L391 160c14-13 36-13 51-1 0 0 0 0 0 1l187 166c15 14 17 37 3 53-14 15-37 17-53 3L454 271zM675 704c0-21 17-38 37-37 21 0 38 17 38 37v92c0 21-17 38-38 37H121c-21 0-38-17-38-37v-92c0-21 17-38 38-37s38 17 37 37v54h517v-54z"></path></g>
<g id="lumo:user"><path d="M500 500c-69 0-125-56-125-125s56-125 125-125 125 56 125 125-56 125-125 125z m-292 292c0-115 131-208 292-209s292 93 292 209H208z"></path></g>
</defs>
</svg>
</vaadin-iconset>`;document.head.appendChild(qs.content);const Ia=v`
  :host([theme~='margin']) {
    margin: var(--lumo-space-m);
  }

  :host([theme~='padding']) {
    padding: var(--lumo-space-m);
  }

  :host([theme~='spacing-xs']) {
    gap: var(--lumo-space-xs);
  }

  :host([theme~='spacing-s']) {
    gap: var(--lumo-space-s);
  }

  :host([theme~='spacing']) {
    gap: var(--lumo-space-m);
  }

  :host([theme~='spacing-l']) {
    gap: var(--lumo-space-l);
  }

  :host([theme~='spacing-xl']) {
    gap: var(--lumo-space-xl);
  }
`;y("vaadin-vertical-layout",Ia,{moduleId:"lumo-vertical-layout"});/**
 * @license
 * Copyright (c) 2017 - 2022 Vaadin Ltd.
 * This program is available under Apache License Version 2.0, available at https://vaadin.com/license/
 */class Ci extends k(z(b)){static get template(){return C`
      <style>
        :host {
          display: flex;
          flex-direction: column;
          align-items: flex-start;
          box-sizing: border-box;
        }

        :host([hidden]) {
          display: none !important;
        }

        /* Theme variations */
        :host([theme~='margin']) {
          margin: 1em;
        }

        :host([theme~='padding']) {
          padding: 1em;
        }

        :host([theme~='spacing']) {
          gap: 1em;
        }
      </style>

      <slot></slot>
    `}static get is(){return"vaadin-vertical-layout"}}customElements.define(Ci.is,Ci);/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/class me{constructor(){this._asyncModule=null,this._callback=null,this._timer=null}setConfig(e,t){this._asyncModule=e,this._callback=t,this._timer=this._asyncModule.run(()=>{this._timer=null,ge.delete(this),this._callback()})}cancel(){this.isActive()&&(this._cancelAsync(),ge.delete(this))}_cancelAsync(){this.isActive()&&(this._asyncModule.cancel(this._timer),this._timer=null)}flush(){this.isActive()&&(this.cancel(),this._callback())}isActive(){return this._timer!=null}static debounce(e,t,s){return e instanceof me?e._cancelAsync():e=new me,e.setConfig(t,s),e}}let ge=new Set;const za=function(i){ge.add(i)},Ma=function(){const i=Boolean(ge.size);return ge.forEach(e=>{try{e.flush()}catch(t){setTimeout(()=>{throw t})}}),i};/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/const ka=function(){let i,e;do i=window.ShadyDOM&&ShadyDOM.flush(),window.ShadyCSS&&window.ShadyCSS.ScopingShim&&window.ShadyCSS.ScopingShim.flush(),e=Ma();while(i||e)};/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/let xi=!1;function La(){if(zi&&!Ii){if(!xi){xi=!0;const i=document.createElement("style");i.textContent="dom-bind,dom-if,dom-repeat{display:none;}",document.head.appendChild(i)}return!0}return!1}/**
@license
Copyright (c) 2017 The Polymer Project Authors. All rights reserved.
This code may only be used under the BSD style license found at http://polymer.github.io/LICENSE.txt
The complete set of authors may be found at http://polymer.github.io/AUTHORS.txt
The complete set of contributors may be found at http://polymer.github.io/CONTRIBUTORS.txt
Code distributed by Google as part of the polymer project is also
subject to an additional IP rights grant found at http://polymer.github.io/PATENTS.txt
*/class $s extends b{static get is(){return"dom-if"}static get template(){return null}static get properties(){return{if:{type:Boolean,observer:"__debounceRender"},restamp:{type:Boolean,observer:"__debounceRender"},notifyDomChange:{type:Boolean}}}constructor(){super(),this.__renderDebouncer=null,this._lastIf=!1,this.__hideTemplateChildren__=!1,this.__template,this._templateInfo}__debounceRender(){this.__renderDebouncer=me.debounce(this.__renderDebouncer,xt,()=>this.__render()),za(this.__renderDebouncer)}disconnectedCallback(){super.disconnectedCallback();const e=_(this).parentNode;(!e||e.nodeType==Node.DOCUMENT_FRAGMENT_NODE&&!_(e).host)&&this.__teardownInstance()}connectedCallback(){super.connectedCallback(),La()||(this.style.display="none"),this.if&&this.__debounceRender()}__ensureTemplate(){if(!this.__template){const e=this;let t=e._templateInfo?e:_(e).querySelector("template");if(!t){let s=new MutationObserver(()=>{if(_(this).querySelector("template"))s.disconnect(),this.__render();else throw new Error("dom-if requires a <template> child")});return s.observe(this,{childList:!0}),!1}this.__template=t}return!0}__ensureInstance(){let e=_(this).parentNode;if(this.__hasInstance()){let t=this.__getInstanceNodes();if(t&&t.length&&_(this).previousSibling!==t[t.length-1])for(let o=0,n;o<t.length&&(n=t[o]);o++)_(e).insertBefore(n,this)}else{if(!e||!this.__ensureTemplate())return!1;this.__createAndInsertInstance(e)}return!0}render(){ka()}__render(){if(this.if){if(!this.__ensureInstance())return}else this.restamp&&this.__teardownInstance();this._showHideChildren(),(!fo||this.notifyDomChange)&&this.if!=this._lastIf&&(this.dispatchEvent(new CustomEvent("dom-change",{bubbles:!0,composed:!0})),this._lastIf=this.if)}__hasInstance(){}__getInstanceNodes(){}__createAndInsertInstance(e){}__teardownInstance(){}_showHideChildren(){}}class Da extends $s{constructor(){super(),this.__instance=null,this.__syncInfo=null}__hasInstance(){return Boolean(this.__instance)}__getInstanceNodes(){return this.__instance.templateInfo.childNodes}__createAndInsertInstance(e){const t=this.__dataHost||this;if(de&&!this.__dataHost)throw new Error("strictTemplatePolicy: template owner not trusted");const s=t._bindTemplate(this.__template,!0);s.runEffects=(o,n,r)=>{let a=this.__syncInfo;if(this.if)a&&(this.__syncInfo=null,this._showHideChildren(),n=Object.assign(a.changedProps,n)),o(n,r);else if(this.__instance)if(a||(a=this.__syncInfo={runEffects:o,changedProps:{}}),r)for(const l in n){const d=M(l);a.changedProps[d]=this.__dataHost[d]}else Object.assign(a.changedProps,n)},this.__instance=t._stampTemplate(this.__template,s),_(e).insertBefore(this.__instance,this)}__syncHostProperties(){const e=this.__syncInfo;e&&(this.__syncInfo=null,e.runEffects(e.changedProps,!1))}__teardownInstance(){const e=this.__dataHost||this;this.__instance&&(e._removeBoundDom(this.__instance),this.__instance=null,this.__syncInfo=null)}_showHideChildren(){const e=this.__hideTemplateChildren__||!this.if;this.__instance&&Boolean(this.__instance.__hidden)!==e&&(this.__instance.__hidden=e,Ls(e,this.__instance.templateInfo.childNodes)),e||this.__syncHostProperties()}}class Ha extends $s{constructor(){super(),this.__ctor=null,this.__instance=null,this.__invalidProps=null}__hasInstance(){return Boolean(this.__instance)}__getInstanceNodes(){return this.__instance.children}__createAndInsertInstance(e){this.__ctor||(this.__ctor=fe(this.__template,this,{mutableData:!0,forwardHostProp:function(t,s){this.__instance&&(this.if?this.__instance.forwardHostProp(t,s):(this.__invalidProps=this.__invalidProps||Object.create(null),this.__invalidProps[M(t)]=!0))}})),this.__instance=new this.__ctor,_(e).insertBefore(this.__instance.root,this)}__teardownInstance(){if(this.__instance){let e=this.__instance.children;if(e&&e.length){let t=_(e[0]).parentNode;if(t){t=_(t);for(let s=0,o;s<e.length&&(o=e[s]);s++)t.removeChild(o)}}this.__invalidProps=null,this.__instance=null}}__syncHostProperties(){let e=this.__invalidProps;if(e){this.__invalidProps=null;for(let t in e)this.__instance._setPendingProperty(t,this.__dataHost[t]);this.__instance._flushProperties()}}_showHideChildren(){const e=this.__hideTemplateChildren__||!this.if;this.__instance&&Boolean(this.__instance.__hidden)!==e&&(this.__instance.__hidden=e,this.__instance._showHideChildren(e)),e||this.__syncHostProperties()}}const Ai=ki?Da:Ha;customElements.define(Ai.is,Ai);class Ei extends b{static get template(){return C`
      <style>
        :host {
          animation: 1ms flow-component-renderer-appear;
        }

        @keyframes flow-component-renderer-appear {
          to {
            opacity: 1;
          }
        }
      </style>
      <slot></slot>
    `}static get is(){return"flow-component-renderer"}static get properties(){return{nodeid:Number,appid:String}}static get observers(){return["_attachRenderedComponentIfAble(appid, nodeid)"]}ready(){super.ready(),this.addEventListener("click",function(e){this.firstChild&&typeof this.firstChild.click=="function"&&e.target===this&&(e.stopPropagation(),this.firstChild.click())}),this.addEventListener("animationend",this._onAnimationEnd)}_asyncAttachRenderedComponentIfAble(){this._debouncer=me.debounce(this._debouncer,Io,()=>this._attachRenderedComponentIfAble())}_attachRenderedComponentIfAble(){if(!this.nodeid||!this.appid)return;const e=this._getRenderedComponent();this.firstChild?e?this.firstChild!==e?(this.replaceChild(e,this.firstChild),this._defineFocusTarget(),this.onComponentRendered()):(this._defineFocusTarget(),this.onComponentRendered()):this._asyncAttachRenderedComponentIfAble():e?(this.appendChild(e),this._defineFocusTarget(),this.onComponentRendered()):this._asyncAttachRenderedComponentIfAble()}_getRenderedComponent(){try{return window.Vaadin.Flow.clients[this.appid].getByNodeId(this.nodeid)}catch(e){console.error("Could not get node %s from app %s",this.nodeid,this.appid),console.error(e)}return null}onComponentRendered(){}_defineFocusTarget(){var e=this._getFirstFocusableDescendant(this.firstChild);e!==null&&e.setAttribute("focus-target","true")}_getFirstFocusableDescendant(e){if(this._isFocusable(e))return e;if(e.hasAttribute&&(e.hasAttribute("disabled")||e.hasAttribute("hidden"))||!e.children)return null;for(var t=0;t<e.children.length;t++){var s=this._getFirstFocusableDescendant(e.children[t]);if(s!==null)return s}return null}_isFocusable(e){return e.hasAttribute&&typeof e.hasAttribute=="function"&&(e.hasAttribute("disabled")||e.hasAttribute("hidden"))?!1:e.tabIndex===0}_onAnimationEnd(e){e.animationName.indexOf("flow-component-renderer-appear")===0&&this._attachRenderedComponentIfAble()}}window.customElements.define(Ei.is,Ei);const Ys=document.createElement("template");Ys.innerHTML=`<style>
  ${eo.cssText}
  ${to.cssText}
</style>`;document.head.appendChild(Ys.content);(function(){function i(t){const s=t._card;s&&(s.className=t.className)}const e=new MutationObserver(t=>{t.forEach(s=>{s.type==="attributes"&&s.attributeName==="class"&&i(s.target)})});window.Vaadin.Flow.notificationConnector={initLazy:function(t){t.$connector||(t.$connector={},t.addEventListener("opened-changed",s=>{s.detail.value&&i(t)}),e.observe(t,{attributes:!0,attributeFilter:["class"]}),i(t))}}})();const Ra=function(i,e=!1){const t=document.createElement("template");t.innerHTML=i,document.head[e?"insertBefore":"appendChild"](t.content,document.head.firstChild)};let ye;const Pi=document.getElementsByTagName("script");for(let i=0;i<Pi.length;i++){const e=Pi[i];if(e.getAttribute("type")=="module"&&e.getAttribute("data-app-id")&&!e["vaadin-bundle"]){ye=e;break}}if(!ye)throw new Error("Could not find the bundle script to identify the application id");ye["vaadin-bundle"]=!0;window.Vaadin.Flow.fallbacks||(window.Vaadin.Flow.fallbacks={});const Ws=window.Vaadin.Flow.fallbacks;Ws[ye.getAttribute("data-app-id")]={};Ws[ye.getAttribute("data-app-id")].loadFallback=function(){return io(()=>import("./generated-flow-imports-fallback.a999971e.js"),["./generated-flow-imports-fallback.a999971e.js","./indexhtml.26664944.js"],import.meta.url)};const Nl=Object.freeze(Object.defineProperty({__proto__:null,addCssBlock:Ra},Symbol.toStringTag,{value:"Module"}));export{xl as $,mr as A,Tl as B,Re as C,Vr as D,k as E,Fe as F,al as G,uo as H,Ur as I,me as J,qa as K,Fr as L,_ as M,xt as N,Ba as O,b as P,Lo as Q,va as R,Nn as S,Ot as T,Qt as U,Va as V,Ya as W,zi as X,w as Y,fe as Z,El as _,rs as a,Et as a0,de as a1,La as a2,fo as a3,ja as a4,Se as a5,za as a6,ka as a7,Zn as a8,Ua as a9,ve as aA,Qa as aB,_r as aC,Os as aD,pt as aE,Le as aF,Na as aG,Sl as aH,ws as aI,Ss as aJ,K as aK,vi as aL,pi as aM,xr as aN,L as aO,Gr as aP,Kr as aQ,wi as aR,Nl as aS,Lt as aa,Ii as ab,$a as ac,Io as ad,us as ae,ns as af,il as ag,xa as ah,Ca as ai,Ni as aj,ht as ak,ul as al,ms as am,Ye as an,Dt as ao,Ea as ap,I as aq,Cs as ar,Nr as as,Br as at,Ve as au,Is as av,Yr as aw,zs as ax,It as ay,ze as az,Mr as b,zr as c,g as d,Ns as e,J as f,Ga as g,C as h,wr as i,Pt as j,Vn as k,Ka as l,Un as m,jn as n,dl as o,Sr as p,ai as q,cl as r,Ps as s,is as t,Es as u,ps as v,gs as w,ys as x,ll as y,rl as z};
