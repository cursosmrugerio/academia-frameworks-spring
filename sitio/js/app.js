/*
 * Lógica del sitio de estudio (JS vanilla, sin dependencias de build).
 * Lee el contenido embebido en <script type="text/markdown"> y lo renderiza
 * con marked (markdown), highlight.js (código) y mermaid (diagramas).
 */
(function () {
  'use strict';

  // 1) Cargar el contenido embebido en index.html
  var temas = Array.prototype.slice
    .call(document.querySelectorAll('script[type="text/markdown"]'))
    .map(function (s) {
      return {
        id: s.dataset.id,
        grupo: s.dataset.grupo || 'General',
        titulo: s.dataset.titulo || s.dataset.id,
        md: s.textContent
      };
    });

  var cont = document.getElementById('contenido');
  var nav = document.getElementById('nav');
  var buscador = document.getElementById('buscador');

  // 2) Configurar librerías
  if (window.marked) marked.setOptions({ gfm: true, breaks: false });
  if (window.mermaid) {
    try { mermaid.initialize({ startOnLoad: false, securityLevel: 'loose', theme: 'default' }); } catch (e) {}
  }

  function porId(id) {
    for (var i = 0; i < temas.length; i++) if (temas[i].id === id) return temas[i];
    return null;
  }

  function rutaActual() { return (location.hash || '#inicio').slice(1); }

  function construirNav(filtro) {
    var q = (filtro || '').toLowerCase().trim();
    nav.innerHTML = '';
    var orden = [], mapa = {};
    temas.forEach(function (t) {
      if (q && t.titulo.toLowerCase().indexOf(q) === -1 && t.md.toLowerCase().indexOf(q) === -1) return;
      if (!mapa[t.grupo]) { mapa[t.grupo] = []; orden.push(t.grupo); }
      mapa[t.grupo].push(t);
    });
    if (!orden.length) { nav.innerHTML = '<p class="nav-vacio">Sin resultados</p>'; return; }
    orden.forEach(function (g) {
      var h = document.createElement('div'); h.className = 'nav-grupo'; h.textContent = g; nav.appendChild(h);
      mapa[g].forEach(function (t) {
        var a = document.createElement('a');
        a.className = 'nav-item'; a.href = '#' + t.id; a.textContent = t.titulo; a.dataset.id = t.id;
        nav.appendChild(a);
      });
    });
    marcarActivo();
  }

  function marcarActivo() {
    var id = rutaActual();
    Array.prototype.forEach.call(nav.querySelectorAll('a'), function (a) {
      a.classList.toggle('activo', a.dataset.id === id);
    });
  }

  function renderMermaid(nodes) {
    if (!window.mermaid || !nodes.length) return;
    try {
      if (typeof mermaid.run === 'function') mermaid.run({ nodes: nodes });   // mermaid v10+
      else mermaid.init(undefined, nodes);                                    // mermaid v9
    } catch (e) { /* diagrama inválido: se ignora */ }
  }

  function render(id) {
    var tema = porId(id) || temas[0];
    if (!tema) { cont.innerHTML = '<p>Sin contenido.</p>'; return; }

    cont.innerHTML = window.marked ? marked.parse(tema.md) : ('<pre>' + tema.md + '</pre>');

    // Bloques ```mermaid -> <div class="mermaid">
    Array.prototype.forEach.call(cont.querySelectorAll('code.language-mermaid'), function (code) {
      var div = document.createElement('div'); div.className = 'mermaid'; div.textContent = code.textContent;
      var pre = code.closest('pre'); if (pre) { pre.replaceWith(div); } else { code.replaceWith(div); }
    });
    // Resaltado de código
    if (window.hljs) Array.prototype.forEach.call(cont.querySelectorAll('pre code'), function (el) {
      try { hljs.highlightElement(el); } catch (e) {}
    });
    // Diagramas
    renderMermaid(cont.querySelectorAll('.mermaid'));

    marcarActivo();
    document.title = tema.titulo + ' · Guía de estudio';
    window.scrollTo(0, 0);
    document.body.classList.remove('nav-abierto');
  }

  window.addEventListener('hashchange', function () { render(rutaActual()); });
  if (buscador) buscador.addEventListener('input', function (e) { construirNav(e.target.value); });

  // Tema claro / oscuro (persistente)
  var toggle = document.getElementById('toggle-tema');
  if (localStorage.getItem('tema') === 'dark') document.documentElement.setAttribute('data-theme', 'dark');
  if (toggle) toggle.addEventListener('click', function () {
    var oscuro = document.documentElement.getAttribute('data-theme') === 'dark';
    if (oscuro) { document.documentElement.removeAttribute('data-theme'); localStorage.setItem('tema', 'light'); }
    else { document.documentElement.setAttribute('data-theme', 'dark'); localStorage.setItem('tema', 'dark'); }
  });

  // Menú lateral en móvil
  var menuBtn = document.getElementById('menu-btn');
  if (menuBtn) menuBtn.addEventListener('click', function () { document.body.classList.toggle('nav-abierto'); });

  construirNav();
  render(rutaActual());
})();
