(function(){var b,a,c=[].indexOf||function(f){for(var e=0,d=this.length;e<d;e++){if(e in this&&this[e]===f){return e}}return -1};b=(function(){d.prototype.default_options={selectors:["id","class","tag","nthchild"]};function d(e){if(e==null){e={}}this.options={};this.setOptions(this.default_options);this.setOptions(e)}d.prototype.setOptions=function(e){var g,f,h;if(e==null){e={}}f=[];for(g in e){h=e[g];if(this.default_options.hasOwnProperty(g)){f.push(this.options[g]=h)}else{f.push(void 0)}}return f};d.prototype.isElement=function(e){return !!((e!=null?e.nodeType:void 0)===1)};d.prototype.getParents=function(f){var g,e;e=[];if(this.isElement(f)){g=f;while(this.isElement(g)){e.push(g);g=g.parentNode}}return e};d.prototype.getTagSelector=function(e){return this.sanitizeItem(e.tagName.toLowerCase())};d.prototype.sanitizeItem=function(f){var e;e=(f.split("")).map(function(g){if(g===":"){return"\\"+(":".charCodeAt(0).toString(16).toUpperCase())+" "}else{if(/[ !"#$%&'()*+,.\/;<=>?@\[\\\]^`{|}~]/.test(g)){return"\\"+g}else{return escape(g).replace(/\%/g,"\\")}}});return e.join("")};d.prototype.getIdSelector=function(f){var g,e;g=f.getAttribute("id");if((g!=null)&&(g!=="")&&!(/\s/.exec(g))&&!(/^\d/.exec(g))){e="#"+(this.sanitizeItem(g));if(f.ownerDocument.querySelectorAll(e).length===1){return e}}return null};d.prototype.getClassSelectors=function(f){var h,g,e;e=[];h=f.getAttribute("class");if(h!=null){h=h.replace(/\s+/g," ");h=h.replace(/^\s|\s$/g,"");if(h!==""){e=(function(){var j,i,m,l;m=h.split(/\s+/);l=[];for(j=0,i=m.length;j<i;j++){g=m[j];l.push("."+(this.sanitizeItem(g)))}return l}).call(this)}}return e};d.prototype.getAttributeSelectors=function(j){var m,i,h,f,l,g,e;e=[];i=["id","class"];l=j.attributes;for(h=0,f=l.length;h<f;h++){m=l[h];if(g=m.nodeName,c.call(i,g)<0){e.push("["+m.nodeName+"="+m.nodeValue+"]")}}return e};d.prototype.getNthChildSelector=function(h){var f,g,e,l,i,j;l=h.parentNode;if(l!=null){f=0;j=l.childNodes;for(g=0,e=j.length;g<e;g++){i=j[g];if(this.isElement(i)){f++;if(i===h){return":nth-child("+f+")"}}}}return null};d.prototype.testSelector=function(g,f){var h,e;h=false;if((f!=null)&&f!==""){e=g.ownerDocument.querySelectorAll(f);if(e.length===1&&e[0]===g){h=true}}return h};d.prototype.getAllSelectors=function(f){var e;e={t:null,i:null,c:null,a:null,n:null};if(c.call(this.options.selectors,"tag")>=0){e.t=this.getTagSelector(f)}if(c.call(this.options.selectors,"id")>=0){e.i=this.getIdSelector(f)}if(c.call(this.options.selectors,"class")>=0){e.c=this.getClassSelectors(f)}if(c.call(this.options.selectors,"attribute")>=0){e.a=this.getAttributeSelectors(f)}if(c.call(this.options.selectors,"nthchild")>=0){e.n=this.getNthChildSelector(f)}return e};d.prototype.testUniqueness=function(f,e){var h,g;g=f.parentNode;h=g.querySelectorAll(e);return h.length===1&&h[0]===f};d.prototype.testCombinations=function(i,n,p){var o,g,f,j,h,e,m;e=this.getCombinations(n);for(g=0,j=e.length;g<j;g++){o=e[g];if(this.testUniqueness(i,o)){return o}}if(p!=null){m=n.map(function(k){return p+k});for(f=0,h=m.length;f<h;f++){o=m[f];if(this.testUniqueness(i,o)){return o}}}return null};d.prototype.getUniqueSelector=function(i){var g,f,e,j,l,h;h=this.getAllSelectors(i);j=this.options.selectors;for(f=0,e=j.length;f<e;f++){l=j[f];switch(l){case"id":if(h.i!=null){return h.i}break;case"tag":if(h.t!=null){if(this.testUniqueness(i,h.t)){return h.t}}break;case"class":if((h.c!=null)&&h.c.length!==0){g=this.testCombinations(i,h.c,h.t);if(g){return g}}break;case"attribute":if((h.a!=null)&&h.a.length!==0){g=this.testCombinations(i,h.a,h.t);if(g){return g}}break;case"nthchild":if(h.n!=null){return h.n}}}return"*"};d.prototype.getSelector=function(j){var e,p,h,g,m,i,n,q,f,o;e=[];n=this.getParents(j);for(h=0,m=n.length;h<m;h++){p=n[h];f=this.getUniqueSelector(p);if(f!=null){e.push(f)}}o=[];for(g=0,i=e.length;g<i;g++){p=e[g];o.unshift(p);q=o.join(" > ");if(this.testSelector(j,q)){return q}}return null};d.prototype.getCombinations=function(m){var o,n,h,f,p,g,e;if(m==null){m=[]}e=[[]];for(o=h=0,p=m.length-1;0<=p?h<=p:h>=p;o=0<=p?++h:--h){for(n=f=0,g=e.length-1;0<=g?f<=g:f>=g;n=0<=g?++f:--f){e.push(e[n].concat(m[o]))}}e.shift();e=e.sort(function(j,i){return j.length-i.length});e=e.map(function(i){return i.join("")});return e};return d})();if(typeof define!=="undefined"&&define!==null?define.amd:void 0){define([],function(){return b})}else{a=typeof exports!=="undefined"&&exports!==null?exports:this;a.CssSelectorGenerator=b}}).call(this);function getXPathForElement(d,b){var a="";var e,c;while(d!==b.documentElement){e=0;c=d;while(c){if(c.nodeType===1&&c.nodeName===d.nodeName){e+=1}c=c.previousSibling}a="*[name()='"+d.nodeName+"' and namespace-uri()='"+(d.namespaceURI===null?"":d.namespaceURI)+"']["+e+"]/"+a;d=d.parentNode}a="/*[name()='"+b.documentElement.nodeName+"' and namespace-uri()='"+(d.namespaceURI===null?"":d.namespaceURI)+"']/"+a;a=a.replace(/\/$/,"");return a}function context(c){c.preventDefault();console.log(c.target);var b=new CssSelectorGenerator();var d=b.getSelector(c.target);var a=getXPathForElement(c.target,document);alert("sel: "+d+"\r\nContents: "+c.target.innerHTML)}document.body.onclick=function(a){context(a)};