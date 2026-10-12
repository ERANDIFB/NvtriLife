package com.example.nvtrilife

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://onuyptgyillbezarwacg.supabase.co",
    supabaseKey = "sb_publishable__x_-pGVKdupspCET5uhd_A_PNq0F9e6"
) {
    install(Auth)
    install(Postgrest)
    install(Storage)
}