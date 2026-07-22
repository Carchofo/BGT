package com.rafel.bgt.data

import android.content.Context
import org.json.JSONObject

/**
 * Variantes de automa/solitario por juego. Vive en assets/automa-variants.json (no
 * hardcodeado) para que una variante aportada por la comunidad se pueda añadir sin
 * recompilar. Ver bgt-games-vault/decisions/2026-07-22-variantes-automa.md.
 */
data class AutomaVariant(
    val id: String,
    val displayNameKey: String,
    val logic: String,
    val author: String,
    val source: String,
    val sourceUrl: String?,
    val addedDate: String,
    val verified: Boolean,
    val isDefault: Boolean
)

object AutomaVariants {
    private var cache: Map<String, List<AutomaVariant>>? = null

    private fun load(context: Context): Map<String, List<AutomaVariant>> {
        cache?.let { return it }
        val json = context.assets.open("automa-variants.json").bufferedReader().use { it.readText() }
        val root = JSONObject(json)
        val result = mutableMapOf<String, List<AutomaVariant>>()
        root.keys().forEach { game ->
            val arr = root.getJSONArray(game)
            val variants = (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                AutomaVariant(
                    id = o.getString("id"),
                    displayNameKey = o.getString("displayNameKey"),
                    logic = o.getString("logic"),
                    author = o.getString("author"),
                    source = o.getString("source"),
                    sourceUrl = if (o.isNull("sourceUrl")) null else o.getString("sourceUrl"),
                    addedDate = o.getString("addedDate"),
                    verified = o.getBoolean("verified"),
                    isDefault = o.getBoolean("isDefault")
                )
            }
            result[game] = variants
        }
        cache = result
        return result
    }

    /** Lista de variantes para un juego. Si el JSON no tiene entradas, devuelve una
     * variante "oficial" mínima por defecto para que el juego siga funcionando igual. */
    fun forGame(context: Context, slug: String): List<AutomaVariant> {
        val variants = load(context)[slug]
        if (!variants.isNullOrEmpty()) return variants
        return listOf(
            AutomaVariant(
                id = "${slug}_default", displayNameKey = "", logic = "DEFAULT",
                author = "BGT (oficial)", source = "reglamento oficial", sourceUrl = null,
                addedDate = "", verified = true, isDefault = true
            )
        )
    }
}
