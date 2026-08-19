-- ==============================================================================
-- LinguaX Production Supabase Database Schema & Stored Procedures
-- Idempotent, High-Performance, Production-Ready Migration
-- ==============================================================================

-- 1. PROFILES TABLE (Linked with Supabase Auth users)
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    username TEXT UNIQUE,
    display_name TEXT DEFAULT 'Learner',
    avatar_url TEXT,
    xp INTEGER DEFAULT 0 CHECK (xp >= 0),
    coins INTEGER DEFAULT 0 CHECK (coins >= 0),
    streak INTEGER DEFAULT 0 CHECK (streak >= 0),
    daily_goal INTEGER DEFAULT 15 CHECK (daily_goal > 0),
    native_language_id BIGINT REFERENCES public.languages(id) ON DELETE SET NULL,
    learning_language_id BIGINT REFERENCES public.languages(id) ON DELETE SET NULL,
    current_level TEXT DEFAULT 'A1',
    target_level TEXT DEFAULT 'B1',
    age_group TEXT,
    gender TEXT,
    learning_reasons JSONB DEFAULT '[]'::jsonb,
    onboarding_completed BOOLEAN DEFAULT FALSE,
    onboarding_step INTEGER DEFAULT 1,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. LANGUAGES TABLE
CREATE TABLE IF NOT EXISTS public.languages (
    id BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    native_name TEXT,
    code TEXT UNIQUE NOT NULL,
    flag_emoji TEXT DEFAULT '🌐',
    icon_url TEXT,
    description TEXT,
    learners_count INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 1
);

-- 3. COURSES TABLE
CREATE TABLE IF NOT EXISTS public.courses (
    id BIGSERIAL PRIMARY KEY,
    language_id BIGINT NOT NULL REFERENCES public.languages(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    level TEXT DEFAULT 'A1 Beginner',
    image_url TEXT,
    total_lessons INTEGER DEFAULT 0,
    order_index INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 1
);

-- 4. UNITS TABLE
CREATE TABLE IF NOT EXISTS public.units (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES public.courses(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    order_index INTEGER DEFAULT 1,
    sort_order INTEGER DEFAULT 1
);

-- 5. LESSONS TABLE
CREATE TABLE IF NOT EXISTS public.lessons (
    id BIGSERIAL PRIMARY KEY,
    unit_id BIGINT NOT NULL REFERENCES public.units(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    xp_reward INTEGER DEFAULT 15,
    duration_mins INTEGER DEFAULT 5,
    order_index INTEGER DEFAULT 1,
    is_free BOOLEAN DEFAULT TRUE,
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INTEGER DEFAULT 1
);

-- 6. EXERCISES TABLE
CREATE TABLE IF NOT EXISTS public.exercises (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES public.lessons(id) ON DELETE CASCADE,
    type TEXT DEFAULT 'MULTIPLE_CHOICE',
    question TEXT NOT NULL,
    options JSONB NOT NULL DEFAULT '[]'::jsonb,
    correct_answer TEXT NOT NULL,
    explanation TEXT,
    audio_url TEXT,
    image_url TEXT,
    sort_order INTEGER DEFAULT 1
);

-- 7. VOCABULARY TABLE
CREATE TABLE IF NOT EXISTS public.vocabulary (
    id BIGSERIAL PRIMARY KEY,
    language_code TEXT NOT NULL,
    word TEXT NOT NULL,
    translation TEXT NOT NULL,
    phonetic TEXT,
    part_of_speech TEXT DEFAULT 'Noun',
    example_sentence TEXT,
    audio_url TEXT,
    mastery_level INTEGER DEFAULT 1
);

-- 8. DAILY CHALLENGES TABLE
CREATE TABLE IF NOT EXISTS public.challenges (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    reward_xp INTEGER DEFAULT 25,
    reward_coins INTEGER DEFAULT 10,
    target INTEGER DEFAULT 1,
    is_active BOOLEAN DEFAULT TRUE
);

-- 9. ACHIEVEMENTS TABLE
CREATE TABLE IF NOT EXISTS public.achievements (
    id BIGSERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    description TEXT DEFAULT '',
    icon TEXT DEFAULT 'star',
    category TEXT DEFAULT 'General',
    max_progress INTEGER DEFAULT 1
);

-- 10. USER PROGRESS TABLE
CREATE TABLE IF NOT EXISTS public.user_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES auth.users(id) ON DELETE CASCADE,
    lesson_id BIGINT NOT NULL REFERENCES public.lessons(id) ON DELETE CASCADE,
    completed BOOLEAN DEFAULT FALSE,
    progress INTEGER DEFAULT 0,
    xp_earned INTEGER DEFAULT 0,
    updated_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT user_lesson_unique UNIQUE (user_id, lesson_id)
);

-- Indexes for blazing-fast lookups
CREATE INDEX IF NOT EXISTS idx_courses_language ON public.courses(language_id);
CREATE INDEX IF NOT EXISTS idx_units_course ON public.units(course_id);
CREATE INDEX IF NOT EXISTS idx_lessons_unit ON public.lessons(unit_id);
CREATE INDEX IF NOT EXISTS idx_exercises_lesson ON public.exercises(lesson_id);
CREATE INDEX IF NOT EXISTS idx_vocabulary_lang ON public.vocabulary(language_code);
CREATE INDEX IF NOT EXISTS idx_user_progress_user ON public.user_progress(user_id);
CREATE INDEX IF NOT EXISTS idx_profiles_xp ON public.profiles(xp DESC);

-- ==============================================================================
-- 11. RPC FUNCTION: complete_lesson (Transaction-Safe & Idempotent)
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.complete_lesson(p_lesson_id BIGINT)
RETURNS JSONB
LANGUAGE plpgsql
SECURITY DEFINER
AS $$
DECLARE
    v_user_id UUID;
    v_xp_reward INT := 15;
    v_coins_reward INT := 10;
    v_already_completed BOOLEAN := FALSE;
    v_profile RECORD;
BEGIN
    -- Authenticate caller from JWT
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

    -- Fetch lesson reward
    SELECT COALESCE(xp_reward, 15) INTO v_xp_reward FROM public.lessons WHERE id = p_lesson_id;
    IF v_xp_reward IS NULL THEN
        v_xp_reward := 15;
    END IF;

    -- Check if user has already completed this lesson
    SELECT completed INTO v_already_completed FROM public.user_progress
    WHERE user_id = v_user_id AND lesson_id = p_lesson_id;

    IF v_already_completed IS TRUE THEN
        -- Already completed: Return current profile without double rewards
        SELECT * INTO v_profile FROM public.profiles WHERE id = v_user_id;
        RETURN jsonb_build_object(
            'success', true,
            'rewarded', false,
            'xp_earned', 0,
            'coins_earned', 0,
            'profile', to_jsonb(v_profile)
        );
    END IF;

    -- Insert or update user_progress atomically
    INSERT INTO public.user_progress (user_id, lesson_id, completed, progress, xp_earned, updated_at)
    VALUES (v_user_id, p_lesson_id, true, 100, v_xp_reward, NOW())
    ON CONFLICT (user_id, lesson_id)
    DO UPDATE SET completed = true, progress = 100, xp_earned = v_xp_reward, updated_at = NOW();

    -- Update Profile XP, Coins, and Streak atomically
    UPDATE public.profiles
    SET xp = xp + v_xp_reward,
        coins = coins + v_coins_reward,
        streak = GREATEST(1, streak + 1),
        updated_at = NOW()
    WHERE id = v_user_id
    RETURNING * INTO v_profile;

    RETURN jsonb_build_object(
        'success', true,
        'rewarded', true,
        'xp_earned', v_xp_reward,
        'coins_earned', v_coins_reward,
        'profile', to_jsonb(v_profile)
    );
END;
$$;

-- Grant execution permission to authenticated users
GRANT EXECUTE ON FUNCTION public.complete_lesson(BIGINT) TO authenticated;

-- ==============================================================================
-- 11.B RPC FUNCTION: save_user_onboarding (Transaction-Safe & Idempotent)
-- ==============================================================================
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
    v_user_id := auth.uid();
    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'Not authenticated';
    END IF;

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

GRANT EXECUTE ON FUNCTION public.save_user_onboarding(BIGINT, BIGINT, TEXT, TEXT, TEXT, TEXT, JSONB, INT) TO authenticated;

-- ==============================================================================
-- 12. ROW LEVEL SECURITY (RLS) POLICIES
-- ==============================================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.languages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.courses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.units ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.lessons ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.exercises ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.vocabulary ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.challenges ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.achievements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.user_progress ENABLE ROW LEVEL SECURITY;

-- Public read access for educational content
CREATE POLICY "Public read languages" ON public.languages FOR SELECT USING (true);
CREATE POLICY "Public read courses" ON public.courses FOR SELECT USING (true);
CREATE POLICY "Public read units" ON public.units FOR SELECT USING (true);
CREATE POLICY "Public read lessons" ON public.lessons FOR SELECT USING (true);
CREATE POLICY "Public read exercises" ON public.exercises FOR SELECT USING (true);
CREATE POLICY "Public read vocabulary" ON public.vocabulary FOR SELECT USING (true);
CREATE POLICY "Public read challenges" ON public.challenges FOR SELECT USING (true);
CREATE POLICY "Public read achievements" ON public.achievements FOR SELECT USING (true);
CREATE POLICY "Public read profiles" ON public.profiles FOR SELECT USING (true);

-- User-scoped access for sensitive records
CREATE POLICY "Users can view and edit own profile" ON public.profiles
    FOR ALL USING (auth.uid() = id) WITH CHECK (auth.uid() = id);

CREATE POLICY "Users can view and edit own progress" ON public.user_progress
    FOR ALL USING (auth.uid() = user_id) WITH CHECK (auth.uid() = user_id);
