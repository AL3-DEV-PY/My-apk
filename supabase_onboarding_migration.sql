-- ==============================================================================
-- LinguaX Production Supabase Database Migration: Onboarding & User Personalization
-- Idempotent, High-Performance, Production-Ready Migration
-- ==============================================================================

-- 1. Extend PROFILES table with Onboarding & Personalization columns
ALTER TABLE public.profiles
    ADD COLUMN IF NOT EXISTS native_language_id BIGINT REFERENCES public.languages(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS learning_language_id BIGINT REFERENCES public.languages(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS current_level TEXT DEFAULT 'A1',
    ADD COLUMN IF NOT EXISTS target_level TEXT DEFAULT 'B1',
    ADD COLUMN IF NOT EXISTS age_group TEXT,
    ADD COLUMN IF NOT EXISTS gender TEXT,
    ADD COLUMN IF NOT EXISTS learning_reasons JSONB DEFAULT '[]'::jsonb,
    ADD COLUMN IF NOT EXISTS onboarding_completed BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS onboarding_step INTEGER DEFAULT 1;

-- 2. Create optimized indices for onboarding lookups
CREATE INDEX IF NOT EXISTS idx_profiles_learning_lang ON public.profiles(learning_language_id);
CREATE INDEX IF NOT EXISTS idx_profiles_native_lang ON public.profiles(native_language_id);
CREATE INDEX IF NOT EXISTS idx_profiles_onboarding ON public.profiles(onboarding_completed);

-- 3. Ensure Row Level Security (RLS) policies are active and up-to-date
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Drop prior user edit policy if present to guarantee clean idempotency
DROP POLICY IF EXISTS "Users can view and edit own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
DROP POLICY IF EXISTS "Users can insert own profile" ON public.profiles;

-- Allow authenticated users to view, insert, and update their own profile
CREATE POLICY "Users can view and edit own profile" ON public.profiles
    FOR ALL
    TO authenticated
    USING (auth.uid() = id)
    WITH CHECK (auth.uid() = id);

-- Allow public read access to basic profiles (for leaderboard)
DROP POLICY IF EXISTS "Public read profiles" ON public.profiles;
CREATE POLICY "Public read profiles" ON public.profiles
    FOR SELECT
    USING (true);

-- 4. RPC FUNCTION: save_user_onboarding (Secure & Transaction-Safe)
CREATE OR REPLACE FUNCTION public.save_user_onboarding(
    p_native_language_id BIGINT,
    p_learning_language_id BIGINT,
    p_current_level TEXT,
    p_target_level TEXT,
    p_age_group TEXT,
    p_gender TEXT,
    p_learning_reasons JSONB,
    p_daily_goal INT
)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user_id UUID;
    v_profile RECORD;
BEGIN
    -- Authenticate caller from JWT
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    -- Update or Insert profile for the authenticated user
    INSERT INTO public.profiles (
        id,
        native_language_id,
        learning_language_id,
        current_level,
        target_level,
        age_group,
        gender,
        learning_reasons,
        daily_goal,
        onboarding_completed,
        onboarding_step,
        updated_at
    )
    VALUES (
        v_user_id,
        p_native_language_id,
        p_learning_language_id,
        COALESCE(p_current_level, 'A1'),
        COALESCE(p_target_level, 'B1'),
        p_age_group,
        p_gender,
        COALESCE(p_learning_reasons, '[]'::jsonb),
        COALESCE(p_daily_goal, 15),
        TRUE,
        8,
        NOW()
    )
    ON CONFLICT (id) DO UPDATE SET
        native_language_id = EXCLUDED.native_language_id,
        learning_language_id = EXCLUDED.learning_language_id,
        current_level = EXCLUDED.current_level,
        target_level = EXCLUDED.target_level,
        age_group = EXCLUDED.age_group,
        gender = EXCLUDED.gender,
        learning_reasons = EXCLUDED.learning_reasons,
        daily_goal = EXCLUDED.daily_goal,
        onboarding_completed = TRUE,
        onboarding_step = 8,
        updated_at = NOW()
    RETURNING * INTO v_profile;

    RETURN jsonb_build_object(
        'success', true,
        'profile', to_jsonb(v_profile)
    );
END;
$$;

-- Grant execution permission to authenticated users
GRANT EXECUTE ON FUNCTION public.save_user_onboarding(BIGINT, BIGINT, TEXT, TEXT, TEXT, TEXT, JSONB, INT) TO authenticated;
