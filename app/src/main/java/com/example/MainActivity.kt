package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.i18n.AppLanguage
import com.example.data.i18n.L10nStrings
import com.example.ui.screens.achievements.AchievementsScreen
import com.example.ui.screens.auth.LandingScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OnboardingScreen
import com.example.ui.screens.auth.SignupScreen
import com.example.ui.screens.challenges.ChallengesScreen
import com.example.ui.screens.courses.CoursesScreen
import com.example.ui.screens.courses.LessonScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.leaderboard.LeaderboardScreen
import com.example.ui.screens.practice.FlashcardReviewScreen
import com.example.ui.screens.practice.PracticeScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.vocabulary.VocabularyScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.FlashcardUiState
import com.example.ui.viewmodel.LessonUiState
import com.example.ui.viewmodel.MainViewModel

enum class AuthSubScreen {
    LANDING, LOGIN, SIGNUP
}

enum class MainTab(val icon: ImageVector) {
    HOME(Icons.Default.Home),
    COURSES(Icons.Default.School),
    PRACTICE(Icons.Default.Psychology),
    LEADERBOARD(Icons.Default.Leaderboard),
    PROFILE(Icons.Default.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinguaXTheme {
                LinguaXApp()
            }
        }
    }
}

@Composable
fun LinguaXApp(viewModel: MainViewModel = viewModel()) {
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val l10n by viewModel.l10n.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var authSubScreen by remember { mutableStateOf(AuthSubScreen.LANDING) }
    var currentTab by remember { mutableStateOf(MainTab.HOME) }

    val selectedTargetLanguage by viewModel.selectedTargetLanguage.collectAsStateWithLifecycle()
    val languagesResource by viewModel.languagesState.collectAsStateWithLifecycle()
    val coursesResource by viewModel.coursesState.collectAsStateWithLifecycle()
    val vocabularyResource by viewModel.vocabularyState.collectAsStateWithLifecycle()
    val challengesResource by viewModel.challengesState.collectAsStateWithLifecycle()
    val achievementsResource by viewModel.achievementsState.collectAsStateWithLifecycle()
    val leaderboardResource by viewModel.leaderboardState.collectAsStateWithLifecycle()
    val lessonState by viewModel.lessonState.collectAsStateWithLifecycle()
    val flashcardState by viewModel.flashcardState.collectAsStateWithLifecycle()

    // Dynamic RTL / LTR layout provider
    CompositionLocalProvider(LocalLayoutDirection provides appLanguage.layoutDirection) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = LinguaXBackground
        ) {
            when (val state = authState) {
                is AuthState.Unauthenticated, is AuthState.Loading, is AuthState.Error -> {
                    when (authSubScreen) {
                        AuthSubScreen.LANDING -> {
                            LandingScreen(
                                l10n = l10n,
                                currentAppLanguage = appLanguage,
                                onLanguageChange = { viewModel.setAppLanguage(it) },
                                onNavigateToLogin = { authSubScreen = AuthSubScreen.LOGIN },
                                onNavigateToSignup = { authSubScreen = AuthSubScreen.SIGNUP }
                            )
                        }
                        AuthSubScreen.LOGIN -> {
                            LoginScreen(
                                l10n = l10n,
                                authState = state,
                                onLogin = { email, pass -> viewModel.login(email, pass) },
                                onNavigateToSignup = {
                                    viewModel.clearAuthError()
                                    authSubScreen = AuthSubScreen.SIGNUP
                                },
                                onBack = {
                                    viewModel.clearAuthError()
                                    authSubScreen = AuthSubScreen.LANDING
                                }
                            )
                        }
                        AuthSubScreen.SIGNUP -> {
                            SignupScreen(
                                l10n = l10n,
                                authState = state,
                                onSignup = { email, pass, name -> viewModel.signup(email, pass, name) },
                                onNavigateToLogin = {
                                    viewModel.clearAuthError()
                                    authSubScreen = AuthSubScreen.LOGIN
                                },
                                onBack = {
                                    viewModel.clearAuthError()
                                    authSubScreen = AuthSubScreen.LANDING
                                }
                            )
                        }
                    }
                }
                is AuthState.Authenticated -> {
                    val profile = state.session.profile

                    if (!profile.onboardingCompleted) {
                        val onboardingState by viewModel.onboardingState.collectAsStateWithLifecycle()
                        OnboardingScreen(
                            l10n = l10n,
                            onboardingState = onboardingState,
                            languagesResource = languagesResource,
                            onSelectNativeLanguage = { viewModel.selectOnboardingNativeLanguage(it) },
                            onSelectLearningLanguage = { viewModel.selectOnboardingLearningLanguage(it) },
                            onSelectCurrentLevel = { viewModel.selectOnboardingCurrentLevel(it) },
                            onSelectTargetLevel = { viewModel.selectOnboardingTargetLevel(it) },
                            onSelectAgeGroup = { viewModel.selectOnboardingAgeGroup(it) },
                            onSelectGender = { viewModel.selectOnboardingGender(it) },
                            onToggleReason = { viewModel.toggleOnboardingReason(it) },
                            onSelectDailyGoal = { viewModel.selectOnboardingDailyGoal(it) },
                            onNextStep = { viewModel.nextOnboardingStep() },
                            onPrevStep = { viewModel.prevOnboardingStep() },
                            onSubmit = { viewModel.submitOnboarding() }
                        )
                    } else if (lessonState !is LessonUiState.Idle) {
                        LessonScreen(
                            l10n = l10n,
                            lessonState = lessonState,
                            targetLanguageCode = selectedTargetLanguage.code,
                            onSelectOption = { viewModel.selectLessonOption(it) },
                            onCheckAnswer = { viewModel.checkLessonAnswer() },
                            onProceed = { viewModel.proceedLessonExercise() },
                            onRetrySave = { viewModel.retrySaveLesson() },
                            onExit = { viewModel.exitLesson() }
                        )
                    } else if (flashcardState !is FlashcardUiState.Idle) {
                        FlashcardReviewScreen(
                            l10n = l10n,
                            flashcardState = flashcardState,
                            targetLanguageCode = selectedTargetLanguage.code,
                            onFlip = { viewModel.flipFlashcard() },
                            onRate = { viewModel.recordFlashcardRating(it) },
                            onExit = { viewModel.exitFlashcards() }
                        )
                    } else {
                        Scaffold(
                            bottomBar = {
                                NavigationBar(
                                    containerColor = LinguaXSurface,
                                    tonalElevation = 8.dp,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                                ) {
                                    MainTab.values().forEach { tab ->
                                        val isSelected = currentTab == tab
                                        val label = when (tab) {
                                            MainTab.HOME -> l10n.homeTab
                                            MainTab.COURSES -> l10n.coursesTab
                                            MainTab.PRACTICE -> l10n.practiceTab
                                            MainTab.LEADERBOARD -> l10n.leaderboardTab
                                            MainTab.PROFILE -> l10n.profileTab
                                        }

                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = tab.icon,
                                                    contentDescription = label
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = LinguaXAccent,
                                                selectedTextColor = LinguaXAccent,
                                                unselectedIconColor = LinguaXTextTertiary,
                                                unselectedTextColor = LinguaXTextTertiary,
                                                indicatorColor = LinguaXPrimaryContainer
                                            ),
                                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .background(LinguaXBackground)
                            ) {
                                when (currentTab) {
                                    MainTab.HOME -> {
                                        HomeScreen(
                                            l10n = l10n,
                                            profile = profile,
                                            selectedTargetLanguage = selectedTargetLanguage,
                                            languagesResource = languagesResource,
                                            coursesResource = coursesResource,
                                            challengesResource = challengesResource,
                                            onLanguageSelected = { viewModel.setSelectedTargetLanguage(it) },
                                            onNavigateToCourses = { currentTab = MainTab.COURSES },
                                            onNavigateToVocabulary = { currentTab = MainTab.PRACTICE },
                                            onNavigateToChallenges = { currentTab = MainTab.PRACTICE },
                                            onOpenSettings = { currentTab = MainTab.PROFILE },
                                            onOpenLesson = { lesson -> viewModel.openLesson(lesson) }
                                        )
                                    }
                                    MainTab.COURSES -> {
                                        CoursesScreen(
                                            l10n = l10n,
                                            selectedTargetLanguage = selectedTargetLanguage,
                                            languagesResource = languagesResource,
                                            coursesResource = coursesResource,
                                            onLanguageSelected = { viewModel.setSelectedTargetLanguage(it) },
                                            onLessonClicked = { lesson -> viewModel.openLesson(lesson) },
                                            onRetryLanguages = { viewModel.loadLanguages() },
                                            onRetryCourses = { viewModel.loadCourses(selectedTargetLanguage.code, selectedTargetLanguage.id) }
                                        )
                                    }
                                    MainTab.PRACTICE -> {
                                        PracticeScreen(
                                            l10n = l10n,
                                            profile = profile,
                                            selectedTargetLanguage = selectedTargetLanguage,
                                            vocabularyResource = vocabularyResource,
                                            challengesResource = challengesResource,
                                            onToggleBookmark = { viewModel.toggleVocabularyBookmark(it) },
                                            onStartFlashcards = { items, isSmartReview ->
                                                viewModel.startFlashcardReview(items, isSmartReview)
                                            },
                                            onClaimChallengeReward = { challenge ->
                                                viewModel.updateProfile(
                                                    newDisplayName = profile.displayName ?: "Learner",
                                                    newGoal = profile.dailyGoal
                                                )
                                            }
                                        )
                                    }
                                    MainTab.LEADERBOARD -> {
                                        LeaderboardScreen(
                                            l10n = l10n,
                                            profile = profile,
                                            selectedTargetLanguage = selectedTargetLanguage,
                                            leaderboardResource = leaderboardResource,
                                            onRetry = { viewModel.loadLeaderboard() }
                                        )
                                    }
                                    MainTab.PROFILE -> {
                                        ProfileScreen(
                                            l10n = l10n,
                                            profile = profile,
                                            currentAppLanguage = appLanguage,
                                            selectedTargetLanguage = selectedTargetLanguage,
                                            achievementsResource = achievementsResource,
                                            onAppLanguageChange = { viewModel.setAppLanguage(it) },
                                            onUpdateProfile = { name, goal -> viewModel.updateProfile(name, goal) },
                                            onLogout = { viewModel.logout() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
