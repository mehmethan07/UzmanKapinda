package com.example.uzmankapinda

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CartManager {
    private const val PREF_NAME = "shopping_cart"
    private const val KEY_CART_ITEMS = "cart_items"

    fun saveCart(context: Context, items: List<CartItem>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(items)

        prefs.edit().putString(KEY_CART_ITEMS, json).apply()
    }

    fun loadCart(context: Context): MutableList<CartItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_CART_ITEMS, null) ?: return mutableListOf()

        val type = object : TypeToken<MutableList<CartItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun clearCart(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_CART_ITEMS)
            .apply()
    }
}