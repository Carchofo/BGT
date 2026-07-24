# Estrategia de donaciones por cohorte — BGT

> Diseñado por Opus. Implementar cuando la app tenga tracción masiva.

## Los dos flags

```kotlin
// Constante en código — se sube el número con cada release para ampliar el umbral
const val COHORT_CUTOFF_BUILD = 42  // <- perilla global de intensidad

// DataStore — se escribe UNA vez al primer arranque, nunca se modifica
val KEY_FIRST_INSTALL_BUILD = intPreferencesKey("first_install_build")
```

**Regla:** `isEarlyAdopter = first_install_build < COHORT_CUTOFF_BUILD`
Los veteranos quedan marcados para siempre aunque actualicen la app.

## Escritura del flag (al primer arranque)

```kotlin
suspend fun ensureFirstInstallBuild(ctx: Context) {
    val store = ctx.dataStore
    val existing = store.data.first()[KEY_FIRST_INSTALL_BUILD]
    if (existing == null) {
        store.edit { it[KEY_FIRST_INSTALL_BUILD] = BuildConfig.VERSION_CODE }
    }
}
```

## Comportamiento por cohorte

| | Veterano (early adopter) | Usuario nuevo (masivo) |
|---|---|---|
| Frecuencia | 1 vez cada ~30 días | cada ~7 días |
| Pantalla | solo Ajustes/Acerca de | banner en Home + Ajustes |
| Formato | texto pequeño, tono de gracias | tarjeta con botón, dismissible |
| Trigger | nunca interrumpe | tras cerrar una herramienta |

## Lógica de decisión

```kotlin
data class DonationPolicy(val show: Boolean, val style: Style)

suspend fun donationPolicy(ctx: Context): DonationPolicy {
    val prefs = ctx.dataStore.data.first()
    val firstBuild = prefs[KEY_FIRST_INSTALL_BUILD] ?: BuildConfig.VERSION_CODE
    val lastShown = prefs[KEY_DONATION_LAST_SHOWN] ?: 0L
    val early = firstBuild < COHORT_CUTOFF_BUILD

    val cooldown = if (early) 30.days else 7.days
    val due = System.currentTimeMillis() - lastShown > cooldown.inWholeMilliseconds

    return DonationPolicy(
        show = due,
        style = if (early) Style.GENTLE_THANKS else Style.CARD_CTA
    )
}
```

## Cómo escalar sin romper a veteranos

Solo mueves `COHORT_CUTOFF_BUILD` hacia arriba en cada release.
Los veteranos tienen su `first_install_build` congelado en un valor bajo — nunca cruzan.
Añadir también `KEY_DONATION_LAST_SHOWN` (Long, DataStore) para respetar la frecuencia.

## Cuándo implementar

- [ ] Cuando haya > 500 descargas activas en Play Store
- [ ] Tener Ko-fi activo antes de activar el CTA para nuevos
