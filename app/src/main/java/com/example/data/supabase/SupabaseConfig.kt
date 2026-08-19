package com.example.data.supabase

import android.util.Log
import com.example.BuildConfig

object SupabaseConfig {
    private const val TAG = "SupabaseConfig"

    private fun clean(value: String): String {
        return value.trim().removeSurrounding("\"").removeSurrounding("'").trim()
    }

    val url: String
        get() {
            val u = clean(try { BuildConfig.SUPABASE_URL } catch (_: Exception) { "" })
            val vU = clean(try { BuildConfig.VITE_SUPABASE_URL } catch (_: Exception) { "" })
            val raw = when {
                u.isNotBlank() && u != "https://your-project.supabase.co" && !u.contains("your-project") -> u
                vU.isNotBlank() && vU != "https://your-project.supabase.co" && !vU.contains("your-project") -> vU
                else -> ""
            }
            return if (raw.endsWith("/")) raw.dropLast(1) else raw
        }

    val anonKey: String
        get() {
            val k = clean(try { BuildConfig.SUPABASE_ANON_KEY } catch (_: Exception) { "" })
            val vK = clean(try { BuildConfig.VITE_SUPABASE_ANON_KEY } catch (_: Exception) { "" })
            return when {
                k.isNotBlank() && k != "your-supabase-anon-key" && !k.contains("your-supabase") -> k
                vK.isNotBlank() && vK != "your-supabase-anon-key" && !vK.contains("your-supabase") -> vK
                else -> ""
            }
        }

    val isConfigured: Boolean
        get() {
            val valid = url.isNotBlank() &&
                    url.startsWith("https://") &&
                    !url.contains("your-project") &&
                    anonKey.isNotBlank() &&
                    anonKey != "your-supabase-anon-key" &&
                    !anonKey.contains("your-supabase")

            if (!valid) {
                Log.w(TAG, "Supabase credentials are not fully configured or are placeholder defaults.")
            }
            return valid
        }
}
