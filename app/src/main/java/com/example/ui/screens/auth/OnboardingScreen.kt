package com.example.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.L10nStrings
import com.example.data.model.LanguageItem
import com.example.data.repository.Resource
import com.example.ui.audio.AudioPlayerManager
import com.example.ui.audio.rememberAudioPlayerManager
import com.example.ui.components.LinguaX3DButton
import com.example.ui.theme.*
import com.example.ui.viewmodel.OnboardingState

@Composable
fun OnboardingScreen(
    l10n: L10nStrings,
    onboardingState: OnboardingState,
    languagesResource: Resource<List<LanguageItem>>,
    onSelectNativeLanguage: (LanguageItem) -> Unit,
    onSelectLearningLanguage: (LanguageItem) -> Unit,
    onSelectCurrentLevel: (String) -> Unit,
    onSelectTargetLevel: (String) -> Unit,
    onSelectAgeGroup: (String) -> Unit,
    onSelectGender: (String) -> Unit,
    onToggleReason: (String) -> Unit,
    onSelectDailyGoal: (Int) -> Unit,
    onNextStep: () -> Boolean,
    onPrevStep: () -> Boolean,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val audioManager = rememberAudioPlayerManager()
    val isArabic = l10n.appName == "LinguaX" && l10n.welcomeTitle.contains("اللغات")

    // Fallback languages list if resource is loading
    val fallbackLanguages = remember {
        listOf(
            LanguageItem(1L, "English", "English", "en", "🇬🇧", null, "English", 120000),
            LanguageItem(2L, "Spanish", "Español", "es", "🇪🇸", null, "Spanish", 85000),
            LanguageItem(3L, "French", "Français", "fr", "🇫🇷", null, "French", 64000),
            LanguageItem(4L, "German", "Deutsch", "de", "🇩🇪", null, "German", 49000),
            LanguageItem(5L, "Turkish", "Türkçe", "tr", "🇹🇷", null, "Turkish", 42000),
            LanguageItem(6L, "Arabic", "العربية", "ar", "🇸🇦", null, "Arabic", 95000),
            LanguageItem(7L, "Italian", "Italiano", "it", "🇮🇹", null, "Italian", 31000),
            LanguageItem(8L, "Japanese", "日本語", "ja", "🇯🇵", null, "Japanese", 58000)
        )
    }

    val availableLanguages = when (languagesResource) {
        is Resource.Success -> if (languagesResource.data.isNotEmpty()) languagesResource.data else fallbackLanguages
        else -> fallbackLanguages
    }

    // Default initialization if none selected
    LaunchedEffect(availableLanguages) {
        if (onboardingState.nativeLanguage == null) {
            val defaultNative = availableLanguages.find { it.code == "ar" } ?: availableLanguages.firstOrNull()
            defaultNative?.let { onSelectNativeLanguage(it) }
        }
        if (onboardingState.learningLanguage == null) {
            val defaultLearn = availableLanguages.find { it.code == "en" } ?: availableLanguages.getOrNull(1)
            defaultLearn?.let { onSelectLearningLanguage(it) }
        }
    }

    val progress by animateFloatAsState(
        targetValue = onboardingState.currentStep.toFloat() / onboardingState.totalSteps.toFloat(),
        animationSpec = tween(durationMillis = 350),
        label = "onboarding_progress"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LinguaXBackground)
    ) {
        // Ambient background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(LinguaXPrimary.copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(size.width * 0.5f, size.height * 0.15f),
                    radius = size.width * 0.8f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(LinguaXAccent.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.8f, size.height * 0.85f),
                    radius = size.width * 0.7f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Top Navigation & Step Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (onboardingState.currentStep > 1) {
                    IconButton(
                        onClick = { onPrevStep() },
                        modifier = Modifier
                            .size(42.dp)
                            .background(LinguaXSurfaceElevated, CircleShape)
                            .testTag("onboarding_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Step",
                            tint = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(42.dp))
                }

                // Step Counter Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LinguaXPrimaryContainer.copy(alpha = 0.3f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isArabic) {
                            "الخطوة ${onboardingState.currentStep} من ${onboardingState.totalSteps}"
                        } else {
                            "Step ${onboardingState.currentStep} of ${onboardingState.totalSteps}"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = LinguaXAccent
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Skip button for optional steps (Age/Gender)
                if (onboardingState.currentStep in listOf(4, 5)) {
                    TextButton(
                        onClick = { onNextStep() },
                        modifier = Modifier.testTag("onboarding_skip_button")
                    ) {
                        Text(
                            text = if (isArabic) "تخطي" else "Skip",
                            color = LinguaXTextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(42.dp))
                }
            }

            // Animated Smooth Linear Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(LinguaXSurfaceElevated)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(LinguaXPrimary, LinguaXAccent)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error banner if any
            AnimatedVisibility(visible = onboardingState.errorMessage != null) {
                onboardingState.errorMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = LinguaXError.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXError.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = LinguaXError)
                            Text(text = msg, style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }
                }
            }

            // Step Content Area (Animated Crossfade)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = onboardingState.currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width / 2 } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width / 2 } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width / 2 } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width / 2 } + fadeOut()
                        }
                    },
                    label = "onboarding_step_content"
                ) { step ->
                    when (step) {
                        1 -> LearningReasonsStep(
                            isArabic = isArabic,
                            selectedReasons = onboardingState.selectedReasons,
                            onToggle = onToggleReason
                        )
                        2 -> LearningLanguageStep(
                            isArabic = isArabic,
                            languages = availableLanguages,
                            selectedLanguage = onboardingState.learningLanguage,
                            audioManager = audioManager,
                            onSelect = onSelectLearningLanguage
                        )
                        3 -> CurrentLevelStep(
                            isArabic = isArabic,
                            selectedLevel = onboardingState.currentLevel,
                            onSelect = onSelectCurrentLevel
                        )
                        4 -> AgeGroupStep(
                            isArabic = isArabic,
                            selectedAgeGroup = onboardingState.ageGroup,
                            onSelect = onSelectAgeGroup
                        )
                        5 -> GenderStep(
                            isArabic = isArabic,
                            selectedGender = onboardingState.gender,
                            onSelect = onSelectGender
                        )
                        6 -> DailyGoalStep(
                            isArabic = isArabic,
                            selectedGoal = onboardingState.dailyGoalMinutes,
                            onSelect = onSelectDailyGoal
                        )
                        7 -> SummaryAndTargetStep(
                            isArabic = isArabic,
                            onboardingState = onboardingState,
                            onSelectTargetLevel = onSelectTargetLevel
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom CTA Button
            val isFinalStep = onboardingState.currentStep == onboardingState.totalSteps
            val canProceed = when (onboardingState.currentStep) {
                1 -> onboardingState.selectedReasons.isNotEmpty()
                2 -> onboardingState.learningLanguage != null
                else -> true
            }

            LinguaX3DButton(
                text = when {
                    onboardingState.isSaving -> if (isArabic) "جاري إعداد خطتك التعليمية..." else "Saving your plan..."
                    isFinalStep -> if (isArabic) "ابدأ التعلم 🚀" else "Start Learning 🚀"
                    else -> if (isArabic) "متابعة" else "Continue"
                },
                onClick = {
                    if (isFinalStep) {
                        onSubmit()
                    } else {
                        onNextStep()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("onboarding_action_button"),
                enabled = canProceed && !onboardingState.isSaving,
                icon = if (!isFinalStep) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.RocketLaunch,
                gradient = LinguaXButtonGradient
            )
        }
    }
}

// ==========================================
// STEP 1: NATIVE LANGUAGE SELECTION
// ==========================================
@Composable
private fun NativeLanguageStep(
    isArabic: Boolean,
    languages: List<LanguageItem>,
    selectedLanguage: LanguageItem?,
    onSelect: (LanguageItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isArabic) "ما هي لغتك الأم؟ 🌍" else "What is your native language? 🌍",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Text(
                text = if (isArabic) "سنقوم بعرض شروحات وترجمات الدروس بلغتك المفضلة." else "We will present lesson explanations and translations in this language.",
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(languages) { lang ->
                val isSelected = selectedLanguage?.code == lang.code
                OnboardingChoiceCard(
                    title = lang.nativeName ?: lang.name,
                    subtitle = lang.name,
                    iconEmoji = lang.flagEmoji,
                    isSelected = isSelected,
                    onClick = { onSelect(lang) },
                    testTag = "native_lang_${lang.code}"
                )
            }
        }
    }
}

// ==========================================
// STEP 2: LEARNING LANGUAGE SELECTION
// ==========================================
@Composable
private fun LearningLanguageStep(
    isArabic: Boolean,
    languages: List<LanguageItem>,
    selectedLanguage: LanguageItem?,
    audioManager: AudioPlayerManager,
    onSelect: (LanguageItem) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isArabic) "ما هي اللغة التي ترغب في تعلمها؟ ✨" else "Which language do you want to learn? ✨",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Text(
                text = if (isArabic) "اختر لغتك المستهدفة لتخصيص الدروس والمفردات المناسبة لك." else "Choose your target language to personalize your courses and exercises.",
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(languages) { lang ->
                val isSelected = selectedLanguage?.code == lang.code
                OnboardingChoiceCard(
                    title = lang.name,
                    subtitle = if (lang.learnersCount > 0) "+${lang.learnersCount / 1000}k متعلم" else lang.nativeName ?: "",
                    iconEmoji = lang.flagEmoji,
                    isSelected = isSelected,
                    onAudioClick = {
                        val sampleGreeting = when (lang.code.lowercase()) {
                            "en" -> "Hello! Welcome to LinguaX."
                            "fr" -> "Bonjour! Bienvenue sur LinguaX."
                            "es" -> "¡Hola! Bienvenido a LinguaX."
                            "de" -> "Hallo! Willkommen bei LinguaX."
                            "tr" -> "Merhaba! LinguaX'e hoş geldiniz."
                            "it" -> "Ciao! Benvenuto su LinguaX."
                            "ja" -> "こんにちは！"
                            else -> "Hello!"
                        }
                        audioManager.play(sampleGreeting, lang.code)
                    },
                    onClick = { onSelect(lang) },
                    testTag = "learn_lang_${lang.code}"
                )
            }
        }
    }
}

// ==========================================
// STEP 3: CURRENT PROFICIENCY LEVEL
// ==========================================
@Composable
private fun CurrentLevelStep(
    isArabic: Boolean,
    selectedLevel: String,
    onSelect: (String) -> Unit
) {
    val levels = listOf(
        LevelOption(
            code = "A1",
            title = if (isArabic) "مبتدئ تماماً (A1)" else "Absolute Beginner (A1)",
            description = if (isArabic) "لا أعرف شيئاً في هذه اللغة أو أعرف كلمات قليلة جداً." else "I am brand new or only know a few words.",
            badge = "A1"
        ),
        LevelOption(
            code = "A2",
            title = if (isArabic) "أساسيات بسيطة (A2)" else "Elementary (A2)",
            description = if (isArabic) "أفهم بعض العبارات اليومية والجمل البسيطة." else "I can understand basic common phrases and greetings.",
            badge = "A2"
        ),
        LevelOption(
            code = "B1",
            title = if (isArabic) "متوسط (B1)" else "Intermediate (B1)",
            description = if (isArabic) "أستطيع إجراء محادثات بسيطة وفهم الأفكار الرئيسية." else "I can have simple conversations and express basic thoughts.",
            badge = "B1"
        ),
        LevelOption(
            code = "B2",
            title = if (isArabic) "فوق المتوسط (B2)" else "Upper Intermediate (B2)",
            description = if (isArabic) "أتحدث بطلاقة معتدلة وأفهم النصوص والمواضيع المعقدة." else "I speak comfortably and understand complex discussions.",
            badge = "B2"
        ),
        LevelOption(
            code = "C1",
            title = if (isArabic) "متقدم / طليق (C1)" else "Advanced / Fluent (C1)",
            description = if (isArabic) "أعبر بطلاقة تامة في السياقات الأكاديمية والمهنية." else "I express fluently and effortlessly in professional contexts.",
            badge = "C1"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isArabic) "ما هو مستواك الحالي؟ 📊" else "What is your current level? 📊",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Text(
                text = if (isArabic) "سنبدأ معك من المكان الذي يناسب مستواك بالضبط." else "We will match your starting lessons to your current skills.",
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )
        }

        levels.forEach { level ->
            val isSelected = selectedLevel == level.code
            OnboardingListOptionCard(
                title = level.title,
                description = level.description,
                badgeText = level.badge,
                isSelected = isSelected,
                onClick = { onSelect(level.code) },
                testTag = "level_option_${level.code}"
            )
        }
    }
}

// ==========================================
// STEP 4: AGE GROUP SELECTION
// ==========================================
@Composable
private fun AgeGroupStep(
    isArabic: Boolean,
    selectedAgeGroup: String?,
    onSelect: (String) -> Unit
) {
    val ageGroups = listOf(
        "< 13" to if (isArabic) "أقل من 13 سنة" else "Under 13 years",
        "13-17" to if (isArabic) "13 - 17 سنة" else "13 - 17 years",
        "18-24" to if (isArabic) "18 - 24 سنة" else "18 - 24 years",
        "25-34" to if (isArabic) "25 - 34 سنة" else "25 - 34 years",
        "35-44" to if (isArabic) "35 - 44 سنة" else "35 - 44 years",
        "45+" to if (isArabic) "45 سنة فأكثر" else "45+ years"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isArabic) "كم عمرك؟ 🎂" else "How old are you? 🎂",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Text(
                text = if (isArabic) "تساعدنا الفئة العمرية في تخصيص أسلوب المحتوى والأمثلة التعليمية." else "Helps us adapt tone and examples to your learning style.",
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )
        }

        ageGroups.forEach { (key, label) ->
            val isSelected = selectedAgeGroup == key
            OnboardingListOptionCard(
                title = label,
                description = null,
                badgeText = key,
                isSelected = isSelected,
                onClick = { onSelect(key) },
                testTag = "age_group_$key"
            )
        }
    }
}

// ==========================================
// STEP 5: GENDER SELECTION
// ==========================================
@Composable
private fun GenderStep(
    isArabic: Boolean,
    selectedGender: String?,
    onSelect: (String) -> Unit
) {
    val genders = listOf(
        Triple("male", if (isArabic) "ذكر" else "Male", "👨"),
        Triple("female", if (isArabic) "أنثى" else "Female", "👩"),
        Triple("unspecified", if (isArabic) "أفضل عدم الإجابة" else "Prefer not to say", "✨")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isArabic) "الجنس 👤" else "Gender 👤",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Text(
                text = if (isArabic) "لتخصيص صيغ المخاطبة المناسبة في قواعد النحو والمحادثات." else "Used for grammatically accurate address forms in selected languages.",
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )
        }

        genders.forEach { (key, label, emoji) ->
            val isSelected = selectedGender == key
            OnboardingListOptionCard(
                title = label,
                description = null,
                badgeText = emoji,
                isSelected = isSelected,
                onClick = { onSelect(key) },
                testTag = "gender_$key"
            )
        }
    }
}

// ==========================================
// STEP 1: REASONS FOR LEARNING (MULTI-SELECT)
// ==========================================
@Composable
private fun LearningReasonsStep(
    isArabic: Boolean,
    selectedReasons: Set<String>,
    onToggle: (String) -> Unit
) {
    val reasons = listOf(
        LearningReason("travel", if (isArabic) "السفر ✈️" else "Travel ✈️", "✈️", if (isArabic) "للتواصل مع السكان المحليين في رحلاتي" else "To navigate and connect while traveling"),
        LearningReason("education", if (isArabic) "الدراسة 🎓" else "Education 🎓", "🎓", if (isArabic) "للدراسة الجامعية أو الامتحانات الدولية" else "For university or certification exams"),
        LearningReason("career", if (isArabic) "العمل 💼" else "Career 💼", "💼", if (isArabic) "لتعزيز فرصي المهنية والترقية" else "To unlock new career opportunities"),
        LearningReason("communication", if (isArabic) "التواصل 🗣️" else "Communication 🗣️", "🗣️", if (isArabic) "للتحدث بطلاقة وتكوين صداقات جديدة" else "To speak fluently and make connections"),
        LearningReason("culture", if (isArabic) "الثقافة 🌍" else "Culture 🌍", "🌍", if (isArabic) "لاكتشاف ثقافات وحضارات جديدة" else "To explore art, cinema, and history"),
        LearningReason("self_growth", if (isArabic) "تطوير نفسي 🚀" else "Self-Growth 🚀", "🚀", if (isArabic) "لتنشيط الذاكرة واكتساب مهارة جديدة قوية" else "To challenge mind and expand worldview")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isArabic) "لماذا تريد تعلم اللغات؟ 🎯" else "Why do you want to learn languages? 🎯",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Text(
                text = if (isArabic) "اختر سببًا أو أكثر لتخصيص محتوى وتمارين الدروس لك." else "Select one or more reasons to personalize your curriculum.",
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )
        }

        reasons.forEach { item ->
            val isSelected = selectedReasons.contains(item.key)
            OnboardingMultiSelectCard(
                title = item.title,
                description = item.description,
                emoji = item.emoji,
                isSelected = isSelected,
                onToggle = { onToggle(item.key) },
                testTag = "reason_${item.key}"
            )
        }
    }
}

// ==========================================
// STEP 6: DAILY STUDY GOAL
// ==========================================
@Composable
private fun DailyGoalStep(
    isArabic: Boolean,
    selectedGoal: Int,
    onSelect: (Int) -> Unit
) {
    val goals = listOf(
        GoalOption(5, if (isArabic) "5 دقائق / يوم" else "5 mins / day", if (isArabic) "مريح وخفيف - للبدايات" else "Casual - Great start", "⚡"),
        GoalOption(10, if (isArabic) "10 دقائق / يوم" else "10 mins / day", if (isArabic) "متوازن وثابت" else "Steady habit", "🌱"),
        GoalOption(15, if (isArabic) "15 دقيقة / يوم" else "15 mins / day", if (isArabic) "منتظم (موصى به)" else "Regular (Recommended)", "🎯"),
        GoalOption(20, if (isArabic) "20 دقيقة / يوم" else "20 mins / day", if (isArabic) "متقدم ونشط" else "Advanced focus", "🔥"),
        GoalOption(30, if (isArabic) "30 دقيقة / يوم" else "30 mins / day", if (isArabic) "مكثف وسريع - نتائج مذهلة" else "Intensive - Fast mastery", "🚀")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isArabic) "كم تريد أن تتعلم يوميًا؟ ⏱️" else "How much time do you want to learn daily? ⏱️",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Text(
                text = if (isArabic) "الالتزام اليومي البسيط يصنع فارقاً هائلاً في حفظ الكلمات والطلاقة." else "A consistent daily habit is key to long-term fluency.",
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )
        }

        goals.forEach { item ->
            val isSelected = selectedGoal == item.minutes
            OnboardingListOptionCard(
                title = item.title,
                description = item.description,
                badgeText = item.emoji,
                isSelected = isSelected,
                onClick = { onSelect(item.minutes) },
                testTag = "goal_${item.minutes}m"
            )
        }
    }
}

// ==========================================
// STEP 8: TARGET LEVEL & PERSONALIZED ROADMAP
// ==========================================
@Composable
private fun SummaryAndTargetStep(
    isArabic: Boolean,
    onboardingState: OnboardingState,
    onSelectTargetLevel: (String) -> Unit
) {
    val targetLevels = listOf("A2", "B1", "B2", "C1", "C2")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = if (isArabic) "خطتك التعليمية المخصصة 🌟" else "Your Personalized Plan 🌟",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = Color.White
            )
            Text(
                text = if (isArabic) "حدد مستواك المستهدف واطّلع على ملخص مسارك التعليمي." else "Select your target level and review your tailored roadmap.",
                style = MaterialTheme.typography.bodyMedium,
                color = LinguaXTextSecondary
            )
        }

        // Target Level Chips
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = if (isArabic) "المستوى المستهدف:" else "Target Level:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                targetLevels.forEach { lvl ->
                    val isSelected = onboardingState.targetLevel == lvl
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) LinguaXPrimary else LinguaXSurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) LinguaXAccent else LinguaXBorder
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onSelectTargetLevel(lvl) }
                            .testTag("target_level_$lvl")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = lvl,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else LinguaXTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Personalized Summary Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = LinguaXSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, LinguaXPrimary.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(20.dp), ambientColor = LinguaXPrimary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Languages Path Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "🌍", fontSize = 24.sp)
                        Text(
                            text = if (isArabic) "لغة التعلم" else "Learning Target",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = LinguaXAccent
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = onboardingState.learningLanguage?.flagEmoji ?: "🌐", fontSize = 24.sp)
                        Text(
                            text = onboardingState.learningLanguage?.name ?: "Target Language",
                            fontWeight = FontWeight.Bold,
                            color = LinguaXAccent
                        )
                    }
                }

                HorizontalDivider(color = LinguaXBorder.copy(alpha = 0.5f))

                // Level Milestone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "المستوى الحالي" else "Starting Level",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinguaXTextTertiary
                        )
                        Text(
                            text = onboardingState.currentLevel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isArabic) "الهدف النهائي" else "Target Goal",
                            style = MaterialTheme.typography.labelSmall,
                            color = LinguaXTextTertiary
                        )
                        Text(
                            text = onboardingState.targetLevel,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                            color = LinguaXAccent
                        )
                    }
                }

                // Daily Commitment & Estimate
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LinguaXPrimaryContainer.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = LinguaXAccent)
                        Text(
                            text = if (isArabic) "${onboardingState.dailyGoalMinutes} دقيقة يومياً" else "${onboardingState.dailyGoalMinutes} mins / day",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = LinguaXAccent.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (isArabic) "مسار ذكي مخصص" else "AI Tailored",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = LinguaXAccent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// REUSABLE ONBOARDING UI COMPONENTS
// ==========================================

@Composable
private fun OnboardingChoiceCard(
    title: String,
    subtitle: String,
    iconEmoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String,
    onAudioClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) LinguaXSurfaceElevated else LinguaXSurface,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) LinguaXAccent else LinguaXBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = iconEmoji, fontSize = 28.sp)

                    if (onAudioClick != null) {
                        IconButton(
                            onClick = onAudioClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Listen",
                                tint = if (isSelected) LinguaXAccent else LinguaXTextTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = LinguaXAccent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1
                    )
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = LinguaXTextTertiary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingListOptionCard(
    title: String,
    description: String?,
    badgeText: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) LinguaXSurfaceElevated else LinguaXSurface,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) LinguaXAccent else LinguaXBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Badge / Icon container
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) LinguaXAccent.copy(alpha = 0.2f) else LinguaXSurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) LinguaXAccent.copy(alpha = 0.5f) else LinguaXBorder
                ),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = if (isSelected) LinguaXAccent else Color.White
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                if (!description.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = LinguaXTextSecondary
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = LinguaXAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun OnboardingMultiSelectCard(
    title: String,
    description: String,
    emoji: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) LinguaXSurfaceElevated else LinguaXSurface,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) LinguaXAccent else LinguaXBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggle() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(text = emoji, fontSize = 28.sp)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = LinguaXTextSecondary
                )
            }

            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = LinguaXAccent,
                    checkmarkColor = Color.Black,
                    uncheckedColor = LinguaXTextTertiary
                )
            )
        }
    }
}

// Data holder classes for onboarding steps
private data class LevelOption(val code: String, val title: String, val description: String, val badge: String)
private data class LearningReason(val key: String, val title: String, val emoji: String, val description: String)
private data class GoalOption(val minutes: Int, val title: String, val description: String, val emoji: String)
