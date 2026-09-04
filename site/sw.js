const CACHE = 'bgt-v4';
const ASSETS = [
  '/BGT/',
  '/BGT/index.html',
  '/BGT/news.html',
  '/BGT/nosotros.html',
  '/BGT/privacy.html',
  '/BGT/splendor.html',
  '/BGT/spooktacular.html',
  '/BGT/criaturas.html',
  '/BGT/tiletum.html',
  '/BGT/maracaibo.html',
  '/BGT/castlecombo.html',
  '/BGT/cascadia.html',
  '/BGT/coimbra.html',
  '/BGT/viernes.html',
  '/BGT/manifest.json',
  '/BGT/assets/logo-icon.png',
  '/BGT/assets/logo-full.png',
  '/BGT/assets/bg-fondo1.jpg',
  '/BGT/assets/bg-main.jpg',
  '/BGT/assets/bg-lapiz.jpg',
  '/BGT/assets/banner_splendor.jpg',
  '/BGT/assets/banner_cascadia.jpg',
  '/BGT/assets/banner_maracaibo.jpg',
  '/BGT/assets/banner_castle_combo.jpg',
  '/BGT/assets/banner_coimbra.jpg',
  '/BGT/assets/banner_tiletum_img.jpg',
  '/BGT/assets/banner_friday.jpg',
  '/BGT/assets/banner_cm_new.jpg',
  '/BGT/assets/banner_spooktacular_new.jpg',
];

self.addEventListener('install', e => {
  e.waitUntil(caches.open(CACHE).then(c => c.addAll(ASSETS)).then(() => self.skipWaiting()));
});

self.addEventListener('activate', e => {
  e.waitUntil(
    caches.keys().then(keys => Promise.all(keys.filter(k => k !== CACHE).map(k => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', e => {
  // Páginas HTML (navegación + fetch() a .html como el de "Últimas novedades"):
  // red primero, para que nunca se quede pegado a una versión vieja del contenido.
  // Solo si no hay red se usa la copia en caché (modo offline real de la PWA).
  const isHtmlRequest = e.request.mode === 'navigate' ||
    (e.request.destination === '' && e.request.url.endsWith('.html'));

  if (isHtmlRequest) {
    e.respondWith(
      fetch(e.request)
        .then(res => {
          const copy = res.clone();
          caches.open(CACHE).then(c => c.put(e.request, copy));
          return res;
        })
        .catch(() => caches.match(e.request).then(cached => cached || caches.match('/BGT/')))
    );
    return;
  }

  // Assets estáticos (imágenes, manifest, etc.): caché primero, red de respaldo.
  e.respondWith(
    caches.match(e.request).then(cached => cached || fetch(e.request).catch(() => caches.match('/BGT/')))
  );
});
