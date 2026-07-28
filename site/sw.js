const CACHE = 'bgt-v2';
const ASSETS = [
  '/BGT/',
  '/BGT/index.html',
  '/BGT/news.html',
  '/BGT/nosotros.html',
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
  e.respondWith(
    caches.match(e.request).then(cached => cached || fetch(e.request).catch(() => caches.match('/BGT/')))
  );
});
